package me.dio.copa.catar.features

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dagger.hilt.android.AndroidEntryPoint
import me.dio.copa.catar.R
import me.dio.copa.catar.domain.extensions.getDate
import me.dio.copa.catar.domain.model.MatchDomain
import me.dio.copa.catar.extensions.observe
import me.dio.copa.catar.notification.scheduler.extensions.NotificationMatcheWorker
import me.dio.copa.catar.ui.theme.Copa2022Theme
import me.dio.copa.catar.ui.theme.Shapes
import me.dio.copa.catar.view_model.MainViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        observerAction()
        setContent {
            Copa2022Theme {
                val state = viewModel.state.collectAsState()
                val matches = state.value.matches

                // A surface container using the 'background' color from the theme
                Scaffold(
                    topBar = {
                        TopAppBar(
                            backgroundColor = MaterialTheme.colors.primary,
                            contentColor = MaterialTheme.colors.secondary,
                            title = {
                                Text("Partidas")
                            }
                        )
                    },
                    modifier = Modifier.fillMaxSize(),
                ) { paddingValues ->
                    LazyColumn(
                        contentPadding = PaddingValues(4.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .background(
                                color = MaterialTheme.colors.primary
                            ),


                        ) {
                        items(matches) { match ->
                            CardGames(match) {
                                viewModel.changeNotificationSetting(it)
                            }
                            Log.d("matche", "id notification? ${match}")
                        }
                    }
                }
            }
        }
    }

    private fun observerAction() {
        viewModel.action.observe(this) { action ->
            when (action) {
                is MainViewModel.MainUiAction.DisableNotification ->
                    NotificationMatcheWorker.stop(applicationContext, action.match)

                is MainViewModel.MainUiAction.EnableNotification ->
                    NotificationMatcheWorker.start(applicationContext, action.match)

                is MainViewModel.MainUiAction.MatchesNotFound -> TODO()
                MainViewModel.MainUiAction.Unexpected -> TODO()
            }

        }
    }

}


@Composable
fun CardGames(match: MatchDomain, onCLick: NotificationOnclick) {
    Card(
        backgroundColor = MaterialTheme.colors.primaryVariant,
        contentColor = MaterialTheme.colors.secondary,
        shape = Shapes.medium,
        modifier = Modifier.padding(4.dp)
    ) {

        NotificationMatche(match, onCLick)

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)


        ) {

            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = match.team1.flag,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally),
                    style = MaterialTheme.typography.h3
                )
                Text(
                    text = match.team1.displayName,
                    modifier = Modifier.padding(top = 8.dp),
                    style = MaterialTheme.typography.h6

                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "VS",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = match.date.getDate(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal
                )
            }
            Column {
                Text(
                    text = match.team2.flag,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally),
                    style = MaterialTheme.typography.h3
                )
                Text(
                    text = match.team2.displayName,
                    modifier = Modifier.padding(top = 8.dp),
                    style = MaterialTheme.typography.h6


                )

            }

        }

    }
}

@Composable
fun NotificationMatche(match: MatchDomain, onCLick: NotificationOnclick) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        horizontalArrangement = Arrangement.End

    ) {
        val notificationSign = if (match.notificationEnabled) R.drawable.ic_notifications_active
        else R.drawable.ic_notifications

        Image(
            painter = painterResource(id = notificationSign),
            modifier = Modifier.clickable {
                onCLick(match)
            },
            contentDescription = ""
        )
    }
}

typealias NotificationOnclick = (match: MatchDomain) -> Unit

