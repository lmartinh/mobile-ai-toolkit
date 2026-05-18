package sample.effectkey

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
fun ProfileScreen(userId: String, load: (String) -> Unit) {
    LaunchedEffect(Unit) {
        load(userId)
    }
}
