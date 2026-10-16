package com.vozbarrial.navigation

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import com.vozbarrial.features.auth.login.ui.AuthRoute
import com.vozbarrial.features.auth.register.ui.RegisterRoute
import com.vozbarrial.features.profile.profile.ui.CommunityRoute
import com.vozbarrial.features.profile.editProfile.ui.EditProfileRoute
import com.vozbarrial.features.reports.presentation.ReportsViewModel
import com.vozbarrial.features.reports.ui.ReportCreateRoute
import com.vozbarrial.features.dashboard.FramesPage
import com.vozbarrial.features.dashboard.StorePage
import com.vozbarrial.features.map.ReportsMap
import com.vozbarrial.features.auth.welcome.ui.WelcomeRoute
import com.vozbarrial.core.result.AuthOperationResult
import com.vozbarrial.core.result.AuthError
import com.vozbarrial.domain.model.Usuario

@Composable
fun AppNavGraph() {
    val context = LocalContext.current
    val reportsViewModel: ReportsViewModel = viewModel()
    val navigationViewModel: NavigationViewModel = viewModel()

    val screen by navigationViewModel.screen.collectAsStateWithLifecycle()
    val dashboardPage by navigationViewModel.dashboardPage.collectAsStateWithLifecycle()

    var signedInName by rememberSaveable { mutableStateOf("") }
    var signedInEmail by rememberSaveable { mutableStateOf("") }
    var signedInPhone by rememberSaveable { mutableStateOf("") }
    var signedInPhoto by rememberSaveable { mutableStateOf<String?>(null) }
    var equippedFrame by rememberSaveable { mutableStateOf("Clásico Cívico") }
    var signedInPoints by rememberSaveable { mutableIntStateOf(1450) }
    var ownedFramesText by rememberSaveable { mutableStateOf("Clásico Cívico|Guardián Dorado|Eco Barrio Verde|Escudo Ciudadano") }
    val ownedFrames = ownedFramesText.split("|").filter { it.isNotBlank() }.toSet()

    val myReports = remember(reportsViewModel.reports, signedInEmail) {
        reportsViewModel.reports.filter { report ->
            report.authorEmail.equals(signedInEmail, ignoreCase = true)
        }
    }

    fun verifyAccount(email: String, password: String, onResult: (Boolean) -> Unit) {
        com.google.firebase.auth.FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    fun createAccount(name: String, email: String, phone: String, password: String, onResult: (Boolean, String?) -> Unit) {
        com.google.firebase.auth.FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid ?: ""
                val usuario = Usuario(uid = uid, name = name, email = email, phone = phone)
                com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("usuarios").document(uid).set(usuario)
                onResult(true, null)
            }
            .addOnFailureListener { e -> onResult(false, e.localizedMessage) }
    }

    fun enterApp(email: String) {
        val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("usuarios")
                .document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    val usuario = document.toObject(Usuario::class.java)
                    if (usuario != null) {
                        signedInName = usuario.name.ifBlank { email.substringBefore("@") }
                        signedInEmail = email
                        signedInPhone = usuario.phone
                        signedInPhoto = usuario.photoUrl
                        equippedFrame = usuario.equippedFrame
                        signedInPoints = usuario.points
                        ownedFramesText = usuario.ownedFrames.joinToString("|")
                    } else {
                        signedInName = email.substringBefore("@")
                        signedInEmail = email
                    }
                    navigationViewModel.navigate("map")
                }
                .addOnFailureListener {
                    signedInName = email.substringBefore("@")
                    signedInEmail = email
                    navigationViewModel.navigate("map")
                }
        }
    }

    LaunchedEffect(Unit) {
        val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            enterApp(currentUser.email.orEmpty())
        }
    }

    when (screen) {
        "auth" -> AuthRoute(
            onBack = { navigationViewModel.navigate("welcome") },
            onRegisterClick = { navigationViewModel.navigate("register") },
            onLogin = ::verifyAccount,
            onRequestPasswordReset = { email, callback ->
                com.google.firebase.auth.FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnSuccessListener { callback(AuthOperationResult.Success(Unit)) }
                    .addOnFailureListener { callback(AuthOperationResult.Failure(AuthError.ProviderFailure(null, it.localizedMessage))) }
            },
            onAuthenticated = { email: String -> enterApp(email) }
        )
        "register" -> RegisterRoute(
            onBack = { navigationViewModel.navigate("auth") },
            onLoginClick = { navigationViewModel.navigate("auth") },
            onAccountCreated = {
                navigationViewModel.navigate("auth")
            },
            onCreateAccount = ::createAccount
        )
        "map" -> ReportsMap(
            name = signedInName,
            profilePhoto = signedInPhoto,
            points = signedInPoints,
            communityReports = reportsViewModel.reports,
            onSignOut = {
                com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                navigationViewModel.navigate("welcome")
            },
            onNavigate = { page ->
                if (page == "createReport" || page.startsWith("reportDetail/")) {
                    navigationViewModel.navigate(page)
                } else {
                    navigationViewModel.showDashboardPage(page)
                }
            }
        )
        "dashboard" -> CommunityRoute(
            page = dashboardPage,
            name = signedInName,
            onBack = { navigationViewModel.navigate("map") },
            onNavigate = { page ->
                when (page) {
                    "editProfile", "frames", "store" -> navigationViewModel.navigate(page)
                    else -> navigationViewModel.showDashboardPage(page)
                }
            },
            onSignOut = {
                com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                navigationViewModel.navigate("welcome")
            },
            onSaveName = { newName ->
                signedInName = newName
            },
            onDeleteAccount = {
                com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                navigationViewModel.navigate("welcome")
            },
            profilePhoto = signedInPhoto,
            points = signedInPoints,
            reports = myReports,
            onEditReport = reportsViewModel::updateReport,
            onDeleteReport = reportsViewModel::deleteReport
        )
        "frames" -> FramesPage(
            name = signedInName,
            activeFrame = equippedFrame,
            points = signedInPoints,
            owned = ownedFrames,
            onBack = { navigationViewModel.showDashboardPage("profile") },
            onEquip = { selected ->
                equippedFrame = selected
            },
            onOpenStore = { navigationViewModel.navigate("store") }
        )
        "store" -> StorePage(
            name = signedInName,
            points = signedInPoints,
            owned = ownedFrames,
            activeFrame = equippedFrame,
            onBack = { navigationViewModel.showDashboardPage("profile") },
            onPurchase = { selected, cost, _, result ->
                signedInPoints -= cost
                result(true, null)
            },
            onEquip = { selected, result ->
                equippedFrame = selected
                result(true, null)
            }
        )
        "createReport" -> ReportCreateRoute(
            name = signedInName,
            onBack = { navigationViewModel.navigate("map") },
            onPublish = { title, description, category, latitude, longitude, photoUris, done ->
                val mockProfile = Usuario(name = signedInName, email = signedInEmail)
                reportsViewModel.createReport(title, description, category, latitude, longitude, photoUris, mockProfile, context) { published, message ->
                    done(published, message)
                    if (published) {
                        navigationViewModel.navigate("map")
                    }
                }
            }
        )
        "editProfile" -> EditProfileRoute(
            name = signedInName,
            phone = signedInPhone,
            photo = signedInPhoto,
            onCancel = { navigationViewModel.showDashboardPage("profile") },
            onSave = { newName: String, newPhone: String, newPhoto: String? ->
                signedInName = newName
                signedInPhone = newPhone
                signedInPhoto = newPhoto
                navigationViewModel.showDashboardPage("profile")
            }
        )
        else -> if (screen.startsWith("reportDetail/")) {
            val reportId = screen.substringAfter("reportDetail/")
            val targetReport = reportsViewModel.reports.firstOrNull { it.id == reportId } ?: reportsViewModel.reports.firstOrNull()
            if (targetReport != null) {
                com.vozbarrial.features.reports.detail.ui.ReportDetailRoute(
                    report = targetReport,
                    userName = signedInName,
                    userPhoto = signedInPhoto,
                    onBack = { navigationViewModel.navigate("map") }
                )
            } else {
                LaunchedEffect(Unit) { navigationViewModel.navigate("map") }
            }
        } else {
            WelcomeRoute(onLoginClick = { navigationViewModel.navigate("auth") })
        }
    }
}
