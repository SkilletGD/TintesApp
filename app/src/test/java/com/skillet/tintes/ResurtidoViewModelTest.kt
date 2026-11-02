package com.skillet.tintes

import com.skillet.tintes.feature.resurtido.data.dto.ReporteItemDto
import com.skillet.tintes.feature.resurtido.domain.usecase.ConfirmarRecepcionUseCase
import com.skillet.tintes.feature.resurtido.domain.usecase.ObtenerFaltantesUseCase
import com.skillet.tintes.feature.resurtido.presentation.ResurtidoUiState
import com.skillet.tintes.feature.resurtido.presentation.ResurtidoViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ResurtidoViewModelTest {

    // 1. Declaramos los Mocks (objetos falsos) para no tocar Supabase ni la red real
    private val obtenerFaltantesUseCase: ObtenerFaltantesUseCase = mockk()
    private val confirmarRecepcionUseCase: ConfirmarRecepcionUseCase = mockk()

    private lateinit var viewModel: ResurtidoViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun onBefore() {
        // Configuramos el hilo principal para que las corrutinas corran de forma síncrona en las pruebas
        Dispatchers.setMain(testDispatcher)
        // Inicializamos el ViewModel con los casos de uso falsos
        viewModel = ResurtidoViewModel(obtenerFaltantesUseCase, confirmarRecepcionUseCase)
    }

    @After
    fun onAfter() {
        // Limpiamos los hilos después de cada prueba
        Dispatchers.resetMain()
    }

    @Test
    fun `cuando cargarFaltantes es exitoso actualiza el estado a Success con cantidad en 0`() = runTest {
        // GIVEN: Preparamos datos falsos que simulan la respuesta de la base de datos
        val listaFalsa = listOf(
            ReporteItemDto(
                sucursalId = "1",
                sucursal = "Sucursal 1",
                codigoBarras = "750123456789",
                marca = "Kuul",
                tono = "Rojo Fuego",
                stockActual = 1,
                stockMinimo = 2,
                cantidadSugerida = 5 // Aunque el servidor sugiera 5...
            )
        )

        // Le ordenamos al Mock qué debe responder cuando se ejecute el Caso de Uso
        coEvery { obtenerFaltantesUseCase(any()) } returns Result.success(listaFalsa)

        // WHEN: Ejecutamos la función del ViewModel que queremos probar
        viewModel.cargarFaltantes("sucursal_1")

        // Avanzamos el tiempo de las corrutinas para que termine de procesar
        testDispatcher.scheduler.advanceUntilIdle()

        // THEN: Verificamos que el estado de la UI cambió a Success y cumple la regla de negocio
        val currentState = viewModel.uiState.value
        assert(currentState is ResurtidoUiState.Success)

        val successState = currentState as ResurtidoUiState.Success
        assertEquals(1, successState.items.size)
        assertEquals("Kuul", successState.items[0].dto.marca)

        // Verificación clave: Validamos que la cantidad a recibir inicia estrictamente en 0
        // (y no en el 5 que sugería el DTO, tal como lo diseñamos para el mostrador)
        assertEquals(0, successState.items[0].cantidadARecibir)
    }

}