package com.skillet.tintes.feature.resurtido.di

import com.skillet.tintes.feature.resurtido.data.repository.ResurtidoRepositoryImpl
import com.skillet.tintes.feature.resurtido.domain.repository.ResurtidoRepository
import com.skillet.tintes.feature.resurtido.domain.usecase.ConfirmarRecepcionUseCase
import com.skillet.tintes.feature.resurtido.domain.usecase.ObtenerFaltantesUseCase
import com.skillet.tintes.feature.resurtido.presentation.ResurtidoViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val resurtidoModule = module {
    single<ResurtidoRepository> { ResurtidoRepositoryImpl(get()) }
    factory { ObtenerFaltantesUseCase(get()) }
    factory { ConfirmarRecepcionUseCase(get()) }
    viewModel { ResurtidoViewModel(get(), get()) }
}