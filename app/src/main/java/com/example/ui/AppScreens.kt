package com.example.ui

import androidx.compose.animation.*
import android.net.Uri
import android.widget.VideoView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.BorderStroke
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Canvas
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.outlined.ListAlt
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import com.example.R
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.*
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.RedAlert
import com.example.ui.theme.AmberPending
import java.text.SimpleDateFormat
import java.util.*

// --- MAIN ENTRY CONTAINER WITH BOTTOM NAVIGATION ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContainer(
    viewModel: AppViewModel,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val dataSaverMode by viewModel.dataSaverMode.collectAsState()
    val selectedTheme by viewModel.selectedTheme.collectAsState()
    val profile by viewModel.currentProfile.collectAsState()

    var showSettings by remember { mutableStateOf(false) }
    var showDrawer by remember { mutableStateOf(false) }
    var showNotifications by remember { mutableStateOf(false) }

    // Intercept standard Android system back-press gestures
    androidx.activity.compose.BackHandler(
        enabled = currentTab != 0 || showDrawer || showNotifications || showSettings
    ) {
        if (showDrawer) {
            showDrawer = false
        } else if (showNotifications) {
            showNotifications = false
        } else if (showSettings) {
            showSettings = false
        } else {
            viewModel.setTab(0)
        }
    }

    Box(modifier = modifier) {
        Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                0 -> HomeScreen(
                    viewModel = viewModel,
                    onOpenSettings = { showSettings = true },
                    onOpenNotifications = { showNotifications = true },
                    onOpenDrawer = { showDrawer = true }
                )
                1 -> PaymentScreen(viewModel)
                2 -> ResultsScreen(viewModel)
                3 -> ScheduleScreen(viewModel)
                4 -> ChatScreen(viewModel)
                5 -> LibraryScreen(viewModel)
                6 -> QrCheckInScreen(viewModel)
                7 -> TpSubmissionScreen(viewModel)
                8 -> StudentForumScreen(viewModel)
                9 -> CareersHubScreen(viewModel)
                10 -> AiTutorScreen(viewModel)
                11 -> DigitalCardScreen(viewModel)
            }

            // Slide in settings screen if dialog triggers
            if (showSettings) {
                SettingsDialog(
                    viewModel = viewModel,
                    onDismiss = { showSettings = false },
                    onLogout = {
                        showSettings = false
                        viewModel.logout()
                        onLogout()
                    }
                )
            }
        }
    }

    // Full-screen Slide-in Lateral Menu (Drawer overlay)
    androidx.compose.animation.AnimatedVisibility(
        visible = showDrawer,
        enter = slideInHorizontally(initialOffsetX = { -it }) + fadeIn(),
        exit = slideOutHorizontally(targetOffsetX = { -it }) + fadeOut()
    ) {
        FullScreenDrawer(
            currentTab = currentTab,
            viewModel = viewModel,
            onClose = { showDrawer = false },
            onLogout = onLogout,
            onOpenSettings = { showSettings = true }
        )
    }

    // Full-screen Notifications Center
    if (showNotifications) {
        NotificationsScreenDialog(
            onDismiss = { showNotifications = false }
        )
    }
}
}

// --- NEW OVERLAYS & CUSTOM COMPOSABLES per USER REQUESTS ---

@Composable
fun LogoSplashScreen(onFinished: () -> Unit) {
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(2200)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF00054F)), // Dark Indigo corporate UDBL blue
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Circle with white background matching the screenshot icon
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .background(Color.White, CircleShape)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = androidx.compose.ui.res.painterResource(id = R.drawable.udbl_logo_asset_1779824734235),
                    contentDescription = "UDBL Logo",
                    modifier = Modifier.fillMaxSize(0.85f)
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Welcome",
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun DrawerMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (isActive) Color(0xFF00054F).copy(alpha = 0.12f) else Color.Transparent
    val contentColor = if (isActive) Color(0xFF00054F) else Color.Black.copy(alpha = 0.8f)
    val fontW = if (isActive) FontWeight.ExtraBold else FontWeight.SemiBold

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = label,
                color = contentColor,
                fontWeight = fontW,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
fun FullScreenDrawer(
    currentTab: Int,
    viewModel: AppViewModel,
    onClose: () -> Unit,
    onLogout: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onClose() }
    ) {
        Column(
            modifier = Modifier
                .width(310.dp)
                .fillMaxHeight()
                .background(Color.White, RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp))
                .clickable(enabled = false) {}
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                Image(
                    painter = androidx.compose.ui.res.painterResource(id = R.drawable.img_university_campus_1779884165837_1779975539656),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF0114C7).copy(alpha = 0.65f),
                                    Color(0xFF00054F).copy(alpha = 0.90f)
                                )
                            )
                        )
                )

                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Fermer",
                        tint = Color.White
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(Color.White, CircleShape)
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = androidx.compose.ui.res.painterResource(id = R.drawable.udbl_logo_asset_1779824734235),
                                contentDescription = "UDBL Logo",
                                modifier = Modifier.fillMaxSize().clip(CircleShape)
                            )
                        }
                        Column {
                            Text(
                                text = "UDBL VISION",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Don Bosco Lubumbashi",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Scientia et Fides",
                        color = Color(0xFFFBBF24),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "NAVIGATION ACADÉMIQUE",
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    DrawerMenuItem(
                        icon = Icons.Default.Home,
                        label = "Accueil",
                        isActive = currentTab == 0,
                        onClick = {
                            viewModel.setTab(0)
                            onClose()
                        }
                    )
                    DrawerMenuItem(
                        icon = Icons.Default.AccountBalanceWallet,
                        label = "Frais & Paiements",
                        isActive = currentTab == 1,
                        onClick = {
                            viewModel.setTab(1)
                            onClose()
                        }
                    )
                    DrawerMenuItem(
                        icon = Icons.Default.Grading,
                        label = "Résultats d'Examens",
                        isActive = currentTab == 2,
                        onClick = {
                            viewModel.setTab(2)
                            onClose()
                        }
                    )
                    DrawerMenuItem(
                        icon = Icons.Default.CalendarToday,
                        label = "Horaire de Cours",
                        isActive = currentTab == 3,
                        onClick = {
                            viewModel.setTab(3)
                            onClose()
                        }
                    )
                    DrawerMenuItem(
                        icon = Icons.Default.Chat,
                        label = "Messagerie & Chat",
                        isActive = currentTab == 4,
                        onClick = {
                            viewModel.setTab(4)
                            onClose()
                        }
                    )
                    DrawerMenuItem(
                        icon = Icons.Default.Settings,
                        label = "Paramètres de Profil",
                        isActive = false,
                        onClick = {
                            onClose()
                            onOpenSettings()
                        }
                    )
                }

                Button(
                    onClick = {
                        onClose()
                        viewModel.logout()
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC70101)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Déconnexion icon",
                            tint = Color.White
                        )
                        Text(
                            text = "Déconnexion",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationsScreenDialog(
    onDismiss: () -> Unit
) {
    var notificationsList by remember {
        mutableStateOf(
            listOf(
                NotificationItemData(
                    id = 1,
                    type = "payment",
                    title = "💳 Reçu de Frais Certifié",
                    description = "Votre tranche académique #1 de 350.00 USD pour le cycle de Licence 2 a été enregistrée avec succès. Reçu #UDBL-9844116-2026 téléchargeable.",
                    time = "Aujourd'hui, 09:12",
                    isUnread = true
                ),
                NotificationItemData(
                    id = 2,
                    type = "academic",
                    title = "📊 Bulletins Semestre 1 Disponibles",
                    description = "Le relevé officiel des cotes de la première session a été publié par le jury de la Faculté des Sciences. Vous pouvez consulter l'onglet Résultats.",
                    time = "Aujourd'hui, 08:30",
                    isUnread = true
                ),
                NotificationItemData(
                    id = 3,
                    type = "schedule",
                    title = "📅 Changement d'Horaire Administratif",
                    description = "Le cours de 'Génie Logiciel approfondi' prévu ce Jeudi à 08:30 est déplacé au Vendredi à 14:00 en Salle M-203 (Bâtiment Principal Lumumba).",
                    time = "Hier, 15:20",
                    isUnread = false
                ),
                NotificationItemData(
                    id = 4,
                    type = "academic",
                    title = "📣 Avis Direct: Recours Approuvé",
                    description = "Votre recours académique concernant le cours de 'Management de l'Information' a été validé par le secrétariat. Votre cote finale passe de 09 à 14/20.",
                    time = "25 Mai 2026",
                    isUnread = false
                )
            )
        )
    }

    var selectedFilter by remember { mutableStateOf("Tous") }

    Dialog(
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        onDismissRequest = onDismiss
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Fermer",
                                tint = Color.Black
                            )
                        }
                        Text(
                            text = "Notifications",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF00054F)
                        )
                    }

                    if (notificationsList.isNotEmpty()) {
                        TextButton(
                            onClick = { notificationsList = emptyList() }
                        ) {
                            Text(
                                "Tout effacer",
                                color = Color(0xFFC70101),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                // Filtering chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val filters = listOf("Tous", "Académique", "Paiements")
                    filters.forEach { filter ->
                        val isSelected = selectedFilter == filter
                        Surface(
                            onClick = { selectedFilter = filter },
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) Color(0xFF00054F) else Color(0xFFF0F2F5),
                            contentColor = if (isSelected) Color.White else Color.Black.copy(alpha = 0.7f),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(filter, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // List
                val filteredList = notificationsList.filter {
                    when (selectedFilter) {
                        "Académique" -> it.type == "academic" || it.type == "schedule"
                        "Paiements" -> it.type == "payment"
                        else -> true
                    }
                }

                if (filteredList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsNone,
                                contentDescription = null,
                                tint = Color.Gray.copy(alpha = 0.5f),
                                modifier = Modifier.size(80.dp)
                            )
                            Text(
                                text = "Aucune notification",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = "Vous resterez notifié des avis de cours, relevés de notes officiels et tranches de caisse validés.",
                                fontSize = 12.sp,
                                color = Color.Gray.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredList.size) { index ->
                            val item = filteredList[index]
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = if (item.isUnread) Color(0xFFF4F7FF) else Color(0xFFFAFAFA)),
                                shape = RoundedCornerShape(14.dp),
                                border = if (item.isUnread) BorderStroke(1.dp, Color(0xFF00054F).copy(alpha = 0.2f)) else null
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Bullet color
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .align(Alignment.CenterVertically)
                                            .background(
                                                color = if (item.isUnread) Color(0xFF00054F) else Color.Transparent,
                                                shape = CircleShape
                                            )
                                    )

                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = item.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color.Black
                                        )
                                        Text(
                                            text = item.description,
                                            fontSize = 12.sp,
                                            color = Color.Black.copy(alpha = 0.75f),
                                            lineHeight = 18.sp
                                        )
                                        Text(
                                            text = item.time,
                                            fontSize = 10.sp,
                                            color = Color.Gray,
                                            fontWeight = FontWeight.Medium
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
}

data class NotificationItemData(
    val id: Int,
    val type: String, // "payment", "academic", "schedule"
    val title: String,
    val description: String,
    val time: String,
    val isUnread: Boolean
)

// --- WELCOME SPLASHES & CUSTOM SHAPES ---

@Composable
fun UdblLogo(modifier: Modifier = Modifier) {
    Image(
        painter = androidx.compose.ui.res.painterResource(id = R.drawable.udbl_logo_asset_1779824734235),
        contentDescription = "UDBL Logo",
        modifier = modifier
    )
}

@Composable
fun StudentAvatar(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        
        // Draw background circle (bright green or gold accent)
        drawCircle(color = Color(0xFF0C9C3E), radius = w * 0.48f)
        
        // Draw head
        drawCircle(
            color = Color(0xFFFFD1A9), // skin tone
            radius = w * 0.25f,
            center = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.38f)
        )
        
        // Draw shirt/shoulders
        val shoulderPath = androidx.compose.ui.graphics.Path().apply {
            moveTo(w * 0.2f, h)
            quadraticTo(w * 0.5f, h * 0.62f, w * 0.8f, h)
            close()
        }
        drawPath(shoulderPath, color = Color(0xFF00054F))
        
        // Draw glasses
        // Left lens
        drawCircle(
            color = Color.Black,
            radius = w * 0.08f,
            center = androidx.compose.ui.geometry.Offset(w * 0.42f, h * 0.38f),
            style = Stroke(width = w * 0.02f)
        )
        // Right lens
        drawCircle(
            color = Color.Black,
            radius = w * 0.08f,
            center = androidx.compose.ui.geometry.Offset(w * 0.58f, h * 0.38f),
            style = Stroke(width = w * 0.02f)
        )
        // Bridge
        drawLine(
            color = Color.Black,
            start = androidx.compose.ui.geometry.Offset(w * 0.47f, h * 0.38f),
            end = androidx.compose.ui.geometry.Offset(w * 0.53f, h * 0.38f),
            strokeWidth = w * 0.02f
        )
        
        // Smile
        val mouthPath = androidx.compose.ui.graphics.Path().apply {
            arcTo(
                rect = androidx.compose.ui.geometry.Rect(
                    left = w * 0.45f,
                    top = h * 0.44f,
                    right = w * 0.55f,
                    bottom = h * 0.52f
                ),
                startAngleDegrees = 0f,
                sweepAngleDegrees = 180f,
                forceMoveTo = true
            )
        }
        drawPath(mouthPath, color = Color.Black, style = Stroke(width = w * 0.02f))
    }
}

@Composable
fun DiamondProgressBar(
    progress: Float, // 0f to 1f
    labelAbove: String? = null,
    labelBelow: String? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (labelAbove != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = labelAbove,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00054F),
                    modifier = Modifier.offset(y = (-2).dp)
                )
            }
        }
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            // Track background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE5E7EB))
            )
            // Progress filled
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF00054F))
            )
            // Diamond thumb aligned exactly at the end of the progress filled
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress),
                contentAlignment = Alignment.CenterEnd
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(Color(0xFF00054F))
                        .graphicsLayer {
                            rotationZ = 45f
                        }
                )
            }
        }
        
        if (labelBelow != null) {
            Text(
                text = labelBelow,
                fontSize = 10.sp,
                color = Color.Black.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
fun GridItemCard(
    date: String,
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    statusColor: Color = Color(0xFF0C9C3E),
    progress: Float,
    labelAbove: String? = null,
    labelBelow: String? = null,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: Date & Green dot
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = date,
                    fontSize = 11.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(statusColor, CircleShape)
                )
            }
            
            // Icon
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF00054F),
                modifier = Modifier.size(36.dp)
            )
            
            // Text: Title & Subtitle
            Column {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
            
            // Progress details
            DiamondProgressBar(
                progress = progress,
                labelAbove = labelAbove,
                labelBelow = labelBelow
            )
        }
    }
}

// --- HOME/ACCUEIL SCREEN ---
@Composable
fun HomeScreen(
    viewModel: AppViewModel,
    onOpenSettings: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenDrawer: () -> Unit
) {
    val profile by viewModel.currentProfile.collectAsState()
    val annonces by viewModel.annoncesState.collectAsState()
    val dataSaverMode by viewModel.dataSaverMode.collectAsState()

    var selectedAnnonceForDetail by remember { mutableStateOf<Annonce?>(null) }
    val displayName = profile?.name ?: "John Doe"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Simple Top AppBar style header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.GridView,
                    contentDescription = "Menu Grid",
                    tint = Color.Black,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { onOpenDrawer() }
                )
                
                Text(
                    text = "Home",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.Black
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsNone,
                        contentDescription = "Notifications",
                        tint = Color.Black,
                        modifier = Modifier
                            .size(28.dp)
                            .clickable { onOpenNotifications() }
                    )
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Paramètres",
                        tint = Color.Black,
                        modifier = Modifier
                            .size(28.dp)
                            .clickable { onOpenSettings() }
                    )
                }
            }
        }

        // Welcome greeting
        item {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Hi  $displayName !",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "Good morning",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Black.copy(alpha = 0.8f)
                )
            }
        }

        // University gorgeous central banner with beautiful photo background
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Image(
                    painter = androidx.compose.ui.res.painterResource(id = R.drawable.img_university_campus_1779884165837_1779975539656),
                    contentDescription = "University Campus Banner Info",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(3.28f),
                    contentScale = ContentScale.FillWidth
                )
            }
        }

        // Services Étudiants horizontaux
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "SERVICES NATIONAUX & ACADÉMIQUES",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(top = 8.dp)
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val quickServices = listOf(
                        Triple("Bibliothèque", Icons.Default.MenuBook, 5),
                        Triple("Présence QR", Icons.Default.QrCode, 6),
                        Triple("Devoirs & TP", Icons.Default.Assignment, 7),
                        Triple("Forum Entraide", Icons.Default.Forum, 8),
                        Triple("Stages & Emplois", Icons.Default.Work, 9),
                        Triple("Tuteur IA AI", Icons.Default.Psychology, 10),
                        Triple("Carte Digitale", Icons.Default.AccountBox, 11)
                    )

                    quickServices.forEach { (label, icon, tabIdx) ->
                        Card(
                            onClick = { viewModel.setTab(tabIdx) },
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(1.dp),
                            modifier = Modifier
                                .width(120.dp)
                                .height(100.dp)
                                .testTag("service_chip_$tabIdx")
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color(0xFF00054F).copy(alpha = 0.08f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = label,
                                        tint = Color(0xFF00054F),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // Circular option filters, matching Image 4
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(Icons.Default.School, Icons.Default.CheckCircle, Icons.Default.MenuBook).forEach { icon ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .size(54.dp)
                            .border(BorderStroke(3.dp, Color(0xFF00054F)), CircleShape)
                            .background(Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Color(0xFF00054F),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        // 2x2 Grid card, matching Image 4
        item {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Row 1
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        GridItemCard(
                            date = "27 mai 2026",
                            title = "Horaire",
                            subtitle = "Hebdomadaire",
                            icon = Icons.Default.CalendarToday,
                            progress = 0.5f,
                            labelBelow = "Mer",
                            onClick = { viewModel.setTab(3) }
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        GridItemCard(
                            date = "27 mai 2026",
                            title = "Evénements",
                            subtitle = "Académique",
                            icon = Icons.Default.Celebration,
                            progress = 0.82f,
                            labelAbove = "Jour J-10",
                            labelBelow = "Fête à Marie Auxiliatrice",
                            onClick = {
                                // show events clicking action dialog
                            }
                        )
                    }
                }

                // Row 2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        GridItemCard(
                            date = "27 mai 2026",
                            title = "Paiements",
                            subtitle = "Académique",
                            icon = Icons.Default.AccountBalance,
                            progress = 0.35f,
                            labelAbove = "10 jrs",
                            labelBelow = "Délai restant",
                            onClick = { viewModel.setTab(1) }
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        GridItemCard(
                            date = "27 mai 2026",
                            title = "Résultats",
                            subtitle = "Académique",
                            icon = Icons.Default.ListAlt,
                            progress = 0.8f,
                            labelAbove = "Jour J-10",
                            onClick = { viewModel.setTab(2) }
                        )
                    }
                }
            }
        }

        // Program of the day section, matching Image 4
        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Text(
                    text = "PROGRAM OF THE DAY",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = Color.Black
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, RoundedCornerShape(16.dp))
                        .background(Color.White, RoundedCornerShape(16.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Circular badge 08:12
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(Color(0xFF0C9C3E), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("08 12", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Matin", color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp)
                        }
                    }
                    // Circular badge 13:17
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .border(BorderStroke(2.dp, Color(0xFF00054F)), CircleShape)
                            .background(Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("13 17", color = Color(0xFF00054F), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Prémidi", color = Color.Gray, fontSize = 10.sp)
                        }
                    }
                    
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Compilation & Génie Logiciel", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Auditoire A1 - Prof. Mukendi", color = Color.Gray, fontSize = 11.sp)
                    }
                }
            }
        }

        // Announcements section below the program grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Avis & Communiqués de l'UDBL",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Icon(
                    imageVector = Icons.Default.OfflinePin,
                    contentDescription = "Mise en cache hors-ligne",
                    tint = Color(0xFF0C9C3E)
                )
            }
        }

        if (annonces.isEmpty()) {
            item {
                Text(
                    text = "Aucun communiqué disponible pour le moment.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        } else {
            items(annonces) { annonce ->
                Box(
                    modifier = Modifier.clickable { selectedAnnonceForDetail = annonce }
                ) {
                    AnnonceCard(annonce = annonce, dataSaver = dataSaverMode)
                }
            }
        }
    }

    if (selectedAnnonceForDetail != null) {
        AnnonceDetailDialog(
            annonce = selectedAnnonceForDetail!!,
            onDismiss = { selectedAnnonceForDetail = null }
        )
    }
}

@Composable
fun ShortcutCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(110.dp)
            .clickable(onClick = onClick)
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)), RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun AnnonceCard(annonce: Annonce, dataSaver: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                BorderStroke(
                    1.dp,
                    if (annonce.category == "Urgent") MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                    else Color.White.copy(alpha = 0.05f)
                ),
                RoundedCornerShape(24.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = if (annonce.category == "Urgent") MaterialTheme.colorScheme.errorContainer
                    else if (annonce.category == "Académique") MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = annonce.category,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (annonce.category == "Urgent") MaterialTheme.colorScheme.onErrorContainer
                        else if (annonce.category == "Académique") MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Text(
                    text = annonce.dateString,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = annonce.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // If low data dataSaver is on, we show shorter compressed announcement snippet by default
            Text(
                text = if (dataSaver && annonce.content.length > 120) {
                    annonce.content.take(120) + "..."
                } else {
                    annonce.content
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )
        }
    }
}

// --- ACADEMIC FEE PAYMENTS SCREEN ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(viewModel: AppViewModel) {
    val payments by viewModel.allPaymentsState.collectAsState()
    val dataSaverMode by viewModel.dataSaverMode.collectAsState()

    var selectedPaymentForReceipt by remember { mutableStateOf<Payment?>(null) }
    var showForm by remember { mutableStateOf(false) }
    var selectedWallet by remember { mutableStateOf("M-Pesa") }
    var amountText by remember { mutableStateOf("") }
    var phoneText by remember { mutableStateOf("") }
    var targetFeeType by remember { mutableStateOf("Frais Académiques") }

    val feeTypes = listOf("Frais Académiques", "Enrôlement Examen", "Frais Connexes (Cartes/Inscriptions)")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.setTab(0) },
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Retour",
                        tint = Color(0xFF00054F)
                    )
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Paiements Mobiles",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Canaux locaux sécurisés",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = { showForm = true },
                    modifier = Modifier.testTag("initiate_payment_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Nouveau")
                }
            }
        }

        // Wallet info card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)), RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Agréé par l'Administration de l'UDBL",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Les transactions s'intègrent instantanément. Évitez les fraudes et les retards bancaires.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "Historique d'Acquittement (Sauvegardé)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (payments.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Aucun paiement trouvé dans votre historique local.")
                }
            }
        } else {
            items(payments) { payment ->
                Box(
                    modifier = Modifier.clickable { selectedPaymentForReceipt = payment }
                ) {
                    PaymentRow(payment = payment)
                }
            }
        }
    }

    if (selectedPaymentForReceipt != null) {
        PaymentReceiptDialog(
            payment = selectedPaymentForReceipt!!,
            onDismiss = { selectedPaymentForReceipt = null }
        )
    }

    // Modal Form for Local Mobiles payments (M-Pesa, Orange, Airtel)
    if (showForm) {
        Dialog(onDismissRequest = { showForm = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Effectuer un Paiement",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    // Selected Mobile Money Wallet
                    Text(
                        text = "Moyen de paiement local",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("M-Pesa", "Airtel", "Orange").forEach { wallet ->
                            val isSelected = selectedWallet == wallet
                            Button(
                                onClick = { selectedWallet = wallet },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(wallet, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Intended fee type select
                    var expandedFeeType by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expandedFeeType,
                        onExpandedChange = { expandedFeeType = !expandedFeeType }
                    ) {
                        OutlinedTextField(
                            value = targetFeeType,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Frais visés") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFeeType) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedFeeType,
                            onDismissRequest = { expandedFeeType = false }
                        ) {
                            feeTypes.forEach { selectionOption ->
                                DropdownMenuItem(
                                    text = { Text(selectionOption) },
                                    onClick = {
                                        targetFeeType = selectionOption
                                        expandedFeeType = false
                                    }
                                )
                            }
                        }
                    }

                    // Amount input (USD)
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("Montant (USD)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("payment_amount_input"),
                        prefix = { Text("$ ") }
                    )

                    // Student phone number
                    OutlinedTextField(
                        value = phoneText,
                        onValueChange = { phoneText = it },
                        label = { Text("Numéro Téléphone Mobile Money") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        placeholder = { Text("Ex: +243812345678") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("payment_phone_input")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showForm = false }) {
                            Text("Annuler")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val amountVal = amountText.toDoubleOrNull() ?: 0.0
                                if (amountVal > 0.0 && phoneText.isNotEmpty()) {
                                    viewModel.payFees(
                                        amount = amountVal,
                                        walletType = selectedWallet,
                                        phone = phoneText,
                                        paymentType = targetFeeType,
                                        onComplete = {
                                            showForm = false
                                        }
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.testTag("confirm_payment_button")
                        ) {
                            Text("Payer")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentRow(payment: Payment) {
    val dateString = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.FRENCH).format(Date(payment.dateMillis))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)), RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = payment.paymentType,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Via ${payment.walletType} • ${payment.phoneNumber}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Réf: ${payment.reference}",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = String.format(Locale.US, "+$%.1f USD", payment.amount),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = GreenSuccess
                )
                Text(
                    text = dateString,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// --- GRADES & RECOURS (RESULTS) SCREEN ---
@Composable
fun VerticalCourseBar(
    name: String,
    grade: Double,
    maxGrade: Double = 20.0,
    maxHeight: androidx.compose.ui.unit.Dp = 120.dp
) {
    val heightFraction = (grade / maxGrade).coerceIn(0.0, 1.0).toFloat()
    val barColor = when {
        grade >= 12.0 -> Color(0xFF0C9C3E) // Green Success
        grade >= 9.0 -> Color(0xFFFBBF24) // Yellow Warning
        else -> Color(0xFFEF4444) // Red Alert/Fail
    }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
        modifier = Modifier.fillMaxHeight()
    ) {
        // Vertical text name
        Text(
            text = name,
            color = Color.White,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .graphicsLayer {
                    rotationZ = -90f
                }
                .padding(bottom = 12.dp)
                .width(70.dp), // fixed width to limit height
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Bar cylinder
        Box(
            modifier = Modifier
                .width(22.dp)
                .height(maxHeight * heightFraction)
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(barColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${grade.toInt()}",
                color = if (grade >= 12.0) Color.White else Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun BanquesCard(
    regle: Double = 600.0,
    dette: Double = 350.0,
    onFloatingButtonClick: () -> Unit
) {
    val total = regle + dette
    val sweepAnglePaid = (regle / total * 360).toFloat()
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF00054F)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Bullet and header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(Color.White, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Banques",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Legend
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .background(Color.Black)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Reglé : ${regle.toInt()}$",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .background(Color.White)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Dettes : - ${dette.toInt()}$",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    // Circular progress arc
                    Box(
                        modifier = Modifier.size(90.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(80.dp)) {
                            // Draw grey track/debt segment in white, and paid segment in black
                            // White track background
                            drawArc(
                                color = Color.White,
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width = 16f)
                            )
                            // Paid black segment
                            drawArc(
                                color = Color.Black,
                                startAngle = -90f,
                                sweepAngle = sweepAnglePaid,
                                useCenter = false,
                                style = Stroke(width = 16f, cap = StrokeCap.Round)
                            )
                        }
                        
                        // Golden circle in center
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(0xFFFBBF24), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$",
                                color = Color.Black,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
            
            // Green floating action button inside the Bank card, in bottom right
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 8.dp, y = 8.dp)
                    .background(Color(0xFF0C9C3E), CircleShape)
                    .clickable { onFloatingButtonClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBalance,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun ResultsScreen(viewModel: AppViewModel) {
    val results by viewModel.academicResultsState.collectAsState()
    val recourses by viewModel.recoursesState.collectAsState()
    val profile by viewModel.currentProfile.collectAsState()

    var selectedResultForDetail by remember { mutableStateOf<AcademicResult?>(null) }
    var showRecourseDialog by remember { mutableStateOf(false) }
    var selectedResultForRecourse by remember { mutableStateOf<AcademicResult?>(null) }

    var expectedGradeText by remember { mutableStateOf("") }
    var explanationText by remember { mutableStateOf("") }

    val displayName = profile?.name ?: "John Doe"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top bars matching Header in Image 5
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.setTab(0) }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Retour",
                        tint = Color.Black,
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                Text(
                    text = "Mes stats",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.Black
                )
                
                Icon(
                    imageVector = Icons.Default.NotificationsNone,
                    contentDescription = "Notifications",
                    tint = Color.Black,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // Header bar chart visual icon and Greetings matching Image 5
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.BarChart,
                    contentDescription = null,
                    tint = Color(0xFF00054F),
                    modifier = Modifier.size(64.dp)
                )
                
                Text(
                    text = "Hi  $displayName !",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "Good morning",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Black.copy(alpha = 0.8f)
                )
            }
        }

        // Top advice pill banner: "Manges 5 fruits et legumes par Jour - Pour mieux te porter"
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF00054F)),
                shape = CircleShape
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Manges 5 fruits et legumes par Jour - Pour mieux te porter",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Black Card of stats "Mes Moyennes", matching Image 5
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .shadow(6.dp, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.Black),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Column indicator header label
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(Color.White, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Mes Moyennes",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Bars container
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        // Math 2, DAO 8, VIH 10, EOE 5, Communications visuelle 9, Algorithmique 5, Base de données 7
                        val chartData = listOf(
                            Pair("Math", 2.0),
                            Pair("DAO", 8.0),
                            Pair("VIH", 10.0),
                            Pair("EOE", 5.0),
                            Pair("Com. Visu.", 9.0),
                            Pair("Algo.", 5.0),
                            Pair("BDD", 7.0)
                        )
                        
                        chartData.forEach { (name, grade) ->
                            VerticalCourseBar(name = name, grade = grade)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Dots indicators
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(4) { index ->
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .size(8.dp)
                                    .background(
                                        if (index == 0) Color.White else Color.White.copy(alpha = 0.3f),
                                        CircleShape
                                    )
                            )
                        }
                    }
                }
            }
        }

        // Banques Payments tracker card, matching Image 5
        item {
            BanquesCard(
                regle = 600.0,
                dette = 350.0,
                onFloatingButtonClick = {
                    viewModel.setTab(1) // redirection to Payments/Wallet Tab
                }
            )
        }

        // Bottom Advice capsule banner, matching Image 5: "Dors 8 heures par jour - pour plus d'efficacité"
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0C9C3E)),
                shape = CircleShape
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Dors 8 heures par jour - pour plus d'efficacité",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Section header for Notes/Result list matching "meme contenu"
        item {
            Text(
                text = "Liste Complète de mes Notes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }

        if (results.isEmpty()) {
            item {
                Text("Aucun résultat répertorié pour le moment.", color = Color.Gray)
            }
        } else {
            items(results) { res ->
                Box(
                    modifier = Modifier.clickable { selectedResultForDetail = res }
                ) {
                    ResultCard(
                        result = res,
                        onRecourseRequest = {
                            selectedResultForRecourse = res
                            expectedGradeText = ""
                            explanationText = ""
                            showRecourseDialog = true
                        }
                    )
                }
            }
        }

        item {
            Text(
                text = "Suivi de mes Recours / Réclamations",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }

        if (recourses.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Aucun recours soumis. Cliquez sur 'faire un recours' sur une note ci-dessus en cas de contestation.",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = Color.Gray
                        )
                    }
                }
            }
        } else {
            items(recourses) { rec ->
                RecourseRow(recourse = rec)
            }
        }
    }

    if (selectedResultForDetail != null) {
        ResultDetailDialog(
            result = selectedResultForDetail!!,
            onDismiss = { selectedResultForDetail = null },
            onRecourseTrigger = {
                selectedResultForRecourse = selectedResultForDetail
                expectedGradeText = ""
                explanationText = ""
                showRecourseDialog = true
            }
        )
    }

    // Modal dialogue for Recourse Submission
    if (showRecourseDialog && selectedResultForRecourse != null) {
        val course = selectedResultForRecourse!!
        Dialog(onDismissRequest = { showRecourseDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Soumettre un Recours",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.errorContainer
                        ) {
                            Text(
                                text = course.courseCode,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(6.dp)
                            )
                        }
                    }

                    Text(
                        text = "Cours concerné : ${course.courseName}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = "Note actuelle enregistrée : ${course.grade}/${course.maxGrade}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )

                    // Expected grade
                    OutlinedTextField(
                        value = expectedGradeText,
                        onValueChange = { expectedGradeText = it },
                        label = { Text("Note estimée (Ex: 14/20)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("expected_grade_input")
                    )

                    // Protest description
                    OutlinedTextField(
                        value = explanationText,
                        onValueChange = { explanationText = it },
                        label = { Text("Description claire (Omission de TPs, examen corrigé etc.)") },
                        minLines = 3,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("explanation_grade_input")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showRecourseDialog = false }) {
                            Text("Annuler")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (expectedGradeText.isNotEmpty() && explanationText.isNotEmpty()) {
                                    viewModel.submitRecourse(
                                        courseCode = course.courseCode,
                                        courseName = course.courseName,
                                        expectedGrade = expectedGradeText,
                                        explanation = explanationText,
                                        onComplete = {
                                            showRecourseDialog = false
                                        }
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.testTag("submit_recourse_claim")
                        ) {
                            Text("Envoyer la réclamation")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ResultCard(
    result: AcademicResult,
    onRecourseRequest: () -> Unit
) {
    val isPassing = result.grade >= 10.0
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)), RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = result.courseName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${result.courseCode} • Crédits: ${result.credits} • ${result.professor}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isPassing) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = String.format(Locale.FRANCE, "%.1f/20", result.grade),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isPassing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Badge(
                        containerColor = if (isPassing) GreenSuccess else RedAlert,
                        modifier = Modifier.size(6.dp)
                    )
                    Text(
                        text = if (isPassing) "Validé d'office" else "Ajourné (Session 2)",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isPassing) GreenSuccess else RedAlert,
                        fontWeight = FontWeight.Medium
                    )
                }

                TextButton(
                    onClick = onRecourseRequest,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.PriorityHigh, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("Faire un Recours", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun RecourseRow(recourse: Recourse) {
    val dateString = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.FRENCH).format(Date(recourse.dateMillis))

    val statusColor = when (recourse.status) {
        "Soumis" -> AmberPending
        "En cours de traitement" -> MaterialTheme.colorScheme.primary
        "Validé" -> GreenSuccess
        else -> RedAlert
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)), RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = recourse.courseName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Attendu: ${recourse.expectedGrade} • Déposé le: $dateString",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = recourse.status,
                        color = statusColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Description : " + recourse.explanation,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}

// --- ACADEMIC TIMETABLE (HORAIRE) SCREEN ---
@Composable
fun ScheduleScreen(viewModel: AppViewModel) {
    val schedules by viewModel.scheduleState.collectAsState()

    val days = listOf("Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi")
    var selectedDay by remember { mutableStateOf("Lundi") }
    var selectedScheduleForDetail by remember { mutableStateOf<ScheduleItem?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.setTab(0) },
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Retour",
                    tint = Color(0xFF00054F)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Horaire de Cours",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.Cached, contentDescription = null, tint = GreenSuccess, modifier = Modifier.size(16.dp))
                    Text(
                        text = "Consultez hors-ligne à tout moment",
                        style = MaterialTheme.typography.bodySmall,
                        color = GreenSuccess,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Horizontal filter for days
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            days.forEach { day ->
                val isSelected = selectedDay == day
                Button(
                    onClick = { selectedDay = day },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(day.take(3), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        val dailyCourses = schedules.filter { it.dayOfWeek == selectedDay }

        if (dailyCourses.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.EventNote,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Aucun cours dispensé ce jour.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(dailyCourses) { item ->
                    Box(
                        modifier = Modifier.clickable { selectedScheduleForDetail = item }
                    ) {
                        ScheduleRow(item = item)
                    }
                }
            }
        }
    }

    if (selectedScheduleForDetail != null) {
        ScheduleDetailDialog(
            item = selectedScheduleForDetail!!,
            onDismiss = { selectedScheduleForDetail = null }
        )
    }
}

@Composable
fun ScheduleRow(item: ScheduleItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)), RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Hour side pill
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.width(90.dp)
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.HourglassEmpty, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.timeSlot.replace(" ", "\n"),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Course Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.courseName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.AccountBox, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = item.professor,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))

                Surface(
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Room, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.secondary)
                        Text(
                            text = item.classroom,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

// --- ADMINISTRATIVE CHAT ROOM SCREEN ---
@Composable
fun ChatScreen(viewModel: AppViewModel) {
    val messages by viewModel.chatMessagesState.collectAsState()
    var messageText by remember { mutableStateOf("") }
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

    // Keep chat scrolled down when messages load
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Chat Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)), RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(24.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(onClick = { viewModel.setTab(0) }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Retour",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.SupportAgent, contentDescription = null, tint = Color.White)
                    }
                }
                Column {
                    Text(
                        text = "Secrétariat Académique UDBL",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Agent en ligne • Réponses Instantanées",
                        style = MaterialTheme.typography.labelSmall,
                        color = GreenSuccess
                    )
                }
            }
        }

        // Chat list area
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { msg ->
                ChatBubble(message = msg)
            }
        }

        // Bottom input controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                placeholder = { Text("Posez votre question administrative...") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_field"),
                shape = RoundedCornerShape(24.dp),
                maxLines = 3
            )

            FloatingActionButton(
                onClick = {
                    if (messageText.trim().isNotEmpty()) {
                        viewModel.sendChatMessage(messageText)
                        messageText = ""
                    }
                },
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("chat_send_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Envoyer le message")
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val isStudent = message.sender == "STUDENT"
    val alignment = if (isStudent) Alignment.End else Alignment.Start
    val bubbleColor = if (isStudent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isStudent) Color.White else MaterialTheme.colorScheme.onSurface
    val shape = if (isStudent) {
        RoundedCornerShape(12.dp, 12.dp, 0.dp, 12.dp)
    } else {
        RoundedCornerShape(12.dp, 12.dp, 12.dp, 0.dp)
    }

    val timeString = SimpleDateFormat("HH:mm", Locale.FRENCH).format(Date(message.dateMillis))

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Surface(
            color = bubbleColor,
            shape = shape,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = timeString,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 8.sp,
                        color = textColor.copy(alpha = 0.7f)
                    )
                    if (isStudent && message.isPending) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.HourglassTop, contentDescription = "En cours d'envoi", modifier = Modifier.size(10.dp), tint = textColor.copy(alpha = 0.7f))
                    }
                }
            }
        }
    }
}

// --- PREFERENCES/CONFIGURATION MODAL ---
@Composable
fun SettingsDialog(
    viewModel: AppViewModel,
    onDismiss: () -> Unit,
    onLogout: () -> Unit
) {
    val selectedTheme by viewModel.selectedTheme.collectAsState()
    val dataSaverMode by viewModel.dataSaverMode.collectAsState()
    val biometricEnabled by viewModel.biometricSetupEnabled.collectAsState()
    
    val pushAnnouncements by viewModel.customPushAnnouncements.collectAsState()
    val pushGrades by viewModel.customPushGrades.collectAsState()
    val pushSchedule by viewModel.customPushSchedule.collectAsState()

    var showThemeSpinner by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            LazyColumn(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Paramètres Généraux",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Fermer")
                        }
                    }
                }

                // Security & Biometric Section
                item {
                    Text("Sécurité & Authentification", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }

                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Fingerprint, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Column {
                                    Text("Empreinte Biométrique", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    Text("Protéger les données académiques", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                            Switch(
                                checked = biometricEnabled,
                                onCheckedChange = { viewModel.updatePreferences(biometricSetup = it) },
                                modifier = Modifier.testTag("biometric_pref_switch")
                            )
                        }
                    }
                }

                // Data saving settings
                item {
                    Text("Optimisation des données", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }

                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.DataUsage, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Column {
                                    Text("Consommation faible", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    Text("Réduire les téléchargements de données d'horaire", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                            Switch(
                                checked = dataSaverMode,
                                onCheckedChange = { viewModel.updatePreferences(dataSaver = it) },
                                modifier = Modifier.testTag("data_saver_pref_switch")
                            )
                        }
                    }
                }

                // Theming Mode selection
                item {
                    Text("Apparence de l'interface", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("System", "Clair", "Sombre").forEach { themeOption ->
                            val isSelected = selectedTheme == themeOption
                            Button(
                                onClick = { viewModel.updatePreferences(theme = themeOption) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(themeOption, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Notifications management
                item {
                    Text("Notifications push personnalisées", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }

                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        NotificationToggleItem(
                            label = "Annonces & Communiqués administratifs",
                            checked = pushAnnouncements,
                            onCheckedChange = { viewModel.updatePreferences(pushAnnouncements = it) }
                        )

                        NotificationToggleItem(
                            label = "Notes & Résultats d'Examens",
                            checked = pushGrades,
                            onCheckedChange = { viewModel.updatePreferences(pushGrades = it) }
                        )

                        NotificationToggleItem(
                            label = "Modifications d'Horaires académiques",
                            checked = pushSchedule,
                            onCheckedChange = { viewModel.updatePreferences(pushSchedule = it) }
                        )
                    }
                }

                // Accounts management / Disconnection
                item {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onLogout,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("logout_pref_button")
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Déconnexion")
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationToggleItem(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.scaleWidget(0.85f)
            )
        }
    }
}

// Extra helper scaling extensions for Switch component
fun Modifier.scaleWidget(scale: Float): Modifier = this.scale(scale)

// --- CONNEXION & INSCRIPTION FORMS SCREENS ---

@Composable
fun GmailLogo(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val red = Color(0xFFEA4335)
        val blue = Color(0xFF4285F4)
        val green = Color(0xFF34A853)
        val yellow = Color(0xFFFBBC05)

        // Draw standard envelope structure with custom colorful lines
        drawRect(color = blue, size = androidx.compose.ui.geometry.Size(w * 0.2f, h), topLeft = androidx.compose.ui.geometry.Offset(0f, 0f))
        drawRect(color = green, size = androidx.compose.ui.geometry.Size(w * 0.2f, h), topLeft = androidx.compose.ui.geometry.Offset(w * 0.8f, 0f))
        drawRect(color = red, size = androidx.compose.ui.geometry.Size(w * 0.8f, h * 0.2f), topLeft = androidx.compose.ui.geometry.Offset(w * 0.1f, 0f))
        
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(w * 0.2f, h * 0.2f)
            lineTo(w * 0.5f, h * 0.55f)
            lineTo(w * 0.8f, h * 0.2f)
        }
        drawPath(path = path, color = red, style = Stroke(width = w * 0.15f, cap = StrokeCap.Round))
    }
}

@Composable
fun AuthScreen(
    viewModel: AppViewModel,
    onAuthSuccess: () -> Unit
) {
    // Splash screens deleted per user request. Startup is instantaneous and loads the Onboarding/Login directly.
    var showOnboarding by remember { mutableStateOf(false) }
    var isLoginMode by remember { mutableStateOf(true) }

    // Forms states
    var emailText by remember { mutableStateOf("") }
    var passwordText by remember { mutableStateOf("") } // local passcode

    // Registration states
    var nameText by remember { mutableStateOf("") }
    var matriculeText by remember { mutableStateOf("") }
    var facultyText by remember { mutableStateOf("Sciences Informatiques") }
    var promotionText by remember { mutableStateOf("Licence 2 (L2)") }

    var biometricDialogShown by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val keyguardManager = remember {
        context.getSystemService(android.content.Context.KEYGUARD_SERVICE) as android.app.KeyguardManager
    }

    val biometricLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            if (viewModel.login(emailText.ifEmpty { "john.doe@udbl.ac.cd" }, simulateBiometrics = true)) {
                onAuthSuccess()
            }
        }
    }

    if (showOnboarding) {
        OnboardingCarousel(onFinished = { showOnboarding = false })
    } else {
        // Main Authentication Form
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            // Background overlapping blue circles matching the target screenshot
            // Top-left giant circles
            Box(
                modifier = Modifier
                    .offset(x = (-130).dp, y = (-200).dp)
                    .size(480.dp)
                    .background(Color(0xFF00054F), CircleShape)
            )
            Box(
                modifier = Modifier
                    .offset(x = 100.dp, y = (-80).dp)
                    .size(260.dp)
                    .background(Color(0xFF0114C7), CircleShape)
            )
            
            // Bottom-right giant curved circle
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 140.dp, y = 140.dp)
                    .size(340.dp)
                    .background(Color(0xFF00054F), CircleShape)
            )

            // Scrollable container for forms with perfect layout alignment matching the screenshot
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(110.dp))

                // Centered UDBL Brand Logo - restored to perfect circular outline layout matching 71.png
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(BorderStroke(2.dp, Color(0xFF0C9C3E)), CircleShape)
                        .padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = androidx.compose.ui.res.painterResource(id = R.drawable.udbl_logo_asset_1779824734235),
                        contentDescription = "UDBL Logo",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Profile silhouette circle directly below logo
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .border(BorderStroke(3.dp, Color(0xFF00054F)), CircleShape)
                        .background(Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color(0xFF00054F),
                        modifier = Modifier.size(44.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (!isLoginMode) {
                    Text(
                        text = "S'ENREGISTRER",
                        color = Color(0xFF00054F),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (isLoginMode) {
                    // Email
                    OutlinedTextField(
                        value = emailText,
                        onValueChange = { emailText = it },
                        placeholder = { Text("Email", color = Color.Gray, fontSize = 15.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .shadow(3.dp, RoundedCornerShape(12.dp))
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .testTag("auth_email_field"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = Color(0xFF00054F),
                            unfocusedBorderColor = Color(0xFF00054F).copy(alpha = 0.5f),
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Password
                    OutlinedTextField(
                        value = passwordText,
                        onValueChange = { passwordText = it },
                        placeholder = { Text("Mot de passe", color = Color.Gray, fontSize = 15.sp) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .shadow(3.dp, RoundedCornerShape(12.dp))
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .testTag("auth_password_field"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = Color(0xFF00054F),
                            unfocusedBorderColor = Color(0xFF00054F).copy(alpha = 0.5f),
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Centered Biometrics option
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                val intent = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                                    keyguardManager.createConfirmDeviceCredentialIntent(
                                        "Validation Académique",
                                        "Authentifiez-vous par empreinte ou code de sécurité"
                                    )
                                } else null

                                if (intent != null) {
                                    biometricLauncher.launch(intent)
                                } else {
                                    emailText = "john.doe@udbl.ac.cd"
                                    passwordText = "12345"
                                    biometricDialogShown = true
                                }
                            },
                            modifier = Modifier.testTag("request_biometric_auth")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fingerprint,
                                contentDescription = null,
                                tint = Color(0xFF00054F),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Connexion Biométrique Rapide", color = Color(0xFF00054F), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Connect button matching Image 1
                    Button(
                        onClick = {
                            if (emailText.isNotEmpty()) {
                                if (viewModel.login(emailText, simulateBiometrics = false)) {
                                    onAuthSuccess()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00054F)),
                        shape = RoundedCornerShape(32.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("submit_login_button")
                    ) {
                        Text("Se connectez", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Divider Row matching Image 1
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(modifier = Modifier.weight(1f).height(1.dp).background(Color.Gray.copy(alpha = 0.3f)))
                        Text(
                            text = "or",
                            color = Color(0xFF0C9C3E), // Green matching screenshot
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 14.dp),
                            fontSize = 14.sp
                        )
                        Box(modifier = Modifier.weight(1f).height(1.dp).background(Color.Gray.copy(alpha = 0.3f)))
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Gmail connector button matching Image 1
                    OutlinedButton(
                        onClick = {
                            emailText = "john.doe@udbl.ac.cd"
                            if (viewModel.login(emailText, simulateBiometrics = false)) {
                                onAuthSuccess()
                            }
                        },
                        border = BorderStroke(1.2.dp, Color(0xFF00054F)),
                        shape = RoundedCornerShape(32.dp),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            GmailLogo(modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Se connectez avec Gmail", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }

                } else {
                    // Registration forms matching Image 1 styling
                    OutlinedTextField(
                            value = nameText,
                            onValueChange = { nameText = it },
                            placeholder = { Text("Nom complet", color = Color.Gray) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(2.dp, RoundedCornerShape(12.dp))
                                .background(Color.White, RoundedCornerShape(12.dp))
                                .testTag("reg_name_field"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = Color.Black,
                                unfocusedBorderColor = Color.Black.copy(alpha = 0.8f),
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = emailText,
                            onValueChange = { emailText = it },
                            placeholder = { Text("Email universitaire (etudiant@udbl.ac.cd)", color = Color.Gray) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(2.dp, RoundedCornerShape(12.dp))
                                .background(Color.White, RoundedCornerShape(12.dp))
                                .testTag("reg_email_field"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = Color.Black,
                                unfocusedBorderColor = Color.Black.copy(alpha = 0.8f),
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = matriculeText,
                            onValueChange = { matriculeText = it },
                            placeholder = { Text("Numéro Matricule UDBL (UDBL/INF/2026/XXXX)", color = Color.Gray) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(2.dp, RoundedCornerShape(12.dp))
                                .background(Color.White, RoundedCornerShape(12.dp))
                                .testTag("reg_matricule_field"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = Color.Black,
                                unfocusedBorderColor = Color.Black.copy(alpha = 0.8f),
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = facultyText,
                            onValueChange = { facultyText = it },
                            placeholder = { Text("Faculté / Département", color = Color.Gray) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(2.dp, RoundedCornerShape(12.dp))
                                .background(Color.White, RoundedCornerShape(12.dp)),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = Color.Black,
                                unfocusedBorderColor = Color.Black.copy(alpha = 0.8f),
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = promotionText,
                            onValueChange = { promotionText = it },
                            placeholder = { Text("Promotion (ex: Licence 2 L2)", color = Color.Gray) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(2.dp, RoundedCornerShape(12.dp))
                                .background(Color.White, RoundedCornerShape(12.dp)),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = Color.Black,
                                unfocusedBorderColor = Color.Black.copy(alpha = 0.8f),
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Button(
                            onClick = {
                                if (emailText.isNotEmpty() && nameText.isNotEmpty() && matriculeText.isNotEmpty()) {
                                    if (viewModel.register(emailText, nameText, matriculeText, promotionText, facultyText)) {
                                        onAuthSuccess()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00054F)),
                            shape = RoundedCornerShape(32.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("submit_register_button")
                        ) {
                            Text("S'enregistrer", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    TextButton(
                        onClick = { isLoginMode = !isLoginMode },
                        modifier = Modifier.testTag("toggle_auth_mode")
                    ) {
                        Text(
                            text = if (isLoginMode) "Pas encore de compte ? S'enregistrer" else "Déjà inscrit ? Se connecter",
                            color = Color(0xFF0C9C3E),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

    // Biometric mock verification Dialog
    if (biometricDialogShown) {
        Dialog(onDismissRequest = { biometricDialogShown = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(BorderStroke(1.dp, Color(0xFF00054F)), RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = null,
                        tint = Color(0xFF00054F),
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = "Touch ID / Face ID",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.Black
                    )
                    Text(
                        text = "Simuler l'authentification biométrique sur UDBL Vision.",
                        textAlign = TextAlign.Center,
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { biometricDialogShown = false }) {
                            Text("Annuler", color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                viewModel.login("john.doe@udbl.ac.cd", simulateBiometrics = true)
                                biometricDialogShown = false
                                onAuthSuccess()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00054F))
                        ) {
                            Text("Authentifier", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

// --- SUB-PAGES & CLICK OVERLAY OPTIONS DIALOGS (OPERATIONAL VIEWS) ---

@Composable
fun AnnonceDetailDialog(annonce: Annonce, onDismiss: () -> Unit) {
    val context = LocalContext.current
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)), RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = if (annonce.category == "Urgent") MaterialTheme.colorScheme.errorContainer
                                else if (annonce.category == "Académique") MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = annonce.category,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (annonce.category == "Urgent") MaterialTheme.colorScheme.onErrorContainer
                                    else if (annonce.category == "Académique") MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Text(
                        text = annonce.dateString,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = annonce.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Text(
                    text = "Publié par : Secrétariat Général Académique • UDBL",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Box(
                    modifier = Modifier
                        .heightIn(max = 200.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = annonce.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 22.sp
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            android.widget.Toast.makeText(context, "Partage du communiqué : " + annonce.title, android.widget.Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.0f)
                    ) {
                        Text("Partager", fontSize = 12.sp)
                    }

                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.0f)
                    ) {
                        Text("Fermer", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentReceiptDialog(payment: Payment, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var isDownloading by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)), RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "RÉPUBLIQUE DÉMOCRATIQUE DU CONGO",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "UNIVERSITÉ DE L'UDBL",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "REÇU NUMÉRIQUE DE PAIEMENT ACADÉMIQUE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = GreenSuccess,
                        textAlign = TextAlign.Center
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ReceiptRow(label = "Type de Frais", value = payment.paymentType)
                    ReceiptRow(label = "Montant Payé", value = String.format(Locale.US, "$%.2f USD", payment.amount), isSuccess = true)
                    ReceiptRow(label = "Référence Trans.", value = payment.reference, isMono = true)
                    ReceiptRow(label = "Canal Local", value = payment.walletType)
                    ReceiptRow(label = "Numéro Mobile", value = payment.phoneNumber)
                    ReceiptRow(label = "Date & Heure", value = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRENCH).format(Date(payment.dateMillis)))
                    ReceiptRow(label = "Statut", value = "APPRÉCIÉ & VALIDÉ", isSuccess = true)
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                    ) {
                        val widthSpace = size.width
                        val barCount = 45
                        val step = widthSpace / barCount
                        for (i in 0 until barCount) {
                            val isBar = (i * 179 + 47) % 3 != 0
                            if (isBar) {
                                drawRect(
                                    color = Color.White.copy(alpha = 0.55f),
                                    topLeft = androidx.compose.ui.geometry.Offset(i * step, 0f),
                                    size = androidx.compose.ui.geometry.Size(step * 0.6f, size.height)
                                )
                            }
                        }
                    }
                    Text(
                        text = "VERIFIED-SECURE-SHA256-${payment.reference.uppercase()}",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        fontSize = 8.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                if (isDownloading) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Téléchargement du PDF officiel...", style = MaterialTheme.typography.bodySmall)
                    }
                    LaunchedEffect(Unit) {
                        kotlinx.coroutines.delay(1500)
                        isDownloading = false
                        android.widget.Toast.makeText(context, "Reçu enregistré dans le dossier Téléchargements !", android.widget.Toast.LENGTH_LONG).show()
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { isDownloading = true },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Télécharger PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(0.8f)
                        ) {
                            Text("Fermer", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReceiptRow(label: String, value: String, isSuccess: Boolean = false, isMono: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            fontFamily = if (isMono) androidx.compose.ui.text.font.FontFamily.Monospace else null,
            color = if (isSuccess) GreenSuccess else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun ResultDetailDialog(
    result: AcademicResult,
    onDismiss: () -> Unit,
    onRecourseTrigger: () -> Unit
) {
    val context = LocalContext.current
    val isPassing = result.grade >= 10.0

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)), RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column {
                    Text(
                        text = result.courseCode,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = result.courseName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(80.dp)
                    ) {
                        CircularProgressIndicator(
                            progress = { (result.grade / 20.0).toFloat() },
                            modifier = Modifier.size(80.dp),
                            color = if (isPassing) GreenSuccess else RedAlert,
                            strokeWidth = 8.dp,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            strokeCap = StrokeCap.Round
                        )
                        Text(
                            text = String.format(Locale.FRANCE, "%.1f", result.grade),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isPassing) GreenSuccess else RedAlert
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isPassing) "Validé d'Office" else "Ajourné (Session 2)",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isPassing) GreenSuccess else RedAlert
                        )
                        Text(
                            text = "Crédits ECTS : ${result.credits}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Enseigné par : ${result.professor}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            android.widget.Toast.makeText(context, "Téléchargement du Syllabus Officiel (${result.courseCode})...", android.widget.Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Télécharger Syllabus du cours", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            android.widget.Toast.makeText(context, "Demande de vérification de copie envoyée avec succès au prof !", android.widget.Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Vérifier copie d'examen", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            onDismiss()
                            onRecourseTrigger()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Introduire un recours académique", fontSize = 12.sp)
                    }
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Annuler")
                }
            }
        }
    }
}

@Composable
fun ScheduleDetailDialog(
    item: ScheduleItem,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var isAlarmEnabled by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)), RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column {
                    Text(
                        text = item.dayOfWeek + " • " + item.timeSlot,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = item.courseName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Plan d'Accès : " + item.classroom,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
                    ) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height

                        for (x in 20..(canvasWidth.toInt()) step 40) {
                            drawLine(
                                color = Color.White.copy(alpha = 0.05f),
                                start = androidx.compose.ui.geometry.Offset(x.toFloat(), 0f),
                                end = androidx.compose.ui.geometry.Offset(x.toFloat(), canvasHeight),
                                strokeWidth = 1f
                            )
                        }
                        for (y in 20..(canvasHeight.toInt()) step 40) {
                            drawLine(
                                color = Color.White.copy(alpha = 0.05f),
                                start = androidx.compose.ui.geometry.Offset(0f, y.toFloat()),
                                end = androidx.compose.ui.geometry.Offset(canvasWidth, y.toFloat()),
                                strokeWidth = 1f
                            )
                        }

                        drawRect(
                            color = Color.LightGray.copy(alpha = 0.25f),
                            topLeft = androidx.compose.ui.geometry.Offset(30f, 20f),
                            size = androidx.compose.ui.geometry.Size(120f, 60f)
                        )

                        drawLine(
                            color = Color.White.copy(alpha = 0.15f),
                            start = androidx.compose.ui.geometry.Offset(canvasWidth * 0.45f, 0f),
                            end = androidx.compose.ui.geometry.Offset(canvasWidth * 0.45f, canvasHeight),
                            strokeWidth = 6f
                        )

                        drawRect(
                            color = Color(0xFF1D4ED8).copy(alpha = 0.45f),
                            topLeft = androidx.compose.ui.geometry.Offset(canvasWidth * 0.55f, 25f),
                            size = androidx.compose.ui.geometry.Size(130f, 60f)
                        )

                        drawCircle(
                            color = Color(0xFFEC4899),
                            radius = 6f,
                            center = androidx.compose.ui.geometry.Offset(canvasWidth * 0.68f, 55f)
                        )
                    }

                    Text(
                        text = "Localisé dans l'Aile Nord • Bâtiment Polytechnique • Niveau 1",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Activer un rappel local", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        Text("Alerte push 15 minutes avant le début", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = isAlarmEnabled,
                        onCheckedChange = {
                            isAlarmEnabled = it
                            val msg = if (it) "Rappel activé pour le cours de " + item.courseName else "Rappel désactivé."
                            android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            android.widget.Toast.makeText(context, "Collision signalée au Secrétariat Académique !", android.widget.Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Signaler Collision", fontSize = 11.sp, color = RedAlert)
                    }
                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("OK", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

// --- WELCOME SLIDING PRESENTATION ONBOARDING COMPOSABLES ---

@Composable
fun OnboardingCarousel(onFinished: () -> Unit) {
    var currentPage by remember { mutableIntStateOf(0) }
    val pages = listOf(
        OnboardingData(
            title = "Paiements Simplifiés 💳",
            subtitle = "Soutien Orange, M-Pesa & Airtel",
            description = "Payez vos frais académiques en toute sécurité sans file d'attente. Les reçus numériques certifiés sont immédiatement enregistrés."
        ),
        OnboardingData(
            title = "Notes & Réclamations 🎯",
            subtitle = "Transparence & Rapidité",
            description = "Consultez vos moyennes, validez vos sessions et soumettez des recours administratifs directs avec notification en temps réel."
        ),
        OnboardingData(
            title = "Horaire Hors-ligne & Chat 💬",
            subtitle = "Restez Toujours Informé",
            description = "Accédez à vos salles et horaires de cours d'examen même sans réseau mobile, et dialoguez directement avec l'administration."
        )
    )

    val backgroundImages = listOf(
        R.drawable.img_university_campus_1779884165837_1779975539656,
        R.drawable.img_university_library_1779885052399,
        R.drawable.img_university_lab_1779885078694
    )

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        AnimatedContent(
            targetState = currentPage,
            transitionSpec = {
                fadeIn(animationSpec = tween(600)).togetherWith(fadeOut(animationSpec = tween(600)))
            },
            label = "background_image_transition",
            modifier = Modifier.fillMaxSize()
        ) { pageIndex ->
            val bgRes = backgroundImages.getOrElse(pageIndex) { backgroundImages[0] }
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = androidx.compose.ui.res.painterResource(id = bgRes),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF00054F).copy(alpha = 0.60f),
                                    Color(0xFF00054F).copy(alpha = 0.90f)
                                )
                            )
                        )
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onFinished,
                    modifier = Modifier.testTag("skip_onboarding_btn")
                ) {
                    Text("Passer", fontWeight = FontWeight.SemiBold, color = Color.White.copy(alpha = 0.8f))
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = currentPage,
                    transitionSpec = {
                        (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> -width } + fadeOut()
                        )
                    },
                    label = "slide_transition"
                ) { pageState ->
                    val data = pages[pageState]
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OnboardingIllustration(pageIndex = pageState)

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = data.title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = data.subtitle,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFBBF24),
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = data.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.85f),
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    pages.forEachIndexed { index, _ ->
                        val isSelected = index == currentPage
                        val width by animateDpAsState(
                            targetValue = if (isSelected) 24.dp else 8.dp,
                            label = "indicator_width"
                        )
                        val color = if (isSelected) Color(0xFFFBBF24) else Color.White.copy(alpha = 0.4f)
                        Box(
                            modifier = Modifier
                                .size(height = 8.dp, width = width)
                                .background(color, CircleShape)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentPage > 0) {
                        OutlinedButton(
                            onClick = { currentPage-- },
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            modifier = Modifier
                                .height(52.dp)
                                .weight(1f)
                                .padding(end = 8.dp)
                        ) {
                            Text("Précédent", fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = {
                            if (currentPage < pages.lastIndex) {
                                currentPage++
                            } else {
                                onFinished()
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .height(52.dp)
                            .weight(1f)
                            .testTag("next_onboarding_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00054F),
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = if (currentPage == pages.lastIndex) "Se Connecter" else "Suivant",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

data class OnboardingData(
    val title: String,
    val subtitle: String,
    val description: String
)

@Composable
fun OnboardingIllustration(pageIndex: Int) {
    Canvas(
        modifier = Modifier
            .size(160.dp)
            .background(Color(0xFF1E1B4B).copy(alpha = 0.35f), CircleShape)
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)), CircleShape)
    ) {
        val centerPoint = center
        val w = size.width
        val h = size.height

        drawCircle(
            color = Color(0xFF312E81).copy(alpha = 0.25f),
            radius = w * 0.45f
        )

        when (pageIndex) {
            0 -> {
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.5f),
                    topLeft = androidx.compose.ui.geometry.Offset(w * 0.35f, h * 0.2f),
                    size = androidx.compose.ui.geometry.Size(w * 0.3f, h * 0.6f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f),
                    style = Stroke(width = 4f)
                )
                drawCircle(color = Color(0xFFFBBF24), radius = 12f, center = androidx.compose.ui.geometry.Offset(w * 0.25f, h * 0.45f))
                drawCircle(color = Color(0xFFF59E0B), radius = 14f, center = androidx.compose.ui.geometry.Offset(w * 0.28f, h * 0.60f))
                drawCircle(color = Color(0xFF34D399), radius = 16f, center = androidx.compose.ui.geometry.Offset(w * 0.75f, h * 0.5f))

                val tickPath = androidx.compose.ui.graphics.Path().apply {
                    moveTo(w * 0.44f, h * 0.5f)
                    lineTo(w * 0.49f, h * 0.55f)
                    lineTo(w * 0.56f, h * 0.44f)
                }
                drawPath(
                    path = tickPath,
                    color = Color(0xFF34D399),
                    style = Stroke(
                        width = 5f,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round,
                        join = androidx.compose.ui.graphics.StrokeJoin.Round
                    )
                )
            }
            1 -> {
                drawCircle(
                    color = Color.White.copy(alpha = 0.08f),
                    radius = w * 0.35f,
                    style = Stroke(width = 8f)
                )
                drawArc(
                    color = Color(0xFF10B981),
                    startAngle = -90f,
                    sweepAngle = 270f,
                    useCenter = false,
                    style = Stroke(
                        width = 10f,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                )
                drawCircle(color = Color(0xFF10B981), radius = 10f, center = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.5f))
                drawLine(
                    color = Color.White,
                    start = androidx.compose.ui.geometry.Offset(w * 0.43f, h * 0.5f),
                    end = androidx.compose.ui.geometry.Offset(w * 0.57f, h * 0.5f),
                    strokeWidth = 3f
                )
                drawLine(
                    color = Color.White,
                    start = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.43f),
                    end = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.57f),
                    strokeWidth = 3f
                )
            }
            2 -> {
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.25f),
                    topLeft = androidx.compose.ui.geometry.Offset(w * 0.22f, h * 0.28f),
                    size = androidx.compose.ui.geometry.Size(w * 0.35f, h * 0.35f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f),
                    style = Stroke(width = 3f)
                )
                drawRoundRect(
                    color = Color(0xFF3B82F6),
                    topLeft = androidx.compose.ui.geometry.Offset(w * 0.48f, h * 0.45f),
                    size = androidx.compose.ui.geometry.Size(w * 0.35f, h * 0.28f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
                )
                val arrowPath = androidx.compose.ui.graphics.Path().apply {
                    moveTo(w * 0.55f, h * 0.73f)
                    lineTo(w * 0.5f, h * 0.78f)
                    lineTo(w * 0.58f, h * 0.73f)
                    close()
                }
                drawPath(path = arrowPath, color = Color(0xFF3B82F6))
            }
        }
    }
}


// ==========================================
// NEW OPERATIONS SCREENS PER USER REQUESTS
// ==========================================

// --- FEATURE 1: BIBLIOTHÈQUE NUMÉRIQUE & SYLLABUS ---
@Composable
fun LibraryScreen(viewModel: AppViewModel) {
    val syllabi by viewModel.syllabiState.collectAsState()
    var selectedSyllabusForReading by remember { mutableStateOf<SyllabusDocument?>(null) }
    var downloadingId by remember { mutableStateOf<Int?>(null) }
    val scope = rememberCoroutineScope()

    androidx.activity.compose.BackHandler(enabled = true) {
        viewModel.setTab(0)
    }

    Scaffold(
        topBar = {
            HeaderBar(
                title = "Bibliothèque Numérique",
                onBack = { viewModel.setTab(0) }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF3F4F6))
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color(0xFF00054F).copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = Color(0xFF00054F),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Syllabus Officiels de l'UDBL",
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Consultez et téléchargez vos supports de cours pour les lire hors-ligne pendant vos déplacements.",
                                color = Color.Gray,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            items(syllabi) { syllabus ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("syllabus_card_${syllabus.id}"),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = syllabus.title,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.Black,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "Code: ${syllabus.courseCode} • ${syllabus.professor}",
                                    color = Color(0xFF0C9C3E),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                            Text(
                                text = syllabus.fileSize,
                                color = Color.Gray,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }

                        Text(
                            text = syllabus.description,
                            color = Color.DarkGray,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(vertical = 10.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (syllabus.isDownloaded) {
                                Button(
                                    onClick = { selectedSyllabusForReading = syllabus },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0C9C3E)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Consulter hors-ligne", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Button(
                                    onClick = {
                                        scope.launch {
                                            downloadingId = syllabus.id
                                            kotlinx.coroutines.delay(2000) // Simulate download cache latency
                                            viewModel.simulateDownloadSyllabus(syllabus.id)
                                            downloadingId = null
                                        }
                                    },
                                    enabled = downloadingId == null,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00054F)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    if (downloadingId == syllabus.id) {
                                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Téléchargement...", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    } else {
                                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Télécharger (${syllabus.fileSize})", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (selectedSyllabusForReading != null) {
        SyllabusReaderDialog(
            syllabus = selectedSyllabusForReading!!,
            onDismiss = { selectedSyllabusForReading = null }
        )
    }
}

@Composable
fun SyllabusReaderDialog(syllabus: SyllabusDocument, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .height(550.dp)
                .padding(8.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Reader top bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF00054F))
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(syllabus.courseCode, color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(syllabus.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer", tint = Color.White)
                    }
                }

                // Simulated PDF view
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.White)
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "CHAPITRE I: CONCEPTS FONDAMENTAUX",
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Le cours dispensé par ${syllabus.professor} s'articule autour des théories avancées appliquées aux sciences informatiques. Ce manuel formalisé a pour but de fournir une autonomie totale de révision hors-connexion Internet aux étudiants de l'UDBL.",
                        color = Color.DarkGray,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    Divider(color = Color.LightGray.copy(alpha = 0.5f))

                    Text(
                        text = "CHAPITRE II: ARCHITECTURES ET LOGIQUE DE FONCTIONNEMENT",
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "1. Modélisation et définitions clés:\nChaque module intègre des algorithmes formels que l'étudiant doit savoir implémenter pas à pas.\n\n2. Aspects de sécurité locale:\nLe caching des données sur UDBL Vision utilise des tables chiffrées SQLite (Room Database) pour assurer une protection robuste de l'intégrité intellectuelle.",
                        color = Color.DarkGray,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF3F4F6), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "[ Fin du document en cache officielle • ${syllabus.fileSize} ]",
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }

                // Reader footer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF3F4F6))
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.OfflinePin, contentDescription = null, tint = Color(0xFF0C9C3E), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Lecture hors-ligne sécurisée • UDBL Vision v1.0", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}


// --- FEATURE 2: POINTAGE PRÉSENCE QR CODE ---
@Composable
fun QrCheckInScreen(viewModel: AppViewModel) {
    val scans by viewModel.scansState.collectAsState()
    val scope = rememberCoroutineScope()
    var scannedMessageResult by remember { mutableStateOf<String?>(null) }
    var scannedSuccess by remember { mutableStateOf(false) }
    var simulatedScanInProgress by remember { mutableStateOf(false) }

    androidx.activity.compose.BackHandler(enabled = true) {
        viewModel.setTab(0)
    }

    Scaffold(
        topBar = {
            HeaderBar(
                title = "Pointage de Présence QR",
                onBack = { viewModel.setTab(0) }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF3F4F6))
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "SCANNER LE QR DU COURS",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = Color.Black
                        )
                        Text(
                            text = "Validez votre présence numérique en scannant le code QR dynamique projeté par l'enseignant ou l'assistant.",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                        )

                        // Simulated Camera Web Viewfinder representation
                        Box(
                            modifier = Modifier
                                .size(240.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color.Black)
                                .border(BorderStroke(2.dp, Color(0xFF00054F)), RoundedCornerShape(24.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (simulatedScanInProgress) {
                                CircularProgressIndicator(color = Color(0xFF0C9C3E), modifier = Modifier.size(48.dp))
                            } else {
                                // Scanning viewport lasers lines
                                Box(
                                    modifier = Modifier
                                        .size(160.dp)
                                        .border(BorderStroke(3.dp, Color(0xFF0C9C3E)), RoundedCornerShape(12.dp))
                                ) {
                                    // Custom visual green sweeping bar simulation
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(4.dp)
                                            .align(Alignment.Center)
                                            .background(Color(0xFF0C9C3E))
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.QrCodeScanner,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.15f),
                                    modifier = Modifier.size(110.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Fast scan simulated beacons for prototyping
                        Text("Simulations d'Auditoire rapides :", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Button(
                                onClick = {
                                    scope.launch {
                                        simulatedScanInProgress = true
                                        kotlinx.coroutines.delay(1200)
                                        simulatedScanInProgress = false
                                        viewModel.registerAttendance("UDBL-PRESENCE|Compilation & Traducteurs|Prof. Mukendi", onResult = { success, msg ->
                                            scannedSuccess = success
                                            scannedMessageResult = msg
                                        })
                                    }
                                },
                                enabled = !simulatedScanInProgress,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00054F).copy(alpha = 0.12f), contentColor = Color(0xFF00054F)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Compilation", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    scope.launch {
                                        simulatedScanInProgress = true
                                        kotlinx.coroutines.delay(1200)
                                        simulatedScanInProgress = false
                                        viewModel.registerAttendance("UDBL-PRESENCE|Génie Logiciel II|Prof. Kasongo", onResult = { success, msg ->
                                            scannedSuccess = success
                                            scannedMessageResult = msg
                                        })
                                    }
                                },
                                enabled = !simulatedScanInProgress,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00054F).copy(alpha = 0.12f), contentColor = Color(0xFF00054F)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Génie Logiciel II", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "HISTORIQUE DE VOS PRÉSENCES",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (scans.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(16.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Aucun pointage enregistré pour le moment.", color = Color.Gray, fontSize = 13.sp)
                    }
                }
            } else {
                items(scans) { scan ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(Color(0xFF0C9C3E).copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF0C9C3E), modifier = Modifier.size(24.dp))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(scan.courseName, fontWeight = FontWeight.ExtraBold, color = Color.Black, fontSize = 14.sp)
                                Text("Vérifié par: ${scan.verifiedProfessor}", color = Color.Gray, fontSize = 11.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                val sdf = remember { java.text.SimpleDateFormat("dd/MM, HH:mm", java.util.Locale.FRANCE) }
                                Text(sdf.format(java.util.Date(scan.scanTimeMillis)), color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Badge(containerColor = Color(0xFF0C9C3E).copy(alpha = 0.15f), contentColor = Color(0xFF0C9C3E), modifier = Modifier.padding(top = 4.dp)) {
                                    Text(scan.status, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (scannedMessageResult != null) {
        Dialog(onDismissRequest = { scannedMessageResult = null }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = if (scannedSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                        contentDescription = null,
                        tint = if (scannedSuccess) Color(0xFF0C9C3E) else Color.Red,
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = if (scannedSuccess) "Présence Enregistrée !" else "Échec de lecture",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.Black
                    )
                    Text(
                        scannedMessageResult!!,
                        textAlign = TextAlign.Center,
                        color = Color.DarkGray,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    Button(
                        onClick = { scannedMessageResult = null },
                        colors = ButtonDefaults.buttonColors(containerColor = if (scannedSuccess) Color(0xFF0C9C3E) else Color(0xFF00054F)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("D'accord", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}


// --- FEATURE 3: GESTIONNAIRE ET SOUMISSION DE TP ---
@Composable
fun TpSubmissionScreen(viewModel: AppViewModel) {
    val tps by viewModel.tpsState.collectAsState()
    var selectedTpForSubmit by remember { mutableStateOf<TpSubmission?>(null) }

    androidx.activity.compose.BackHandler(enabled = true) {
        viewModel.setTab(0)
    }

    Scaffold(
        topBar = {
            HeaderBar(
                title = "Travaux Pratiques (TP)",
                onBack = { viewModel.setTab(0) }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF3F4F6))
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "PORTAIL DE SOUMISSION DE TP",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            color = Color.Black
                        )
                        Text(
                            text = "Ne ratez aucun délai d'envoi. Soumettez directement vos codes sources, rapports .pdf, ou archives zip aux assistants.",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            items(tps) { tp ->
                val remainingTime = tp.deadlineMillis - System.currentTimeMillis()
                val isExpired = remainingTime <= 0

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tp_card_${tp.id}")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(tp.courseName, fontWeight = FontWeight.Bold, color = Color(0xFF00054F), fontSize = 12.sp)
                                Text(tp.title, fontWeight = FontWeight.ExtraBold, color = Color.Black, fontSize = 15.sp, modifier = Modifier.padding(top = 2.dp))
                            }

                            if (tp.isSubmitted) {
                                Badge(containerColor = Color(0xFF0C9C3E).copy(alpha = 0.15f), contentColor = Color(0xFF0C9C3E)) {
                                    Text("Soumis", fontWeight = FontWeight.Bold)
                                }
                            } else if (isExpired) {
                                Badge(containerColor = Color.Red.copy(alpha = 0.1f), contentColor = Color.Red) {
                                    Text("Expiré", fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Badge(containerColor = Color(0xFFFBBF24).copy(alpha = 0.15f), contentColor = Color(0xFFB45309)) {
                                    Text("À faire", fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Text(tp.instructions, color = Color.DarkGray, fontSize = 13.sp, modifier = Modifier.padding(vertical = 10.dp))

                        Divider(color = Color.LightGray.copy(alpha = 0.5f), modifier = Modifier.padding(bottom = 10.dp))

                        if (tp.isSubmitted) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Ficher: ${tp.submittedFileName}", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    val sdf = remember { java.text.SimpleDateFormat("dd/MM, HH:mm", java.util.Locale.FRANCE) }
                                    Text("Envoi: ${sdf.format(java.util.Date(tp.submittedDateMillis))}", color = Color.Gray, fontSize = 12.sp)
                                }
                                if (tp.grade >= 0) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Évaluation de l'Assistant :", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Black)
                                        Text("${tp.grade} / 20", fontWeight = FontWeight.ExtraBold, color = Color(0xFF0C9C3E), fontSize = 16.sp)
                                    }
                                } else {
                                    Text("Statut d'évaluation: En attente de correction", fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, color = Color.Gray, fontSize = 12.sp)
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Délai d'envoi :", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    if (isExpired) {
                                        Text("Clôturé", color = Color.Red, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                                    } else {
                                        val hours = remainingTime / 3600000
                                        val mins = (remainingTime % 3600000) / 60000
                                        Text("Reste ${hours}h ${mins}min", color = Color(0xFFB45309), fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                                    }
                                }

                                Button(
                                    onClick = { selectedTpForSubmit = tp },
                                    enabled = !isExpired,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00054F)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("DÉPOSER LE TRAVAIL", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (selectedTpForSubmit != null) {
        SubmitTpDialog(
            tp = selectedTpForSubmit!!,
            viewModel = viewModel,
            onDismiss = { selectedTpForSubmit = null }
        )
    }
}

@Composable
fun SubmitTpDialog(tp: TpSubmission, viewModel: AppViewModel, onDismiss: () -> Unit) {
    var fileName by remember { mutableStateOf("tp_udbl_solution.zip") }
    var textComments by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    var isSubmittingLocal by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Soumettre le TP",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = Color.Black
                )
                Text(
                    text = tp.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Divider(color = Color.LightGray.copy(alpha = 0.5f))

                Text("Fichier sélectionné :", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)

                // Simulated File Selector dropdown/chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF3F4F6), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.AttachFile, contentDescription = null, tint = Color.Gray)
                    Text(fileName, fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 13.sp)
                    TextButton(
                        onClick = {
                            fileName = if (fileName.endsWith(".zip")) "solution_code_source.kt" else "tp_udbl_solution.zip"
                        }
                    ) {
                        Text("Modifier", fontWeight = FontWeight.Bold, color = Color(0xFF00054F), fontSize = 12.sp)
                    }
                }

                Text("Commentaires à l'Assistant :", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)

                OutlinedTextField(
                    value = textComments,
                    onValueChange = { textComments = it },
                    placeholder = { Text("Écrivez vos notes de TP ou la liste des packages utilisés...", color = Color.Gray, fontSize = 13.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(84.dp)
                        .testTag("tp_text_comments"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedBorderColor = Color(0xFF00054F),
                        unfocusedBorderColor = Color.LightGray
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Annuler", color = Color.Black)
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                isSubmittingLocal = true
                                kotlinx.coroutines.delay(1200) // Latency animation
                                viewModel.submitAssignment(tp.id, fileName, textComments, onComplete = onDismiss)
                                isSubmittingLocal = false
                            }
                        },
                        enabled = !isSubmittingLocal,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0C9C3E)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.2f)
                    ) {
                        if (isSubmittingLocal) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Text("SOUMETTRE", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}


// --- FEATURE 4: FORUM DE TUTORAT ET D'ENTRAIDE ---
@Composable
fun StudentForumScreen(viewModel: AppViewModel) {
    val activeModule by viewModel.selectedCourseModuleForForum.collectAsState()
    val messages by viewModel.forumMessagesState.collectAsState()
    var postText by remember { mutableStateOf("") }
    var submitAnonymous by remember { mutableStateOf(false) }

    val modules = listOf("Compilation", "Intelligence Artificielle", "Génie Logiciel II", "Systèmes Distribués")

    androidx.activity.compose.BackHandler(enabled = true) {
        viewModel.setTab(0)
    }

    Scaffold(
        topBar = {
            HeaderBar(
                title = "Forum d'Entraide UDBL",
                onBack = { viewModel.setTab(0) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF3F4F6))
                .padding(padding)
        ) {
            // Horizontal scrolling filters chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(vertical = 10.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                modules.forEach { module ->
                    val isSelected = activeModule == module
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setForumModule(module) },
                        label = { Text(module, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF00054F),
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // Message Board
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 12.dp)
            ) {
                if (messages.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(36.dp))
                                Text("Aucune discussion lancée", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 14.sp)
                                Text("Soyez le premier à poser une question d'entraide pour le cours de $activeModule !", color = Color.Gray, fontSize = 12.sp, textAlign = TextAlign.Center)
                            }
                        }
                    }
                } else {
                    items(messages) { msg ->
                        val isSelf = msg.senderName != "Utilisateur Anonyme" && !msg.isAnonymous
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .background(
                                                    if (msg.isAnonymous) Color.LightGray else Color(0xFF0C9C3E).copy(alpha = 0.15f),
                                                    CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (msg.isAnonymous) Icons.Default.VisibilityOff else Icons.Default.Person,
                                                contentDescription = null,
                                                tint = if (msg.isAnonymous) Color.DarkGray else Color(0xFF0C9C3E),
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                        Text(
                                            text = msg.senderName,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 13.sp,
                                            color = if (msg.isAnonymous) Color.Gray else Color.Black
                                        )
                                    }
                                    val sdf = remember { java.text.SimpleDateFormat("dd/MM, HH:mm", java.util.Locale.FRANCE) }
                                    Text(sdf.format(java.util.Date(msg.timestampMillis)), color = Color.Gray, fontSize = 10.sp)
                                }
                                Text(
                                    msg.messageContent,
                                    color = Color.Black,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp,
                                    modifier = Modifier.padding(top = 8.dp, start = 4.dp, end = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Input message box at the bottom
            Surface(
                color = Color.White,
                tonalElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = submitAnonymous,
                            onCheckedChange = { submitAnonymous = it },
                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF00054F))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Anonyme (Masquer mon identité)", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = postText,
                            onValueChange = { postText = it },
                            placeholder = { Text("Écrivez un message d'entraide...", color = Color.Gray, fontSize = 13.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("forum_post_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                focusedBorderColor = Color(0xFF00054F)
                            ),
                            shape = RoundedCornerShape(24.dp),
                            singleLine = true
                        )

                        IconButton(
                            onClick = {
                                if (postText.trim().isNotEmpty()) {
                                    viewModel.sendForumMessage(activeModule, postText, submitAnonymous)
                                    postText = ""
                                }
                            },
                            modifier = Modifier
                                .background(Color(0xFF00054F), CircleShape)
                                .size(44.dp)
                                .testTag("forum_post_send_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Poster", tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}


// --- FEATURE 5: PORTAIL DE STAGES, TFE ET EMPLOIS ---
@Composable
fun CareersHubScreen(viewModel: AppViewModel) {
    val offers by viewModel.offersState.collectAsState()
    val scope = rememberCoroutineScope()
    var successOfferTitle by remember { mutableStateOf<String?>(null) }
    var activeOfferIdInProgress by remember { mutableStateOf<Int?>(null) }

    androidx.activity.compose.BackHandler(enabled = true) {
        viewModel.setTab(0)
    }

    Scaffold(
        topBar = {
            HeaderBar(
                title = "Stages, TFE & Emplois",
                onBack = { viewModel.setTab(0) }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF3F4F6))
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "ORIENTATION ET CANAUX CORPORATIFS",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            color = Color.Black
                        )
                        Text(
                            text = "Trouvez des opportunités de stages professionnels ou de projets de fin d'étude (TFE) parrainés directement par les grandes entreprises du Katanga.",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            items(offers) { offer ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("offer_card_${offer.id}")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Badge(containerColor = Color(0xFF00054F).copy(alpha = 0.1f), contentColor = Color(0xFF00054F)) {
                                    Text(offer.type, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                }
                                Text(offer.title, fontWeight = FontWeight.ExtraBold, color = Color.Black, fontSize = 15.sp, modifier = Modifier.padding(top = 4.dp))
                                Text(offer.companyName, fontWeight = FontWeight.Bold, color = Color(0xFF0C9C3E), fontSize = 12.sp)
                            }
                            Text(offer.location, color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Text(
                            text = offer.description,
                            color = Color.DarkGray,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(vertical = 10.dp)
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF3F4F6), RoundedCornerShape(12.dp))
                                .padding(10.dp)
                        ) {
                            Text("Exigence: ${offer.requirements}", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        if (offer.isApplied) {
                            OutlinedButton(
                                onClick = {},
                                enabled = false,
                                border = BorderStroke(1.2.dp, Color(0xFF0C9C3E)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF0C9C3E))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("CANDIDATURE SOUMISE (En attente)", color = Color(0xFF0C9C3E), fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Button(
                                onClick = {
                                    scope.launch {
                                        activeOfferIdInProgress = offer.id
                                        kotlinx.coroutines.delay(1200) // Simulated submission animation
                                        viewModel.applyToStageOffer(offer.id, onComplete = {
                                            successOfferTitle = offer.title
                                        })
                                        activeOfferIdInProgress = null
                                    }
                                },
                                enabled = activeOfferIdInProgress == null,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00054F)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (activeOfferIdInProgress == offer.id) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.SendAndArchive, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("POSTULER EN 1 CLIC (Relevé UDBL)", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (successOfferTitle != null) {
        Dialog(onDismissRequest = { successOfferTitle = null }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(Color(0xFF0C9C3E).copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color(0xFF0C9C3E), modifier = Modifier.size(36.dp))
                    }
                    Text("Candidature Transmise !", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                    Text(
                        text = "Félicitations, votre profil étudiant ainsi que vos notes académiques ont bien été envoyés aux recruteurs du Katanga pour le poste:\n\n$successOfferTitle",
                        textAlign = TextAlign.Center,
                        color = Color.DarkGray,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    Button(
                        onClick = { successOfferTitle = null },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0C9C3E)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("D'accord", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}


// --- FEATURE 6: IA ACADEMIC TUTOR CHAT (GEMINI) ---
@Composable
fun AiTutorScreen(viewModel: AppViewModel) {
    val messages by viewModel.tutorMessagesState.collectAsState()
    val isLoading by viewModel.tutorIsLoading.collectAsState()
    var currentInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    androidx.activity.compose.BackHandler(enabled = true) {
        viewModel.setTab(0)
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            HeaderBar(
                title = "Tuteur Académique IA",
                onBack = { viewModel.setTab(0) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF3F4F6))
                .padding(padding)
        ) {
            // Chat prompt logs area
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
            ) {
                items(messages) { msg ->
                    val isUser = msg.sender == "STUDENT"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                    ) {
                        Card(
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (isUser) 16.dp else 4.dp,
                                bottomEnd = if (isUser) 4.dp else 16.dp
                            ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isUser) Color(0xFFE5E7EB) else Color(0xFF00054F)
                            ),
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = if (isUser) "Vous" else "Assistant IA UDBL",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (isUser) Color.Black.copy(alpha = 0.6f) else Color(0xFFFBBF24)
                                )
                                Text(
                                    text = msg.content,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp,
                                    color = if (isUser) Color.Black else Color.White,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }

                if (isLoading) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF00054F).copy(alpha = 0.85f)),
                                modifier = Modifier.padding(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                    Text("L'IA formule la réponse...", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Keyboard input tray
            Surface(
                color = Color.White,
                tonalElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = currentInput,
                        onValueChange = { currentInput = it },
                        placeholder = { Text("Posez-moi une question sur vos devoirs...", color = Color.Gray, fontSize = 13.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("ai_tutor_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedBorderColor = Color(0xFF00054F)
                        ),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true
                    )

                    IconButton(
                        onClick = {
                            if (currentInput.trim().isNotEmpty()) {
                                viewModel.sendTutorQuestion(currentInput)
                                currentInput = ""
                            }
                        },
                        modifier = Modifier
                            .background(Color(0xFF00054F), CircleShape)
                            .size(44.dp)
                            .testTag("ai_tutor_send_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Demander", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}


// --- FEATURE 7: CARTE D'ÉTUDIANT DIGITALE AVEC QR TOURNANT ---
@Composable
fun DigitalCardScreen(viewModel: AppViewModel) {
    val secondsRemaining by viewModel.secondsUntilQrRefresh.collectAsState()
    val qrCodeString by viewModel.dynamicIdQrState.collectAsState()
    val profile by viewModel.currentProfile.collectAsState()

    androidx.activity.compose.BackHandler(enabled = true) {
        viewModel.setTab(0)
    }

    Scaffold(
        topBar = {
            HeaderBar(
                title = "Carte d'Étudiant Digitale",
                onBack = { viewModel.setTab(0) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF3F4F6))
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "CARTE DE SÉCURITÉ CONCÉDÉE PAR L'UDBL",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 11.sp,
                color = Color.Gray,
                letterSpacing = 0.5.sp
            )

            // Dynamic cosmic metal smart card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .shadow(12.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF00054F)) // Deep indigo corporate blue
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Right background circular graphics details representation
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = 60.dp, y = 60.dp)
                            .size(200.dp)
                            .background(Color(0xFFFBBF24).copy(alpha = 0.08f), CircleShape)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Card Header
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(Color.White, CircleShape)
                                        .padding(4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = androidx.compose.ui.res.painterResource(id = R.drawable.udbl_logo_asset_1779824734235),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                Text("UDBL VISION CARD", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            Badge(containerColor = Color(0xFF0C9C3E), contentColor = Color.White) {
                                Text("ACTIF", fontWeight = FontWeight.ExtraBold)
                            }
                        }

                        // Code matrix ID
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // High contrast photo thumbnail
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .border(BorderStroke(2.dp, Color(0xFFFBBF24)), RoundedCornerShape(12.dp))
                                    .background(Color.White, RoundedCornerShape(12.dp))
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF00054F), modifier = Modifier.size(48.dp))
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = profile?.name ?: "Mélanie Mwange",
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 17.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Matricule: ${profile?.matricule ?: "UDBL/INF/2026/0147"}",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "Dép: ${profile?.faculty ?: "Sciences Informatiques"}",
                                    color = Color(0xFFFBBF24),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = profile?.promotion ?: "Licence 2 (L2)",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Divider(color = Color.White.copy(alpha = 0.15f), modifier = Modifier.padding(vertical = 4.dp))

                        // Card Footer
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("UNIVERSITÉ DE LUBUMBASHI", color = Color.White.copy(alpha = 0.5f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text("Scientia et Fides", color = Color(0xFFFBBF24), fontSize = 10.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // QR code generator
            Card(
                modifier = Modifier
                    .size(220.dp)
                    .shadow(4.dp, RoundedCornerShape(24.dp))
                    .testTag("dynamic_student_qr"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCode,
                        contentDescription = "Dynamic Identity QR Code",
                        tint = Color(0xFF00054F),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Dynamic secure hash info
            Text(
                text = "Hash: " + qrCodeString.hashCode().toString().uppercase(),
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                color = Color.DarkGray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )

            // Dynamic Countdown
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFE5E7EB))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Default.Timer, contentDescription = null, tint = Color(0xFF00054F), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Changement de code QR dans : $secondsRemaining sec...",
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF00054F),
                    fontSize = 12.sp
                )
            }
        }
    }
}


@Composable
fun HeaderBar(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(56.dp)
            .background(Color.White)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Retour", tint = Color(0xFF00054F))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            fontWeight = FontWeight.ExtraBold,
            color = Color.Black,
            fontSize = 18.sp
        )
    }
}

