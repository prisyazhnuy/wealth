package com.prisyazhnuy.wealth.ui.screen

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.prisyazhnuy.wealth.R
import com.prisyazhnuy.wealth.ui.base.BaseView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.include_progress.*

class MainActivity : AppCompatActivity(), BaseView {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initBottomNavigation()
    }

    private fun initBottomNavigation() {
        bottomNavigation.setOnNavigationItemSelectedListener { item ->
            true.apply {
                when (item.itemId) {
                    R.id.action_dashboard -> replaceFragment(WealthFragment.newInstance(), false)
                    else -> replaceFragment(Fragment(), false)
                }
            }
        }
        bottomNavigation.selectedItemId = R.id.action_dashboard
    }

    override fun setProgressVisibility(isVisible: Boolean) {
        progressContainer.visibility = View.VISIBLE.takeIf { isVisible } ?: View.GONE
    }

    private fun replaceFragment(fragment: Fragment, needToAddToBackStack: Boolean = true) {
        val name = fragment.javaClass.simpleName
        with(supportFragmentManager.beginTransaction()) {
            replace(R.id.container, fragment, name)
            if (needToAddToBackStack) {
                addToBackStack(name)
            }
            commit()
        }
    }
}
