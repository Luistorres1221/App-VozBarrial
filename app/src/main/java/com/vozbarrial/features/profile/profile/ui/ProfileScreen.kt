package com.vozbarrial.features.profile.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.vozbarrial.ui.components.UserAvatarWithFrame
import com.vozbarrial.R
import com.vozbarrial.domain.model.Nivel
import com.vozbarrial.domain.model.Report
import com.vozbarrial.domain.model.ReportStatus
import com.vozbarrial.features.profile.profile.presentation.ProfileUiState
import com.vozbarrial.features.profile.profile.presentation.ProfileViewModel
import com.vozbarrial.ui.theme.*

@Composable
fun CommunityRoute(
    page: String,
    name: String,
    equippedFrame: String = "",
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    onSignOut: () -> Unit,
    onSaveName: (String) -> Unit,
    onDeleteAccount: () -> Unit,
    profilePhoto: String?,
    points: Int,
    monedas: Int,
    nivel: String,
    reports: List<Report>,
    onEditReport: (String, String, String, (Boolean, String?) -> Unit) -> Unit,
    onDeleteReport: (String, (Boolean, String?) -> Unit) -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val availableFilters = listOf("Todas", "ACTIVO", "RESUELTO")

    val editSuccessMsg = stringResource(R.string.report_updated_success)
    val editErrorMsg = stringResource(R.string.report_updated_error)
    val deleteSuccessMsg = stringResource(R.string.report_deleted_success)
    val deleteErrorMsg = stringResource(R.string.report_deleted_error)

    LaunchedEffect(page, name, profilePhoto, points, monedas, nivel, reports) {
        viewModel.updateProfileData(page, name, profilePhoto, points, monedas, nivel, reports, availableFilters)
    }

    CommunityContent(
        uiState = uiState,
        equippedFrame = equippedFrame,
        onBack = onBack,
        onNavigate = onNavigate,
        onSignOut = onSignOut,
        onDeleteAccount = onDeleteAccount,
        onFilterSelected = viewModel::onActivityFilterSelected,
        onOpenNameEditor = viewModel::onOpenNameEditor,
        onNameDraftChange = viewModel::onNameDraftChange,
        onDismissNameEditor = viewModel::onDismissNameEditor,
        onSaveName = { viewModel.onSaveName(onSaveName) },
        onOpenEditReport = viewModel::onOpenEditReport,
        onEditTitleChange = { viewModel.onEditTitleChange(it, 100) },
        onEditDescriptionChange = { viewModel.onEditDescriptionChange(it, 1000) },
        onDismissEditReport = viewModel::onDismissEditReport,
        onConfirmEditReport = {
            viewModel.onConfirmEditReport(onEditReport, editSuccessMsg, editErrorMsg)
        },
        onOpenDeleteReport = viewModel::onOpenDeleteReport,
        onDismissDeleteReport = viewModel::onDismissDeleteReport,
        onConfirmDeleteReport = {
            viewModel.onConfirmDeleteReport(onDeleteReport, deleteSuccessMsg, deleteErrorMsg)
        },
        onShowDeleteAccountDialog = viewModel::onShowDeleteAccountDialog,
        onDismissOperationMessage = viewModel::onDismissOperationMessage
    )
}

@Composable
fun CommunityContent(
    uiState: ProfileUiState,
    equippedFrame: String = "",
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    onSignOut: () -> Unit,
    onDeleteAccount: () -> Unit,
    onFilterSelected: (String) -> Unit,
    onOpenNameEditor: () -> Unit,
    onNameDraftChange: (String) -> Unit,
    onDismissNameEditor: () -> Unit,
    onSaveName: () -> Unit,
    onOpenEditReport: (Report) -> Unit,
    onEditTitleChange: (String) -> Unit,
    onEditDescriptionChange: (String) -> Unit,
    onDismissEditReport: () -> Unit,
    onConfirmEditReport: () -> Unit,
    onOpenDeleteReport: (Report) -> Unit,
    onDismissDeleteReport: () -> Unit,
    onConfirmDeleteReport: () -> Unit,
    onShowDeleteAccountDialog: (Boolean) -> Unit,
    onDismissOperationMessage: () -> Unit,
) {
    Column(Modifier.fillMaxSize().background(VozBackground)) {
        Row(
            Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back), tint = VozNavy)
            }
            Text(
                when (uiState.currentPage) {
                    "profile" -> stringResource(R.string.profile_title)
                    "activity" -> stringResource(R.string.activity_title)
                    else -> stringResource(R.string.badges_title)
                },
                Modifier.weight(1f), color = VozNavy, fontWeight = FontWeight.Bold, fontSize = 16.sp,
            )
            IconButton(onClick = { onNavigate("activity") }) {
                Box(contentAlignment = Alignment.TopEnd) {
                    Icon(Icons.Default.Notifications, contentDescription = stringResource(R.string.activity_title), tint = VozNavy)
                    if (uiState.monedas > 0 || uiState.points > 0) {
                        Box(Modifier.size(7.dp).clip(CircleShape).background(Color(0xFFE35454)))
                    }
                }
            }
        }

        LazyColumn(
            Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            when (uiState.currentPage) {
                "profile" -> {
                    val currentLevelName = Nivel.fromValue(uiState.nivel).displayName
                    val levelNum = when (Nivel.fromValue(uiState.nivel)) {
                        Nivel.NOVATO -> 1
                        Nivel.COLABORADOR -> 2
                        Nivel.GUARDIAN -> 3
                        Nivel.HEROE -> 4
                    }

                    // 1. Profile Header Card
                    item {
                        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp)) {
                            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    UserAvatarWithFrame(
                                        profilePhoto = uiState.profilePhoto,
                                        name = uiState.name,
                                        frameName = equippedFrame,
                                        size = 64.dp
                                    )
                                    Spacer(Modifier.width(14.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            uiState.name.ifBlank { stringResource(R.string.default_user_name) },
                                            color = VozNavy,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp
                                        )
                                        Spacer(Modifier.height(3.dp))
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = Color(0xFFEAF9F5)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Default.Shield, null, tint = VozGreen, modifier = Modifier.size(12.dp))
                                                Spacer(Modifier.width(4.dp))
                                                Text(currentLevelName, color = VozGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                                    Button(
                                        onClick = { onNavigate("editProfile") },
                                        modifier = Modifier.weight(1f).height(40.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        contentPadding = PaddingValues(horizontal = 2.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEAF1F8))
                                    ) {
                                        Icon(Icons.Default.Edit, null, tint = VozNavy, modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(2.dp))
                                        Text("Editar", color = VozNavy, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                    }
                                    Button(
                                        onClick = { onNavigate("frames") },
                                        modifier = Modifier.weight(1f).height(40.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        contentPadding = PaddingValues(horizontal = 2.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEAF1F8))
                                    ) {
                                        Icon(Icons.Default.Collections, null, tint = VozNavy, modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(2.dp))
                                        Text("Marcos", color = VozNavy, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                    }
                                    Button(
                                        onClick = { onNavigate("store") },
                                        modifier = Modifier.weight(1f).height(40.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        contentPadding = PaddingValues(horizontal = 2.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD3F9E8))
                                    ) {
                                        Icon(Icons.Default.Store, null, tint = Color(0xFF0D9488), modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(2.dp))
                                        Text("Tienda", color = Color(0xFF0D9488), fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                    }
                                }
                            }
                        }
                    }

                    // 2. Level & Progress Card
                    item {
                        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp)) {
                            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(shape = CircleShape, color = Color(0xFFEAF9F5), modifier = Modifier.size(32.dp)) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(Icons.Default.MilitaryTech, null, tint = VozGreen, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                        Spacer(Modifier.width(8.dp))
                                        Text(currentLevelName, color = VozNavy, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    }
                                    Surface(shape = RoundedCornerShape(50), color = Color(0xFFEAF1F8)) {
                                        Text("${uiState.points} Pts", modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp), color = VozNavy, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                val (progress, remainingText) = when {
                                    uiState.points <= 250 -> Triple(uiState.points / 250f, "${251 - uiState.points} pts para Colaborador", "Colaborador")
                                    uiState.points <= 800 -> Triple((uiState.points - 251f) / (800f - 251f), "${801 - uiState.points} pts para Guardián", "Guardián")
                                    uiState.points <= 2000 -> Triple((uiState.points - 801f) / (2000f - 801f), "${2001 - uiState.points} pts para Héroe", "Héroe")
                                    else -> Triple(1.0f, "¡Nivel máximo alcanzado!", "Nivel Máximo")
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Progreso actual", color = VozMuted, fontSize = 11.sp)
                                    Text(remainingText, color = VozNavy, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                LinearProgressIndicator(
                                    progress = { progress.coerceIn(0f, 1f) },
                                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(50)),
                                    color = VozGreen,
                                    trackColor = Color(0xFFEAF1F8)
                                )

                                // Levels breakdown
                                val levelsList = listOf(
                                    Triple("Novato", "0 - 250 pts", 1),
                                    Triple("Colaborador", "251 - 800 pts", 2),
                                    Triple("Guardián", "801 - 2,000 pts", 3),
                                    Triple("Héroe", "2,001+ pts", 4),
                                )

                                levelsList.forEach { (title, range, lNum) ->
                                    val isCurrent = levelNum == lNum
                                    val isCompleted = levelNum > lNum
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isCurrent) Color(0xFFEAF1F8) else Color(0xFFF9FAFB),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    if (isCompleted) Icons.Default.CheckCircle else if (isCurrent) Icons.Default.Shield else Icons.Default.Lock,
                                                    null,
                                                    tint = if (isCompleted || isCurrent) VozGreen else VozMuted,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(Modifier.width(10.dp))
                                                Column {
                                                    Text(title, color = VozNavy, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                    Text(range, color = VozMuted, fontSize = 10.sp)
                                                }
                                            }
                                            Surface(
                                                shape = RoundedCornerShape(50),
                                                color = if (isCurrent) VozNavy else if (isCompleted) Color(0xFFD3F9E8) else Color(0xFFE5E7EB)
                                            ) {
                                                Text(
                                                    text = if (isCurrent) "Actual" else if (isCompleted) "Completado" else "Próximo",
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                    color = if (isCurrent) Color.White else if (isCompleted) VozGreen else VozMuted,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 3. Summary Tiles (3 cards)
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            SummaryTile(
                                label = "Reportes Creados",
                                value = uiState.reports.size.toString(),
                                subtitle = "Total publicado",
                                modifier = Modifier.weight(1f)
                            )
                            SummaryTile(
                                label = "Aprobados",
                                value = uiState.verifiedCount.toString(),
                                subtitle = "${uiState.effectivenessPercentage}% efectividad",
                                modifier = Modifier.weight(1f)
                            )
                            SummaryTile(
                                label = "Votos Vecinales",
                                value = uiState.totalVotes.toString(),
                                subtitle = "Es importante",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // 4. Insignias y Logros Cívicos Banner
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().clickable { onNavigate("badges") }
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(shape = CircleShape, color = Color(0xFFFFF2E7), modifier = Modifier.size(46.dp)) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.EmojiEvents, null, tint = Color(0xFFD97706), modifier = Modifier.size(24.dp))
                                    }
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Insignias y Logros Cívicos", color = VozNavy, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Spacer(Modifier.height(2.dp))
                                    Surface(shape = RoundedCornerShape(50), color = Color(0xFFD3F9E8)) {
                                        Text("4/5 Desbloqueadas", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), color = VozGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text("Descubre tus medallas ganadas, progreso a Héroe y recompensas.", color = VozMuted, fontSize = 11.sp)
                                }
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = VozNavy, modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    // Carrusel de 4 en 4
                    item {
                        Spacer(Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Mis Publicaciones",
                                color = VozNavy,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                "${uiState.reports.size} en total",
                                color = VozMuted,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        val filterOptions = listOf(
                            "Todas" to uiState.reports.size,
                            "Por verificar" to uiState.reports.count { it.status.equals("POR_VERIFICAR", true) },
                            "Aprobados" to uiState.reports.count { it.status.equals("VERIFICADO", true) || it.status.equals("RESUELTO", true) },
                            "Rechazados" to uiState.reports.count { it.status.equals("RECHAZADO", true) }
                        )
                        androidx.compose.foundation.lazy.LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filterOptions) { (filterName, count) ->
                                val isSelected = uiState.activityFilter == filterName || (uiState.activityFilter.isBlank() && filterName == "Todas")
                                Surface(
                                    modifier = Modifier.clickable { onFilterSelected(filterName) },
                                    shape = RoundedCornerShape(50),
                                    color = if (isSelected) Color(0xFF071C2D) else Color(0xFFEAF1F8)
                                ) {
                                    Text(
                                        text = "$filterName ($count)",
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                        color = if (isSelected) Color.White else VozNavy,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }

                    val reportChunks = uiState.filteredActivityReports.chunked(3)
                    if (reportChunks.isEmpty()) {
                        item { EmptyState(stringResource(R.string.no_reports_yet)) }
                    } else {
                        item {
                            val pagerState = rememberPagerState(pageCount = { reportChunks.size })
                            Column(Modifier.fillMaxWidth()) {
                                HorizontalPager(
                                    state = pagerState,
                                    modifier = Modifier.fillMaxWidth()
                                ) { pageIndex ->
                                    val chunk = reportChunks[pageIndex]
                                    Column(
                                        Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        chunk.forEach { report ->
                                            ReportCard(
                                                report = report,
                                                onEdit = if (report.status.equals("RECHAZADO", true) && report.allowResubmit) { { onNavigate("editReport/${report.id}") } } else null,
                                                onDelete = { onOpenDeleteReport(report) }
                                            )
                                        }
                                    }
                                }
                                if (reportChunks.size > 1) {
                                    Spacer(Modifier.height(10.dp))
                                    Row(
                                        Modifier.align(Alignment.CenterHorizontally),
                                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        repeat(reportChunks.size) { index ->
                                            Box(
                                                modifier = Modifier
                                                    .size(if (pagerState.currentPage == index) 8.dp else 6.dp)
                                                    .clip(CircleShape)
                                                    .background(if (pagerState.currentPage == index) VozGreen else Color(0xFFD1D5DB))
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 6. Account Actions (Cerrar sesión & Eliminar cuenta)
                    item {
                        Spacer(Modifier.height(6.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().clickable(onClick = onSignOut)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.AutoMirrored.Filled.Logout, null, tint = VozNavy, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(12.dp))
                                    Text("Cerrar sesión", color = VozNavy, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = VozMuted, modifier = Modifier.size(18.dp))
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().clickable { onShowDeleteAccountDialog(true) }
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.DeleteForever, null, tint = Color(0xFFDC2626), modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(12.dp))
                                    Text("Eliminar cuenta", color = Color(0xFFDC2626), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                }
                "activity" -> {
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            uiState.availableFilters.forEach { filter ->
                                FilterChip(
                                    selected = uiState.activityFilter == filter,
                                    onClick = { onFilterSelected(filter) },
                                    label = { Text(filter) },
                                )
                            }
                        }
                    }
                    val activityReports = uiState.filteredActivityReports
                    if (activityReports.isEmpty()) {
                        item { EmptyState(stringResource(R.string.no_activity_filter)) }
                    } else {
                        items(activityReports, key = { "activity:${it.id}" }) { report -> ReportCard(report) }
                    }
                }
                else -> {
                    item {
                        Card(colors = CardDefaults.cardColors(containerColor = VozNavy)) {
                            Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(stringResource(R.string.community_participation), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text(
                                    stringResource(R.string.participation_summary, uiState.points, uiState.reports.size, uiState.verifiedCount),
                                    color = Color.White
                                )
                                LinearProgressIndicator(
                                    progress = { (uiState.reports.size / 10f).coerceIn(0f, 1f) },
                                    modifier = Modifier.fillMaxWidth(),
                                    color = Color(0xFF55E0B0),
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // --- Diálogos dinámicos ---
    if (uiState.showNameEditor) AlertDialog(
        onDismissRequest = onDismissNameEditor,
        title = { Text(stringResource(R.string.edit_name)) },
        text = { OutlinedTextField(value = uiState.nameDraft, onValueChange = onNameDraftChange, label = { Text(stringResource(R.string.full_name)) }, singleLine = true) },
        confirmButton = { TextButton(onClick = onSaveName) { Text(stringResource(R.string.save)) } },
        dismissButton = { TextButton(onClick = onDismissNameEditor) { Text(stringResource(R.string.cancel)) } },
    )

    uiState.reportToEdit?.let {
        AlertDialog(
            onDismissRequest = onDismissEditReport,
            title = { Text(stringResource(R.string.edit_report)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(uiState.reportToEditTitle, onEditTitleChange, label = { Text(stringResource(R.string.title)) }, singleLine = true)
                    OutlinedTextField(uiState.reportToEditDescription, onEditDescriptionChange, label = { Text(stringResource(R.string.description)) })
                }
            },
            confirmButton = { TextButton(onClick = onConfirmEditReport) { Text(stringResource(R.string.save)) } },
            dismissButton = { TextButton(onClick = onDismissEditReport) { Text(stringResource(R.string.cancel)) } },
        )
    }

    uiState.reportToDelete?.let {
        AlertDialog(
            onDismissRequest = onDismissDeleteReport,
            title = { Text(stringResource(R.string.delete_report)) },
            text = { Text(stringResource(R.string.delete_report_confirmation)) },
            confirmButton = {
                TextButton(onClick = onConfirmDeleteReport) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = onDismissDeleteReport) { Text(stringResource(R.string.cancel)) } },
        )
    }

    if (uiState.showAccountDelete) AlertDialog(
        onDismissRequest = { onShowDeleteAccountDialog(false) },
        title = { Text(stringResource(R.string.delete_account)) },
        text = { Text(stringResource(R.string.delete_account_confirmation)) },
        confirmButton = {
            TextButton(onClick = { onShowDeleteAccountDialog(false); onDeleteAccount() }) {
                Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = { TextButton(onClick = { onShowDeleteAccountDialog(false) }) { Text(stringResource(R.string.cancel)) } },
    )

    uiState.operationMessage?.let { message ->
        AlertDialog(
            onDismissRequest = onDismissOperationMessage,
            title = { Text(stringResource(R.string.app_name)) },
            text = { Text(message) },
            confirmButton = { TextButton(onClick = onDismissOperationMessage) { Text(stringResource(R.string.accept)) } },
        )
    }
}

@Composable
private fun SummaryTile(label: String, value: String, subtitle: String, modifier: Modifier = Modifier) {
    Surface(modifier.height(92.dp), color = VozBlueSurface, shape = RoundedCornerShape(12.dp)) {
        Column(
            Modifier.fillMaxSize().padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(value, color = VozNavy, fontWeight = FontWeight.Bold, fontSize = 18.sp, maxLines = 1)
            Spacer(Modifier.height(2.dp))
            Text(
                label,
                color = VozNavy,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                maxLines = 2,
                lineHeight = 12.sp
            )
            Spacer(Modifier.height(2.dp))
            Text(
                subtitle,
                color = VozGreen,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun ReportCard(report: Report, onEdit: (() -> Unit)? = null, onDelete: (() -> Unit)? = null) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(14.dp)) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(report.title, Modifier.weight(1f), color = VozNavy, fontWeight = FontWeight.Bold)
                val statusLabel = ReportStatus.fromString(report.status).label
                Text(
                    statusLabel,
                    Modifier.background(VozBlueSurface, RoundedCornerShape(50.dp)).padding(horizontal = 8.dp, vertical = 4.dp),
                    color = VozNavy,
                    fontSize = 10.sp,
                )
            }
            Text(report.category, color = VozGreen, fontSize = 12.sp)
            Text(report.description, color = VozMuted, fontSize = 13.sp)
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(report.place, color = VozMuted, fontSize = 11.sp, modifier = Modifier.weight(1f))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (onEdit != null) {
                        TextButton(
                            onClick = onEdit,
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(stringResource(R.string.edit), color = VozNavy, fontSize = 11.sp)
                        }
                    }
                    if (onDelete != null) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable(onClick = onDelete)
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                Icons.Default.DeleteForever,
                                contentDescription = null,
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                stringResource(R.string.delete),
                                color = Color(0xFFDC2626),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(message: String) {
    Surface(Modifier.fillMaxWidth(), color = Color.White, shape = RoundedCornerShape(12.dp)) {
        Text(message, Modifier.padding(18.dp), color = VozMuted)
    }
}
