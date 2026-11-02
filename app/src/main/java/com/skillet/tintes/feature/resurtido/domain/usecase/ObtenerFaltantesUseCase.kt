package com.skillet.tintes.feature.resurtido.domain.usecase

import com.skillet.tintes.feature.resurtido.data.dto.ReporteItemDto
import com.skillet.tintes.feature.resurtido.domain.repository.ResurtidoRepository

class ObtenerFaltantesUseCase(
    private val repository: ResurtidoRepository
) {
    suspend operator fun invoke(sucursalId: String): Result<List<ReporteItemDto>> {
        return repository.obtenerFaltantesPorSucursal(sucursalId)
    }
}