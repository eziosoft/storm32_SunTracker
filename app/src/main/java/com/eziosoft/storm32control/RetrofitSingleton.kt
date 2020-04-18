/*
 * *
 *  * Created by Bartosz Szczygiel on 4/15/20 8:47 PM
 *  * Copyright (c) 2020 . All rights reserved.
 *  * Last modified 4/15/20 12:14 AM
 *
 */

package com.eziosoft.storm32control

import com.eziosoft.storm32control.data.SATPosition
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

object RetrofitSingleton {
    private val retrofit by lazy {
        Retrofit.Builder().baseUrl("http://google.com").addConverterFactory(
            GsonConverterFactory.create()
        ).build()
    }

    fun getInstance() = retrofit
}

interface RetrofitInterface {
    @GET("https://www.n2yo.com/rest/v1/satellite/positions/{id}/{lat}/{lon}/{alt}/1/&apiKey={key}")
    fun getSatPosition(
        @Path("id") catalogNumber: Int,
        @Path("lat") lat: Double,
        @Path("lon") lon: Double,
        @Path("alt") alt:Double,
        @Path("key") APIKey: String
    ): Call<SATPosition>
}