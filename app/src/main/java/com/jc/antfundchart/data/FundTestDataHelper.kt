package com.jc.antfundchart.data

import android.content.Context
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * @author JereChen
 */
class FundTestDataHelper(private val context: Context) {

    companion object {
        const val LATEST_ONE_MONTH_DATA = "latest_one_month_data.json"
        const val LATEST_THREE_MONTH_DATA = "latest_three_month_data.json"
        const val LATEST_SIX_MONTH_DATA = "latest_six_month_data.json"
        const val LATEST_THREE_YEARS_DATA = "latest_three_years_data.json"
        const val LATEST_ONE_YEAR_DATA = "latest_one_year_data.json"
    }

    suspend fun getFundReturnRateData(localJsonFileName: String = LATEST_ONE_YEAR_DATA): FundReturnRateBean {
        val localJsonString: String = withContext(Dispatchers.IO) {
            context.assets.open(localJsonFileName)
                .bufferedReader()
                .use {
                    it.readText()
                }
        }

        return Gson().fromJson(localJsonString, FundReturnRateBean::class.java)
    }
}