package com.eknow.blurbubble

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView

/**
 * @Description: 显示个气泡弹窗，自动确定箭头位置
 * @author: Eknow
 * @date: 2022/4/12 14:52
 */
open class BlurBubbleDialog @JvmOverloads constructor(
    activity: Activity,
    themeResId: Int = R.style.blur_bubble_dialog
) : Dialog(activity, themeResId) {

    private var mActivity: Activity
    private val mStatusBarHeight: Int

    private lateinit var mBubbleLayout: BlurBubbleView
    private var mAddView: View? = null
    private var mWidth = 0
    private var mHeight = 0
    private var mMargin = 0
    private var mPosition: Position = Position.TOP //弹窗位置

    private var mSoftShowUp = false
    private var mClickedRect: Rect? = null
    private var mOffsetX = 0
    private var mOffsetY = 0
    private var mRelativeOffset = 0
    private val clickedViewLocation = IntArray(2)
    private var mOnGlobalLayoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null


    /**
     * 设置弹窗内容视图
     */
    fun <T : BlurBubbleDialog> setDialogContentView(view: View): T {
        mAddView = view
        return this as T
    }

    /**
     * 设置弹窗位置
     */
    fun <T : BlurBubbleDialog> setDialogPosition(position: Position): T {
        mPosition = position
        return this as T
    }

    /**
     * 设置被点击的 view 来设置弹窗弹出位置
     */
    fun <T : BlurBubbleDialog> setClickedView(view: View): T {
        mClickedRect = Rect(0, 0, view.width, view.height)
        view.getLocationOnScreen(clickedViewLocation)
        handleGlobalLayoutListener()
        return this as T
    }

    /**
     * 设置宽高
     */
    fun <T : BlurBubbleDialog> setLayout(width: Int, height: Int, margin: Int): T {
        mWidth = width
        mHeight = height
        mMargin = margin
        return this as T
    }

    /**
     * 设置x方向偏移量
     */
    fun <T : BlurBubbleDialog> setOffsetX(offsetX: Int): T {
        mOffsetX = offsetX
        return this as T
    }

    /**
     * 设置y方向偏移量
     */
    fun <T : BlurBubbleDialog?> setOffsetY(offsetY: Int): T {
        mOffsetY = offsetY
        return this as T
    }

    /**
     * 设置dialog相对与被点击View的偏移
     */
    fun <T : BlurBubbleDialog> setRelativeOffset(relativeOffset: Int): T {
        mRelativeOffset = relativeOffset
        return this as T
    }

    /**
     * 自定义气泡布局
     */
    fun <T : BlurBubbleDialog> setBubbleLayout(bl: BlurBubbleView): T {
        mBubbleLayout = bl
        return this as T
    }

    /**
     * 背景全透明
     */
    fun <T : BlurBubbleDialog> setTransParentBackground(): T {
        val window = window ?: return this as T
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        return this as T
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!this::mBubbleLayout.isInitialized) {
            mBubbleLayout = BlurBubbleView(mActivity)
        }
        if (mAddView != null) {
            mBubbleLayout.addView(mAddView)
        }
        setContentView(mBubbleLayout)

        val window = window ?: return
        // 当软件盘弹出时Dialog上移
        if (mSoftShowUp) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        }
        window.setLayout(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        mBubbleLayout.measure(0, 0)
        setArrowAt()
        dialogPosition()

        mOnGlobalLayoutListener = object : ViewTreeObserver.OnGlobalLayoutListener {
            var lastWidth = 0
            var lastHeight = 0
            override fun onGlobalLayout() {
                if (lastWidth == mBubbleLayout.measuredWidth && lastHeight == mBubbleLayout.measuredHeight) {
                    return
                }
                dialogPosition()
                lastWidth = mBubbleLayout.measuredWidth
                lastHeight = mBubbleLayout.measuredHeight
            }
        }
        mBubbleLayout.viewTreeObserver.addOnGlobalLayoutListener(mOnGlobalLayoutListener)

    }

    /**
     * 设置箭头方位
     */
    private fun setArrowAt() {
        when (mPosition) {
            Position.LEFT -> mBubbleLayout.arrowAt = BlurBubbleView.ArrowAt.RIGHT
            Position.TOP -> mBubbleLayout.arrowAt = BlurBubbleView.ArrowAt.BOTTOM
            Position.RIGHT -> mBubbleLayout.arrowAt = BlurBubbleView.ArrowAt.LEFT
            Position.BOTTOM -> mBubbleLayout.arrowAt = BlurBubbleView.ArrowAt.TOP
        }
    }

    /**
     * 计算弹窗位置和箭头所在边的位置
     */
    private fun dialogPosition() {
        if (mClickedRect == null) {
            return
        }
        val window = window ?: return
        window.setGravity(Gravity.LEFT or Gravity.TOP)
        val params = window.attributes
        if (mWidth != 0) {
            params.width = mWidth
        }
        if (mHeight != 0) {
            params.height = mHeight
        }
        if (mMargin != 0) {
            val bubbleParams = mBubbleLayout.layoutParams as FrameLayout.LayoutParams?
            if (mPosition == Position.TOP || mPosition == Position.BOTTOM) {
                bubbleParams!!.leftMargin = mMargin
                bubbleParams.rightMargin = mMargin
            } else {
                bubbleParams!!.topMargin = mMargin
                bubbleParams.bottomMargin = mMargin
            }
            mBubbleLayout.layoutParams = bubbleParams
        }
        when (mPosition) {
            Position.TOP, Position.BOTTOM -> {
                params.x =
                    clickedViewLocation[0] + mClickedRect!!.width() / 2 - mBubbleLayout.measuredWidth / 2 + mOffsetX

                if (mMargin != 0 && mWidth == ViewGroup.LayoutParams.MATCH_PARENT) {
                    mBubbleLayout.arrowPosition =
                        clickedViewLocation[0] - mMargin + mClickedRect!!.width() / 2 - mBubbleLayout.arrowWidth / 2
                } else if (params.x <= 0) {
                    mBubbleLayout.arrowPosition =
                        clickedViewLocation[0] + mClickedRect!!.width() / 2 - mBubbleLayout.arrowWidth / 2
                } else if (params.x + mBubbleLayout.measuredWidth > getScreenWH(context)[0]) {
                    mBubbleLayout.arrowPosition =
                        clickedViewLocation[0] - (getScreenWH(context)[0] - mBubbleLayout.measuredWidth) + mClickedRect!!.width() / 2 - mBubbleLayout.arrowWidth / 2
                } else {
                    mBubbleLayout.arrowPosition =
                        clickedViewLocation[0] - params.x + mClickedRect!!.width() / 2 - mBubbleLayout.arrowWidth / 2
                }

                if (mPosition == Position.BOTTOM) {
                    // 弹窗在下面
                    if (mRelativeOffset != 0) {
                        mOffsetY = mRelativeOffset
                    }
                    params.y =
                        clickedViewLocation[1] + mClickedRect!!.height() + mOffsetY - mStatusBarHeight
                } else {
                    if (mRelativeOffset != 0) {
                        mOffsetY = -mRelativeOffset
                    }
                    params.y =
                        clickedViewLocation[1] - mBubbleLayout.measuredHeight + mOffsetY - mStatusBarHeight
                }
            }
            Position.LEFT, Position.RIGHT -> {
                params.y =
                    clickedViewLocation[1] + mOffsetY + mClickedRect!!.height() / 2 - mBubbleLayout.measuredHeight / 2 - mStatusBarHeight

                if (mMargin != 0 && mHeight == ViewGroup.LayoutParams.MATCH_PARENT) {
                    mBubbleLayout.arrowPosition =
                        clickedViewLocation[1] - mMargin + mClickedRect!!.height() / 2 - mBubbleLayout.arrowWidth / 2 - mStatusBarHeight
                } else if (params.y <= 0) {
                    mBubbleLayout.arrowPosition =
                        clickedViewLocation[1] + mClickedRect!!.height() / 2 - mBubbleLayout.arrowWidth / 2 - mStatusBarHeight
                } else if (params.y + mBubbleLayout.measuredHeight > getScreenWH(context)[1]) {
                    mBubbleLayout.arrowPosition =
                        clickedViewLocation[1] - (getScreenWH(context)[1] - mBubbleLayout.measuredHeight) + mClickedRect!!.height() / 2 - mBubbleLayout.arrowWidth / 2
                } else {
                    mBubbleLayout.arrowPosition =
                        clickedViewLocation[1] - params.y + mClickedRect!!.height() / 2 - mBubbleLayout.arrowWidth / 2 - mStatusBarHeight
                }

                if (mPosition == Position.RIGHT) {
                    if (mRelativeOffset != 0) {
                        mOffsetX = mRelativeOffset
                    }
                    params.x = clickedViewLocation[0] + mClickedRect!!.width() + mOffsetX
                } else {
                    if (mRelativeOffset != 0) {
                        mOffsetX = -mRelativeOffset
                    }
                    params.x = clickedViewLocation[0] - mBubbleLayout.measuredWidth + mOffsetX
                }
            }
        }
        mBubbleLayout.invalidate()
        window.attributes = params
    }

    private fun handleGlobalLayoutListener() {
        if (mOnGlobalLayoutListener != null) {
            setArrowAt()
            dialogPosition()
        }
    }

    override fun dismiss() {
        if (mSoftShowUp) {
            hide(this)
        }
        mBubbleLayout.viewTreeObserver.removeOnGlobalLayoutListener(mOnGlobalLayoutListener)
        super.dismiss()
    }

    /**
     * 获取状态栏高度
     */
    private fun getStatusBarByTop(activity: Activity): Int {
        val rect = Rect()
        activity.window.decorView.getWindowVisibleDisplayFrame(rect)
        return rect.top
    }


    /**
     * 获取屏幕宽高
     */
    private fun getScreenWH(context: Context): IntArray {
        val manager =
            context.applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val outMetrics = DisplayMetrics()
        manager.defaultDisplay.getMetrics(outMetrics)
        return intArrayOf(outMetrics.widthPixels, outMetrics.heightPixels)
    }

    /**
     * 隐藏软键盘
     */
    private fun hide(dialog: Dialog) {
        val view = dialog.currentFocus
        if (view is TextView) {
            val mInputMethodManager =
                dialog.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            mInputMethodManager.hideSoftInputFromWindow(
                view.getWindowToken(),
                InputMethodManager.RESULT_UNCHANGED_SHOWN
            )
        }
    }

    init {
        setCancelable(true)
        mActivity = activity
        mStatusBarHeight = getStatusBarByTop(mActivity)
    }

    /**
     * 气泡位置
     */
    enum class Position {
        LEFT,
        TOP,
        RIGHT,
        BOTTOM
    }
}