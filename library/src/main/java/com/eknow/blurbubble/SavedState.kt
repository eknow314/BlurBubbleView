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
    var color = 0

    @JvmField
    var borderColor = 0

    constructor(superState: Parcelable?) : super(superState)

    private constructor(parcel: Parcel) : super(parcel) {
        color = parcel.readInt()

    }

    override fun writeToParcel(out: Parcel, flags: Int) {
        super.writeToParcel(out, flags)
        out.writeInt(color)

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