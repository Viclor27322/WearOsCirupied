package com.example.cirupied2.presentation

import retrofit2.http.GET

interface ListaApi2 {
    @GET("citas-hoy-hora")
    suspend fun getList(): List<ListModel>
}


