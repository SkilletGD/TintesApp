package com.skillet.tintes.feature.resurtido.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skillet.tintes.core.util.ErrorHandler
import com.skillet.tintes.feature.resurtido.domain.usecase.ConfirmarRecepcionUseCase
import com.skillet.tintes.feature.resurtido.domain.usecase.ObtenerFaltantesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ResurtidoViewModel(
    private val obtenerFaltantesUseCase: ObtenerFaltantesUseCase,
    private val confirmarRecepcionUseCase: ConfirmarRecepcionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ResurtidoUiState>(ResurtidoUiState.Loading)
    val uiState: StateFlow<ResurtidoUiState> = _uiState.asStateFlow()

    fun cargarFaltantes(sucursalId: String) {
        _uiState.value = ResurtidoUiState.Loading
        viewModelScope.launch {
            obtenerFaltantesUseCase(sucursalId)
                .onSuccess { lista ->
                    if (lista.isEmpty()) {
                        _uiState.value = ResurtidoUiState.Empty
                    } else {
                        val uiItems = lista.map { dto ->
                            ItemResurtidoUi(dto = dto, cantidadARecibir = 0)
                        }
                        _uiState.value = ResurtidoUiState.Success(items = uiItems)
                    }
                }
                .onFailure { error ->
                    _uiState.value = ResurtidoUiState.Error(ErrorHandler.parseUserFriendlyMessage(error))
                }
        }
    }

    fun modificarCantidad(codigoBarras: String, delta: Int) {
        val currentState = _uiState.value
        if (currentState is ResurtidoUiState.Success) {
            val updated = currentState.items.map { item ->
                if (item.dto.codigoBarras == codigoBarras) {
                    val nuevaCantidad = (item.cantidadARecibir + delta).coerceAtLeast(0)
                    item.copy(cantidadARecibir = nuevaCantidad)
                } else {
                    item
                }
            }
            _uiState.value = currentState.copy(items = updated)
        }
    }

    fun guardarRecepcion(sucursalId: String, onCompletado: () -> Unit) {
        val currentState = _uiState.value
        if (currentState is ResurtidoUiState.Success) {
            _uiState.value = currentState.copy(guardando = true)
            viewModelScope.launch {
                val mapaCantidades = currentState.items.associate {
                    it.dto.codigoBarras to it.cantidadARecibir
                }
                confirmarRecepcionUseCase(sucursalId, mapaCantidades)
                    .onSuccess {
                        cargarFaltantes(sucursalId)
                        onCompletado()
                    }
                    .onFailure { error ->
                        _uiState.value = ResurtidoUiState.Error(ErrorHandler.parseUserFriendlyMessage(error))
                    }
            }
        }
    }
}