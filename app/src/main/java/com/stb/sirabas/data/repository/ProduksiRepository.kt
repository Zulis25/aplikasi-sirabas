package com.stb.sirabas.data.repository

import com.stb.sirabas.data.remote.LabelsRequest
import com.stb.sirabas.data.remote.ProductionBatchDto
import com.stb.sirabas.data.remote.ProductionOutputRequest
import com.stb.sirabas.data.remote.ProductionRequest
import com.stb.sirabas.data.remote.RawMaterialCreatedDto
import com.stb.sirabas.data.remote.RawMaterialDto
import com.stb.sirabas.data.remote.RawMaterialRequest
import com.stb.sirabas.data.remote.SideOutputRequest
import com.stb.sirabas.data.remote.SirabasApi
import com.stb.sirabas.data.remote.TraceDto

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val statusCode: Int = 0) : ApiResult<Nothing>()
}

class ProduksiRepository(private val api: SirabasApi) {

    suspend fun getRawMaterials(): ApiResult<List<RawMaterialDto>> {
        return try {
            val response = api.getRawMaterials()
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error("Gagal mengambil data bahan baku (${response.code()})", response.code())
            }
        } catch (e: Exception) {
            ApiResult.Error("Gagal terhubung ke server: ${e.localizedMessage}")
        }
    }

    suspend fun createRawMaterial(request: RawMaterialRequest): ApiResult<RawMaterialCreatedDto> {
        return try {
            val response = api.createRawMaterial(request)
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                val err = response.errorBody()?.string().orEmpty()
                ApiResult.Error("Gagal mencatat bahan baku: $err", response.code())
            }
        } catch (e: Exception) {
            ApiResult.Error("Gagal terhubung ke server: ${e.localizedMessage}")
        }
    }

    suspend fun getProductionBatches(): ApiResult<List<ProductionBatchDto>> {
        return try {
            val response = api.getProductionBatches()
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error("Gagal mengambil batch produksi (${response.code()})", response.code())
            }
        } catch (e: Exception) {
            ApiResult.Error("Gagal terhubung ke server: ${e.localizedMessage}")
        }
    }

    suspend fun createProductionBatch(request: ProductionRequest): ApiResult<Unit> {
        return try {
            val response = api.createProductionBatch(request)
            if (response.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                val err = response.errorBody()?.string().orEmpty()
                ApiResult.Error("Gagal mencatat produksi: $err", response.code())
            }
        } catch (e: Exception) {
            ApiResult.Error("Gagal terhubung ke server: ${e.localizedMessage}")
        }
    }

    suspend fun createLabels(kodeBatch: String, kodeProduk: String, beratKarungKg: List<Double>): ApiResult<Unit> {
        return try {
            val response = api.createLabels(kodeBatch, LabelsRequest(kodeProduk, beratKarungKg))
            if (response.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                val err = response.errorBody()?.string().orEmpty()
                ApiResult.Error("Gagal membuat label karung: $err", response.code())
            }
        } catch (e: Exception) {
            ApiResult.Error("Gagal terhubung ke server: ${e.localizedMessage}")
        }
    }

    suspend fun traceLabel(kodeKarung: String): ApiResult<TraceDto> {
        return try {
            val response = api.traceLabel(kodeKarung)
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error("Label karung tidak ditemukan (${response.code()})", response.code())
            }
        } catch (e: Exception) {
            ApiResult.Error("Gagal terhubung ke server: ${e.localizedMessage}")
        }
    }
}
