package com.spendora.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.spendora.app.R
import com.spendora.app.ui.theme.BorderDark

@Composable
fun SpendoraLogo(
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    showContainer: Boolean = true
) {
    if (showContainer) {
        val cornerRadius = size * 0.28f
        Box(
            modifier = modifier
                .size(size)
                .clip(RoundedCornerShape(cornerRadius))
                .border(1.dp, BorderDark, RoundedCornerShape(cornerRadius)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_spendora_logo),
                contentDescription = "Spendora Logo",
                modifier = Modifier.fillMaxSize()
            )
        }
    } else {
        Image(
            painter = painterResource(id = R.drawable.ic_spendora_logo),
            contentDescription = "Spendora Logo",
            modifier = modifier.size(size)
        )
    }
}
