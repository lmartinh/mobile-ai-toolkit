package sample.businesslogic

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun CheckoutScreen(total: Double, vip: Boolean, onPay: () -> Unit) {
    Button(onClick = {
        val finalAmount = if (vip && total > 100) total * 0.9 else total
        if (finalAmount <= 0.0) {
            return@Button
        }
        onPay()
    }) {
        Text("Pay")
    }
}
