package com.vozbarrial.features.profile.publicProfile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.vozbarrial.domain.model.Nivel
import com.vozbarrial.domain.model.Report
import com.vozbarrial.domain.model.ReportStatus
import com.vozbarrial.domain.model.Usurious
import com.vozbarrial.ui.components.UserAvatarWithFrame
import com.vozbarrial.ui.theme.*

@Composable
fun UserProfileScreen(
    authorId: String,
    onBack: () -> Unit
) {
    var profileUser by remember { mutableStateOf<Usurious?>(null) }
    var authorReports by remember { mutableStateOf<List<Report>>(emptyList()) }
    var fallbackReportName by remember { mutableStateOf<String?>(null) }
    var fallbackReportNivel by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(authorId) {
        val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()

        firestore.collection("reportes").get()
            .addOnSuccessListener { snapshot ->
                val all = snapshot.documents.mapNotNull { it.toObject(Report::class.java)?.copy(id = it.id) }
                val matched = all.filter { r ->
                    r.authorUid == authorId ||
                    r.authorEmail.equals(authorId, true) ||
                    r.authorName.equals(authorId, true)
                }
                authorReports = matched
                val firstWithAuthor = matched.firstOrNull { it.authorName.isNotBlank() }
                if (firstWithAuthor != null) {
                    fallbackReportName = firstWithAuthor.authorName
                    fallbackReportNivel = firstWithAuthor.authorNivel
                }

                if (authorId.contains("@")) {
                    firestore.collection("usuarios").whereEqualTo("email", authorId).get()
                        .addOnSuccessListener { querySnapshot ->
                            val doc = querySnapshot.documents.firstOrNull()
                            if (doc != null) {
                                profileUser = doc.toObject(Usurious::class.java)
                            }
                            isLoading = false
                        }
                        .addOnFailureListener { isLoading = false }
                } else {
                    firestore.collection("usuarios").document(authorId).get()
                        .addOnSuccessListener { doc ->
                            if (doc.exists()) {
                                profileUser = doc.toObject(Usurious::class.java)
                                isLoading = false
                            } else {
                                firestore.collection("usuarios").whereEqualTo("uid", authorId).get()
                                    .addOnSuccessListener { qSnap ->
                                        val uDoc = qSnap.documents.firstOrNull()
                                        if (uDoc != null) {
                                            profileUser = uDoc.toObject(Usurious::class.java)
                                        }
                                        isLoading = false
                                    }
                                    .addOnFailureListener { isLoading = false }
                            }
                        }
                        .addOnFailureListener { isLoading = false }
                }
            }
            .addOnFailureListener { isLoading = false }
    }

    val user = profileUser
    val rawName = user?.name.orEmpty()
    val isUidString = authorId.length > 15 && !authorId.contains(" ") && !authorId.contains("@")
    val name = when {
        rawName.isNotBlank() -> rawName
        !fallbackReportName.isNullOrBlank() -> fallbackReportName!!
        isUidString -> "Ciudadano Vecinal"
        else -> authorId.substringBefore("@")
    }

    val points = user?.points ?: (authorReports.size * 50)
    val nivel = user?.nivel ?: fallbackReportNivel ?: "novato"
    val nivelEnum = Nivel.fromValue(nivel)
    val photo = user?.photoUrl

    val createdCount = authorReports.size.coerceAtLeast(if (authorReports.isEmpty() && isUidString) 1 else authorReports.size)
    val verifiedCount = authorReports.count { ReportStatus.fromString(it.status) == ReportStatus.VERIFICADO || ReportStatus.fromString(it.status) == ReportStatus.RESUELTO }
    val totalVotes = authorReports.sumOf { it.votes }

    val levelNum = when {
        points <= 250 -> 1
        points <= 800 -> 2
        points <= 2000 -> 3
        else -> 4
    }

    val levelName = when (levelNum) {
        1 -> "Novato"
        2 -> "Colaborador"
        3 -> "Guardián"
        else -> "Héroe Comunitario"
    }

    val (progress, remainingText) = when {
        points <= 250 -> Pair(points / 250f, "${251 - points} pts para Colaborador")
        points <= 800 -> Pair((points - 251f) / (800f - 251f), "${801 - points} pts para Guardián")
        points <= 2000 -> Pair((points - 801f) / (2000f - 801f), "${2001 - points} pts para Héroe")
        else -> Pair(1.0f, "¡Nivel máximo alcanzado!")
    }

    val efectividad = if (createdCount > 0) (verifiedCount * 100) / createdCount else 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VozBackground)
            .verticalScroll(rememberScrollState())
    ) {
        // Top Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = VozNavy)
                }
                Box(
                    modifier = Modifier.size(28.dp).clip(CircleShape).background(VozNavy),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Shield, null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
                Text("Perfil Ciudadano", color = VozNavy, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.weight(1f))
                if (!photo.isNullOrBlank()) {
                    AsyncImage(model = photo, contentDescription = null, modifier = Modifier.size(32.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                } else {
                    Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(VozGreen), contentAlignment = Alignment.Center) {
                        Text(name.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Main User Profile Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            shape = RoundedCornerShape(18.dp),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box {
                    UserAvatarWithFrame(
                        profilePhoto = photo,
                        name = name,
                        frameName = user?.equippedFrame ?: "Clásico Cívico",
                        size = 64.dp
                    )
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF07885F))
                            .align(Alignment.BottomEnd),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CheckCircle, null, tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(name, color = VozNavy, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color(0xFFD6FAEA)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF087A55)))
                            Spacer(Modifier.width(4.dp))
                            Text(levelName, color = Color(0xFF087A55), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Level & Points Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            shape = RoundedCornerShape(18.dp),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFFE8F8F0)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.MilitaryTech, null, tint = VozGreen, modifier = Modifier.size(22.dp))
                        }
                        Text("Nivel $levelNum: $levelName", color = VozNavy, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color(0xFFEFF6FF)
                    ) {
                        Text("$points Pts", modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp), color = Color(0xFF1D4ED8), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }

                Spacer(Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Progreso actual", color = VozMuted, fontSize = 11.sp)
                    Text(remainingText, color = Color(0xFFD97706), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(50)),
                    color = VozGreen,
                    trackColor = Color(0xFFE2E8F0)
                )
                Spacer(Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("$points pts", color = VozMuted, fontSize = 10.sp)
                    Text("${(progress * 100).toInt()}% completado", color = VozMuted, fontSize = 10.sp)
                    Text(if (points <= 250) "250 pts" else if (points <= 800) "800 pts" else "2,000 pts", color = VozMuted, fontSize = 10.sp)
                }

                Spacer(Modifier.height(14.dp))

                // Milestones list
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    MilestoneItem("1. Novato", "0 - 250 pts", levelNum > 1, levelNum == 1)
                    MilestoneItem("2. Colaborador", "251 - 800 pts", levelNum > 2, levelNum == 2)
                    MilestoneItem("3. Guardián", "801 - 2,000 pts", levelNum > 3, levelNum == 3)
                    MilestoneItem("4. Héroe Comunitario", "2,001+ pts", false, levelNum == 4)
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Stats Cards Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(
                title = "Reportes Creados",
                value = createdCount.toString(),
                subtitle = "Total publicado",
                icon = Icons.Default.Campaign,
                tint = Color(0xFF3B82F6),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Verificados",
                value = verifiedCount.toString(),
                subtitle = "$efectividad% efectividad",
                icon = Icons.Default.CheckCircle,
                tint = Color(0xFF10B981),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Votos Vecinales",
                value = totalVotes.toString(),
                subtitle = "\"Es importante\"",
                icon = Icons.Default.Favorite,
                tint = Color(0xFFF97316),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MilestoneItem(title: String, subtitle: String, completed: Boolean, isActual: Boolean = false) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = if (isActual) Color(0xFFEFF6FF) else Color(0xFFF8FAFC)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    if (completed || isActual) Icons.Default.CheckCircle else Icons.Default.Lock,
                    null,
                    tint = if (completed || isActual) Color(0xFF16A34A) else VozMuted,
                    modifier = Modifier.size(16.dp)
                )
                Column {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = VozNavy)
                    Text(subtitle, fontSize = 10.sp, color = VozMuted)
                }
            }
            Surface(
                shape = RoundedCornerShape(50),
                color = if (isActual) Color(0xFF1D4ED8) else if (completed) Color(0xFFDCFCE7) else Color(0xFFE2E8F0)
            ) {
                Text(
                    text = if (isActual) "Actual" else if (completed) "Completado" else "Próximo",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    color = if (isActual) Color.White else if (completed) Color(0xFF16A34A) else VozMuted,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun StatCard(title: String, value: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, tint: Color, modifier: Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(32.dp).clip(CircleShape).background(tint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = tint, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = VozNavy)
            Text(title, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = VozNavy)
            Spacer(Modifier.height(2.dp))
            Text(subtitle, fontSize = 9.sp, color = VozMuted, maxLines = 1)
        }
    }
}
