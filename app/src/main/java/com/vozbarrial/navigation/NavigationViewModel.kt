package com.vozbarrial.navigation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow

class NavigationViewModel(private val state: SavedStateHandle) : ViewModel() {
    val screen: StateFlow<String> = state.getStateFlow(SCREEN, "welcome")
    val dashboardPage: StateFlow<String> = state.getStateFlow(DASHBOARD, "profile")

    fun navigate(route: String) { state[SCREEN] = route }
    fun showDashboardPage(page: String) { state[DASHBOARD] = page; navigate("dashboard") }

    companion object {
        private const val SCREEN = "navigation_screen"
        private const val DASHBOARD = "dashboard_page"
    }
}
