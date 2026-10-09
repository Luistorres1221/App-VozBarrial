package com.vozbarrial.navigation

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vozbarrial.data.auth.AuthRepository
import com.vozbarrial.data.profile.ProfileRepository
import com.vozbarrial.data.reports.ReportsRepository
import com.vozbarrial.data.store.StoreRepository
import com.vozbarrial.features.auth.Auth
import com.vozbarrial.features.auth.presentation.viewmodel.AuthViewModel
import com.vozbarrial.features.auth.presentation.viewmodel.AuthViewModelFactory
import com.vozbarrial.features.profile.ProfileViewModel
import com.vozbarrial.features.profile.ProfileViewModelFactory
import com.vozbarrial.features.reports.ReportsViewModel
import com.vozbarrial.features.reports.ReportsViewModelFactory
import com.vozbarrial.features.store.StoreViewModel
import com.vozbarrial.features.store.StoreViewModelFactory
import com.vozbarrial.features.dashboard.CommunityPage
import com.vozbarrial.features.dashboard.EditProfile
import com.vozbarrial.features.dashboard.FramesPage
import com.vozbarrial.features.dashboard.ReportCreate
import com.vozbarrial.features.dashboard.StorePage
import com.vozbarrial.features.map.ReportsMap
import com.vozbarrial.features.welcome.Welcome

@Composable
fun AppNavGraph() {
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(remember { AuthRepository() }))
    val profileViewModel: ProfileViewModel = viewModel(factory = ProfileViewModelFactory(remember { ProfileRepository() }))
    val reportsViewModel: ReportsViewModel = viewModel(factory = ReportsViewModelFactory(remember { ReportsRepository() }))
    val storeViewModel: StoreViewModel = viewModel(factory = StoreViewModelFactory(remember { StoreRepository() }))
    val navigationViewModel: NavigationViewModel = viewModel()
    val profileState by profileViewModel.uiState.collectAsStateWithLifecycle()
    val reportsState by reportsViewModel.uiState.collectAsStateWithLifecycle()
    val screen by navigationViewModel.screen.collectAsStateWithLifecycle()
    val dashboardPage by navigationViewModel.dashboardPage.collectAsStateWithLifecycle()

    val profile = profileState.profile
    val name = profile?.name.orEmpty()
    val phone = profile?.phone.orEmpty()
    val photo = profile?.photoUrl
    val frame = profile?.equippedFrame ?: "Clásico Cívico"
    val points = profile?.points ?: 0
    val ownedFrames = profile?.ownedFrames?.toSet().orEmpty()
    val myReports = remember(reportsState.reports, profile?.uid, profile?.email) {
        val profileUid = profile?.uid.orEmpty()
        val profileEmail = profile?.email.orEmpty()
        reportsState.reports.filter { report ->
            if (profileUid.isNotBlank() && report.authorUid.isNotBlank()) report.authorUid == profileUid
            else report.authorEmail.equals(profileEmail, ignoreCase = true)
        }
    }

    fun enterApp() { profileViewModel.loadProfile { loaded ->
        if (loaded) { reportsViewModel.startObserving(); navigationViewModel.navigate("map") }
    } }

    LaunchedEffect(Unit) {
        if (profileViewModel.hasActiveSession()) enterApp()
    }

    when (screen) {
        "auth" -> Auth(
            onBack = { navigationViewModel.navigate("welcome") },
            onLogin = authViewModel::signIn,
            onRegister = authViewModel::createAccount,
            onRequestPasswordReset = authViewModel::requestPasswordReset,
            onAuthenticated = { enterApp() },
        )
        "map" -> ReportsMap(
            name = name,
            profilePhoto = photo,
            points = points,
            communityReports = reportsState.reports,
            onSignOut = { reportsViewModel.stopObserving(); profileViewModel.signOut(); navigationViewModel.navigate("welcome") },
            onNavigate = { page ->
                if (page == "createReport") navigationViewModel.navigate("createReport")
                else navigationViewModel.showDashboardPage(page)
            },
        )
        "dashboard" -> CommunityPage(
            page = dashboardPage,
            name = name,
            onBack = { navigationViewModel.navigate("map") },
            onNavigate = { page ->
                when (page) {
                    "editProfile", "frames", "store" -> navigationViewModel.navigate(page)
                    else -> navigationViewModel.showDashboardPage(page)
                }
            },
            onSignOut = { reportsViewModel.stopObserving(); profileViewModel.signOut(); navigationViewModel.navigate("welcome") },
            onSaveName = { newName -> profileViewModel.updateProfile(newName, phone, photo) },
            onDeleteAccount = { profileViewModel.deleteAccount { deleted ->
                if (deleted) { reportsViewModel.stopObserving(); navigationViewModel.navigate("welcome") }
            } },
            profilePhoto = photo,
            points = points,
            reports = myReports,
            onEditReport = reportsViewModel::updateReport,
            onDeleteReport = reportsViewModel::deleteReport,
        )
        "frames" -> FramesPage(
            name = name,
            activeFrame = frame,
            points = points,
            owned = ownedFrames,
            onBack = { navigationViewModel.showDashboardPage("profile") },
            onEquip = { selected -> storeViewModel.equip(selected) { ok, _ -> if (ok) profileViewModel.acceptEquippedFrame(selected) } },
            onOpenStore = { navigationViewModel.navigate("store") },
        )
        "store" -> StorePage(
            name = name,
            points = points,
            owned = ownedFrames,
            activeFrame = frame,
            onBack = { navigationViewModel.showDashboardPage("profile") },
            onPurchase = { selected, cost, _, result -> storeViewModel.purchase(selected, cost, profileViewModel::acceptProfile, result) },
            onEquip = { selected, result -> storeViewModel.equip(selected) { ok, error ->
                if (ok) profileViewModel.acceptEquippedFrame(selected)
                result(ok, error)
            } },
        )
        "createReport" -> ReportCreate(
            name = name,
            onBack = { navigationViewModel.navigate("map") },
            onPublish = { title, description, category, latitude, longitude, photoUri, done ->
                reportsViewModel.createReport(title, description, category, latitude, longitude, photoUri, profile) { published, message ->
                    if (published) navigationViewModel.navigate("map")
                    done(published, message)
                }
            },
        )
        "editProfile" -> EditProfile(
            name = name,
            phone = phone,
            photo = photo,
            onCancel = { navigationViewModel.showDashboardPage("profile") },
            onSave = { newName, newPhone, newPhoto ->
                profileViewModel.updateProfile(newName, newPhone, newPhoto) { saved ->
                    if (saved) navigationViewModel.showDashboardPage("profile")
                }
            },
        )
        else -> Welcome(onLoginClick = { navigationViewModel.navigate("auth") })
    }
}
