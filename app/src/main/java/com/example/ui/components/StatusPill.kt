package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.DueStatus

@Composable
fun StatusPill(
    status: DueStatus,
    modifier: Modifier = Modifier,
    customText: String? = null
) {
    val (bgColor, textColor, text) = when (status) {
        DueStatus.DUE_TODAY -> Triple(Color(0xFFECFDF5), Color(0xFF059669), customText ?: "Due Today")
        DueStatus.TOMORROW -> Triple(Color(0xFFFFFBEB), Color(0xFFD97706), customText ?: "Tomorrow")
        DueStatus.DUE_SOON -> Triple(Color(0xFFFFFBEB), Color(0xFFD97706), customText ?: "Due Soon")
        DueStatus.UPCOMING -> Triple(Color(0xFFF1F5F9), Color(0xFF475569), customText ?: "Upcoming")
        DueStatus.OVERDUE -> Triple(Color(0xFFFFE4E6), Color(0xFFE11D48), customText ?: "Overdue")
        DueStatus.COMPLETED -> Triple(Color(0xFFECFDF5), Color(0xFF059669), customText ?: "Paid")
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
