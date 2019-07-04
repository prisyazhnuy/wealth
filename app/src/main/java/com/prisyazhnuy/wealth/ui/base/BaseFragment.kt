package com.prisyazhnuy.wealth.ui.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.prisyazhnuy.wealth.EMPTY_STRING

abstract class BaseFragment<T : BaseViewModel> : Fragment() {

    companion object {
        const val NO_TOOLBAR = -1
        const val NO_TITLE = -1
        const val NO_TITLE_CUSTOM_TEXT = -2
        const val DEFAULT_NAVIGATION_ICON = -1
    }

    protected var toolbar: Toolbar? = null


    abstract val viewModelClass: Class<T>

    protected val viewModel: T by lazy {
        ViewModelProviders.of(this).get(viewModelClass).apply {
            this@BaseFragment.lifecycle.addObserver(this)
        }
    }

    protected abstract fun observeLiveData()

    protected abstract val layoutId: Int
    private var baseCallback: BaseView? = null

    override fun onResume() {
        super.onResume()
        initToolbar()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(layoutId, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeAllData()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        baseCallback = bindInterface(context)
    }

    private fun observeAllData() {
        observeLiveData()
        with(viewModel) {
            isLoadingLiveData.observe(this@BaseFragment, Observer<Boolean> { it?.let { baseCallback?.setProgressVisibility(it) } })
            errorLiveData.observe(this@BaseFragment, Observer<String> {
                it?.let {
                    onError(it)
                    errorLiveData.value = null
                }
            })
        }
    }

    override fun onDetach() {
        baseCallback = null
        super.onDetach()
    }

    protected fun setupActionBar(actionBar: ActionBar) {
        actionBar.apply {
            title = getStringScreenTitle()
            setDisplayHomeAsUpEnabled(needToShowBackNav())
        }
    }

    protected open fun needToShowBackNav() = true

    protected open fun getStringScreenTitle() =
            when (getScreenTitle()) {
                NO_TITLE -> EMPTY_STRING
                NO_TITLE_CUSTOM_TEXT -> getCustomTitle()
                else -> getString(getScreenTitle())
            }

    protected open fun getCustomTitle() = EMPTY_STRING

    @StringRes
    protected abstract fun getScreenTitle(): Int

    protected abstract fun hasToolbar(): Boolean

    @IdRes
    protected abstract fun getToolbarId(): Int

    @DrawableRes
    protected open fun getNavigationIconId() = DEFAULT_NAVIGATION_ICON

    protected fun initToolbar() {
        view?.apply {
            if (hasToolbar() && getToolbarId() != NO_TOOLBAR) {
                toolbar = findViewById(getToolbarId())
                with(activity as AppCompatActivity) {
                    if (getNavigationIconId() != DEFAULT_NAVIGATION_ICON) toolbar?.setNavigationIcon(getNavigationIconId())
                    setSupportActionBar(toolbar)
                    supportActionBar?.let {
                        setupActionBar(it)
                        if (needToShowBackNav()) {
                            toolbar?.setNavigationOnClickListener { handleNavigation() }
                        }
                    }
                }
            }
        }
    }

    protected open fun handleNavigation() = Unit

    protected open fun onError(error: String) {
        Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
    }

    protected fun setToolBarTitle() {
        toolbar?.title = getCustomTitle()
    }

    protected fun setToolBarSubtitle(subtitle: String?) {
        toolbar?.subtitle = subtitle
    }

    protected inline fun <reified T> bindInterface(vararg objects: Any?):
            T = objects.find { it is T } as T
}