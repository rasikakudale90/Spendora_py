package com.spendora.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spendora.app.ui.theme.*

@Composable
fun SpendoraCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color? = null,
    gradientBrush: Brush? = CardSurfaceGradient,
    borderColor: Color = BorderDark,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val backgroundModifier = when {
        backgroundColor != null -> Modifier.background(backgroundColor)
        gradientBrush != null -> Modifier.background(gradientBrush)
        else -> Modifier.background(CardSurfaceGradient)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, borderColor, RoundedCornerShape(24.dp))
            .then(backgroundModifier)
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            content = content
        )
    }
}

@Composable
fun SpendoraButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    gradientBrush: Brush? = PrimaryGradient,
    containerColor: Color? = null,
    contentColor: Color = OnPrimaryColor
) {
    val shape = RoundedCornerShape(16.dp)

    val backgroundModifier = if (containerColor != null) {
        Modifier.background(if (enabled) containerColor else containerColor.copy(alpha = 0.5f), shape)
    } else if (gradientBrush != null) {
        Modifier.background(if (enabled) gradientBrush else Brush.linearGradient(listOf(SurfaceElevated, SurfaceElevated)), shape)
    } else {
        Modifier.background(PrimaryGradient, shape)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .shadow(if (enabled && !isLoading) 8.dp else 0.dp, shape = shape, spotColor = PrimaryCyan.copy(alpha = 0.35f))
            .clip(shape)
            .then(backgroundModifier)
            .clickable(enabled = enabled && !isLoading, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = contentColor,
                strokeWidth = 2.5.dp
            )
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = if (enabled) contentColor else contentColor.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun SpendoraTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    leadingIcon: ImageVector? = null,
    isPassword: Boolean = false,
    errorMessage: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                if (placeholder.isNotEmpty()) {
                    Text(text = placeholder, color = TextMuted)
                }
            },
            leadingIcon = if (leadingIcon != null) {
                { Icon(imageVector = leadingIcon, contentDescription = null, tint = TextSecondary) }
            } else null,
            trailingIcon = if (isPassword) {
                {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = TextSecondary
                        )
                    }
                }
            } else null,
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            isError = !errorMessage.isNullOrBlank(),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = singleLine,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = if (SpendoraTheme.colors.isDark) Color(0xFF0A0E16) else SurfaceDark,
                unfocusedContainerColor = if (SpendoraTheme.colors.isDark) Color(0xFF0A0E16) else SurfaceDark,
                focusedBorderColor = PrimaryCyan,
                unfocusedBorderColor = BorderDark,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                errorBorderColor = RoseDanger,
                cursorColor = PrimaryCyan
            )
        )

        AnimatedVisibility(visible = !errorMessage.isNullOrBlank()) {
            Text(
                text = errorMessage ?: "",
                color = RoseDanger,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }
    }
}

@Composable
fun PasswordStrengthIndicator(password: String) {
    val hasMinLength = password.length >= 8
    val hasUpper = password.any { it.isUpperCase() }
    val hasLower = password.any { it.isLowerCase() }
    val hasDigit = password.any { it.isDigit() }
    val hasSpecial = password.any { !it.isLetterOrDigit() }

    val passedCount = listOf(hasMinLength, hasUpper, hasLower, hasDigit, hasSpecial).count { it }
    val strengthScore = (passedCount / 5f).coerceIn(0f, 1f)

    val (strengthColor, strengthLabel) = when {
        passedCount <= 1 -> RoseDanger to "Weak"
        passedCount in 2..3 -> AmberWarning to "Medium"
        passedCount == 4 -> EmeraldSuccess to "Strong"
        else -> EmeraldSuccess to "Very Strong"
    }

    Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Password Strength", style = MaterialTheme.typography.bodySmall, color = TextMuted)
            Text(text = strengthLabel, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = strengthColor)
        }

        Spacer(modifier = Modifier.height(6.dp))

        LinearProgressIndicator(
            progress = { strengthScore },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = strengthColor,
            trackColor = SurfaceElevated
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Criteria checklist
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            CriteriaItem(label = "8+ Chars", passed = hasMinLength)
            CriteriaItem(label = "Uppercase", passed = hasUpper)
            CriteriaItem(label = "Lowercase", passed = hasLower)
            CriteriaItem(label = "Number", passed = hasDigit)
            CriteriaItem(label = "Symbol", passed = hasSpecial)
        }
    }
}

@Composable
private fun CriteriaItem(label: String, passed: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = if (passed) Icons.Default.Check else Icons.Default.Close,
            contentDescription = null,
            tint = if (passed) EmeraldSuccess else TextMuted,
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(2.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = if (passed) TextPrimary else TextMuted
        )
    }
}
