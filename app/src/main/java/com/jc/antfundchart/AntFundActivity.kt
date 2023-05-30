package com.jc.antfundchart

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.jc.antfundchart.data.FundTestDataHelper
import com.jc.antfundchart.data.FundTestDataHelper.Companion.LATEST_ONE_MONTH_DATA
import com.jc.antfundchart.data.FundTestDataHelper.Companion.LATEST_ONE_YEAR_DATA
import com.jc.antfundchart.data.FundTestDataHelper.Companion.LATEST_SIX_MONTH_DATA
import com.jc.antfundchart.data.FundTestDataHelper.Companion.LATEST_THREE_MONTH_DATA
import com.jc.antfundchart.data.FundTestDataHelper.Companion.LATEST_THREE_YEARS_DATA
import com.jc.antfundchart.data.TotalReturnRate
import com.jc.antfundchart.databinding.ActivityAntFundBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AntFundActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAntFundBinding
    private val fundTestDataHelper by lazy {
        FundTestDataHelper(this@AntFundActivity)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAntFundBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setRateData(isFirstInit = true)

        binding.apply {
            latestOneYear.isSelected = true

            latestOneMonth.setOnClickListener(dateRangeClickListener)
            latestThreeMonths.setOnClickListener(dateRangeClickListener)
            latestSixMonths.setOnClickListener(dateRangeClickListener)
            latestOneYear.setOnClickListener(dateRangeClickListener)
            latestThreeYears.setOnClickListener(dateRangeClickListener)
        }
    }

    private fun setRateData(
        localJsonFileName: String = LATEST_ONE_YEAR_DATA, isFirstInit: Boolean = false
    ) {
        lifecycleScope.launch(Dispatchers.IO) {
            if (isFirstInit) {
                delay(100)
            }
            fundTestDataHelper.getFundReturnRateData(localJsonFileName).apply {
                binding.returnOnFoundsChart.setData(dayRateList, isShowWithAnimator = true)

                withContext(Dispatchers.Main) {
                    setTotalRateData(totalReturnRate)
                }
            }
        }

    }

    private fun setTotalRateData(totalReturnRate: TotalReturnRate) {
        binding.apply {
            totalYieldTv.setTotalReturnRate(totalReturnRate.totalYield)
            totalFundTypeTv.setTotalReturnRate(totalReturnRate.totalFundTypeYield)
            totalIndexYieldTv.setTotalReturnRate(totalReturnRate.totalIndexYield)
        }
    }

    private val dateRangeClickListener by lazy {
        View.OnClickListener { v ->
            binding.apply {
                latestOneMonth.isSelected = false
                latestThreeMonths.isSelected = false
                latestSixMonths.isSelected = false
                latestOneYear.isSelected = false
                latestThreeYears.isSelected = false
                val localJsonFileName = when (v) {
                    latestOneMonth -> {
                        latestOneMonth.isSelected = true
                        LATEST_ONE_MONTH_DATA
                    }

                    latestThreeMonths -> {
                        latestThreeMonths.isSelected = true
                        LATEST_THREE_MONTH_DATA
                    }

                    latestSixMonths -> {
                        latestSixMonths.isSelected = true
                        LATEST_SIX_MONTH_DATA
                    }

                    latestOneYear -> {
                        latestOneYear.isSelected = true
                        LATEST_ONE_YEAR_DATA
                    }

                    latestThreeYears -> {
                        latestThreeYears.isSelected = true
                        LATEST_THREE_YEARS_DATA
                    }

                    else -> {
                        LATEST_ONE_YEAR_DATA
                    }
                }

                setRateData(localJsonFileName)

            }
        }
    }

    private fun TextView.setTotalReturnRate(totalReturn: String) {
        this.text = if (totalReturn.contains("-")) "$totalReturn%" else "+$totalReturn%"
        this.setTextColor(
            ContextCompat.getColor(
                this@AntFundActivity, if (totalReturn.contains("-")) {
                    R.color.fund_total_rate_down_text_color
                } else {
                    R.color.fund_total_rate_raise_text_color
                }
            )
        )
    }
}