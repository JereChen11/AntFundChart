# 前言
通过自定义View来复刻蚂蚁基金业绩走势图。

# 成果展示
<img src="https://github.com/JereChen11/AntFundChart/raw/main/images/fund_chart_result.gif">

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

<img src="https://github.com/JereChen11/AntFundChart/raw/main/images/rect-distribution.png">

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
		...省略代码...

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
> 另外，如果你觉得项目不错，对你有所帮助，请给我点个`star`，就当鼓励，谢谢～Peace！

