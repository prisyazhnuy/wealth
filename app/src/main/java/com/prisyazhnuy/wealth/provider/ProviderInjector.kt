package com.prisyazhnuy.wealth.provider

object ProviderInjector {
    val assetProvider by lazy { AssetProviderImpl() }
}