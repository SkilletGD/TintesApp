package com.skillet.tintes.feature.resurtido.domain.repository

import com.skillet.tintes.feature.resurtido.data.dto.ReporteItemDto

interface ResurtidoRepository {
    suspend fun obtenerFaltantesPorSucursal(sucursalId: String): Result<List<ReporteItemDto>>
    suspend fun sumarStockMercancia(sucursalId: String, codigoBarras: String, cantidad: Int): Result<Unit>
}