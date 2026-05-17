package sample.bad

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun LoginScreen(onNavigateHome: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Column {
        TextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
        TextField(value = password, onValueChange = { password = it }, label = { Text("Password") })

        Button(onClick = {
            // Business logic incorrectly embedded in UI layer.
            if (!email.contains("@") || password.length < 8) {
                error = "Invalid credentials format"
            } else if (email.endsWith("@blocked.com")) {
                error = "Domain is not allowed"
            } else {
                error = null
                onNavigateHome()
            }
        }) {
            Text("Login")
        }

        if (error != null) {
            Text(error!!)
        }
    }
}
