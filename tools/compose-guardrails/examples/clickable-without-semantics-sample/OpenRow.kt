package sample.semantics

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun OpenRow(onOpen: () -> Unit) {
    Box(modifier = Modifier.clickable { onOpen() }) {
        Text("Open")
    }
}
