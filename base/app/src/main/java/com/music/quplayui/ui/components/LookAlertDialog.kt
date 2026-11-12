package com.music.quplayui.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.music.quplayui.ui.immersive.sheets.ImmersiveAlertDialog

/**
 * The alert dialog of the app: a glass dialog in Immersive.
 */
@Composable
fun LookAlertDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    containerColor: Color = Color.Unspecified,
    dismissButton: (@Composable () -> Unit)? = null,
    icon: (@Composable () -> Unit)? = null,
    title: (@Composable () -> Unit)? = null,
    text: (@Composable () -> Unit)? = null,
) {
    ImmersiveAlertDialog(onDismissRequest, confirmButton, dismissButton, icon, title, text)
}
