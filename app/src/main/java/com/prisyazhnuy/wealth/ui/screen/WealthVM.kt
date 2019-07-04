package com.prisyazhnuy.wealth.ui.screen

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.prisyazhnuy.wealth.models.Client
import com.prisyazhnuy.wealth.provider.ProviderInjector
import com.prisyazhnuy.wealth.ui.base.BaseViewModel
import io.reactivex.Single
import io.reactivex.SingleTransformer
import org.joda.time.DateTime
import java.math.BigDecimal

class WealthVM(app: Application) : BaseViewModel(app) {

    companion object {
        private const val ALL = "ALL"
    }

    val wealthLD = MutableLiveData<List<Pair<DateTime?, BigDecimal>>>()
    val wealthQuarterLD = MutableLiveData<List<Pair<DateTime?, BigDecimal>>>()
    val categoryWealthQuarterLD = MutableLiveData<List<Pair<DateTime?, BigDecimal>>>()
    val categoriesLD = MutableLiveData<List<String>>()
    val clientLD = MutableLiveData<Client>()

    fun loadWealth() {
        ProviderInjector.assetProvider.loadClients()
                .map { it[0] }
                .doOnSuccess { clientLD.postValue(it) }
                .doOnSuccess { categoriesLD.postValue(it.assets?.mapNotNull { it.category }) }
                .compose(wealthTransformer(ALL))
                .doAsync(wealthLD)
    }

    private fun wealthTransformer(category: String) = SingleTransformer<Client, List<Pair<DateTime?, BigDecimal>>> {
        it
                .map {
                    it.assets?.filter { category == ALL || it.category == category }?.mapNotNull { it.historicalValuations }?.flatten()?.groupBy({ it.valuationDate }, { it.valuationInCurrency })
                }
                .map {
                    it.map { it.key to it.value.filterNotNull().fold(BigDecimal.ZERO, BigDecimal::add) }
                }
                .map { it.sortedBy { it.first } }
                .map {
                    val result = mutableListOf<Pair<DateTime?, BigDecimal>>()
                    it.forEachIndexed { index, pair ->
                        result.add(index, pair.first
                                to (index.takeIf { it > 1 }?.let { pair.second.add(result[it - 1].second) }
                                ?: pair.second))
                    }
                    result.toList()
                }
    }

    fun loadByDate(from: DateTime, to: DateTime) {
        wealthLD.value?.let {
            Single.just(it)
                    .map {
                        it.filter { it.first?.isAfter(from) == true && it.first?.isBefore(to) == true }
                    }
                    .doAsync(wealthQuarterLD)
        }
    }

    fun loadCategoryChange(category: String, from: DateTime, to: DateTime) {
        clientLD.value?.let {
            Single.just(it)
                    .compose(wealthTransformer(category))
                    .map {
                        it.filter { it.first?.isAfter(from) == true && it.first?.isBefore(to) == true }
                    }
                    .doAsync(categoryWealthQuarterLD)
        }
    }
}