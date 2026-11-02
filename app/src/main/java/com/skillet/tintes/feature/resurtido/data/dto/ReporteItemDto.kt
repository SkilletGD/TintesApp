package com.skillet.tintes.feature.resurtido.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReporteItemDto(
    @SerialName("sucursal_id") val sucursalId: String,
    val sucursal: String,
    @SerialName("codigo_barras") val codigoBarras: String,
    val marca: String,
    val tono: String,
    @SerialName("stock_actual") val stockActual: Int,
    @SerialName("stock_minimo") val stockMinimo: Int,
    @SerialName("cantidad_sugerida") val cantidadSugerida: Int
)