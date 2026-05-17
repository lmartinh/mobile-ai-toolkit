package sample.clean

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun Greeting(name: String) {
    Text("Hello, $name")
}

@Preview
@Composable
fun GreetingPreview() {
    Greeting(name = "Compose")
}
