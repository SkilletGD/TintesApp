package com.skillet.tintes.feature.resurtido.presentation

import com.skillet.tintes.feature.resurtido.data.dto.ReporteItemDto

data class ItemResurtidoUi(
    val dto: ReporteItemDto,
    val cantidadARecibir: Int
)

sealed interface ResurtidoUiState {
    data object Loading : ResurtidoUiState
    data class Success(
        val items: List<ItemResurtidoUi>,
        val guardando: Boolean = false
    ) : ResurtidoUiState
    data object Empty : ResurtidoUiState
    data class Error(val message: String) : ResurtidoUiState
}