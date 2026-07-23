package com.warehouse.shipping.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle

@Composable
fun ClickToCopyText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = TextStyle.Default
) {
    val clipboardManager = LocalClipboardManager.current
    Text(
        text = text,
        style = style,
        modifier = modifier.clickable {
            clipboardManager.setText(AnnotatedString(text))
        }
    )
}
