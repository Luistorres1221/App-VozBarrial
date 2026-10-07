package com.vozbarrial

import android.os.Bundle
import android.util.Base64
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
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

class MainActivity : ComponentActivity() {
    private val accounts by lazy { getSharedPreferences("voz_barrial_accounts", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val baseDensity = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(baseDensity.density, baseDensity.fontScale * 1.10f)) {
            MaterialTheme {
                var screen by rememberSaveable { mutableStateOf("welcome") }
                var signedInName by rememberSaveable { mutableStateOf("") }
                var signedInEmail by rememberSaveable { mutableStateOf("") }
                var signedInPhone by rememberSaveable { mutableStateOf("") }
                var signedInPhoto by rememberSaveable { mutableStateOf<String?>(null) }
                var equippedFrame by rememberSaveable { mutableStateOf("Clásico Cívico") }
                var signedInPoints by rememberSaveable { androidx.compose.runtime.mutableIntStateOf(1450) }
                var ownedFramesText by rememberSaveable { mutableStateOf("Clásico Cívico|Guardián Dorado|Eco Barrio Verde|Escudo Ciudadano") }
                var dashboardPage by rememberSaveable { mutableStateOf("profile") }
                var reportTitle by rememberSaveable { mutableStateOf<String?>(null) }
                var reportDescription by rememberSaveable { mutableStateOf<String?>(null) }
                var reportCategory by rememberSaveable { mutableStateOf<String?>(null) }
                var reportLatitude by rememberSaveable { mutableStateOf<Double?>(null) }
                var reportLongitude by rememberSaveable { mutableStateOf<Double?>(null) }

                when (screen) {
                    "auth" -> Auth(
                        onBack = { screen = "welcome" },
                        onLogin = ::verifyAccount,
                        onRegister = ::createAccount,
                        onAccountExists = { email -> accounts.contains(accountKey(email) + "_hash") },
                        onResetPassword = ::resetPassword,
                        onResolveName = ::displayName,
                        onAuthenticated = { email -> signedInEmail = email; signedInName = displayName(email); signedInPhone = accounts.getString(accountKey(email) + "_phone", "").orEmpty(); signedInPhoto = accounts.getString(accountKey(email) + "_photo", null); equippedFrame = accounts.getString(accountKey(email) + "_frame", "Clásico Cívico") ?: "Clásico Cívico"; signedInPoints = accounts.getInt(accountKey(email) + "_points", 1450); ownedFramesText = accounts.getString(accountKey(email) + "_owned_frames", "Clásico Cívico|Guardián Dorado|Eco Barrio Verde|Escudo Ciudadano") ?: "Clásico Cívico|Guardián Dorado|Eco Barrio Verde|Escudo Ciudadano"; screen = "map" },
                    )
                    "map" -> ReportsMap(signedInName, onSignOut = { screen = "welcome" }, onNavigate = { page -> if (page == "createReport") screen = "createReport" else { dashboardPage = page; screen = "dashboard" } }, points = signedInPoints, submittedTitle = reportTitle, submittedDescription = reportDescription, submittedCategory = reportCategory, submittedLatitude = reportLatitude, submittedLongitude = reportLongitude, onReportConsumed = { reportTitle = null; reportDescription = null; reportCategory = null; reportLatitude = null; reportLongitude = null })
                    "dashboard" -> CommunityPage(dashboardPage, signedInName, onBack = { screen = "map" }, onNavigate = { page -> if (page == "editProfile") screen = "editProfile" else if (page == "frames") screen = "frames" else if (page == "store") screen = "store" else dashboardPage = page }, onSignOut = { screen = "welcome" }, onSaveName = { newName -> signedInName = newName; accounts.edit().putString(accountKey(signedInEmail) + "_name", newName).apply() }, onDeleteAccount = { val key = accountKey(signedInEmail); accounts.edit().remove(key + "_name").remove(key + "_salt").remove(key + "_hash").remove(key + "_phone").remove(key + "_photo").remove(key + "_frame").apply(); signedInName = ""; signedInEmail = ""; signedInPhone = ""; signedInPhoto = null; equippedFrame = "Clásico Cívico"; screen = "welcome" }, profilePhoto = signedInPhoto, points = signedInPoints)
                    "frames" -> FramesPage(signedInName, equippedFrame, points = signedInPoints, onBack = { screen = "dashboard"; dashboardPage = "profile" }, onEquip = { frame -> equippedFrame = frame; accounts.edit().putString(accountKey(signedInEmail) + "_frame", frame).apply() })
                    "store" -> StorePage(signedInName, signedInPoints, ownedFramesText.split("|").filter { it.isNotBlank() }.toSet(), equippedFrame, onBack = { screen = "dashboard"; dashboardPage = "profile" }, onPurchase = { frame, cost, owned -> signedInPoints -= cost; ownedFramesText = owned.joinToString("|"); val key = accountKey(signedInEmail); accounts.edit().putInt(key + "_points", signedInPoints).putString(key + "_owned_frames", ownedFramesText).apply() }, onEquip = { frame -> equippedFrame = frame; accounts.edit().putString(accountKey(signedInEmail) + "_frame", frame).apply() })
                    "createReport" -> ReportCreate(signedInName, onBack = { screen = "map" }, onPublish = { title, description, category, latitude, longitude -> reportTitle = title; reportDescription = description; reportCategory = category; reportLatitude = latitude; reportLongitude = longitude; screen = "map" })
                    "editProfile" -> EditProfile(signedInName, signedInPhone, signedInPhoto, onCancel = { screen = "dashboard"; dashboardPage = "profile" }, onSave = { newName, newPhone, newPhoto -> signedInName = newName; signedInPhone = newPhone; signedInPhoto = newPhoto; val key = accountKey(signedInEmail); accounts.edit().putString(key + "_name", newName).putString(key + "_phone", newPhone).putString(key + "_photo", newPhoto ?: "").apply(); screen = "dashboard"; dashboardPage = "profile" })
                    else -> Welcome(onLoginClick = { screen = "auth" })
                }
            }
            }
        }
    }

    private fun resetPassword(email: String, password: String): Boolean {
        val key = accountKey(email)
        if (!accounts.contains(key + "_hash")) return false
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        accounts.edit().putString(key + "_salt", Base64.encodeToString(salt, Base64.NO_WRAP)).putString(key + "_hash", passwordHash(salt, password)).apply()
        return true
    }

    private fun createAccount(name: String, email: String, password: String): Boolean {
        val key = accountKey(email)
        if (accounts.contains("${key}_hash")) return false
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        accounts.edit()
            .putString("${key}_name", name)
            .putString("${key}_salt", Base64.encodeToString(salt, Base64.NO_WRAP))
            .putString("${key}_hash", passwordHash(salt, password))
            .apply()
        return true
    }

    private fun verifyAccount(email: String, password: String): Boolean {
        val key = accountKey(email)
        val saltText = accounts.getString("${key}_salt", null) ?: return false
        val expected = accounts.getString("${key}_hash", null) ?: return false
        val salt = Base64.decode(saltText, Base64.NO_WRAP)
        val actual = passwordHash(salt, password)
        return MessageDigest.isEqual(expected.toByteArray(), actual.toByteArray())
    }

    private fun displayName(email: String): String {
        val key = accountKey(email)
        val saved = accounts.getString("${key}_name", null)?.trim().orEmpty()
        val malformed = saved.contains("%") || saved.equals(email.substringBefore("@"), ignoreCase = true)
        if (saved.isNotBlank() && !malformed) return saved
        val repaired = if (email.equals("torresluisalberto95@gmail.com", ignoreCase = true)) "Luis Alberto Torres Berrio" else email.substringBefore("@").replace(Regex("[0-9]+$"), "")
        accounts.edit().putString("${key}_name", repaired).apply()
        return repaired
    }

    private fun accountKey(email: String): String = "account_${email.trim().lowercase()}"

    private fun passwordHash(salt: ByteArray, password: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(salt + password.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { byte -> "%02x".format(byte.toInt() and 0xff) }
    }
}