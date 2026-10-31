package com.btremote.app.ui.screens.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bluetooth
import androidx.compose.material.icons.outlined.Keyboard
import androidx.compose.material.icons.outlined.Mouse
import androidx.compose.material.icons.outlined.Navigation
import androidx.compose.material.icons.outlined.PlayCircleOutline
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.material3.ExperimentalMaterial3Api

import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.btremote.app.bluetooth.ConnectionState
import com.btremote.app.bluetooth.isConnected
import com.btremote.app.ui.components.ConnectionBar

import com.btremote.app.ui.screens.connect.ConnectScreen
import com.btremote.app.ui.screens.keyboard.KeyboardScreen
import com.btremote.app.ui.screens.media.MediaScreen
import com.btremote.app.ui.screens.touchpad.TouchpadScreen
import com.btremote.app.ui.screens.tv.TvRemoteScreen
import com.btremote.app.ui.theme.ErrorRed
import com.btremote.app.ui.theme.GlassBorderDark
import com.btremote.app.ui.theme.GlassDark
import com.btremote.app.ui.theme.GlowCyan
import com.btremote.app.ui.theme.GlowPink
import com.btremote.app.ui.theme.GlowPrimary
import com.btremote.app.ui.theme.NeonGreen

private enum class Tab(val label: String, val icon: ImageVector, val accentColor: @Composable () -> Color) {
    Touchpad("Pad",      Icons.Outlined.Mouse,            { GlowPrimary }),
    Keyboard("Keys",     Icons.Outlined.Keyboard,         { GlowCyan }),
    Media   ("Media",    Icons.Outlined.PlayCircleOutline, { NeonGreen }),
    TvRemote("TV",       Icons.Outlined.Tv,               { Color(0xFFFFD93D) }),
    Connect ("Pair",     Icons.Outlined.Bluetooth,        { GlowCyan })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state       by viewModel.connectionState.collectAsStateWithLifecycle()
    val quick       by viewModel.quickPairTarget.collectAsStateWithLifecycle()
    val pairedHistory by viewModel.pairedHistory.collectAsStateWithLifecycle()
    val keyboardLayout by viewModel.keyboardLayout.collectAsStateWithLifecycle()
    var current     by remember { mutableStateOf(Tab.Touchpad) }

    val context = LocalContext.current
    DisposableEffect(current, keyboardLayout) {
        val activity = context as? Activity
        val landscape = current == Tab.Keyboard && keyboardLayout == "laptop"
        activity?.requestedOrientation = if (landscape) {
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (current != Tab.Keyboard) {
                TopAppBar(
                    title = {
                        Text(
                            "PADKEYS",
                            style = MaterialTheme.typography.titleMedium.copy(
                                letterSpacing = 4.sp
                            )
                        )
                    },
                    actions = {
                        IconButton(onClick = onOpenSettings) {
                            Icon(Icons.Outlined.Settings, "Settings", tint = MaterialTheme.colorScheme.onSurface)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        },
        bottomBar = {
            if (current != Tab.Keyboard) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp
                ) {
                    Tab.entries.forEach { t ->
                        val selected = current == t
                        NavigationBarItem(
                            selected = selected,
                            onClick = { current = t },
                            icon = {
                                Icon(t.icon, contentDescription = t.label)
                            },
                            label = { Text(t.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                selectedTextColor = MaterialTheme.colorScheme.onSurface,
                                indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            // Device switcher bar
            ConnectionBar(
                state           = state,
                pairedHistory   = pairedHistory,
                onClick         = { current = Tab.Connect },
                quickPairLabel  = quick?.name,
                onQuickPair     = if (quick != null) { { viewModel.quickPair() } } else null,
                onSwitchDevice  = { entry -> viewModel.reconnectTo(entry) },
                onScanRequested = { current = Tab.Connect },
                onDisconnect    = { viewModel.disconnect() },
                onForgetDevice  = { entry -> viewModel.forgetDevice(entry) },
                onBack          = if (current == Tab.Keyboard) { { current = Tab.Touchpad } } else null,
                keyboardLayout  = if (current == Tab.Keyboard) keyboardLayout else null,
                onKeyboardLayoutChange = if (current == Tab.Keyboard) {
                    { layout -> viewModel.setKeyboardLayout(layout) }
                } else null
            )
            Box(modifier = Modifier.fillMaxSize()) {
                AnimatedContent(
                    targetState = current,
                    transitionSpec = { (slideInHorizontally(tween(220)) { it / 6 } + fadeIn(tween(220))) togetherWith (slideOutHorizontally(tween(180)) { -it / 6 } + fadeOut(tween(180))) },
                    label = "tabContent"
                ) { tab ->
                    when (tab) {
                        Tab.Touchpad -> TouchpadScreen()
                        Tab.Keyboard -> KeyboardScreen()
                        Tab.Media    -> MediaScreen()
                        Tab.TvRemote -> TvRemoteScreen()
                        Tab.Connect  -> ConnectScreen()
                    }
                }
            }
        }
    }
}

// ────────────────────────────────────────────────────────────────────────────
// SoulExtender Synth Nav Bar
// ────────────────────────────────────────────────────────────────────────────

@Composable
private fun SynthNavBar(
    current: Tab,
    connectionState: ConnectionState,
    onSelect: (Tab) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(Color.Transparent, MaterialTheme.colorScheme.surface)
                )
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(GlassDark)
                .border(
                    1.dp,
                    GlassBorderDark,
                    RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                )
                .navigationBarsPadding()
                .padding(horizontal = 6.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Tab.entries.forEach { t ->
                    val disconnectBadge = t == Tab.Connect && !connectionState.isConnected
                    SynthNavItem(
                        tab         = t,
                        selected    = current == t,
                        showBadge   = disconnectBadge,
                        onClick     = { onSelect(t) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SynthNavItem(
    tab: Tab,
    selected: Boolean,
    showBadge: Boolean,
    onClick: () -> Unit
) {
    val accent = tab.accentColor()

    Box {
        Column(
            modifier = Modifier
                .height(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (selected)
                        Brush.verticalGradient(
                            listOf(accent.copy(alpha = 0.25f), accent.copy(alpha = 0.10f))
                        )
                    else
                        Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))
                )
                .then(
                    if (selected)
                        Modifier.border(1.dp, accent.copy(alpha = 0.55f), RoundedCornerShape(16.dp))
                    else
                        Modifier
                )
                .pointerInput(tab) { detectTapGestures(onTap = { onClick() }) }
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = tab.icon,
                contentDescription = tab.label,
                tint = if (selected) accent else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text  = tab.label,
                style = MaterialTheme.typography.labelSmall,
                color = if (selected) accent else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Red dot badge on Connect tab when disconnected
        if (showBadge && !selected) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = (-4).dp, y = 2.dp)
                    .clip(CircleShape)
                    .background(ErrorRed)
            )
        }
    }
}
