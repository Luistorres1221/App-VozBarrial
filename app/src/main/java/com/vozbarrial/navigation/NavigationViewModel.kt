package com.vozbarrial.navigation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow

class NavigationViewModel(private val state: SavedStateHandle) : ViewModel() {
    val screen: StateFlow<String> = state.getStateFlow(SCREEN, "welcome")
    val dashboardPage: StateFlow<String> = state.getStateFlow(DASHBOARD, "profile")

    private val backStack: ArrayList<String>
        get() = state.get<ArrayList<String>>(BACK_STACK) ?: arrayListOf()

    val canGoBack: Boolean
        get() = backStack.isNotEmpty()

    fun navigate(route: String, clearStack: Boolean = false) {
        val current = screen.value
        val stack = if (clearStack) arrayListOf() else backStack
        if (clearStack || current != route) {
            if (!clearStack) {
                stack.add(current)
            }
            state[BACK_STACK] = stack
        }
        state[SCREEN] = route
    }

    fun goBack(): Boolean {
        val stack = backStack
        if (stack.isNotEmpty()) {
            val previous = stack.removeAt(stack.size - 1)
            state[BACK_STACK] = stack
            state[SCREEN] = previous
            return true
        }
        return false
    }

    fun showDashboardPage(page: String) {
        state[DASHBOARD] = page
        navigate("dashboard")
    }

    companion object {
        private const val SCREEN = "navigation_screen"
        private const val DASHBOARD = "dashboard_page"
        private const val BACK_STACK = "navigation_back_stack"
    }
}