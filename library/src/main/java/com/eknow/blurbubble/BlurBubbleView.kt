package com.eknow.blurbubble

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
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

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawPath(mPath, mPaint)
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

    @ColorInt
    private var mBubbleColor: Int

    @ColorInt
    private var mBubbleBorderColor: Int
    private var mBubbleBorderSize: Int
    private var mBubblePadding: Int
    private var mBubbleRadius: Int
    private var mLTR: Int
        get() = if (field == -1) mBubbleRadius else field
    private var mRTR: Int
        get() = if (field == -1) mBubbleRadius else field
    private var mLBR: Int
        get() = if (field == -1) mBubbleRadius else field
    private var mRBR: Int
        get() = if (field == -1) mBubbleRadius else field

    private var mArrowAt: ArrowAt
    private var mArrowPosition: Int
    private var mArrowWidth: Int
    private var mArrowLength: Int

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
            mArrowAt = when (getInt(R.styleable.BlurBubbleView_bbv_arrowAt, 0)) {
                1 -> ArrowAt.LEFT
                2 -> ArrowAt.TOP
                3 -> ArrowAt.RIGHT
                4 -> ArrowAt.BOTTOM
                else -> ArrowAt.LEFT
            }
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

            recycle()
        }

        initPadding()
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

    companion object {

    }
}