package com.example

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.net.URL

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(darkTheme = true, dynamicColor = false) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF111318) // Sophisticated Dark Background
                ) {
                    AnimeWallpaperApp()
                }
            }
        }
    }
}

// ==========================================
// MODELS
// ==========================================
data class AnimeWallpaper(
    val id: Int,
    val title: String,
    val category: String,
    val imageUrl: String,
    val author: String,
    val downloads: String,
    val tags: List<String>
)

data class LiveAnimation(
    val id: Int,
    val title: String,
    val videoUrl: String,
    val thumbnailUrl: String,
    val author: String,
    val size: String
)

enum class Tab(val title: String) {
    Home("Home"),
    Wallpapers("Wallpapers"),
    LiveAnimations("Live Theme")
}

enum class WallpaperType {
    HOME, LOCK, BOTH
}

// ==========================================
// STATIC SAMPLE DATA PROVIDER
// ==========================================
object SampleDataProvider {
    val wallpapers = listOf(
        AnimeWallpaper(
            id = 1,
            title = "Cyberpunk Ronin",
            category = "Cyberpunk",
            imageUrl = "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=1080&auto=format&fit=crop",
            author = "Kenji Sato",
            downloads = "14.2K",
            tags = listOf("Cyberpunk", "Neon", "Sword", "Katana")
        ),
        AnimeWallpaper(
            id = 2,
            title = "Tokyo Rain Transit",
            category = "Landscape",
            imageUrl = "https://images.unsplash.com/photo-1541562232579-512a21360020?w=1080&auto=format&fit=crop",
            author = "Miku Chan",
            downloads = "9.5K",
            tags = listOf("City", "Neon", "Rain", "Street")
        ),
        AnimeWallpaper(
            id = 3,
            title = "Celestial Lanterns",
            category = "Fantasy",
            imageUrl = "https://images.unsplash.com/photo-1502404790914-7c98a5091300?w=1080&auto=format&fit=crop",
            author = "Yuki Sen",
            downloads = "18.3K",
            tags = listOf("Fantasy", "Sky", "Mystery", "Clouds")
        ),
        AnimeWallpaper(
            id = 4,
            title = "Kyoto Shrine Path",
            category = "Kyoto",
            imageUrl = "https://images.unsplash.com/photo-1528360983277-13d401cdc186?w=1080&auto=format&fit=crop",
            author = "Haru Tanaka",
            downloads = "26.1K",
            tags = listOf("Kyoto", "Nature", "Cherry Blossom", "Traditional")
        ),
        AnimeWallpaper(
            id = 5,
            title = "Mecha Workshop",
            category = "Mecha",
            imageUrl = "https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?w=1080&auto=format&fit=crop",
            author = "Takahiro Kuro",
            downloads = "8.1K",
            tags = listOf("Mecha", "Robot", "Sci-Fi", "Machine")
        ),
        AnimeWallpaper(
            id = 6,
            title = "Vaporwave Skyline",
            category = "Cyberpunk",
            imageUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=1080&auto=format&fit=crop",
            author = "Rin Retro",
            downloads = "12.8K",
            tags = listOf("Vaporwave", "Sunset", "Aesthetics", "1980s")
        ),
        AnimeWallpaper(
            id = 7,
            title = "Mystic Forest Shrine",
            category = "Kyoto",
            imageUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=1080&auto=format&fit=crop",
            author = "Sora Sky",
            downloads = "6.4K",
            tags = listOf("Nature", "Forest", "Spirit", "Anime Path")
        ),
        AnimeWallpaper(
            id = 8,
            title = "Arcade Retro Dream",
            category = "Fantasy",
            imageUrl = "https://images.unsplash.com/photo-1560942485-b2a11cc13456?w=1080&auto=format&fit=crop",
            author = "Ami A.",
            downloads = "15.0K",
            tags = listOf("Retro", "Arcade", "Vaporwave", "Vibrant")
        )
    )

    val liveAnimations = listOf(
        LiveAnimation(
            id = 1,
            title = "Neon Umbrella Anime Vibe",
            videoUrl = "https://assets.mixkit.co/videos/preview/mixkit-anime-girl-with-glowing-neon-umbrella-43951-large.mp4",
            thumbnailUrl = "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=400&q=80",
            author = "Kimi G.",
            size = "12.4 MB"
        ),
        LiveAnimation(
            id = 2,
            title = "Kyoto Japanese River Flow",
            videoUrl = "https://assets.mixkit.co/videos/preview/mixkit-animation-of-a-scenic-river-in-a-japanese-style-44026-large.mp4",
            thumbnailUrl = "https://images.unsplash.com/photo-1528360983277-13d401cdc186?w=400&q=80",
            author = "Artisan Kyoto",
            size = "15.6 MB"
        ),
        LiveAnimation(
            id = 3,
            title = "Night Neon Tokyo Overpass",
            videoUrl = "https://assets.mixkit.co/videos/preview/mixkit-timelapse-of-street-lights-and-highway-traffic-at-night-42171-large.mp4",
            thumbnailUrl = "https://images.unsplash.com/photo-1541562232579-512a21360020?w=400&q=80",
            author = "Speed Racer",
            size = "8.1 MB"
        ),
        LiveAnimation(
            id = 4,
            title = "Deep Space Cosmic Cosmos",
            videoUrl = "https://assets.mixkit.co/videos/preview/mixkit-sparkles-of-light-on-a-starry-blue-sky-40453-large.mp4",
            thumbnailUrl = "https://images.unsplash.com/photo-1502404790914-7c98a5091300?w=400&q=80",
            author = "Cosmos Design",
            size = "5.3 MB"
        )
    )
}

// ==========================================
// WALLPAPER UTILITIES (REAL DEVICE ACTIONS)
// ==========================================
object WallpaperHelper {
    suspend fun downloadImageToGallery(context: Context, imageUrl: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = URL(imageUrl)
            val connection = url.openConnection()
            connection.doInput = true
            connection.connect()
            val input = connection.getInputStream()
            val bitmap = BitmapFactory.decodeStream(input) ?: return@withContext false

            val filename = "anime_wallpaper_${System.currentTimeMillis()}.jpg"

            val resolver = context.contentResolver
            val imageCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }

            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/AnimeWallpapers")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }

            val imageUri = resolver.insert(imageCollection, contentValues) ?: return@withContext false

            val outputStream = resolver.openOutputStream(imageUri)
            if (outputStream != null) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.close()
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(imageUri, contentValues, null, null)
            }
            true
        } catch (e: Exception) {
            Log.e("WallpaperHelper", "Error downloading wallpaper", e)
            false
        }
    }

    suspend fun setStaticWallpaper(context: Context, imageUrl: String, type: WallpaperType): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = URL(imageUrl)
            val connection = url.openConnection()
            connection.doInput = true
            connection.connect()
            val input = connection.getInputStream()
            val bitmap = BitmapFactory.decodeStream(input) ?: return@withContext false

            val wallpaperManager = WallpaperManager.getInstance(context)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val flag = when (type) {
                    WallpaperType.HOME -> WallpaperManager.FLAG_SYSTEM
                    WallpaperType.LOCK -> WallpaperManager.FLAG_LOCK
                    WallpaperType.BOTH -> WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
                }
                wallpaperManager.setBitmap(bitmap, null, true, flag)
            } else {
                wallpaperManager.setBitmap(bitmap)
            }
            true
        } catch (e: Exception) {
            Log.e("WallpaperHelper", "Error applying static wallpaper", e)
            false
        }
    }
}

// ==========================================
// VIEWMODEL FOR STATE MANAGEMENT
// ==========================================
class AnimeViewModel : ViewModel() {
    private val _wallpapers = MutableStateFlow(SampleDataProvider.wallpapers)
    val wallpapers: StateFlow<List<AnimeWallpaper>> = _wallpapers.asStateFlow()

    private val _liveAnimations = MutableStateFlow(SampleDataProvider.liveAnimations)
    val liveAnimations: StateFlow<List<LiveAnimation>> = _liveAnimations.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _favorites = MutableStateFlow<Set<Int>>(emptySet())
    val favorites: StateFlow<Set<Int>> = _favorites.asStateFlow()

    private val _activeLiveVideoUrl = MutableStateFlow(SampleDataProvider.liveAnimations[0].videoUrl)
    val activeLiveVideoUrl: StateFlow<String> = _activeLiveVideoUrl.asStateFlow()

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleFavorite(id: Int) {
        val current = _favorites.value
        _favorites.value = if (current.contains(id)) current - id else current + id
    }

    fun setActiveLiveVideo(url: String) {
        _activeLiveVideoUrl.value = url
    }
}

// ==========================================
// MAIN COMPOSE SCREEN
// ==========================================
@Composable
fun AnimeWallpaperApp(viewModel: AnimeViewModel = viewModel()) {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf(Tab.Home) }
    var selectedWallpaperForDetail by remember { mutableStateOf<AnimeWallpaper?>(null) }
    val scope = rememberCoroutineScope()

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF1D1B20),
                contentColor = Color(0xFFE2E2E6),
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                Tab.values().forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = when (tab) {
                                    Tab.Home -> if (isSelected) Icons.Filled.Home else Icons.Outlined.Home
                                    Tab.Wallpapers -> if (isSelected) Icons.Filled.PhotoLibrary else Icons.Outlined.PhotoLibrary
                                    Tab.LiveAnimations -> if (isSelected) Icons.Filled.PlayCircle else Icons.Outlined.PlayCircle
                                },
                                contentDescription = tab.title
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color(0xFFD0BCFF) else Color(0xFF919094)
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF381E72),
                            unselectedIconColor = Color(0xFF919094),
                            indicatorColor = Color(0xFFEADDFF)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Screen switching with slide animations
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "TabTransition"
            ) { tab ->
                when (tab) {
                    Tab.Home -> HomeScreen(
                        onWallpaperSelect = { selectedWallpaperForDetail = it },
                        onNavigateToWallpapers = { currentTab = Tab.Wallpapers }
                    )
                    Tab.Wallpapers -> WallpapersScreen(
                        onWallpaperSelect = { selectedWallpaperForDetail = it },
                        viewModel = viewModel
                    )
                    Tab.LiveAnimations -> LiveAnimationsScreen(
                        viewModel = viewModel
                    )
                }
            }

            // High-fidelity Full-Screen Detail Modal for Wallpapers
            selectedWallpaperForDetail?.let { wallpaper ->
                WallpaperDetailOverlay(
                    wallpaper = wallpaper,
                    isFavorite = viewModel.favorites.collectAsStateWithLifecycle().value.contains(wallpaper.id),
                    onToggleFavorite = { viewModel.toggleFavorite(wallpaper.id) },
                    onDismiss = { selectedWallpaperForDetail = null }
                )
            }
        }
    }
}

// ==========================================
// TAB 1: HOME SCREEN
// ==========================================
@Composable
fun HomeScreen(
    onWallpaperSelect: (AnimeWallpaper) -> Unit = {},
    onNavigateToWallpapers: () -> Unit,
    viewModel: AnimeViewModel = viewModel()
) {
    val wallpapers by viewModel.wallpapers.collectAsStateWithLifecycle()
    val liveAnimations by viewModel.liveAnimations.collectAsStateWithLifecycle()
    
    // Featured hero wallpaper (first elements)
    val featuredHero = wallpapers.firstOrNull()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
    ) {
        // Hero Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
        ) {
            if (featuredHero != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(featuredHero.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Featured Anime Hero",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // Cinema Gradient shadow
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0x80111318),
                                    Color(0xFF111318)
                                )
                            )
                        )
                )

                // Info Cards at Bottom of Hero
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(Color(0xFFE53170), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ElectricBolt,
                            contentDescription = "Trending Label",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "TRENDING HERO",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = featuredHero.title,
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Captured by ${featuredHero.author} • ${featuredHero.downloads} Downloads",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row {
                        Button(
                            onClick = { onWallpaperSelect(featuredHero) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFD0BCFF),
                                contentColor = Color(0xFF381E72)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("hero_preview_btn")
                        ) {
                            Icon(Icons.Default.Visibility, contentDescription = "Preview", tint = Color(0xFF381E72))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Preview Now", color = Color(0xFF381E72), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFFD0BCFF))
                }
            }
        }

        // Quick Curated Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Featured Grids",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "See All",
                color = Color(0xFFD0BCFF),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clickable { onNavigateToWallpapers() }
                    .padding(4.dp)
            )
        }

        // Horizontal Row of curated items
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(wallpapers.drop(1)) { item ->
                Card(
                    modifier = Modifier
                        .width(140.dp)
                        .height(200.dp)
                        .clickable { onWallpaperSelect(item) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1D1B20)),
                    border = BorderStroke(1.dp, Color(0xFF44474E))
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(item.imageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = item.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color(0xCC111318))
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(8.dp)
                        ) {
                            Text(
                                text = item.title,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = item.downloads,
                                color = Color.Gray,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }
        }

        // Live Animation Theme Card Preview
        Text(
            text = "Active Live Wallpapers",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 20.dp, top = 24.dp, bottom = 12.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(130.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1D1B20)),
            border = BorderStroke(1.dp, Color(0xFFD0BCFF).copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(120.dp)
                ) {
                    AsyncImage(
                        model = SampleDataProvider.liveAnimations[0].thumbnailUrl,
                        contentDescription = "Live Preview",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(36.dp)
                            .background(Color.Black.copy(alpha = 0.6f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = "Looping Movie Icon",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Animated live loops",
                        color = Color(0xFFE53170),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Full MP4 Live Theme Support",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Enable moving wallpapers seamlessly in high definition.",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}

// ==========================================
// TAB 2: WALLPAPERS GRID SCREEN
// ==========================================
@Composable
fun WallpapersScreen(
    onWallpaperSelect: (AnimeWallpaper) -> Unit = {},
    viewModel: AnimeViewModel
) {
    val wallpapers by viewModel.wallpapers.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    val categories = listOf("All", "Cyberpunk", "Fantasy", "Kyoto", "Mecha", "Landscape")

    // Filter logic
    val filteredWallpapers = remember(wallpapers, selectedCategory, searchQuery) {
        wallpapers.filter { wallpaper ->
            val matchesCategory = selectedCategory == "All" || wallpaper.category.equals(selectedCategory, ignoreCase = true)
            val matchesSearch = searchQuery.isEmpty() || wallpaper.title.contains(searchQuery, ignoreCase = true) || 
                    wallpaper.tags.any { it.contains(searchQuery, ignoreCase = true) }
            matchesCategory && matchesSearch
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Manga Wallpapers",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black
        )
        Text(
            text = "Set amazing static backgrounds instantly",
            color = Color.Gray,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            placeholder = { Text("Search cyberpunk, Kyoto, retro...", color = Color.Gray, fontSize = 14.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search icon", tint = Color.Gray) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear search", tint = Color.Gray)
                    }
                }
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF1D1B20),
                unfocusedContainerColor = Color(0xFF1D1B20),
                focusedBorderColor = Color(0xFFD0BCFF),
                unfocusedBorderColor = Color(0xFF44474E),
                focusedTextColor = Color(0xFFE2E2E6),
                unfocusedTextColor = Color(0xFFE2E2E6)
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("wallpaper_search_input")
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Categories list
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categories) { category ->
                val isSelected = selectedCategory == category
                AssistChip(
                    onClick = { viewModel.selectCategory(category) },
                    label = { Text(category, fontWeight = FontWeight.Bold) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (isSelected) Color(0xFFD0BCFF) else Color(0xFF2F3033),
                        labelColor = if (isSelected) Color(0xFF381E72) else Color(0xFFE2E2E6)
                    ),
                    border = if (isSelected) null else BorderStroke(1.dp, Color(0xFF44474E)),
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredWallpapers.isEmpty()) {
            // Friendly Empty State
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterVintage,
                        contentDescription = "Empty",
                        tint = Color.Gray,
                        modifier = Modifier.size(60.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No matching wallpapers found",
                        color = Color.LightGray,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Try search terms like neon, sunset, or city",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            // 2 column Grid structure
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("wallpapers_grid")
            ) {
                items(filteredWallpapers) { item ->
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .clickable { onWallpaperSelect(item) },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1D1B20)),
                        border = BorderStroke(1.dp, Color(0xFF44474E))
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(item.imageUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = item.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            // Ambient dark overlay at bottom of card
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(Color.Transparent, Color(0xDD111318))
                                        )
                                    )
                            )
                            // Info Text overlays
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = item.title,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Download,
                                        contentDescription = "Downloads",
                                        tint = Color(0xFFD0BCFF),
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "${item.downloads} downloads",
                                        color = Color.Gray,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// TAB 3: LIVE ANIMATIONS SCREEN
// ==========================================
@Composable
fun LiveAnimationsScreen(
    viewModel: AnimeViewModel
) {
    val liveAnimations by viewModel.liveAnimations.collectAsStateWithLifecycle()
    val activeVideoUrl by viewModel.activeLiveVideoUrl.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val currentSelection = remember(liveAnimations, activeVideoUrl) {
        liveAnimations.firstOrNull { it.videoUrl == activeVideoUrl } ?: liveAnimations[0]
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, bottom = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Live Animations",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black
        )
        Text(
            text = "Experience moving high definition video loops",
            color = Color.Gray,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Large Premium Looping Video Preview Card
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(310.dp)
                .testTag("media3_player_container"),
            colors = CardDefaults.cardColors(containerColor = Color.Black),
            border = BorderStroke(1.dp, Color(0xFFD0BCFF).copy(alpha = 0.3f))
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Interactive video preview controller in Compose
                VideoPlayerView(videoUrl = activeVideoUrl)

                // High-fidelity Neon overlay tags for the player
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(14.dp)
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(Color(0xFFD0BCFF), CircleShape) // Premium violet pulse
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LIVE RENDER PREVIEW",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Selected Info Panel
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = currentSelection.title,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Theme Designer: ${currentSelection.author} • Size: ${currentSelection.size}",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Direct Action to App Live Wallpaper Engine
        Button(
            onClick = {
                // 1. Save chosen video link in preferences so service can play it
                val sharedPrefs = context.getSharedPreferences("wallpaper_prefs", Context.MODE_PRIVATE)
                sharedPrefs.edit().putString("wallpaper_video_url", activeVideoUrl).apply()

                Toast.makeText(context, "Opening Live Wallpaper Chooser...", Toast.LENGTH_SHORT).show()

                // 2. Trigger Android Live wallpaper changer
                try {
                    val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
                        putExtra(
                            WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                            ComponentName(context, MyLiveWallpaperService::class.java)
                        )
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                } catch (e: Exception) {
                    // Fallback to general wallpaper service configuration if direct package intent is blocked
                    try {
                        val fallbackIntent = Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(fallbackIntent)
                    } catch (e2: Exception) {
                        Toast.makeText(context, "Failed to launch wallpaper manager. Please set it manually.", Toast.LENGTH_LONG).show()
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFD0BCFF),
                contentColor = Color(0xFF381E72)
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("apply_live_wallpaper_btn")
        ) {
            Icon(Icons.Default.Wallpaper, contentDescription = "Live Apply", tint = Color(0xFF381E72))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Set as Live Home Wallpaper", color = Color(0xFF381E72), fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Select Video Theme",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Selectable Loop rows
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            liveAnimations.forEach { item ->
                val isSelected = item.videoUrl == activeVideoUrl
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = if (isSelected) Color(0xFF2F3033) else Color(0xFF1D1B20),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = if (isSelected) Color(0xFFD0BCFF) else Color(0xFF44474E),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { viewModel.setActiveLiveVideo(item.videoUrl) }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Small thumbnail preview
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        AsyncImage(
                            model = item.thumbnailUrl,
                            contentDescription = item.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.3f))
                        )
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "play-icon",
                            tint = Color.White,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = item.title,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Size: ${item.size} • ${item.author}",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                    }

                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .padding(end = 6.dp)
                                .size(8.dp)
                                .background(Color(0xFFFF8906), CircleShape)
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// MEDIA3 JETPACK COMPOSE MEDIA CONTROLLER VIEW
// ==========================================
@Composable
fun VideoPlayerView(videoUrl: String) {
    val context = LocalContext.current

    // Initialize ExoPlayer once or recreate when url changes
    val exoPlayer = remember(videoUrl) {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(Uri.parse(videoUrl))
            setMediaItem(mediaItem)
            repeatMode = Player.REPEAT_MODE_ALL // Seamless video loop
            volume = 0f // Previews inside the app are muted for elegance
            prepare()
            playWhenReady = true
        }
    }

    // Lifecycle Observer to safely release and pause playback
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, exoPlayer) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    exoPlayer.pause()
                }
                Lifecycle.Event.ON_RESUME -> {
                    exoPlayer.play()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            exoPlayer.release()
        }
    }

    // Embed classic Android PlayerView within Compose
    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = false // No ugly controls, just seamless live canvas preview
                resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            }
        },
        update = { parentView ->
            parentView.player = exoPlayer
        },
        modifier = Modifier.fillMaxSize()
    )
}

// ==========================================
// WALLPAPER FULL-SCREEN OVERLAY DETAIL SCREEN
// ==========================================
@Composable
fun WallpaperDetailOverlay(
    wallpaper: AnimeWallpaper,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isSavingWallpaper by remember { mutableStateOf(false) }
    var isApplyingWallpaper by remember { mutableStateOf(false) }
    var showApplyOptionDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {} // Capture clicks so background doesn't trigger
            )
    ) {
        // High quality Background canvas loading
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(wallpaper.imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = wallpaper.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Movie theatrical gradient fade
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x99000000),
                            Color.Transparent,
                            Color(0xDD000000)
                        )
                    )
                )
        )

        // Top Actions (Back Button & Favorite)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    .testTag("detail_back_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) Color(0xFFD0BCFF) else Color.White
                )
            }
        }

        // Bottom Actions Pane
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(20.dp)
        ) {
            // Text Meta info
            Text(
                text = wallpaper.title,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "Artist: ${wallpaper.author} • Category: ${wallpaper.category}",
                color = Color.LightGray,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Download Button
                Button(
                    onClick = {
                        if (!isSavingWallpaper) {
                            scope.launch {
                                isSavingWallpaper = true
                                Toast.makeText(context, "Saving background to Pictures...", Toast.LENGTH_SHORT).show()
                                val success = WallpaperHelper.downloadImageToGallery(context, wallpaper.imageUrl)
                                isSavingWallpaper = false
                                if (success) {
                                    Toast.makeText(context, "Image successfully saved inside Gallery!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Downloading failed. Network or Storage issue.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2F3033),
                        contentColor = Color(0xFFE2E2E6)
                    ),
                    border = BorderStroke(1.dp, Color(0xFF44474E)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("download_wallpaper_btn"),
                    enabled = !isSavingWallpaper
                ) {
                    if (isSavingWallpaper) {
                        CircularProgressIndicator(color = Color(0xFFE2E2E6), modifier = Modifier.size(20.dp))
                    } else {
                        Icon(Icons.Default.Download, contentDescription = "Download", tint = Color(0xFFE2E2E6))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Gallery", color = Color(0xFFE2E2E6), fontWeight = FontWeight.Bold)
                    }
                }

                // Set wallpaper Button
                Button(
                    onClick = { showApplyOptionDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD0BCFF),
                        contentColor = Color(0xFF381E72)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("set_wallpaper_btn"),
                    enabled = !isApplyingWallpaper
                ) {
                    if (isApplyingWallpaper) {
                        CircularProgressIndicator(color = Color(0xFF381E72), modifier = Modifier.size(20.dp))
                    } else {
                        Icon(Icons.Default.Wallpaper, contentDescription = "Set Wall", tint = Color(0xFF381E72))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Set Wallpaper", color = Color(0xFF381E72), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Apply Option Selection Dialog Overlay
        if (showApplyOptionDialog) {
            Dialog(
                onDismissRequest = { showApplyOptionDialog = false },
                properties = DialogProperties(usePlatformDefaultWidth = true)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1D1B20)),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, Color(0xFF44474E)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Apply Wallpaper",
                            color = Color(0xFFE2E2E6),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Where would you like to set this artwork?",
                            color = Color(0xFF919094),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Home Screen Option
                        Button(
                            onClick = {
                                showApplyOptionDialog = false
                                scope.launch {
                                    isApplyingWallpaper = true
                                    Toast.makeText(context, "Setting Home Screen Wallpaper...", Toast.LENGTH_SHORT).show()
                                    val success = WallpaperHelper.setStaticWallpaper(context, wallpaper.imageUrl, WallpaperType.HOME)
                                    isApplyingWallpaper = false
                                    if (success) {
                                        Toast.makeText(context, "Applied successfully!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Application failed.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().testTag("apply_home_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2F3033),
                                contentColor = Color(0xFFE2E2E6)
                            ),
                            border = BorderStroke(1.dp, Color(0xFF44474E)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Home Screen ScreenOnly", color = Color(0xFFE2E2E6))
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Lock Screen Option
                        Button(
                            onClick = {
                                showApplyOptionDialog = false
                                scope.launch {
                                    isApplyingWallpaper = true
                                    Toast.makeText(context, "Setting Lock Screen Wallpaper...", Toast.LENGTH_SHORT).show()
                                    val success = WallpaperHelper.setStaticWallpaper(context, wallpaper.imageUrl, WallpaperType.LOCK)
                                    isApplyingWallpaper = false
                                    if (success) {
                                        Toast.makeText(context, "Applied successfully!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Application failed.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().testTag("apply_lock_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2F3033),
                                contentColor = Color(0xFFE2E2E6)
                            ),
                            border = BorderStroke(1.dp, Color(0xFF44474E)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Lock Screen ScreenOnly", color = Color(0xFFE2E2E6))
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Both Option
                        Button(
                            onClick = {
                                showApplyOptionDialog = false
                                scope.launch {
                                    isApplyingWallpaper = true
                                    Toast.makeText(context, "Setting both screens...", Toast.LENGTH_SHORT).show()
                                    val success = WallpaperHelper.setStaticWallpaper(context, wallpaper.imageUrl, WallpaperType.BOTH)
                                    isApplyingWallpaper = false
                                    if (success) {
                                        Toast.makeText(context, "Applied successfully!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Application failed.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().testTag("apply_both_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFD0BCFF),
                                contentColor = Color(0xFF381E72)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Home & Lock Screen Both", color = Color(0xFF381E72), fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        TextButton(
                            onClick = { showApplyOptionDialog = false }
                        ) {
                            Text("Cancel", color = Color(0xFF919094), fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}
