package com.prisyazhnuy.wealth.ui.screen

import android.graphics.Color
import android.graphics.DashPathEffect
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.github.mikephil.charting.components.Legend.LegendForm
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.prisyazhnuy.wealth.EMPTY_STRING
import com.prisyazhnuy.wealth.R
import com.prisyazhnuy.wealth.ui.base.BaseFragment
import com.prisyazhnuy.wealth.utils.getDisplayedFullDate
import kotlinx.android.synthetic.main.fragment_wealth.*
import org.joda.time.DateTime
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.*


class WealthFragment : BaseFragment<WealthVM>(),
        AdapterView.OnItemSelectedListener {

    companion object {
        fun newInstance() = WealthFragment().apply { arguments = Bundle() }
    }

    override val viewModelClass = WealthVM::class.java

    override val layoutId = R.layout.fragment_wealth
    override fun getScreenTitle() = NO_TITLE
    override fun hasToolbar() = true
    override fun getToolbarId() = R.id.toolbar
    override fun getCustomTitle() = title

    private var title = EMPTY_STRING
    private val categoryListener by lazy {
        object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val category = parent?.adapter?.getItem(position).toString()
                val (from, to) = getQuarterDates(spnPerformance.selectedItemPosition)

                viewModel.loadCategoryChange(category, from, to)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState).also {
            setupUI()
            if (savedInstanceState == null) viewModel.loadWealth()
        }
    }

    override fun observeLiveData() {
        with(viewModel) {
            wealthLD.observe(this@WealthFragment, Observer {
                val (from, to) = getQuarterDates(0)
                viewModel.loadByDate(from, to)

                //need to be start of current year
                setIncome(it.find { it.first?.isAfter(DateTime(2016, 1, 1, 0, 0)) == true },
                        it.findLast { it.first?.isBeforeNow == true } ?: it.lastOrNull())
                setTotalWorth(it.lastOrNull())
            })
            wealthQuarterLD.observe(this@WealthFragment, Observer {
                setData(it)
            })
            categoriesLD.observe(this@WealthFragment, Observer { setupCategory(it) })
            clientLD.observe(this@WealthFragment, Observer {
                title = it.clientName.orEmpty()
                setToolBarTitle()
            })
            categoryWealthQuarterLD.observe(this@WealthFragment, Observer { setChange(it.firstOrNull(), it.lastOrNull()) })
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) = Unit

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val (from, to) = getQuarterDates(position)
        viewModel.loadByDate(from, to)

        val category = spnCategory?.adapter?.getItem(spnCategory.selectedItemPosition).toString()
        viewModel.loadCategoryChange(category, from, to)
    }

    private fun setChange(startQuarter: Pair<DateTime?, BigDecimal>?, endQuarter: Pair<DateTime?, BigDecimal>?) {
        val startValue = startQuarter?.second ?: BigDecimal.ZERO
        val endValue = endQuarter?.second ?: BigDecimal.ZERO

        val income = endValue.minus(startValue)
        if (income > BigDecimal.ZERO) {
            tvChange.apply {
                setCompoundDrawablesWithIntrinsicBounds(null, null, resources.getDrawable(R.drawable.ic_arrow_upward_black_24dp, context.theme), null)
                val value = NumberFormat.getIntegerInstance(Locale.UK).format(income).replace(",", "'")
                text = "£$value"
                setTextColor(ContextCompat.getColor(context, R.color.green))
            }
        } else {
            tvChange.apply {
                setCompoundDrawablesWithIntrinsicBounds(null, null, resources.getDrawable(R.drawable.ic_arrow_downward_black_24dp, context.theme), null)
                val value = NumberFormat.getIntegerInstance(Locale.UK).format(income.multiply(BigDecimal(-1))).replace(",", "'")
                text = "£$value"
                setTextColor(ContextCompat.getColor(context, R.color.red))
            }
        }
    }

    private fun setTotalWorth(totalWorth: Pair<DateTime?, BigDecimal>?) {
        val (value, abbr) = accurateIncomeFormat(totalWorth?.second ?: BigDecimal.ZERO)
        tvTotalNetWorth.text = "£$value"
        tvTotalNetWorthAbbr.text = abbr
    }

    private fun setIncome(startQuarter: Pair<DateTime?, BigDecimal>?, endQuarter: Pair<DateTime?, BigDecimal>?) {
        val startValue = startQuarter?.second ?: BigDecimal.ZERO
        val endValue = endQuarter?.second ?: BigDecimal.ZERO

        val income = endValue.minus(startValue)
        if (income > BigDecimal.ZERO) {
            tvNetIncomeYTD.apply {
                setCompoundDrawablesWithIntrinsicBounds(resources.getDrawable(R.drawable.ic_arrow_upward_black_24dp, context.theme), null, null, null)
                val (value, abbr) = accurateIncomeFormat(income)
                text = value
                setTextColor(ContextCompat.getColor(context, R.color.green))
                tvNetIncomeYTDAbbr.text = abbr
            }
        } else {
            tvNetIncomeYTD.apply {
                setCompoundDrawablesWithIntrinsicBounds(resources.getDrawable(R.drawable.ic_arrow_downward_black_24dp, context.theme), null, null, null)
                val (value, abbr) = accurateIncomeFormat(income.multiply(BigDecimal(-1)))
                text = value
                setTextColor(ContextCompat.getColor(context, R.color.red))
                tvNetIncomeYTDAbbr.text = abbr
            }
        }
    }

    private fun setupUI() {
        context?.let { ctx ->
            ArrayAdapter.createFromResource(
                    ctx,
                    R.array.performance_array,
                    android.R.layout.simple_spinner_item)
                    .also { adapter ->
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spnPerformance.adapter = adapter
                        spnPerformance.onItemSelectedListener = this
                    }
            initChart()
        }
    }

    private fun setupCategory(categories: List<String>) {
        context?.let { ctx ->
            ArrayAdapter<String>(ctx, android.R.layout.simple_spinner_item, categories)
                    .also { adapter ->
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spnCategory.adapter = adapter
                        spnCategory.onItemSelectedListener = categoryListener
                    }
        }
    }

    private fun getQuarterDates(quarter: Int) = when (quarter) {
        0 -> {
            DateTime(2016, 1, 1, 0, 0) to
                    DateTime(2016, 3, 31, 23, 59)
        }
        1 -> {
            DateTime(2016, 4, 1, 0, 0) to
                    DateTime(2016, 6, 30, 23, 59)
        }
        2 -> {
            DateTime(2016, 7, 1, 0, 0) to
                    DateTime(2016, 9, 30, 23, 59)
        }
        else -> {
            DateTime(2016, 10, 1, 0, 0) to
                    DateTime(2016, 12, 31, 23, 59)
        }
    }

    private fun initChart() {
        chartWealth?.apply {
            setBackgroundColor(Color.WHITE)
            description.isEnabled = false
            setTouchEnabled(true)
            setDrawGridBackground(false)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)

            // // X-Axis Style // //
            xAxis.apply {
                granularity = 1000 * 60 * 60 * 24 * 31f
                setCenterAxisLabels(true)
                position = XAxis.XAxisPosition.BOTTOM
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return getDisplayedFullDate(DateTime(value.toLong()))
                    }
                }
            }

            axisLeft.isEnabled = false

            // horizontal grid lines
            axisRight.apply {
                enableGridDashedLine(10f, 10f, 0f)
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return when (value) {
                            in 1000F..999999F -> "${value.toInt() / 1000}k"
                            in 1000000F..999999999F -> "${value.toInt() / 1000000}m"
                            else -> value.toInt().toString()
                        }
                    }
                }
            }

            // draw points over time
            animateX(1500)
        }
    }

    private fun accurateIncomeFormat(value: BigDecimal) = when {
        value >= BigDecimal(1000) && value <= BigDecimal(999999) -> value.divide(BigDecimal(1000)) to "k"
        value >= BigDecimal(1000000) && value <= BigDecimal(999999999) -> value.divide(BigDecimal(1000000)) to "m"
        else -> value to ""
    }.run {
        NumberFormat.getInstance().apply { maximumFractionDigits = 1 }.format(first) to second
    }

    private fun setData(data: List<Pair<DateTime?, BigDecimal>>) {
        val values = data.map { Entry(it.first?.millis?.toFloat() ?: 0.0f, it.second.toFloat()) }

        if (chartWealth.data != null && chartWealth.data.dataSetCount > 0) {
            (chartWealth.data.getDataSetByIndex(0) as LineDataSet).apply {
                this.values = values
                notifyDataSetChanged()
                chartWealth.data.notifyDataChanged()
                chartWealth.notifyDataSetChanged()
                chartWealth.invalidate()
            }
        } else {
            // create a dataset and give it a type
            LineDataSet(values, "").apply {
                setDrawValues(false)
                setDrawCircleHole(false)
                setDrawCircles(false)
                setDrawIcons(false)

                // draw dashed line
                disableDashedLine()

                // black lines and points
                color = Color.BLACK

                // line thickness and point size
                lineWidth = 1f

                // customize legend entry
                formLineWidth = 1f
                formLineDashEffect = DashPathEffect(floatArrayOf(10f, 5f), 0f)
                formSize = 15f

                // text size of values
                valueTextSize = 9f

                // draw selection line as dashed
                enableDashedHighlightLine(10f, 5f, 0f)

                // set the filled area
                setDrawFilled(true)

                context?.let { ctx ->
                    fillDrawable = ContextCompat.getDrawable(ctx, R.drawable.fade_red)
                }

                val dataSets = mutableListOf<LineDataSet>()
                dataSets.add(this) // add the data sets

                // create a data object with the data sets
                val lineData = LineData(dataSets as? List<ILineDataSet>?)

                // set data
                chartWealth.data = lineData

                // get the legend (only possible after setting data)
                val l = chartWealth.legend
                l.form = LegendForm.NONE
            }
        }
    }

}