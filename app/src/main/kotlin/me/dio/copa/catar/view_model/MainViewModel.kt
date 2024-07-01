package me.dio.copa.catar.view_model

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.dio.copa.catar.core.BaseViewModel
import me.dio.copa.catar.domain.model.Match
import me.dio.copa.catar.domain.model.MatchDomain
import me.dio.copa.catar.domain.usecase.DisableNotificationUseCase
import me.dio.copa.catar.domain.usecase.EnableNotificationUseCase
import me.dio.copa.catar.domain.usecase.GetMatchesUseCase
import me.dio.copa.catar.remote.NotFoundException
import me.dio.copa.catar.remote.UnexpectedException
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getMatchesUseCase: GetMatchesUseCase,
    private val enableNotificationUseCase: EnableNotificationUseCase,
    private val disableNotificationUseCase: DisableNotificationUseCase
) : BaseViewModel<MainViewModel.MainUiState, MainViewModel.MainUiAction>(MainUiState()) {

    init {
        fetchMatches()
    }

    private fun fetchMatches() = viewModelScope.launch {
        getMatchesUseCase()
            .flowOn(Dispatchers.IO)
            .catch {
                when (it) {
                    is NotFoundException ->
                        sendAction(MainUiAction.MatchesNotFound(it.message ?: "No message"))

                    is UnexpectedException ->
                        sendAction(MainUiAction.Unexpected)
                }
            }
            .collect { matches ->
                setState { copy(matches = matches) }
            }

    }

    fun changeNotificationSetting(match: MatchDomain) = viewModelScope.launch {
        runCatching {
            withContext(Dispatchers.IO) {
                val notificationAction = if (match.notificationEnabled) {
                    disableNotificationUseCase(match.id)
                    MainUiAction.DisableNotification(match)
                } else {
                    enableNotificationUseCase(match.id)
                    MainUiAction.EnableNotification(match)
                }
                sendAction(notificationAction)
            }
        }
    }

    // State of the screen
    data class MainUiState(
        val matches: List<Match> = emptyList()
    )

    sealed interface MainUiAction {
        data class MatchesNotFound(val message: String) : MainUiAction
        object Unexpected : MainUiAction
        data class EnableNotification(val match: MatchDomain) : MainUiAction
        data class DisableNotification(val match: MatchDomain) : MainUiAction
    }
}

