package sample.androidlifecycle

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.compose.runtime.collectAsState

class OrdersVm : ViewModel() {
    private val _state = MutableStateFlow("idle")
    val state: StateFlow<String> = _state
}

@Composable
fun OrdersScreen(vm: OrdersVm) {
    val state by vm.state.collectAsState()
}
