package com.spendora.app.data.model

import com.google.gson.annotations.SerializedName

data class UserDto(
    @SerializedName("id") val id: Int,
    @SerializedName("email") val email: String,
    @SerializedName("full_name") val fullName: String,
    @SerializedName("is_active") val isActive: Boolean,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String? = null
)

data class UserRegisterRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("full_name") val fullName: String
)

data class UserRegisterResponse(
    @SerializedName("message") val message: String,
    @SerializedName("user") val user: UserDto
)

data class UserLoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class AuthSuccessResponse(
    @SerializedName("user") val user: UserDto,
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String,
    @SerializedName("expires_in") val expiresIn: Int
)

data class PasswordResetRequest(
    @SerializedName("email") val email: String
)

data class PasswordResetResponse(
    @SerializedName("message") val message: String,
    @SerializedName("dev_otp") val devOtp: String? = null,
    @SerializedName("expires_in_seconds") val expiresInSeconds: Int? = 50
)

data class VerifyOtpRequest(
    @SerializedName("email") val email: String,
    @SerializedName("otp") val otp: String
)

data class VerifyOtpResponse(
    @SerializedName("message") val message: String,
    @SerializedName("valid") val valid: Boolean
)

data class PasswordResetConfirm(
    @SerializedName("email") val email: String,
    @SerializedName("otp") val otp: String,
    @SerializedName("new_password") val newPassword: String
)

data class PasswordChangeRequest(
    @SerializedName("current_password") val currentPassword: String,
    @SerializedName("new_password") val newPassword: String
)

data class GenericMessageResponse(
    @SerializedName("message") val message: String
)

data class ErrorResponse(
    @SerializedName("detail") val detail: Any? = null
) {
    fun getErrorMessage(): String {
        return when (detail) {
            is String -> detail
            is List<*> -> detail.joinToString("\n") { it.toString() }
            else -> "An error occurred. Please try again."
        }
    }
}
