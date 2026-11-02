package com.skillet.tintes.feature.resurtido.domain.usecase

import com.skillet.tintes.feature.resurtido.domain.repository.ResurtidoRepository

class ConfirmarRecepcionUseCase(
    private val repository: ResurtidoRepository
) {
    suspend operator fun invoke(
        sucursalId: String,
        articulosRecibidos: Map<String, Int> // Map<codigoBarras, cantidad>
    ): Result<Unit> = runCatching {
        articulosRecibidos.forEach { (codigo, cantidad) ->
            if (cantidad > 0) {
                repository.sumarStockMercancia(sucursalId, codigo, cantidad).getOrThrow()
            }
        }
    }
}