package com.skillet.tintes.feature.scanner.di

import com.skillet.tintes.feature.scanner.data.repository.ScannerRepositoryImpl
import com.skillet.tintes.feature.scanner.domain.repository.ScannerRepository
import com.skillet.tintes.feature.scanner.domain.usecase.DeshacerVentaUseCase
import com.skillet.tintes.feature.scanner.domain.usecase.RegistrarVentaUseCase
import com.skillet.tintes.feature.scanner.presentation.ScannerViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val scannerModule = module {
    single<ScannerRepository> { ScannerRepositoryImpl(get()) }
    factory { RegistrarVentaUseCase(get()) }
    factory { DeshacerVentaUseCase(get()) }
    viewModel { ScannerViewModel(get(), get()) }
}