package com.example.cirupied.presentation

import retrofit2.http.GET

interface ListaApi {
    @GET("citas-hoy")
    suspend fun getList(): List<ListModel>
}
