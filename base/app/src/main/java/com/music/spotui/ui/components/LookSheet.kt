package com.music.spotui.ui.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import com.music.spotui.ui.immersive.sheets.ImmersiveGlassSheet
import com.music.spotui.ui.navigation.isImmersive

/**
 * Ensures that the system navigation bar remains transparent and uses dark-theme icons
 * inside a bottom sheet's separate [DialogWindowProvider] window.
 */
@Composable
fun ConfigureSheetWindow() {
    val view = LocalView.current
    SideEffect {
        var current: Any? = view
        var window: android.view.Window? = null
        while (current != null) {
            if (current is DialogWindowProvider) {
                window = current.window
                break
            }
            current = (current as? android.view.View)?.parent
        }
        window?.let { w ->
            w.navigationBarColor = android.graphics.Color.TRANSPARENT
            w.isNavigationBarContrastEnforced = false
            WindowCompat.getInsetsController(w, view).isAppearanceLightNavigationBars = false
        }
    }
}

/**
 * The bottom sheet of the screens shared by both looks: Material's [ModalBottomSheet] on
 * [containerColor] in Classic (exactly as before), a glass sheet in Immersive.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LookSheet(
    onDismissRequest: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(),
    containerColor: Color,
    content: @Composable ColumnScope.() -> Unit,
) {
    if (isImmersive()) ImmersiveGlassSheet(onDismissRequest, sheetState, content)
    else ModalBottomSheet(onDismissRequest = onDismissRequest, sheetState = sheetState, containerColor = containerColor) {
        ConfigureSheetWindow()
        content()
    }
}
