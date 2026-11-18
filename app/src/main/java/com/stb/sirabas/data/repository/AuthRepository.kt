package com.stb.sirabas.data.repository

import com.stb.sirabas.data.remote.LoginRequest
import com.stb.sirabas.data.remote.ProfilePatchRequest
import com.stb.sirabas.data.remote.RegisterRequest
import com.stb.sirabas.data.remote.RegisterResponse
import com.stb.sirabas.data.remote.SirabasApi
import com.stb.sirabas.data.remote.TokenManager
import com.stb.sirabas.data.remote.UserProfileDto

sealed class AuthResult<out T> {
    data class Success<T>(val data: T) : AuthResult<T>()
    data class Error(val message: String, val statusCode: Int = 0) : AuthResult<Nothing>()
    object PendingActivation : AuthResult<Nothing>()
}

class AuthRepository(
    private val api: SirabasApi,
    private val tokenManager: TokenManager
) {

    suspend fun login(username: String, password: String): AuthResult<UserProfileDto> {
        return try {
            val response = api.login(LoginRequest(username = username.trim(), password = password))
            if (!response.isSuccessful) {
                val statusCode = response.code()
                return if (statusCode == 403) {
                    AuthResult.PendingActivation
                } else {
                    val err = response.errorBody()?.string().orEmpty()
                    AuthResult.Error(
                        message = if (err.contains("Username atau kata sandi", ignoreCase = true)) {
                            "Username atau kata sandi tidak sesuai."
                        } else {
                            "Login gagal ($statusCode)"
                        },
                        statusCode = statusCode
                    )
                }
            }

            val token = response.body()?.access_token
            if (token.isNullOrBlank()) {
                return AuthResult.Error("Server tidak mengembalikan token akses.")
            }

            tokenManager.saveToken(token)

            // Fetch profile after login
            val profileResponse = api.me()
            if (!profileResponse.isSuccessful) {
                if (profileResponse.code() == 403) {
                    return AuthResult.PendingActivation
                }
                return AuthResult.Error("Gagal memuat profil (${profileResponse.code()})")
            }

            val profile = profileResponse.body()
                ?: return AuthResult.Error("Data profil tidak ditemukan.")

            AuthResult.Success(profile)
        } catch (e: Exception) {
            AuthResult.Error("Gagal terhubung ke server: ${e.localizedMessage}")
        }
    }

    suspend fun register(
        username: String,
        password: String,
        namaLengkap: String,
        nomorWhatsapp: String,
        namaPerusahaan: String? = null
    ): AuthResult<RegisterResponse> {
        return try {
            val response = api.register(
                RegisterRequest(
                    username = username.trim(),
                    password = password,
                    nama_lengkap = namaLengkap.trim(),
                    nomor_whatsapp = nomorWhatsapp.trim(),
                    nama_perusahaan = namaPerusahaan?.trim()
                )
            )

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    AuthResult.Success(body)
                } else {
                    AuthResult.Error("Pendaftaran berhasil tetapi data server kosong.")
                }
            } else {
                val err = response.errorBody()?.string().orEmpty()
                AuthResult.Error("Pendaftaran gagal (${response.code()}): $err", response.code())
            }
        } catch (e: Exception) {
            AuthResult.Error("Gagal terhubung ke server: ${e.localizedMessage}")
        }
    }

    suspend fun getProfile(): AuthResult<UserProfileDto> {
        return try {
            val response = api.me()
            if (response.isSuccessful) {
                val profile = response.body()
                if (profile != null) {
                    AuthResult.Success(profile)
                } else {
                    AuthResult.Error("Data profil kosong.")
                }
            } else {
                if (response.code() == 401) {
                    tokenManager.clearToken()
                }
                AuthResult.Error("Gagal memuat profil (${response.code()})", response.code())
            }
        } catch (e: Exception) {
            AuthResult.Error("Gagal terhubung ke server: ${e.localizedMessage}")
        }
    }

    suspend fun updateProfile(
        namaLengkap: String? = null,
        nomorWhatsapp: String? = null,
        namaPerusahaan: String? = null
    ): AuthResult<UserProfileDto> {
        return try {
            val response = api.updateProfile(
                ProfilePatchRequest(
                    nama_lengkap = namaLengkap,
                    nomor_whatsapp = nomorWhatsapp,
                    nama_perusahaan = namaPerusahaan
                )
            )
            if (response.isSuccessful && response.body() != null) {
                AuthResult.Success(response.body()!!)
            } else {
                AuthResult.Error("Gagal memperbarui profil (${response.code()})")
            }
        } catch (e: Exception) {
            AuthResult.Error("Gagal memperbarui profil: ${e.localizedMessage}")
        }
    }

    fun logout() {
        tokenManager.clearToken()
    }
}
