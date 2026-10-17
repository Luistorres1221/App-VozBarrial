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
import com.vozbarrial.features.reports.create.presentation.ReportsViewModel
import com.vozbarrial.features.reports.create.ui.ReportCreateRoute
import com.vozbarrial.features.dashboard.FramesPage
import com.vozbarrial.features.dashboard.StorePage
import com.vozbarrial.features.map.ReportsMap
import com.vozbarrial.features.auth.welcome.ui.WelcomeRoute
import com.vozbarrial.core.result.AuthOperationResult
import com.vozbarrial.core.result.AuthError
import com.vozbarrial.domain.model.Usurious

@Composable
fun AppNavGraph() {
    val context = LocalContext.current
    val reportsViewModel: ReportsViewModel = viewModel()
    val navigationViewModel: NavigationViewModel = viewModel()

    androidx.activity.compose.BackHandler(enabled = navigationViewModel.canGoBack) {
        navigationViewModel.goBack()
    }

    val screen by navigationViewModel.screen.collectAsStateWithLifecycle()
    val dashboardPage by navigationViewModel.dashboardPage.collectAsStateWithLifecycle()

    val authUser = remember { com.google.firebase.auth.FirebaseAuth.getInstance().currentUser }
    var signedInName by remember { mutableStateOf(authUser?.email?.substringBefore("@").orEmpty()) }
    var signedInEmail by remember { mutableStateOf(authUser?.email.orEmpty()) }
    var signedInPhoto by remember { mutableStateOf<String?>(null) }
    var signedInUid by remember { mutableStateOf(authUser?.uid.orEmpty()) }
    var equippedFrame by remember { mutableStateOf("Clásico Cívico") }
    var signedInPoints by remember { mutableIntStateOf(1450) }
    var signedInMonedas by remember { mutableIntStateOf(0) }
    var signedInNivel by remember { mutableStateOf("novato") }
    var ownedFramesText by remember { mutableStateOf("Clásico Cívico|Guardián Dorado|Eco Barrio Verde|Escudo Ciudadano") }
    val ownedFrames = ownedFramesText.split("|").filter { it.isNotBlank() }.toSet()

    val reportList = reportsViewModel.reports
    val myReports = remember(reportList.toList(), signedInEmail, signedInUid, signedInName) {
        val currentAuthUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        val currentUid = signedInUid.ifBlank { currentAuthUser?.uid.orEmpty() }.trim()
        val currentEmail = signedInEmail.ifBlank { currentAuthUser?.email.orEmpty() }.trim()

        reportList.filter { report ->
            val rEmail = report.authorEmail.trim()
            val rUid = report.authorUid.trim()
            val authEmail = currentAuthUser?.email?.trim().orEmpty()
            val authUid = currentAuthUser?.uid?.trim().orEmpty()

            (currentUid.isNotBlank() && rUid == currentUid) ||
            (currentEmail.isNotBlank() && rEmail.equals(currentEmail, true)) ||
            (authUid.isNotBlank() && rUid == authUid) ||
            (authEmail.isNotBlank() && rEmail.equals(authEmail, true)) ||
            rEmail.isBlank() || rUid.isBlank()
        }
    }

    DisposableEffect(Unit) {
        val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        var listener: com.google.firebase.firestore.ListenerRegistration? = null
        if (currentUser != null) {
            signedInUid = currentUser.uid
            listener = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("usuarios")
                .document(currentUser.uid)
                .addSnapshotListener { document, _ ->
                    val usuario = document?.toObject(Usurious::class.java)
                    if (usuario != null) {
                        signedInName = usuario.name.ifBlank { currentUser.email?.substringBefore("@").orEmpty() }
                        signedInEmail = currentUser.email.orEmpty()
                        signedInPhoto = usuario.photoUrl
                        equippedFrame = usuario.equippedFrame
                        signedInPoints = usuario.points
                        signedInMonedas = usuario.monedas
                        signedInNivel = usuario.nivel
                        signedInUid = currentUser.uid
                        ownedFramesText = usuario.ownedFrames.joinToString("|")
                    }
                }
        }
        onDispose {
            listener?.remove()
        }
    }

    fun verifyAccount(email: String, password: String, onResult: (Boolean) -> Unit) {
        com.google.firebase.auth.FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    fun createAccount(name: String, email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        com.google.firebase.auth.FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid ?: ""
                val usuario = Usurious(uid = uid, name = name, email = email)
                com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("usuarios").document(uid).set(usuario)
                onResult(true, null)
            }
            .addOnFailureListener { e -> onResult(false, e.localizedMessage) }
    }

    fun enterApp(email: String) {
        val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            reportsViewModel.startObserving()
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("usuarios")
                .document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    val usuario = document.toObject(Usurious::class.java)
                    if (usuario != null) {
                        signedInName = usuario.name.ifBlank { email.substringBefore("@") }
                        signedInEmail = email
                        signedInPhoto = usuario.photoUrl
                        equippedFrame = usuario.equippedFrame
                        signedInPoints = usuario.points
                        signedInMonedas = usuario.monedas
                        signedInNivel = usuario.nivel
                        signedInUid = currentUser.uid
                        ownedFramesText = usuario.ownedFrames.joinToString("|")
                    } else {
                        signedInName = email.substringBefore("@")
                        signedInEmail = email
                    }
                    navigationViewModel.navigate("map", clearStack = true)
                }
                .addOnFailureListener {
                    signedInName = email.substringBefore("@")
                    signedInEmail = email
                    navigationViewModel.navigate("map", clearStack = true)
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
            onBack = { navigationViewModel.goBack() },
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
            onBack = { navigationViewModel.goBack() },
            onLoginClick = { navigationViewModel.goBack() },
            onAccountCreated = {
                navigationViewModel.goBack()
            },
            onCreateAccount = ::createAccount
        )
        "map" -> ReportsMap(
            name = signedInName,
            profilePhoto = signedInPhoto,
            monedas = signedInMonedas,
            nivel = signedInNivel,
            communityReports = reportsViewModel.reports,
            onSignOut = {
                com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                navigationViewModel.navigate("welcome", clearStack = true)
            },
            onNavigate = { page ->
                if (page == "createReport" || page.startsWith("reportDetail/")) {
                    navigationViewModel.navigate(page)
                } else {
                    navigationViewModel.showDashboardPage(page)
                }
            },
            onEarnRewards = { m ->
                signedInMonedas += m
            }
        )
        "dashboard" -> CommunityRoute(
            page = dashboardPage,
            name = signedInName,
            onBack = { navigationViewModel.goBack() },
            onNavigate = { page ->
                when (page) {
                    "editProfile", "frames", "store" -> navigationViewModel.navigate(page)
                    else -> navigationViewModel.showDashboardPage(page)
                }
            },
            onSignOut = {
                com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                navigationViewModel.navigate("welcome", clearStack = true)
            },
            onSaveName = { newName ->
                signedInName = newName
            },
            onDeleteAccount = {
                com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                navigationViewModel.navigate("welcome", clearStack = true)
            },
            profilePhoto = signedInPhoto,
            points = signedInPoints,
            monedas = signedInMonedas,
            nivel = signedInNivel,
            reports = myReports,
            onEditReport = reportsViewModel::updateReport,
            onDeleteReport = reportsViewModel::deleteReport
        )
        "frames" -> FramesPage(
            name = signedInName,
            activeFrame = equippedFrame,
            points = signedInPoints,
            owned = ownedFrames,
            onBack = { navigationViewModel.goBack() },
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
            onBack = { navigationViewModel.goBack() },
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
            onBack = { navigationViewModel.goBack() },
            onPublish = { title, description, category, latitude, longitude, photoUris, done ->
                val authUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                val mockProfile = Usurious(
                    name = signedInName.ifBlank { authUser?.email?.substringBefore("@").orEmpty() },
                    email = signedInEmail.ifBlank { authUser?.email.orEmpty() },
                    uid = signedInUid.ifBlank { authUser?.uid.orEmpty() }
                )
                signedInMonedas += 50
                reportsViewModel.createReport(title, description, category, latitude, longitude, photoUris, mockProfile, context) { published, message ->
                    done(published, message)
                    if (published) {
                        navigationViewModel.goBack()
                    }
                }
            }
        )
        "editProfile" -> EditProfileRoute(
            name = signedInName,
            photo = signedInPhoto,
            onCancel = { navigationViewModel.goBack() },
            onSave = {
                navigationViewModel.goBack()
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
                    onBack = { navigationViewModel.goBack() }
                )
            } else {
                LaunchedEffect(Unit) { navigationViewModel.goBack() }
            }
        } else {
            WelcomeRoute(onLoginClick = { navigationViewModel.navigate("auth") })
        }
    }
}
