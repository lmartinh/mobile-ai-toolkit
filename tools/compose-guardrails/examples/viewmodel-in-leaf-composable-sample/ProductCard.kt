package sample.viewmodelleaf

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel

class ProductViewModel

@Composable
fun ProductCard() {
    val vm: ProductViewModel = viewModel()
}
