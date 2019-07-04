package com.prisyazhnuy.wealth.ui.base

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.Flowable
import io.reactivex.FlowableTransformer
import io.reactivex.Single
import io.reactivex.SingleTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers

abstract class BaseViewModel(app: Application) : AndroidViewModel(app), LifecycleObserver {

    val errorLiveData = MediatorLiveData<String>()
    val isLoadingLiveData = MediatorLiveData<Boolean>()

    protected open val onErrorConsumer = Consumer<Throwable> {
        errorLiveData.value = it.message
    }

    private var compositeDisposable: CompositeDisposable? = null

    override fun onCleared() {
        isLoadingLiveData.value = false
        clearSubscription()
        super.onCleared()
    }

    fun showLoadingProgress() {
        isLoadingLiveData.value = true
    }

    fun hideLoadingProgress() {
        isLoadingLiveData.value = false
    }

    protected fun <T> Flowable<T>.doAsync(successful: Consumer<T>,
                                          error: Consumer<Throwable> = onErrorConsumer,
                                          isShowProgress: Boolean = true) {
        preSubscribe(isShowProgress)
                .subscribe(successful, error)
                .addSubscription()
    }

    protected fun <T> Flowable<T>.doAsync(successful: MutableLiveData<T>,
                                          error: Consumer<Throwable> = onErrorConsumer,
                                          isShowProgress: Boolean = true) {
        preSubscribe(isShowProgress)
                .subscribe(Consumer { successful.value = it }, error)
                .addSubscription()
    }

    protected fun <T> Single<T>.doAsync(successful: Consumer<T>,
                                        error: Consumer<Throwable> = onErrorConsumer,
                                        isShowProgress: Boolean = true) {
        preSubscribe(isShowProgress)
                .subscribe(successful, error)
                .addSubscription()
    }

    protected fun <T> Single<T>.doAsync(successful: MutableLiveData<T>,
                                        error: Consumer<Throwable> = onErrorConsumer,
                                        isShowProgress: Boolean = true) {
        preSubscribe(isShowProgress)
                .subscribe(Consumer { successful.value = it }, error)
                .addSubscription()
    }

    private fun <T> Single<T>.preSubscribe(isShowProgress: Boolean = true): Single<T> {
        setProgressVisibility(isShowProgress)
        return compose(ioToMainSingle()).doOnEvent { _, _ -> hideLoadingProgress() }
    }

    private fun <T> Flowable<T>.preSubscribe(isShowProgress: Boolean = true): Flowable<T> {
        setProgressVisibility(isShowProgress)
        return compose(ioToMainFlowable()).doOnEach { hideLoadingProgress() }
    }

    private fun setProgressVisibility(hideOrShowFlag: Boolean) {
        isLoadingLiveData.value = hideOrShowFlag
    }

    private fun clearSubscription() {
        compositeDisposable?.apply {
            if (!isDisposed) dispose()
            compositeDisposable = null
        }
    }

    private fun addBackgroundSubscription(subscription: Disposable) {
        compositeDisposable?.apply {
            add(subscription)
        } ?: let {
            compositeDisposable = CompositeDisposable()
            compositeDisposable?.add(subscription)
        }
    }

    private fun Disposable.addSubscription() = addBackgroundSubscription(this)

    fun <T> ioToMainSingle() = SingleTransformer<T, T> {
        it
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun <T> ioToMainFlowable() = FlowableTransformer<T, T> { inObservable ->
        inObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

}