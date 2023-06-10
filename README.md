[中文文档](https://github.com/JereChen11/AntFundChart/edit/main/README_CN.md)

# **Preface**

It is often said that if you don't manage your finances, your finances won't manage you. However, now that I have started managing my finances, it seems like my money has decided to leave me altogether, and I have become a helpless and inexperienced investor.

As I look at my lackluster investment portfolio, I find myself pondering: what can mutual funds really offer me? What can I learn from them?

Well, despite being an inexperienced investor, I am also a programmer. Looking at it from a coding perspective, at the very least, I can try to understand how the performance chart of this mutual fund is implemented. It will also give me an opportunity to practice my custom View skills.

So, for now, let's put aside the dilemma of whether to buy more and hold or to redeem and cut losses. Today, let's attempt to replicate the performance chart of this mutual fund.

Let's start by replicating this mutual fund's performance chart.

# **Presentation of Results**

Let's start by presenting the replicated results. For a better experience, I recommend downloading the APK. Click [here](https://github.com/JereChen11/AntFundChart/releases/tag/v1.0) to access the download page.

<img src="https://github.com/JereChen11/AntFundChart/raw/main/image/fund_chart_result.gif" width="250">


# **Fetching Data**

In order to replicate the same effect, we need to have the data. Here, we can fetch the data through an API to obtain JSON data for the past 1 month, 3 months, and 1 year.

The content format is as follows:

```kotlin
{
  "data": [
    {
      "date": "2023-03-01",
      "yield": "-0.23",
      "indexYield": "1.41",
      "fundTypeYield": "0.86",
      "benchQuote": "0.01"
    },
    {
      "date": "2023-03-02",
      "yield": "-1.17",
      "indexYield": "1.19",
      "fundTypeYield": "0.17",
      "benchQuote": "-0.64"
    },
    //... omit some code ...
  ],
  "total": {
    "totalYield": "-5.10",
    "totalIndexYield": "-0.46",
    "totalFundTypeYield": "-1.46",
    "totalBenchQuote": "-2.31"
  },
  "success": true,
  "totalCount": 24,
  "name": "xxxx"
}
```

The data is primarily divided into two parts:

1. A set of lists containing the daily yield:
    - **`date`**: representing the date.
    - **`yield`**: representing the daily yield of this fund.
    - **`indexYield`**: representing the daily yield of the Shanghai and Shenzhen 300 Index.
    - **`fundTypeYield`**: representing the daily yield of the average fund in the same category.
2. Overall yield data for the given period:
    - **`totalYield`**: representing the total yield of this fund.
    - **`totalIndexYield`**: representing the total yield of the Shanghai and Shenzhen 300 Index.
    - **`totalFundTypeYield`**: representing the total yield of the average fund in the same category.

Next, we can convert the JSON data into corresponding bean files.

```kotlin
data class FundReturnRateBean(
    @SerializedName("data")
    var dayRateList: List<DayRateDetail>,
    @SerializedName("total")
    var totalReturnRate: TotalReturnRate,
    var name: String,
    var success: Boolean,
    var totalCount: Int
)

data class DayRateDetail(
    var benchQuote: String,
    var fundTypeYield: String,//average return of the same category
    var indexYield: String,//rate of return of the Shanghai-Shenzhen 300
    var pdate: String,//date
    var yield: String//fund rate
)

data class TotalReturnRate(
    var totalYield: String,//total fund return
    var totalIndexYield: String,//total Shanghai-Shenzhen 300 return
    var totalFundTypeYield: String,//total average return of the same category
    var totalBenchQuote: String
)
```

With the data, we can implement the function by hand🧑‍💻.

# **Implementation Details**

## **Requirements Analysis**

Let's analyze the elements present in the chart:

- X-axis: Dates.
- Y-axis: Yield.
- Trend lines: Representing the yield trends of the fund, average fund, and Shanghai and Shenzhen 300 Index.
- Label: Ant Fund.

To achieve this, we can use custom Drawables to divide the chart into three layers:

- **`FundGridDrawable`**: Used to draw the x-axis and y-axis.
- **`RateLineDrawable`**: Used to draw the yield trend lines of the fund, average fund, and Shanghai and Shenzhen 300 Index.
- **`FundLabelDrawable`**: Used to draw the label.

We'll proceed with drawing each layer in the following order:

1. Draw **`fundGridDrawable`**.
2. Draw **`rateLineDrawable`**.
3. Draw **`fundLabelDrawable`**.

Now, let's implement each layer step by step.

## **Area Partitioning**

Since we are drawing in layers, we need to partition the area based on the requirements.

First, we need to determine the core area, which is the area where the trend lines will be drawn (**`rateLineDrawable.bounds`**). We can refer to this area as **`lineChartRect`**. Once we have determined the core area, we can use it to determine the bounds of **`fundGridDrawable`** and **`fundLabelDrawable`**.

<img src="https://github.com/JereChen11/AntFundChart/raw/main/image/rect-distribution.png">

With the area distribution map, we can implement area allocation through code.

```kotlin
private var chartRect = Rect()
private val defaultPadding = 5f.px.toInt()
private val paddingTop = 15f.px.toInt()
private val paddingBottom = 25f.px.toInt()
private val paddingStart = 60f.px.toInt()
private val paddingEnd = 20f.px.toInt()
private val labelWidth = 35f.px.toInt()
private val labelHeight = 100f.px.toInt()

override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
    super.onSizeChanged(w, h, oldw, oldh)
    chartRect = Rect(0, 0, w, h)
    rateLineDrawable.bounds = Rect(
        paddingStart,
        chartRect.top + paddingTop,
        chartRect.right - paddingEnd,
        chartRect.bottom - paddingBottom
    )
    fundGridDrawable.bounds = Rect(
        chartRect.left + defaultPadding,
        rateLineDrawable.bounds.top - defaultPadding,
        rateLineDrawable.bounds.right,
        chartRect.bottom - defaultPadding
    )
    fundGridDrawable.lineChartRect = rateLineDrawable.bounds

    fundLabelDrawable.bounds = Rect(
        rateLineDrawable.bounds.left,
        rateLineDrawable.bounds.bottom - labelWidth,
        rateLineDrawable.bounds.left + labelHeight,
        rateLineDrawable.bounds.bottom
    )
}
```

After the area is allocated, we will talk about the specific drawing work.

## **Drawing the Axes**

The axes can be divided into the x-axis and y-axis, representing the dates and yields, respectively.

Let's start with the x-axis.

### **X-Axis**

The x-axis represents the dates and consists of three parts:

- Three dates in the format of **`"yyyy-MM-dd"`**: They represent the initial date, end date, and middle date of the fund's time period. These dates are positioned at the leftmost, middle, and rightmost positions of **`lineChartRect`**.
- Three short vertical lines: Starting from **`lineChartRect.bottom`** and extending downwards by **`5dp`**. These lines are also positioned at the leftmost, middle, and rightmost positions of **`lineChartRect`**.
- One solid gray line: Its width spans the entire width of **`lineChartRect`**.

```kotlin
FundGridDrawable.kt

private fun drawDateTimeText(canvas: Canvas) {
    textPaint.getTextBounds(minDateTime, 0, minDateTime.length, textBoundsRect)

    dateTimeTextPxY = lineChartRect.bottom + textBoundsRect.height() + paddingBottom
    textPaint.textAlign = Paint.Align.LEFT
    //draw initial date
    canvas.drawText(
        minDateTime,
        lineChartRect.left.toFloat(),
        dateTimeTextPxY,
        textPaint
    )
    //draw the leftmost vertical line
    canvas.drawLine(
        lineChartRect.left.toFloat(),
        lineChartRect.bottom.toFloat(),
        lineChartRect.left.toFloat(),
        lineChartRect.bottom.toFloat() + paddingBottom / 2,
        bottomLinePaint
    )
    //... omit some code ...

    //draw horizontal line
    canvas.drawLine(
        lineChartRect.left.toFloat(),
        lineChartRect.bottom.toFloat(),
        lineChartRect.right.toFloat(),
        lineChartRect.bottom.toFloat(),
        bottomLinePaint
    )
}
```

### **Y-Axis**

The y-axis represents the yields and consists of two parts:

- Yield percentage text: Positioned on the left side of **`lineChartRect`**.
- Yield dashed lines: Its width spans the entire width of **`lineChartRect`**.

After carefully observing the Ant Fund chart, I noticed that the yield on the y-axis is divided into **four equal parts**, which means there are five lines to be drawn. Apart from the top line representing the highest yield and the bottom line representing the lowest yield, there is also a middle line representing 0 yield. Additionally, I noticed that even though the yield percentages have two decimal places, they are all 0, which means the intervals between them are whole numbers.

Therefore, after obtaining the **`List<DayRateDetail>`** from the API, we should process it to obtain the actual yield data required for drawing.

```kotlin
theRateRangeInterval = (maxRate - minRate).div(3).roundToInt()
private fun calcRateAbscissa(): MutableList<Int> {
    val rateAbscissaLines = mutableListOf<Int>()

    if (theRateRangeInterval == 0) {
        return rateAbscissaLines
    }

    rateAbscissaLines.clear()
    rateAbscissaLines.add(0)

    for (i in 1..5) {
        rateAbscissaLines.add(theRateRangeInterval * i)
        if (theRateRangeInterval * i > maxRate) {
            break
        }
    }

    for (i in 1..5) {
        rateAbscissaLines.add(-theRateRangeInterval * i)
        if (-theRateRangeInterval * i < minRate) {
            break
        }
    }

    rateAbscissaLines.sort()
    Log.e(TAG, "calcRateAbscissa: after sort rateAbscissaLines = $rateAbscissaLines")
    return rateAbscissaLines
}
```

**`maxRate`** and **`minRate`** represent the maximum and minimum yield values returned by the API, respectively. We can use the difference between them to calculate **`theRateRangeInterval`**. Starting from 0, we can iterate through a loop, adding yield values upwards and downwards, with **`maxRate`** and **`minRate`** as the termination conditions.

Once we have calculated the actual **`rateAbscissaLines`**, we can proceed with the drawing.

```kotlin
private var yPx = 0f
private fun drawRateTextAndLines(canvas: Canvas) {
    rateAbscissaLines.forEach {

        yPx = lineChartRect.top + (maxRate - it).div(yPxSpec).toFloat()

	//draw the dotted line of the rate of return
        canvas.drawLine(
            lineChartRect.left.toFloat(),
            yPx,
            lineChartRect.right.toFloat(),
            yPx,
            rateLinePaint
        )

	//draw the percent text of the rate of return
        textPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText(
            "${it.toStringAsFixed(2)}%",
            lineChartRect.left.toFloat() - 10f.px,
            yPx + 5f.px,
            textPaint
        )
    }
}
```

## **Drawing the Trend Lines**

There are three trend lines to be drawn: the fund's trend line, the average fund's trend line, and the Shanghai and Shenzhen 300 Index's trend line.

A line is composed of multiple points. Each element in the **`List<DayRateDetail>`** returned by the API represents a point on the line. The positioning of these points is crucial 🤔.

If you have been reading this article from top to bottom, you now know that we have obtained the **`lineChartRect`** and the yield difference data. With these two pieces of data, we can calculate the pixel specifications for the y-axis.

```kotlin
yPxSpec = (maxRate - minRate).div(lineChartRect.bounds.height())
```

The pixel specifications for the x-axis are relatively straightforward.

```kotlin
xPxSpec = lineChartRect.bounds.width().div(dayRateDetailList.size.toDouble())
```

With **`xPxSpec`** and **`yPxSpec`**, we can easily determine the position of each point. Then, we can use a **`Path`** to connect the points and form the trend line.

Let's take the fund's trend line as an example.

```kotlin
private var x = 0f
private var yYield = 0f
private fun calcData() {
    yieldLinePath.reset()

    dayRateDetailList.forEachIndexed { index, dayRateDetail ->
        x = bounds.left + index.times(xPxSpec).toFloat()
        yYield = bounds.top + (maxRate - dayRateDetail.yield.toDouble()).div(yPxSpec).toFloat()

        if (index == 0) {
            yieldLinePath.moveTo(x, yYield)
        } else {
            yieldLinePath.lineTo(x, yYield)
        }
    }
}
```

## **Drawing the Label**

Drawing the label is relatively simple. Here, we will replace "Ant Fund" with "JC基金复刻" for the label.

```kotlin
private fun drawLabelTag(canvas: Canvas) {
    canvas.drawText(
        "JC基金复刻",
        bounds.left.toFloat(),
        bounds.bottom - paddingBottom,
        labelTextPaint
    )
}
```

# **Conclusion**

The main purpose of this article is actually to practice custom View and implementing the fund's performance trend line. While it's not overly complex to achieve the fund's performance trend line, it's not necessarily easy to replicate the same effect. What I have provided is just a starting point. If you're interested, you can further enhance it by adding animations, marker points for transactions, or exploring more customizations.

The article primarily shares the implementation principles. If you want to see all the code mentioned in the text, please check out my GitHub project **[AntFundChart](https://link.juejin.cn/?target=https%3A%2F%2Fgithub.com%2FJereChen11%2FAntFundChart)**. Creating and sharing content requires effort, so if this article has been helpful to you, I would appreciate it if you could give it a star. Thank you very much! 🙏

The article concludes here👻.

> In fact, the main purpose of sharing articles is to wait for someone to point out any mistakes. If you find any errors, please feel free to point them out. I'm open to learning from others.
>
> Also, if you think the article is good and helpful to you, please give it a thumbs-up as an encouragement. Thank you! Peace out! ✌️


