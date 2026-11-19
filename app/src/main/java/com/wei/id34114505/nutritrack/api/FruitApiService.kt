package com.wei.id34114505.nutritrack.api

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Data model for fruit nutrition information
 */
data class FruitResponse(
    val name: String,
    val genus: String,
    val family: String,
    val order: String,
    val nutritions: Nutritions
)

/**
 * Nutritional information for a fruit
 */
data class Nutritions(
    val calories: Int,
    val fat: Double,
    val sugar: Double,
    val carbohydrates: Double,
    val protein: Double
)

/**
 * API service interface for Fruityvice
 */
interface FruitApiService {
    @GET("api/fruit/{name}")
    suspend fun getFruitByName(@Path("name") name: String): Response<FruitResponse>

    @GET("api/fruit/all")
    suspend fun getAllFruits(): Response<List<FruitResponse>>

    companion object {
        private const val BASE_URL = "https://www.fruityvice.com/"

        fun create(): FruitApiService {
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return retrofit.create(FruitApiService::class.java)
        }
    }
}