package com.vozbarrial.features.auth.register.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vozbarrial.R
import com.vozbarrial.ui.theme.*

@Composable
fun TermsRoute(
    onBack: () -> Unit
) {
    TermsScreen(onBack = onBack)
}

@Composable
fun TermsScreen(
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VozBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back), tint = VozNavy)
            }
            Spacer(Modifier.width(8.dp))
            Text(
                stringResource(R.string.terms_and_conditions_title),
                color = VozNavy,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    TermSection(
                        title = stringResource(R.string.terms_section_1_title),
                        content = stringResource(R.string.terms_section_1_content)
                    )
                    TermSection(
                        title = stringResource(R.string.terms_section_2_title),
                        content = stringResource(R.string.terms_section_2_content)
                    )
                    TermSection(
                        title = stringResource(R.string.terms_section_3_title),
                        content = stringResource(R.string.terms_section_3_content)
                    )
                    TermSection(
                        title = stringResource(R.string.terms_section_4_title),
                        content = stringResource(R.string.terms_section_4_content)
                    )
                }
            }
        }
    }
}

@Composable
private fun TermSection(title: String, content: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            color = VozNavy,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        Text(
            text = content,
            color = VozMuted,
            fontSize = 12.sp,
            lineHeight = 18.sp
        )
    }
}
