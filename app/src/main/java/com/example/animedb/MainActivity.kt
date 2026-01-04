package com.example.animedb

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.coroutines.*
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initSecurePrefs()
        setContent {
            AnimeDbApp()
        }
    }

    private fun initSecurePrefs() {
        val masterKey = MasterKey.Builder(this)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            this,
            "secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
}

@Composable
fun AnimeDbApp() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var currentScreen by remember { mutableStateOf("search") }
    val prefs = EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    var token by remember { mutableStateOf(prefs.getString("token", "") ?: "") }
    var searchQuery by remember { mutableStateOf("") }
    var animeList by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var todaysAnime by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var savedMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // Fetch today's anime on start
    LaunchedEffect(Unit) {
        if (token.isNotEmpty()) {
            todaysAnime = fetchTodaysAnime(token)
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentScreen == "search",
                    onClick = { currentScreen = "search" },
                    icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    label = { Text(stringResource(id = R.string.search_hint).substring(0, 6)) }
                )
                NavigationBarItem(
                    selected = currentScreen == "today",
                    onClick = { currentScreen = "today" },
                    icon = { Icon(Icons.Default.Today, contentDescription = "Today") },
                    label = { Text(stringResource(id = R.string.today)) }
                )
                NavigationBarItem(
                    selected = currentScreen == "my_list",
                    onClick = { currentScreen = "my_list" },
                    icon = { Icon(Icons.Default.Book, contentDescription = "My List") },
                    label = { Text(stringResource(id = R.string.my_list)) }
                )
                NavigationBarItem(
                    selected = currentScreen == "settings",
                    onClick = { currentScreen = "settings" },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text(stringResource(id = R.string.settings)) }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            when (currentScreen) {
                "search" -> {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text(stringResource(id = R.string.search_hint)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            if (token.isEmpty()) {
                                Toast.makeText(context, "Please enter API token in Settings", Toast.LENGTH_LONG).show()
                                return@Button
                            }
                            coroutineScope.launch {
                                isLoading = true
                                try {
                                    animeList = searchAnimes(searchQuery, token)
                                } catch (e: Exception) {
                                    Toast.makeText(context, stringResource(id = R.string.error), Toast.LENGTH_SHORT).show()
                                }
                                isLoading = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        } else {
                            Text(stringResource(id = R.string.search_hint))
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    if (animeList.isEmpty() && !isLoading) {
                        Text(stringResource(id = R.string.no_results))
                    } else {
                        LazyColumn {
                            items(animeList) { anime ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Column(Modifier.padding(16.dp)) {
                                        Text(
                                            text = (anime["title"] as? Map<*, *>)?.get("english") as? String
                                                ?: (anime["title"] as? Map<*, *>)?.get("romaji") as? String
                                                ?: "Unknown Title"
                                        )
                                        Text("Year: ${anime["seasonYear"]}, Episodes: ${anime["episodes"]}")
                                        Button(
                                            onClick = {
                                                // Save to local JSON
                                                val dbDir = File(context.getExternalFilesDir(null), "anime_db")
                                                dbDir.mkdirs()
                                                val animeId = anime["id"] as? Int ?: 0
                                                if (animeId != 0) {
                                                    File(dbDir, "$animeId.json").writeText(Gson().toJson(anime))
                                                    savedMessage = stringResource(id = R.string.anime_saved)
                                                    Toast.makeText(context, stringResource(id = R.string.data_folder), Toast.LENGTH_LONG).show()
                                                }
                                            }
                                        ) {
                                            Text(stringResource(id = R.string.add_to_list))
                                        }
                                        if (savedMessage.isNotEmpty()) {
                                            Text(savedMessage, color = MaterialTheme.colorScheme.primary)
                                            savedMessage = ""
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                "today" -> {
                    Text("Today's Anime (Airing Today):", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn {
                        items(todaysAnime) { anime ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Text(
                                        text = (anime["media"] as? Map<*, *>)?.get("title")?.let { (it as? Map<*, *>)?.get("english") as? String } ?: "Unknown"
                                    )
                                    val nextAiring = (anime["nextAiringEpisode"] as? Map<*, *>)?.get("airingAt") as? Long
                                    if (nextAiring != null) {
                                        val timeLeft = nextAiring - (System.currentTimeMillis() / 1000)
                                        if (timeLeft > 0) {
                                            val hours = timeLeft / 3600
                                            val minutes = (timeLeft % 3600) / 60
                                            Text("Airs in: $hours h $minutes m")
                                        } else {
                                            Text("Airing now!")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                "my_list" -> {
                    val dbDir = File(context.getExternalFilesDir(null), "anime_db")
                    val files = dbDir.listFiles()?.filter { it.extension == "json" } ?: emptyList()
                    Text("Saved Anime: ${files.size} items", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn {
                        items(files) { file ->
                            val content = file.readText()
                            val json = JsonParser.parseString(content).asJsonObject
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    val title = json.getAsJsonObject("title")
                                        .get("english")?.asString
                                        ?: json.getAsJsonObject("title").get("romaji")?.asString
                                        ?: "Unknown"
                                    Text(title)
                                    Text("ID: ${json.get("id").asInt}")
                                }
                            }
                        }
                    }
                }
                "settings" -> {
                    var newToken by remember { mutableStateOf(token) }
                    Text(stringResource(id = R.string.token_label), style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newToken,
                        onValueChange = { newToken = it },
                        label = { Text(stringResource(id = R.string.token_label)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            prefs.edit().putString("token", newToken).apply()
                            token = newToken
                            Toast.makeText(context, stringResource(id = R.string.token_saved), Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(id = R.string.save))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Your data is saved in:", style = MaterialTheme.typography.bodySmall)
                    Text("/Android/media/com.example.animedb/files/anime_db/", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

// Helper functions
suspend fun searchAnimes(query: String, token: String): List<Map<String, Any>> = withContext(Dispatchers.IO) {
    val url = URL("https://graphql.anilist.co")
    val connection = url.openConnection() as HttpURLConnection
    connection.requestMethod = "POST"
    connection.setRequestProperty("Authorization", "Bearer $token")
    connection.setRequestProperty("Content-Type", "application/json")
    connection.doOutput = true

    val body = """
        {
          "query": "query { Page(page: 1, perPage: 10) { media(search: \"$query\", type: ANIME) { id title { english romaji } seasonYear episodes description status } } }"
        }
    """.trimIndent()

    connection.outputStream.write(body.toByteArray())
    val response = connection.inputStream.bufferedReader().readText()
    connection.disconnect()

    val json = JsonParser.parseString(response).asJsonObject
    val mediaList = json.getAsJsonObject("data")
        .getAsJsonObject("Page")
        .getAsJsonArray("media")

    mediaList.map { it.asJsonObject }.map { anime ->
        mapOf(
            "id" to anime.get("id").asInt,
            "title" to mapOf(
                "english" to anime.getAsJsonObject("title").get("english")?.asString,
                "romaji" to anime.getAsJsonObject("title").get("romaji")?.asString
            ),
            "seasonYear" to anime.get("seasonYear")?.asInt,
            "episodes" to anime.get("episodes")?.asInt,
            "description" to anime.get("description")?.asString,
            "status" to anime.get("status")?.asString
        )
    }
}

suspend fun fetchTodaysAnime(token: String): List<Map<String, Any>> = withContext(Dispatchers.IO) {
    val url = URL("https://graphql.anilist.co")
    val connection = url.openConnection() as HttpURLConnection
    connection.requestMethod = "POST"
    connection.setRequestProperty("Authorization", "Bearer $token")
    connection.setRequestProperty("Content-Type", "application/json")
    connection.doOutput = true

    val currentTime = System.currentTimeMillis() / 1000
    val tomorrowTime = currentTime + 86400

    val body = """
        {
          "query": "query { Page(page: 1, perPage: 20) { airingSchedules(airingAt_greater: $currentTime, airingAt_lesser: $tomorrowTime) { media { id title { english romaji } } nextAiringEpisode { airingAt episode } } } }"
        }
    """.trimIndent()

    connection.outputStream.write(body.toByteArray())
    val response = connection.inputStream.bufferedReader().readText()
    connection.disconnect()

    val json = JsonParser.parseString(response).asJsonObject
    val schedules = json.getAsJsonObject("data")
        .getAsJsonObject("Page")
        .getAsJsonArray("airingSchedules")

    schedules.map { it.asJsonObject }.map { schedule ->
        mapOf(
            "media" to schedule.getAsJsonObject("media"),
            "nextAiringEpisode" to schedule.getAsJsonObject("nextAiringEpisode")
        )
    }
}
