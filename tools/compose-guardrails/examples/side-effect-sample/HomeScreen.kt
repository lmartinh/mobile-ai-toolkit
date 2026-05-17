package sample.sideeffect

import androidx.compose.runtime.Composable

interface Repo { fun sync() }

@Composable
fun HomeScreen(repo: Repo) {
    repo.sync()
}
