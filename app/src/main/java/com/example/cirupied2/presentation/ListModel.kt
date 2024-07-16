package com.example.cirupied.presentation

data class ListModel(
   var IdCita: Int,
   var IdUser: Int,
   var IdDependencia: Int,
   var idPaciente: Int,
   var HorarioInicio: String,
   var HoraFin: String,
   var Descripcion: String,
   var Estado: Boolean,
   var Nombre: String,
   var ApellidoP: String
)
