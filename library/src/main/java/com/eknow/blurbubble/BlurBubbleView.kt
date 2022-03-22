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

        var topOffset = max(
            if (mArrowPosition + mArrowLength > mBottom) {
                mBottom - mArrowWidth
            } else {
                mArrowPosition
            },
            mShadowRadius
        )

        var leftOffset = max(
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
                // 判断是否足够画箭头，偏移的量 > 气泡圆角
                if (topOffset >= mLTR) {
                    mPath.moveTo(mLeft.toFloat(), (topOffset).toFloat())
                    mPath.rCubicTo(
                        0f,
                        0f,
                        -mArrowLength.toFloat(),
                        mArrowWidth / 2f,
                        -mArrowLength.toFloat(),
                        mArrowWidth / 2f
                    )
                } else {
                    // 起点移动到箭头尖
                    mPath.moveTo((mLeft - mArrowLength).toFloat(), topOffset + mArrowWidth / 2f)
                }

                // 判断是否足够画箭头，偏移的量 + 箭头宽 <= 气泡高 - 气泡圆角
                if (topOffset + mArrowWidth < mBottom - mLBR) {
                    mPath.rCubicTo(
                        0f, 0f,
                        mArrowLength.toFloat(), mArrowWidth / 2f,
                        mArrowLength.toFloat(), mArrowWidth / 2f
                    )
                    mPath.lineTo(mLeft.toFloat(), (mBottom - mLBR).toFloat())
                }
                mPath.quadTo(
                    mLeft.toFloat(), mBottom.toFloat(), (
                            mLeft + mLBR).toFloat(), mBottom.toFloat()
                )
                mPath.lineTo((mRight - mRBR).toFloat(), mBottom.toFloat())
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

            }
            ArrowAt.RIGHT -> {

            }
            ArrowAt.BOTTOM -> {

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
    private var mBubbleColor = Color.WHITE

    @ColorInt
    private var mBubbleBorderColor = Color.BLACK
    private var mBubbleBorderSize = 0
    private var mBubblePadding = 0
    private var mBubbleRadius = 0
    private var mLTR = 0
    private var mRTR = 0
    private var mLBR = 0
    private var mRBR = 0

    private var mArrowAt: ArrowAt = ArrowAt.LEFT
    private var mArrowPosition = 0
    private var mArrowWidth = 0
    private var mArrowLength = 0

    @ColorInt
    private var mShadowColor = Color.GRAY
    private var mShadowRadius = 0
    private var mShadowX = 0
    private var mShadowY = 0

    /**
     * 是否开启背景模糊效果
     */
    private var mOpenBlur = false

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
            mBubblePadding = getDimensionPixelOffset(R.styleable.BlurBubbleView_bbv_padding, 0)
            mBubbleRadius = getDimensionPixelOffset(R.styleable.BlurBubbleView_bbv_radius, 0)
            mLTR = getDimensionPixelOffset(R.styleable.BlurBubbleView_bbv_leftTopRadius, 0)
            mRTR = getDimensionPixelOffset(R.styleable.BlurBubbleView_bbv_rightTopRadius, 0)
            mLBR = getDimensionPixelOffset(R.styleable.BlurBubbleView_bbv_leftBottomRadius, 0)
            mRBR = getDimensionPixelOffset(R.styleable.BlurBubbleView_bbv_rightBottomRadius, 0)
            mArrowAt = when (getInt(R.styleable.BlurBubbleView_bbv_arrowAt, 0)) {
                1 -> ArrowAt.LEFT
                2 -> ArrowAt.TOP
                3 -> ArrowAt.RIGHT
                4 -> ArrowAt.BOTTOM
                else -> ArrowAt.LEFT
            }
            mArrowPosition =
                getDimensionPixelOffset(R.styleable.BlurBubbleView_bbv_arrowPosition, 0)
            mArrowWidth = getDimensionPixelOffset(R.styleable.BlurBubbleView_bbv_arrowWidth, 0)
            mArrowLength = getDimensionPixelOffset(R.styleable.BlurBubbleView_bbv_arrowLength, 0)
            mShadowColor = getColor(R.styleable.BlurBubbleView_bbv_shadowColor, Color.GRAY)
            mShadowRadius = getDimensionPixelOffset(R.styleable.BlurBubbleView_bbv_shadowRadius, 0)
            mShadowX = getDimensionPixelOffset(R.styleable.BlurBubbleView_bbv_shadowX, 0)
            mShadowY = getDimensionPixelOffset(R.styleable.BlurBubbleView_bbv_shadowY, 0)
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
        BOTTOM(4);
    }

    companion object {

    }
}