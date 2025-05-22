package ru.arisubest.smartshopper.data.remote

import com.google.gson.JsonElement
import retrofit2.http.GET
import retrofit2.http.Path

interface ProductApiService {
    @GET("api/v3/product/{barcode}")
    suspend fun getProduct(@Path("barcode") barcode: String): JsonElement

    companion object {
        const val BASE_URL = "https://world.openfoodfacts.org/"
    }
} 