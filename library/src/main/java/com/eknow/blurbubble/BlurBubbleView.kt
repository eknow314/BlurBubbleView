package com.eknow.blurbubble

import android.content.Context
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.os.Parcelable
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import kotlin.math.max


/**
 * @Description: 背景虚化的气泡布局
 * @author: Eknow
 * @date: 2022/3/22 15:27
 */
class BlurBubbleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    /**
     * 气泡区域字段
     */
    private var mPath: Path
    private var mPaint: Paint
    private var mBubbleBorderPaint: Paint

    private var mWidth = 0
    private var mHeight = 0
    private var mLeft = 0
    private var mTop = 0
    private var mRight = 0
    private var mBottom = 0

    /**
     * RenderScript
     */
    private var mRenderScript: RenderScript? = null
    private var mBlurScript: ScriptIntrinsicBlur? = null
    private val mDownSampleFactor = 8

    /**
     * 被模糊的背景字段
     */
    private var mBlurBgDstRectF: RectF
    private var mBlurBgBeforePaint: Paint
    private var mBlurBgPaint: Paint

    private var mBlurredView: View? = null
    private var mBlurredViewWidth = 0
    private var mBlurredViewHeight = 0

    private var mBlurringCanvas: Canvas? = null
    private var mBitmapToBlur: Bitmap? = null
    private var mBlurredBitmap: Bitmap? = null
    private var mBlurInput: Allocation? = null
    private var mBlurOutput: Allocation? = null

    /**
     * 计算参数，构造绘制路径数据
     */
    private fun calculateData() {
        mPaint.setShadowLayer(
            mShadowRadius.toFloat(),
            mShadowX.toFloat(),
            mShadowY.toFloat(),
            mShadowColor
        )
        mPaint.color = mBubbleColor

        mBubbleBorderPaint.color = mBubbleBorderColor
        mBubbleBorderPaint.strokeWidth = mBubbleBorderSize.toFloat()
        mBubbleBorderPaint.style = Paint.Style.STROKE

        mLeft =
            mShadowRadius + (if (mShadowX < 0) -mShadowX else 0) + if (mArrowAt == ArrowAt.LEFT) mArrowLength else 0
        mTop =
            mShadowRadius + (if (mShadowY < 0) -mShadowY else 0) + if (mArrowAt == ArrowAt.TOP) mArrowLength else 0
        mRight =
            mWidth - mShadowRadius + (if (mShadowX > 0) -mShadowX else 0) - if (mArrowAt == ArrowAt.RIGHT) mArrowLength else 0
        mBottom =
            mHeight - mShadowRadius + (if (mShadowY > 0) -mShadowY else 0) - if (mArrowAt == ArrowAt.BOTTOM) mArrowLength else 0

        mPath.reset()

        // 顶部偏移量
        val topOffset = max(
            if (mArrowPosition + mArrowLength > mBottom) {
                mBottom - mArrowWidth
            } else {
                mArrowPosition
            },
            mShadowRadius
        )
        // 左侧偏移量
        val leftOffset = max(
            if (mArrowPosition + mArrowLength > mRight) {
                mRight - mArrowWidth
            } else {
                mArrowPosition
            },
            mShadowRadius
        )

        // 根据箭头所在位置，计算箭头位置和形状
        when (mArrowAt) {
            ArrowAt.LEFT -> {
                // 绘制箭头上半部，判断是否足够画箭头，偏移的量 > 气泡圆角
                if (topOffset >= mLTR) {
                    mPath.moveTo(mLeft.toFloat(), topOffset.toFloat())
                    mPath.rCubicTo(
                        0f,
                        0f,
                        -mArrowLength.toFloat(),
                        mArrowWidth / 2f,
                        -mArrowLength.toFloat(),
                        mArrowWidth / 2f
                    )
                } else {
                    // 将起点移动到箭头尖
                    mPath.moveTo((mLeft - mArrowLength).toFloat(), topOffset + mArrowWidth / 2f)
                }
                // 绘制箭头下半部，判断是否足够画箭头，偏移的量 + 箭头宽 <= 气泡高 - 气泡圆角
                if (topOffset + mArrowWidth < mBottom - mLBR) {
                    mPath.rCubicTo(
                        0f,
                        0f,
                        mArrowLength.toFloat(),
                        mArrowWidth / 2f,
                        mArrowLength.toFloat(),
                        mArrowWidth / 2f
                    )
                    mPath.lineTo(mLeft.toFloat(), (mBottom - mLBR).toFloat())
                }
                // 绘制左下圆角
                mPath.quadTo(
                    mLeft.toFloat(),
                    mBottom.toFloat(),
                    (mLeft + mLBR).toFloat(),
                    mBottom.toFloat()
                )
                mPath.lineTo((mRight - mRBR).toFloat(), mBottom.toFloat())
                // 绘制右下圆角
                mPath.quadTo(
                    mRight.toFloat(),
                    mBottom.toFloat(),
                    mRight.toFloat(),
                    (mBottom - mRBR).toFloat()
                )
                mPath.lineTo(mRight.toFloat(), (mTop + mRTR).toFloat())
                // 绘制右上圆角
                mPath.quadTo(
                    mRight.toFloat(),
                    mTop.toFloat(),
                    (mRight - mRTR).toFloat(),
                    mTop.toFloat()
                )
                mPath.lineTo((mLeft + mLTR).toFloat(), mTop.toFloat())
                // 绘制左上圆角
                if (topOffset >= mLTR) {
                    mPath.quadTo(
                        mLeft.toFloat(),
                        mTop.toFloat(),
                        mLeft.toFloat(),
                        (mTop + mLTR).toFloat()
                    )
                } else {
                    mPath.quadTo(
                        mLeft.toFloat(),
                        mTop.toFloat(),
                        (mLeft - mArrowLength).toFloat(),
                        topOffset + mArrowWidth / 2f
                    )
                }
            }
            ArrowAt.TOP -> {
                if (leftOffset >= mLTR) {
                    mPath.moveTo(leftOffset.toFloat(), mTop.toFloat())
                    mPath.rCubicTo(
                        0f,
                        0f,
                        mArrowWidth / 2f,
                        -mArrowLength.toFloat(),
                        mArrowWidth / 2f,
                        -mArrowLength.toFloat()
                    )
                } else {
                    mPath.moveTo(leftOffset + mArrowWidth / 2f, (mTop - mArrowLength).toFloat())
                }

                if (leftOffset + mArrowWidth < mRight - mRTR) {
                    mPath.rCubicTo(
                        0f,
                        0f,
                        mArrowWidth / 2f,
                        mArrowLength.toFloat(),
                        mArrowWidth / 2f,
                        mArrowLength.toFloat()
                    )
                    mPath.lineTo((mRight - mRTR).toFloat(), mTop.toFloat())
                }
                mPath.quadTo(
                    mRight.toFloat(),
                    mTop.toFloat(),
                    mRight.toFloat(),
                    (mTop + mRTR).toFloat()
                )
                mPath.lineTo(mRight.toFloat(), (mBottom - mRBR).toFloat())
                mPath.quadTo(
                    mRight.toFloat(),
                    mBottom.toFloat(),
                    (mRight - mRBR).toFloat(),
                    mBottom.toFloat()
                )
                mPath.lineTo((mLeft + mLBR).toFloat(), mBottom.toFloat())
                mPath.quadTo(
                    mLeft.toFloat(),
                    mBottom.toFloat(),
                    mLeft.toFloat(),
                    (mBottom - mLBR).toFloat()
                )
                mPath.lineTo(mLeft.toFloat(), (mTop + mLTR).toFloat())
                if (leftOffset >= mLTR) {
                    mPath.quadTo(
                        mLeft.toFloat(),
                        mTop.toFloat(),
                        (mLeft + mLTR).toFloat(),
                        mTop.toFloat()
                    )
                } else {
                    mPath.quadTo(
                        mLeft.toFloat(),
                        mTop.toFloat(),
                        leftOffset + mArrowWidth / 2f,
                        (mTop - mArrowLength).toFloat()
                    )
                }
            }
            ArrowAt.RIGHT -> {
                if (topOffset >= mRTR) {
                    mPath.moveTo(mRight.toFloat(), topOffset.toFloat())
                    mPath.rCubicTo(
                        0f,
                        0f,
                        mArrowLength.toFloat(),
                        mArrowWidth / 2f,
                        mArrowLength.toFloat(),
                        mArrowWidth / 2f
                    )
                } else {
                    mPath.moveTo((mRight + mArrowLength).toFloat(), topOffset + mArrowWidth / 2f)
                }

                if (topOffset + mArrowWidth < mBottom - mRBR) {
                    mPath.rCubicTo(
                        0f,
                        0f,
                        -mArrowLength.toFloat(),
                        mArrowWidth / 2f,
                        -mArrowLength.toFloat(),
                        mArrowWidth / 2f
                    )
                    mPath.lineTo(mRight.toFloat(), (mBottom - mRBR).toFloat())
                }
                mPath.quadTo(
                    mRight.toFloat(),
                    mBottom.toFloat(),
                    (mRight - mRBR).toFloat(),
                    mBottom.toFloat()
                )
                mPath.lineTo((mLeft + mLBR).toFloat(), mBottom.toFloat())
                mPath.quadTo(
                    mLeft.toFloat(),
                    mBottom.toFloat(),
                    mLeft.toFloat(),
                    (mBottom - mLBR).toFloat()
                )
                mPath.lineTo(mLeft.toFloat(), (mTop + mLTR).toFloat())
                mPath.quadTo(
                    mLeft.toFloat(),
                    mTop.toFloat(),
                    (mLeft + mLTR).toFloat(),
                    mTop.toFloat()
                )
                mPath.lineTo((mRight - mRTR).toFloat(), mTop.toFloat())
                if (topOffset >= mRTR) {
                    mPath.quadTo(
                        mRight.toFloat(),
                        mTop.toFloat(),
                        mRight.toFloat(),
                        (mTop + mRTR).toFloat()
                    )
                } else {
                    mPath.quadTo(
                        mRight.toFloat(),
                        mTop.toFloat(),
                        (mRight + mArrowLength).toFloat(),
                        topOffset + mArrowWidth / 2f
                    )
                }
            }
            ArrowAt.BOTTOM -> {
                if (leftOffset >= mLBR) {
                    mPath.moveTo(leftOffset.toFloat(), mBottom.toFloat())
                    mPath.rCubicTo(
                        0f,
                        0f,
                        mArrowWidth / 2f,
                        mArrowLength.toFloat(),
                        mArrowWidth / 2f,
                        mArrowLength.toFloat()
                    )
                } else {
                    mPath.moveTo(leftOffset + mArrowWidth / 2f, (mBottom + mArrowLength).toFloat())
                }

                if (leftOffset + mArrowWidth < mRight - mRBR) {
                    mPath.rCubicTo(
                        0f,
                        0f,
                        mArrowWidth / 2f,
                        -mArrowLength.toFloat(),
                        mArrowWidth / 2f,
                        -mArrowLength.toFloat()
                    )
                    mPath.lineTo((mRight - mRBR).toFloat(), mBottom.toFloat())
                }
                mPath.quadTo(
                    mRight.toFloat(),
                    mBottom.toFloat(),
                    mRight.toFloat(),
                    (mBottom - mRBR).toFloat()
                )
                mPath.lineTo(mRight.toFloat(), (mTop + mRTR).toFloat())
                mPath.quadTo(
                    mRight.toFloat(),
                    mTop.toFloat(),
                    (mRight - mRTR).toFloat(),
                    mTop.toFloat()
                )
                mPath.lineTo((mLeft + mLTR).toFloat(), mTop.toFloat())
                mPath.quadTo(
                    mLeft.toFloat(),
                    mTop.toFloat(),
                    mLeft.toFloat(),
                    (mTop + mLTR).toFloat()
                )
                mPath.lineTo(mLeft.toFloat(), (mBottom - mLBR).toFloat())
                if (leftOffset >= mLBR) {
                    mPath.quadTo(
                        mLeft.toFloat(),
                        mBottom.toFloat(),
                        (mLeft + mLBR).toFloat(),
                        mBottom.toFloat()
                    )
                } else {
                    mPath.quadTo(
                        mLeft.toFloat(),
                        mBottom.toFloat(),
                        leftOffset + mArrowWidth / 2f,
                        (mBottom + mArrowLength).toFloat()
                    )
                }
            }
        }

        mPath.close()
    }

    /**
     * 背景模糊参数准备，获取模糊的 bitmap，成功返回 true 再进行画板绘制
     */
    private fun blurPrepare(): Boolean {
        val width = mBlurredView!!.width
        val height = mBlurredView!!.height

        if (mBlurringCanvas == null || mBlurredViewWidth != width || mBlurredViewHeight != height) {
            mBlurredViewWidth = width
            mBlurredViewHeight = height

            var scaledWidth: Int = width / mDownSampleFactor
            var scaledHeight: Int = height / mDownSampleFactor

            scaledWidth = scaledWidth - scaledWidth % 4 + 4
            scaledHeight = scaledHeight - scaledHeight % 4 + 4

            if (mBlurredBitmap == null || mBlurredBitmap?.width != scaledWidth || mBlurredBitmap?.height != scaledHeight) {
                mBitmapToBlur = Bitmap.createBitmap(
                    scaledWidth,
                    scaledHeight,
                    Bitmap.Config.ARGB_8888
                )
                if (mBitmapToBlur == null) {
                    return false
                }
                mBlurredBitmap = Bitmap.createBitmap(
                    scaledWidth,
                    scaledHeight,
                    Bitmap.Config.ARGB_8888
                )
                if (mBlurredBitmap == null) {
                    return false
                }
            }

            mBlurringCanvas = Canvas(mBitmapToBlur!!)
            mBlurringCanvas?.scale(1f / mDownSampleFactor, 1f / mDownSampleFactor)
            mBlurInput = Allocation.createFromBitmap(
                mRenderScript,
                mBitmapToBlur,
                Allocation.MipmapControl.MIPMAP_NONE,
                Allocation.USAGE_SCRIPT
            )
            mBlurOutput = Allocation.createTyped(mRenderScript, mBlurInput?.type)
        }

        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // 绘制模糊背景
        if (mOpenBlur && mBlurredView != null) {
            if (blurPrepare()) {
                mBlurredView?.let { view ->
                    if (view.background != null && view.background is ColorDrawable) {
                        mBitmapToBlur?.eraseColor((view.background as ColorDrawable).color)
                    } else {
                        mBitmapToBlur?.eraseColor(Color.TRANSPARENT)
                    }

                    view.draw(mBlurringCanvas)
                    mBlurInput?.copyFrom(mBitmapToBlur)
                    mBlurScript?.setInput(mBlurInput)
                    mBlurScript?.forEach(mBlurOutput)
                    mBlurOutput?.copyTo(mBlurredBitmap)

                    mBlurredBitmap?.let { bitmap ->
                        canvas.save()
                        mPath.computeBounds(mBlurBgDstRectF, true)
                        val layer = canvas.saveLayer(mBlurBgDstRectF, null)
                        canvas.drawPath(mPath, mBlurBgBeforePaint)
                        canvas.translate(view.x - x, view.y - y)
                        canvas.scale(mDownSampleFactor.toFloat(), mDownSampleFactor.toFloat())
                        canvas.drawBitmap(bitmap, 0f, 0f, mBlurBgPaint)
                        canvas.restoreToCount(layer)
                    }

                }
            }
        }
        // 绘制基础气泡
        canvas.drawPath(mPath, mPaint)
        // 绘制气泡边框
        if (mBubbleBorderSize != 0) {
            canvas.drawPath(mPath, mBubbleBorderPaint)
        }
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mWidth = w
        mHeight = h
        calculateData()
    }

    override fun invalidate() {
        calculateData()
        super.invalidate()
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        val ss = SavedState(superState)
        ss.bubbleColor = mBubbleColor
        ss.bubbleBorderColor = mBubbleBorderColor
        ss.bubbleBorderSize = mBubbleBorderSize
        ss.bubblePadding = mBubblePadding
        ss.bubbleRadius = mBubbleRadius
        ss.lTR = mLTR
        ss.rTR = mRTR
        ss.lBR = mLBR
        ss.rBR = mRBR
        ss.arrowAt = getArrowAtValue(mArrowAt)
        ss.arrowPosition = mArrowPosition
        ss.arrowWidth = mArrowWidth
        ss.arrowLength = mArrowLength
        ss.shadowColor = mShadowColor
        ss.shadowRadius = mShadowRadius
        ss.shadowX = mShadowX
        ss.shadowY = mShadowY
        ss.openBlur = if (mOpenBlur) 1 else 0
        ss.blurRadius = mBlurRadius
        return ss
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }
        val ss: SavedState = state
        super.onRestoreInstanceState(ss.superState)
        mBubbleColor = ss.bubbleColor
        mBubbleBorderColor = ss.bubbleBorderColor
        mBubbleBorderSize = ss.bubbleBorderSize
        mBubblePadding = ss.bubblePadding
        mBubbleRadius = ss.bubbleRadius
        mLTR = ss.lTR
        mRTR = ss.rTR
        mLBR = ss.lBR
        mRBR = ss.rBR
        mArrowAt = getArrowAt(ss.arrowAt)
        mArrowPosition = ss.arrowPosition
        mArrowWidth = ss.arrowWidth
        mArrowLength = ss.arrowLength
        mShadowColor = ss.shadowColor
        mShadowRadius = ss.shadowRadius
        mShadowX = ss.shadowX
        mShadowY = ss.shadowY
        mOpenBlur = ss.openBlur == 1
        mBlurRadius = ss.blurRadius
    }

    /**
     * 内存回收
     */
    fun recycle() {
        mRenderScript = null
        mBlurScript = null
        mBlurringCanvas = null
        mBitmapToBlur?.let {
            if (!it.isRecycled) {
                it.recycle()
            }
        }
        mBlurredBitmap?.let {
            if (!it.isRecycled) {
                it.recycle()
            }
        }
        mBlurInput?.destroy()
        mBlurOutput?.destroy()
    }


    /**
     * 需要被模糊的背景
     */
    var blurredView: View?
        get() = mBlurredView
        set(value) {
            mBlurredView = value
            invalidate()
        }

    /**
     * 模糊程度
     */
    var blurRadius: Int
        get() = mBlurRadius
        set(value) {
            mBlurRadius = value
            mBlurScript?.setRadius(mBlurRadius.toFloat())
            invalidate()
        }

    /**
     * 气泡颜色
     */
    @ColorInt
    private var mBubbleColor: Int

    /**
     * 气泡边框属性
     */
    @ColorInt
    private var mBubbleBorderColor: Int
    private var mBubbleBorderSize: Int

    /**
     * 气泡内容区域 padding
     */
    private var mBubblePadding: Int

    /**
     * 气泡圆角属性
     */
    private var mBubbleRadius: Int
    private var mLTR: Int
        get() = if (field == -1) mBubbleRadius else field
    private var mRTR: Int
        get() = if (field == -1) mBubbleRadius else field
    private var mLBR: Int
        get() = if (field == -1) mBubbleRadius else field
    private var mRBR: Int
        get() = if (field == -1) mBubbleRadius else field

    /**
     * 箭头属性
     */
    private var mArrowAt: ArrowAt
    private var mArrowPosition: Int
    private var mArrowWidth: Int
    private var mArrowLength: Int

    /**
     * 边框阴影属性
     */
    @ColorInt
    private var mShadowColor: Int
    private var mShadowRadius: Int
    private var mShadowX: Int
    private var mShadowY: Int

    /**
     * 是否开启背景模糊效果
     */
    private var mOpenBlur: Boolean

    /**
     * 背景模糊程度
     */
    private var mBlurRadius: Int

    /**
     * 初始化气泡框内容 padding 大小
     */
    private fun initPadding() {
        val p = mBubblePadding + mShadowRadius
        when (mArrowAt) {
            ArrowAt.BOTTOM -> setPadding(
                p,
                p,
                p + mShadowX,
                mArrowLength + p + mShadowY
            )
            ArrowAt.TOP -> setPadding(
                p,
                p + mArrowLength,
                p + mShadowX,
                p + mShadowY
            )
            ArrowAt.LEFT -> setPadding(
                p + mArrowLength,
                p,
                p + mShadowX,
                p + mShadowY
            )
            ArrowAt.RIGHT -> setPadding(
                p,
                p,
                p + mArrowLength + mShadowX,
                p + mShadowY
            )
        }
    }

    /**
     * 初始化模糊背景工具脚本
     */
    private fun initRenderScript() {
        mRenderScript = RenderScript.create(context)
        mBlurScript = ScriptIntrinsicBlur.create(mRenderScript, Element.U8_4(mRenderScript))
        mBlurScript?.setRadius(mBlurRadius.toFloat())
    }

    private fun dp2px(dp: Float): Int {
        val scale = resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        setWillNotDraw(false)
        mPath = Path()
        mPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG)
        mPaint.style = Paint.Style.FILL
        mBubbleBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG)
        mBlurBgDstRectF = RectF()
        mBlurBgBeforePaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG)
        mBlurBgPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG)
        mBlurBgPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)

        context.obtainStyledAttributes(attrs, R.styleable.BlurBubbleView).apply {
            mBubbleColor = getColor(R.styleable.BlurBubbleView_bbv_color, Color.WHITE)
            mBubbleBorderColor = getColor(R.styleable.BlurBubbleView_bbv_borderColor, Color.BLACK)
            mBubbleBorderSize =
                getDimensionPixelOffset(R.styleable.BlurBubbleView_bbv_borderSize, 0)
            mBubblePadding =
                getDimensionPixelOffset(R.styleable.BlurBubbleView_bbv_padding, dp2px(10f))
            mBubbleRadius =
                getDimensionPixelOffset(R.styleable.BlurBubbleView_bbv_radius, dp2px(10f))
            mLTR = getDimensionPixelOffset(R.styleable.BlurBubbleView_bbv_leftTopRadius, -1)
            mRTR = getDimensionPixelOffset(R.styleable.BlurBubbleView_bbv_rightTopRadius, -1)
            mLBR = getDimensionPixelOffset(R.styleable.BlurBubbleView_bbv_leftBottomRadius, -1)
            mRBR = getDimensionPixelOffset(R.styleable.BlurBubbleView_bbv_rightBottomRadius, -1)
            mArrowAt = getArrowAt(getInt(R.styleable.BlurBubbleView_bbv_arrowAt, 0))
            mArrowPosition =
                getDimensionPixelOffset(R.styleable.BlurBubbleView_bbv_arrowPosition, dp2px(30f))
            mArrowWidth =
                getDimensionPixelOffset(R.styleable.BlurBubbleView_bbv_arrowWidth, dp2px(14f))
            mArrowLength =
                getDimensionPixelOffset(R.styleable.BlurBubbleView_bbv_arrowLength, dp2px(12f))
            mShadowColor = getColor(R.styleable.BlurBubbleView_bbv_shadowColor, Color.GRAY)
            mShadowRadius =
                getDimensionPixelOffset(R.styleable.BlurBubbleView_bbv_shadowRadius, dp2px(5f))
            mShadowX = getDimensionPixelOffset(R.styleable.BlurBubbleView_bbv_shadowX, dp2px(1f))
            mShadowY = getDimensionPixelOffset(R.styleable.BlurBubbleView_bbv_shadowY, dp2px(1f))
            mOpenBlur = getBoolean(R.styleable.BlurBubbleView_bbv_blur, false)
            mBlurRadius = getInt(R.styleable.BlurBubbleView_bbv_blurRadius, 15)

            recycle()
        }

        initPadding()

        if (mOpenBlur) {
            initRenderScript()
        }
    }

    /**
     * 箭头所在边的位置
     */
    enum class ArrowAt(var value: Int) {
        LEFT(1),
        TOP(2),
        RIGHT(3),
        BOTTOM(4)
    }

    private fun getArrowAtValue(type: ArrowAt) = when (type) {
        ArrowAt.LEFT -> 1
        ArrowAt.TOP -> 2
        ArrowAt.RIGHT -> 3
        ArrowAt.BOTTOM -> 4
    }

    private fun getArrowAt(value: Int) = when (value) {
        1 -> ArrowAt.LEFT
        2 -> ArrowAt.TOP
        3 -> ArrowAt.RIGHT
        4 -> ArrowAt.BOTTOM
        else -> ArrowAt.LEFT
    }

}