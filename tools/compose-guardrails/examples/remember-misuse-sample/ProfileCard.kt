package sample.remembermisuse

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

data class Profile(val name: String)

fun loadProfile(userId: String): Profile = Profile(userId)

@Composable
fun ProfileCard(userId: String) {
    val profile = remember { loadProfile(userId) }
    Text(profile.name)
}
