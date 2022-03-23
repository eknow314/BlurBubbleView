package com.eknow.blurbubble

import android.os.Parcel
import android.os.Parcelable
import android.view.View.BaseSavedState

/**
 * @Description:
 * @author: Eknow
 * @date: 2022/3/22 16:26
 */
class SavedState : BaseSavedState {

    @JvmField
    var bubbleColor = 0

    @JvmField
    var bubbleBorderColor = 0

    @JvmField
    var bubbleBorderSize = 0

    @JvmField
    var bubblePadding = 0

    @JvmField
    var bubbleRadius = 0

    @JvmField
    var lTR = 0

    @JvmField
    var rTR = 0

    @JvmField
    var lBR = 0

    @JvmField
    var rBR = 0

    @JvmField
    var arrowAt = 0

    @JvmField
    var arrowPosition = 0

    @JvmField
    var arrowWidth = 0

    @JvmField
    var arrowLength = 0

    @JvmField
    var shadowColor = 0

    @JvmField
    var shadowRadius = 0

    @JvmField
    var shadowX = 0

    @JvmField
    var shadowY = 0

    @JvmField
    var openBlur = 0

    @JvmField
    var blurRadius = 0

    constructor(superState: Parcelable?) : super(superState)

    private constructor(parcel: Parcel) : super(parcel) {
        bubbleColor = parcel.readInt()
        bubbleBorderColor = parcel.readInt()
        bubbleBorderSize = parcel.readInt()
        bubblePadding = parcel.readInt()
        bubbleRadius = parcel.readInt()
        lTR = parcel.readInt()
        rTR = parcel.readInt()
        lBR = parcel.readInt()
        rBR = parcel.readInt()
        arrowAt = parcel.readInt()
        arrowPosition = parcel.readInt()
        arrowWidth = parcel.readInt()
        arrowLength = parcel.readInt()
        shadowColor = parcel.readInt()
        shadowRadius = parcel.readInt()
        shadowX = parcel.readInt()
        shadowY = parcel.readInt()
        openBlur = parcel.readInt()
        blurRadius = parcel.readInt()
    }

    override fun writeToParcel(out: Parcel, flags: Int) {
        super.writeToParcel(out, flags)
        out.writeInt(bubbleColor)
        out.writeInt(bubbleBorderColor)
        out.writeInt(bubbleBorderSize)
        out.writeInt(bubblePadding)
        out.writeInt(bubbleRadius)
        out.writeInt(lTR)
        out.writeInt(rTR)
        out.writeInt(lBR)
        out.writeInt(rBR)
        out.writeInt(arrowAt)
        out.writeInt(arrowPosition)
        out.writeInt(arrowWidth)
        out.writeInt(arrowLength)
        out.writeInt(shadowColor)
        out.writeInt(shadowRadius)
        out.writeInt(shadowX)
        out.writeInt(shadowY)
        out.writeInt(openBlur)
        out.writeInt(blurRadius)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SavedState> {
        override fun createFromParcel(parcel: Parcel): SavedState {
            return SavedState(parcel)
        }

        override fun newArray(size: Int): Array<SavedState?> {
            return arrayOfNulls(size)
        }
    }
}