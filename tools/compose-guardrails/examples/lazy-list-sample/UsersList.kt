package sample.lazylist

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

data class User(val id: String, val name: String)

@Composable
fun UsersList(users: List<User>) {
    LazyColumn {
        items(users) { user ->
            Text(user.name)
        }
    }
}
