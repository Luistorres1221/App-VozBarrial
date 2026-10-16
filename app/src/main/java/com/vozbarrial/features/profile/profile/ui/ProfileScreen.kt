package com.vozbarrial.features.profile.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
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
import com.vozbarrial.R
import com.vozbarrial.domain.model.Report
import com.vozbarrial.features.profile.profile.presentation.ProfileUiState
import com.vozbarrial.features.profile.profile.presentation.ProfileViewModel
import com.vozbarrial.ui.theme.*

@Composable
fun CommunityRoute(
    page: String,
    name: String,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    onSignOut: () -> Unit,
    onSaveName: (String) -> Unit,
    onDeleteAccount: () -> Unit,
    profilePhoto: String?,
    points: Int,
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

    LaunchedEffect(page, name, profilePhoto, points, reports) {
        viewModel.updateProfileData(page, name, profilePhoto, points, reports, availableFilters)
    }

    CommunityContent(
        uiState = uiState,
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
                Icon(Icons.Default.Notifications, contentDescription = stringResource(R.string.activity_title), tint = VozNavy)
            }
        }

        LazyColumn(
            Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            when (uiState.currentPage) {
                "profile" -> {
                    item {
                        Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    AsyncImage(
                                        model = uiState.profilePhoto,
                                        contentDescription = stringResource(R.string.profile_photo),
                                        modifier = Modifier.size(62.dp).clip(CircleShape).background(VozBlueSurface),
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            uiState.name.ifBlank { stringResource(R.string.default_user_name) },
                                            color = VozNavy,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp
                                        )
                                        Text(stringResource(R.string.user_account_subtitle), color = VozMuted, fontSize = 12.sp)
                                    }
                                    IconButton(onClick = onOpenNameEditor) {
                                        Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit_name), tint = VozNavy)
                                    }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    SummaryTile(stringResource(R.string.points_label), uiState.points.toString(), Modifier.weight(1f))
                                    SummaryTile(stringResource(R.string.reports_label), uiState.reports.size.toString(), Modifier.weight(1f))
                                    SummaryTile(stringResource(R.string.resolved_label), uiState.verifiedCount.toString(), Modifier.weight(1f))
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(onClick = { onNavigate("frames") }, modifier = Modifier.weight(1f)) {
                                        Text(stringResource(R.string.my_frames))
                                    }
                                    Button(onClick = { onNavigate("store") }, modifier = Modifier.weight(1f)) {
                                        Text(stringResource(R.string.store))
                                    }
                                }
                            }
                        }
                    }
                    item {
                        Text(
                            stringResource(R.string.my_publications_count, uiState.reports.size),
                            color = VozNavy,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                    if (uiState.sortedReports.isEmpty()) {
                        item { EmptyState(stringResource(R.string.no_reports_yet)) }
                    } else {
                        items(uiState.sortedReports, key = { it.id }) { report ->
                            ReportCard(
                                report = report,
                                onEdit = { onOpenEditReport(report) },
                                onDelete = { onOpenDeleteReport(report) },
                            )
                        }
                    }
                    item {
                        OutlinedButton(onClick = onSignOut, modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.sign_out))
                        }
                        Spacer(Modifier.height(4.dp))
                        TextButton(onClick = { onShowDeleteAccountDialog(true) }, modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.delete_account_permanently), color = MaterialTheme.colorScheme.error)
                        }
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

        NavigationBar(containerColor = Color.White) {
            listOf("profile" to stringResource(R.string.profile_tab), "activity" to stringResource(R.string.activity_tab), "badges" to stringResource(R.string.badges_tab)).forEach { (route, label) ->
                NavigationBarItem(
                    selected = uiState.currentPage == route || (route == "badges" && uiState.currentPage !in setOf("profile", "activity")),
                    onClick = { onNavigate(route) },
                    icon = { Text(when (route) { "profile" -> "●"; "activity" -> "◷"; else -> "★" }) },
                    label = { Text(label) },
                )
            }
        }
    }

    // --- Diálogos dinámicos con Strings desde recursos ---
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
private fun SummaryTile(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(modifier, color = VozBlueSurface, shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.padding(horizontal = 8.dp, vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, color = VozNavy, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(label, color = VozMuted, fontSize = 10.sp)
        }
    }
}

@Composable
private fun ReportCard(report: Report, onEdit: (() -> Unit)? = null, onDelete: (() -> Unit)? = null) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(report.title, Modifier.weight(1f), color = VozNavy, fontWeight = FontWeight.Bold)
                Text(
                    report.status,
                    Modifier.background(VozBlueSurface, RoundedCornerShape(50.dp)).padding(horizontal = 8.dp, vertical = 4.dp),
                    color = VozNavy,
                    fontSize = 10.sp,
                )
            }
            Text(report.category, color = VozGreen, fontSize = 12.sp)
            Text(report.description, color = VozMuted, fontSize = 13.sp)
            Text(report.place, color = VozMuted, fontSize = 11.sp)
            if (onEdit != null || onDelete != null) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    if (onEdit != null) TextButton(onClick = onEdit) { Text(stringResource(R.string.edit)) }
                    if (onDelete != null) TextButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                        Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
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
