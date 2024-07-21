package com.example.cirupied2.presentation

import retrofit2.http.GET

interface ListaApi {
    @GET("citas-hoy")
    suspend fun getList(): List<ListModel>
}


