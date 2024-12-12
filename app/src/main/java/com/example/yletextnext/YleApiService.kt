package com.example.yletextnext

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

// määrittelee API pyynnöt
interface YleApiService {

    @GET("v1/teletext/pages/{pageNumber}.json")
    suspend fun getTeletextPage(
        @Path("pageNumber") pageNumber: Int,
        @Query("app_id") appId: String,
        @Query("app_key") appKey: String
    ): TeletextResponse

    companion object {

        fun create(): YleApiService {
            // logging-interceptor debuggausta varten
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            // client, sisältää logging-interceptorin
            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .build()

            // Rakentaa Retrofit-instanssin, jossa määritellään perus-URL ja Gson-muunnin
            val retrofit = Retrofit.Builder()
                .baseUrl("https://external.api.yle.fi/") //API:n perus-URL
                .client(client)                         //käytetään luotua OkHttpClientiä
                .addConverterFactory(GsonConverterFactory.create()) // gson muunnin JSON tietojen käsittelyyn
                .build()

            return retrofit.create(YleApiService::class.java)
        }
    }
}
