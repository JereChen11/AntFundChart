package com.jc.antfundchart.data

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * @author JereChen
 */
@Keep
data class FundReturnRateBean(
    @SerializedName("data")
    var dayRateList: List<DayRateDetail>,
    @SerializedName("total")
    var totalReturnRate: TotalReturnRate,
    var name: String,
    var success: Boolean,
    var totalCount: Int
)

@Keep
data class DayRateDetail(
    var benchQuote: String,//业绩比较基准收益率
    var fundTypeYield: String,//同类平均收益率
    var indexYield: String,//沪深300收益率
    var date: String,//日期
    var yield: String//本基金收益率
)

@Keep
data class TotalReturnRate(
    var totalYield: String,//本基金总收益率
    var totalIndexYield: String,//沪深300总收益率
    var totalFundTypeYield: String,//同类平均总收益率
    var totalBenchQuote: String
)