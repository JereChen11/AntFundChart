[中文文档](https://github.com/JereChen11/AntFundChart/edit/main/README_CN.md)

# **Preface**

It is often said that if you don't manage your finances, your finances won't manage you. However, now that I have started managing my finances, it seems like my money has decided to leave me altogether, and I have become a helpless and inexperienced investor.

As I look at my lackluster investment portfolio, I find myself pondering: what can mutual funds really offer me? What can I learn from them?

Well, despite being an inexperienced investor, I am also a programmer. Looking at it from a coding perspective, at the very least, I can try to understand how the performance chart of this mutual fund is implemented. It will also give me an opportunity to practice my custom View skills.

So, for now, let's put aside the dilemma of whether to buy more and hold or to redeem and cut losses. Today, let's attempt to replicate the performance chart of this mutual fund.

Let's start by replicating this mutual fund's performance chart.

# **Presentation of Results**

Let's start by presenting the replicated results. For a better experience, I recommend downloading the APK. Click [here](https://github.com/JereChen11/AntFundChart/releases/tag/v1.0) to access the download page.

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
    var fundTypeYield: String,//同类平均收益率
    var indexYield: String,//沪深300收益率
    var pdate: String,//日期
    var yield: String//本基金收益率
)

data class TotalReturnRate(
    var totalYield: String,//本基金总收益率
    var totalIndexYield: String,//沪深300总收益率
    var totalFundTypeYield: String,//同类平均总收益率
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

![rect-distribution.png](%5BEnglish%20blog%5D%20AntFundChart%20a895393daf37432d98196b74ebb907e6/rect-distribution.png)

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

Also, if you think the article is good and helpful to you, please give it a thumbs-up as an encouragement. Thank you! Peace out! ✌️
>

# 前言
本项目来自于某天突发奇想，通过自定义View来复刻蚂蚁基金业绩走势图，分享给大家。

# 成果展示
<img src="https://github.com/JereChen11/AntFundChart/raw/main/image/fund_chart_result.gif" width="250">

# 具体实现

## 需求分析

分析一下图表中存在的元素，有：

*   横坐标：日期。
*   纵坐标：收益率。
*   走势线：分别代表着本基金、同类平均、沪深300的收益率走势线。
*   标签：蚂蚁基金。

这里我们可以使用`自定义Drawable`来将整个图表进行拆分，我们可以分为3层：

*   `FundGridDrawable`：用于绘制横坐标与纵坐标。
*   `RateLineDrawable`：用于绘制本基金、同类平均、沪深300的收益率走势线。
*   `FundLabelDrawable`：用于绘制标签。

然后按照绘制顺序进行逐层绘制。

1.  绘制`fundGridDrawable`。
2.  绘制`rateLineDrawable`。
3.  绘制`fundLabelDrawable`。

接着我们逐层进行实现。

## 区域划分

因为我们是分层绘制，所以我们需要按需求进行区域划分。

我们可以先确认好核心区域，也就是绘制走势线的区域，即`rateLineDrawable.bounds`。（这个核心区域，下文会用 `lineChartRect` 表示。）。确认好了核心区域后，我们就可以利用它来确认`fundGridDrawable.bounds`、`fundLabelDrawable.bounds`。

<img src="https://github.com/JereChen11/AntFundChart/raw/main/image/rect-distribution.png">

有了区域分布图以后，我们就可以通过代码来实现区域分配了。

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

区域分配好之后，我们就要来聊聊具体的绘制工作了。

## 绘制坐标轴

坐标轴这边分为横轴与竖轴，分别表示日期与收益率。

我们先来看横轴。

### 横轴

横轴表示日期，由三部分构成，分别是：

*   三个`”yyyy-MM-dd”`格式的日期：分别代表着该段周期基金的初始日期、末尾日期以及居中日期。位于 `lineChartRect` 最左边、中间以及最右边位置。
*   三根短小的竖线：以 `lineChartRect.bottom` 为起点，向下延伸`5dp`。同样位于 `lineChartRect` 最左边、中间以及最右边位置。
*   一根灰色的实线：跨度刚好是 `lineChartRect` 的宽度。

```kotlin
FundGridDrawable.kt

private fun drawDateTimeText(canvas: Canvas) {
    textPaint.getTextBounds(minDateTime, 0, minDateTime.length, textBoundsRect)

    dateTimeTextPxY = lineChartRect.bottom + textBoundsRect.height() + paddingBottom
    textPaint.textAlign = Paint.Align.LEFT
    //绘制初始日期
    canvas.drawText(
        minDateTime,
        lineChartRect.left.toFloat(),
        dateTimeTextPxY,
        textPaint
    )
    //绘制最左边竖线
    canvas.drawLine(
        lineChartRect.left.toFloat(),
        lineChartRect.bottom.toFloat(),
        lineChartRect.left.toFloat(),
        lineChartRect.bottom.toFloat() + paddingBottom / 2,
        bottomLinePaint
    )
	
    //...省略代码...

    //绘制横线
    canvas.drawLine(
        lineChartRect.left.toFloat(),
        lineChartRect.bottom.toFloat(),
        lineChartRect.right.toFloat(),
        lineChartRect.bottom.toFloat(),
        bottomLinePaint
    )
}
```

### 竖轴

竖轴表示收益率，由两部分构成，分别是：

*   收益率百分比文字：位于 `lineChartRect` 左边。
*   收益率虚线：跨度刚好是 `lineChartRect` 的宽度。

仔细观察了蚂蚁基金，我发现其将竖轴上的收益率**等分成4份**，也就是画5条线，且除了最上方的最高收益率线与最下方的最低收益率线，中间还要有一条0收益率线。还发现，虽然收益率百分比保留了两位小数，但都是0，也就是说等分的间距其实取整了。

所以在拿到接口返回的`List<DayRateDetail>`后，我们还应进行一番处理，从而得出绘制所需的真实收益率数据。

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

`maxRate` 与 `minRate` 分别表示真实接口返回的最大收益率与最小收益率，利用其差额来计算出`theRateRangeInterval`。接着以0位起点，遍历循环，向上向下添加收益率，以`maxRate`与`minRate`作为边界终止条件。

计算出真实的`rateAbscissaLines`后，我们就可以进行绘制了。

```kotlin
private var yPx = 0f
private fun drawRateTextAndLines(canvas: Canvas) {
    rateAbscissaLines.forEach {

        yPx = lineChartRect.top + (maxRate - it).div(yPxSpec).toFloat()

		//绘制收益率虚线
        canvas.drawLine(
            lineChartRect.left.toFloat(),
            yPx,
            lineChartRect.right.toFloat(),
            yPx,
            rateLinePaint
        )

		//绘制收益率百分比文字
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

## 绘制走势线

走势线一共有三条，分别是本基金走势线、同类平均走势线以及沪深300走势线。

线其实是由很多的点组成的，接口返回的`List<DayRateDetail>`列表中，每个元素其实就是线上的一个点。而点的定位，正是核心之处了🤔。

如果你阅读该文章是从上往下一步一步看下来的，那此刻你就知道，我们现在已经拿到了`lineChartRect` 与`收益率差额`这两个数据了。通过这两个数据，我们就可以计算出竖轴的像素规格了。

```kotlin
yPxSpec = (maxRate - minRate).div(lineChartRect.bounds.height())
```

而横轴的像素规格就更加简单了。

```kotlin
xPxSpec = lineChartRect.bounds.width().div(dayRateDetailList.size.toDouble())
```

通过 `xPxSpec` 与 `yPxSpec` 就可以很方便的完成点的定位啦。再通过`Path`将点连成线，就构成了走势线。

以本基金走势线为例。

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

## 绘制标签

标签的绘制就很简单了，这里我们取代`“蚂蚁基金”`，改为`“JC基金复刻”`。

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

# 总结

其实本文最主要的目的是练习自定义View，实现基金的业绩走势线不算复杂，但你想实现相同的效果，其实也不简单。我也只是起了个开头，如果你有兴趣，后期可以添加上动画，添加下单标记点，还可进一步进行自定义。

<br>到此文章就结束啦\~

> 其实分享的最大目的正是等待着有人指出我的错误，如果你发现哪里有错误，请毫无保留的指出即可，虚心请教。
>
> 另外，如果你觉得项目不错，对你有所帮助，请给我点个`star`，就当鼓励，谢谢～Peace✌️！

