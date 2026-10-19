package com.vozbarrial.features.reports.detail.presentation

import com.vozbarrial.domain.model.Report

data class Contribution(
    val id: String = "",
    val authorName: String = "",
    val authorRole: String = "",
    val timeAgo: String = "",
    val content: String = "",
    val isOfficial: Boolean = false,
    val imageUrls: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)

data class ReportDetailUiState(
    val report: Report = Report(),
    val contributions: List<Contribution> = emptyList(),
    val newContributionText: String = "",
    val contributionPhotoUris: List<String> = emptyList(),
    val isUploading: Boolean = false,
    val isSupported: Boolean = false,
    val supportCount: Int = 42
)
