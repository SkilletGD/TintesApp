package com.skillet.tintes.core.di

import com.skillet.tintes.feature.resurtido.di.resurtidoModule
import com.skillet.tintes.feature.scanner.di.scannerModule


val appModules = listOf(
    coreModule,
    scannerModule,
    resurtidoModule
)