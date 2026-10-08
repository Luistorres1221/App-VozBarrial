package com.vozbarrial.navigation

import android.content.Context
import android.util.Base64
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import com.vozbarrial.features.auth.Auth
import com.vozbarrial.features.dashboard.CommunityPage
import com.vozbarrial.features.dashboard.EditProfile
import com.vozbarrial.features.dashboard.FramesPage
import com.vozbarrial.features.dashboard.StorePage
import com.vozbarrial.features.dashboard.ReportCreate
import com.vozbarrial.features.map.ReportsMap
import com.vozbarrial.features.welcome.Welcome
import java.security.MessageDigest
import java.security.SecureRandom

@Composable
fun AppNavGraph() {
    val context = LocalContext.current
    val accounts by lazy { context.getSharedPreferences("voz_barrial_accounts", Context.MODE_PRIVATE) }

    var screen by rememberSaveable { mutableStateOf("com/vozbarrial/features/welcome") }
    var signedInName by rememberSaveable { mutableStateOf("") }
    var signedInEmail by rememberSaveable { mutableStateOf("") }
    var signedInPhone by rememberSaveable { mutableStateOf("") }
    var signedInPhoto by rememberSaveable { mutableStateOf<String?>(null) }
    var equippedFrame by rememberSaveable { mutableStateOf("Clásico Cívico") }
    var signedInPoints by rememberSaveable { mutableIntStateOf(1450) }
    var ownedFramesText by rememberSaveable { mutableStateOf("Clásico Cívico|Guardián Dorado|Eco Barrio Verde|Escudo Ciudadano") }
    var dashboardPage by rememberSaveable { mutableStateOf("profile") }
    var reportTitle by rememberSaveable { mutableStateOf<String?>(null) }
    var reportDescription by rememberSaveable { mutableStateOf<String?>(null) }
    var reportCategory by rememberSaveable { mutableStateOf<String?>(null) }
    var reportLatitude by rememberSaveable { mutableStateOf<Double?>(null) }
    var reportLongitude by rememberSaveable { mutableStateOf<Double?>(null) }

    fun accountKey(email: String): String = "account_${email.trim().lowercase()}"

    fun passwordHash(salt: ByteArray, password: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(salt + password.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { byte -> "%02x".format(byte.toInt() and 0xff) }
    }

    fun displayName(email: String): String {
        val key = accountKey(email)
        val saved = accounts.getString("${key}_name", null)?.trim().orEmpty()
        val malformed = saved.contains("%") || saved.equals(email.substringBefore("@"), ignoreCase = true)
        if (saved.isNotBlank() && !malformed) return saved
        val repaired = if (email.equals("torresluisalberto95@gmail.com", ignoreCase = true)) "Luis Alberto Torres Berrio" else email.substringBefore("@").replace(Regex("[0-9]+$"), "")
        accounts.edit().putString("${key}_name", repaired).apply()
        return repaired
    }

    fun resetPassword(email: String, password: String): Boolean {
        val key = accountKey(email)
        if (!accounts.contains(key + "_hash")) return false
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        accounts.edit().putString(key + "_salt", Base64.encodeToString(salt, Base64.NO_WRAP)).putString(key + "_hash", passwordHash(salt, password)).apply()
        return true
    }

    fun createAccount(name: String, email: String, phone: String, password: String, onResult: (Boolean, String?) -> Unit) {
        com.google.firebase.auth.FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid ?: ""
                val usuario = com.vozbarrial.domain.Usuario(uid = uid, name = name, email = email, phone = phone)
                com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("usuarios").document(uid).set(usuario)
                onResult(true, null)
            }
            .addOnFailureListener { e -> onResult(false, e.localizedMessage) }
    }

    fun verifyAccount(email: String, password: String, onResult: (Boolean) -> Unit) {
        com.google.firebase.auth.FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    when (screen) {
        "com/vozbarrial/features/auth" -> Auth(
            onBack = { screen = "com/vozbarrial/features/welcome" },
            onLogin = ::verifyAccount,
            onRegister = ::createAccount,
            onAccountExists = { email -> accounts.contains(accountKey(email) + "_hash") },
            onResetPassword = ::resetPassword,
            onResolveName = ::displayName,
            onAuthenticated = { email -> 
                val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                if (currentUser != null) {
                    com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        .collection("usuarios")
                        .document(currentUser.uid)
                        .get()
                        .addOnSuccessListener { document ->
                            val usuario = document.toObject(com.vozbarrial.domain.Usuario::class.java)
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
                            screen = "com/vozbarrial/features/map"
                        }
                        .addOnFailureListener {
                            signedInName = email.substringBefore("@")
                            signedInEmail = email
                            screen = "com/vozbarrial/features/map"
                        }
                } else {
                    signedInName = email.substringBefore("@")
                    signedInEmail = email
                    screen = "com/vozbarrial/features/map"
                }
            },
        )
        "com/vozbarrial/features/map" -> ReportsMap(signedInName, profilePhoto = signedInPhoto, onSignOut = { screen = "com/vozbarrial/features/welcome" }, onNavigate = { page -> if (page == "createReport") screen = "createReport" else { dashboardPage = page; screen = "com/vozbarrial/features/dashboard" } }, points = signedInPoints, submittedTitle = reportTitle, submittedDescription = reportDescription, submittedCategory = reportCategory, submittedLatitude = reportLatitude, submittedLongitude = reportLongitude, onReportConsumed = { reportTitle = null; reportDescription = null; reportCategory = null; reportLatitude = null; reportLongitude = null })
        "com/vozbarrial/features/dashboard" -> CommunityPage(dashboardPage, signedInName, onBack = { screen = "com/vozbarrial/features/map" }, onNavigate = { page -> if (page == "editProfile") screen = "editProfile" else if (page == "frames") screen = "frames" else if (page == "store") screen = "store" else dashboardPage = page }, onSignOut = { screen = "com/vozbarrial/features/welcome" }, onSaveName = { newName -> signedInName = newName; accounts.edit().putString(accountKey(signedInEmail) + "_name", newName).apply() }, onDeleteAccount = { val key = accountKey(signedInEmail); accounts.edit().remove(key + "_name").remove(key + "_salt").remove(key + "_hash").remove(key + "_phone").remove(key + "_photo").remove(key + "_frame").apply(); signedInName = ""; signedInEmail = ""; signedInPhone = ""; signedInPhoto = null; equippedFrame = "Clásico Cívico"; screen = "com/vozbarrial/features/welcome" }, profilePhoto = signedInPhoto, points = signedInPoints)
        "frames" -> FramesPage(signedInName, equippedFrame, points = signedInPoints, onBack = { screen = "com/vozbarrial/features/dashboard"; dashboardPage = "profile" }, onEquip = { frame -> equippedFrame = frame; accounts.edit().putString(accountKey(signedInEmail) + "_frame", frame).apply() })
        "store" -> StorePage(signedInName, signedInPoints, ownedFramesText.split("|").filter { it.isNotBlank() }.toSet(), equippedFrame, onBack = { screen = "com/vozbarrial/features/dashboard"; dashboardPage = "profile" }, onPurchase = { frame, cost, owned -> signedInPoints -= cost; ownedFramesText = owned.joinToString("|"); val key = accountKey(signedInEmail); accounts.edit().putInt(key + "_points", signedInPoints).putString(key + "_owned_frames", ownedFramesText).apply() }, onEquip = { frame -> equippedFrame = frame; accounts.edit().putString(accountKey(signedInEmail) + "_frame", frame).apply() })
        "createReport" -> ReportCreate(signedInName, onBack = { screen = "com/vozbarrial/features/map" }, onPublish = { title, description, category, latitude, longitude -> reportTitle = title; reportDescription = description; reportCategory = category; reportLatitude = latitude; reportLongitude = longitude; screen = "com/vozbarrial/features/map" })
        "editProfile" -> EditProfile(signedInName, signedInPhone, signedInPhoto, onCancel = { screen = "com/vozbarrial/features/dashboard"; dashboardPage = "profile" }, onSave = { newName, newPhone, newPhoto -> 
            signedInName = newName
            signedInPhone = newPhone
            signedInPhoto = newPhoto
            val key = accountKey(signedInEmail)
            accounts.edit().putString(key + "_name", newName).putString(key + "_phone", newPhone).putString(key + "_photo", newPhoto ?: "").apply()
            
            val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                val updates = mapOf(
                    "name" to newName,
                    "phone" to newPhone,
                    "photoUrl" to (newPhoto ?: "")
                )
                com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("usuarios")
                    .document(currentUser.uid)
                    .update(updates)
            }
            screen = "com/vozbarrial/features/dashboard"
            dashboardPage = "profile" 
        })
        else -> Welcome(onLoginClick = { screen = "com/vozbarrial/features/auth" })
    }
}
