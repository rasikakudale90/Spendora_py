package com.spendora.app.ui.components

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.spendora.app.BuildConfig
import com.spendora.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun GoogleLogoIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        
        // 4-Color Google Brand "G"
        // Red Top
        val redPath = Path().apply {
            moveTo(w * 0.5f, h * 0.1f)
            lineTo(w * 0.85f, h * 0.25f)
            lineTo(w * 0.7f, h * 0.45f)
            lineTo(w * 0.5f, h * 0.35f)
            close()
        }
        drawPath(redPath, color = Color(0xFFEA4335), style = Fill)
        
        // Yellow Left
        val yellowPath = Path().apply {
            moveTo(w * 0.15f, h * 0.5f)
            lineTo(w * 0.35f, h * 0.35f)
            lineTo(w * 0.5f, h * 0.35f)
            lineTo(w * 0.35f, h * 0.65f)
            close()
        }
        drawPath(yellowPath, color = Color(0xFFFBBC05), style = Fill)
        
        // Green Bottom
        val greenPath = Path().apply {
            moveTo(w * 0.15f, h * 0.5f)
            lineTo(w * 0.35f, h * 0.65f)
            lineTo(w * 0.5f, h * 0.65f)
            lineTo(w * 0.85f, h * 0.75f)
            lineTo(w * 0.5f, h * 0.9f)
            close()
        }
        drawPath(greenPath, color = Color(0xFF34A853), style = Fill)
        
        // Blue Right
        val bluePath = Path().apply {
            moveTo(w * 0.5f, h * 0.35f)
            lineTo(w * 0.9f, h * 0.35f)
            lineTo(w * 0.9f, h * 0.65f)
            lineTo(w * 0.5f, h * 0.65f)
            close()
        }
        drawPath(bluePath, color = Color(0xFF4285F4), style = Fill)
    }
}

@Composable
fun GoogleSignInButton(
    onTokenReceived: (String) -> Unit,
    modifier: Modifier = Modifier,
    buttonText: String = "Continue with Google",
    isLoading: Boolean = false
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    Surface(
        onClick = {
            if (isLoading) return@Surface
            val activity = context as? Activity
            if (activity == null) {
                Toast.makeText(context, "Activity not available", Toast.LENGTH_SHORT).show()
                return@Surface
            }

            scope.launch {
                try {
                    val googleIdOption = GetGoogleIdOption.Builder()
                        .setFilterByAuthorizedAccounts(false)
                        .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                        .setAutoSelectEnabled(false)
                        .build()

                    val request = GetCredentialRequest.Builder()
                        .addCredentialOption(googleIdOption)
                        .build()

                    val result = credentialManager.getCredential(
                        request = request,
                        context = activity
                    )

                    val credential = result.credential
                    if (credential is androidx.credentials.CustomCredential &&
                        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                    ) {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        val idToken = googleIdTokenCredential.idToken
                        onTokenReceived(idToken)
                    } else {
                        Toast.makeText(context, "Unexpected credential type", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: GetCredentialCancellationException) {
                    // User dismissed the popup
                } catch (e: GetCredentialException) {
                    Toast.makeText(context, "Google Sign-In: ${e.localizedMessage ?: "Failed"}", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Google Sign-In: ${e.localizedMessage ?: "Failed"}", Toast.LENGTH_LONG).show()
                }
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(12.dp),
        color = SurfaceElevated,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = PrimaryIndigoLight,
                    strokeWidth = 2.dp
                )
            } else {
                GoogleLogoIcon(modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = buttonText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }
        }
    }
}
