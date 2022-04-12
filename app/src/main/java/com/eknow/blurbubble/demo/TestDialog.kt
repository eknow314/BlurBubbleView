package com.eknow.blurbubble.demo

import android.app.Activity
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import com.eknow.blurbubble.BlurBubbleDialog
import com.eknow.blurbubble.BlurBubbleView

/**
 * @Description:
 * @author: Eknow
 * @date: 2022/4/12 17:41
 */
class TestDialog(activity: Activity, view: View) : BlurBubbleDialog(activity) {

    init {
        setDialogPosition<TestDialog>(Position.TOP)

        val bubbleView = BlurBubbleView(activity)
        bubbleView.setGradientColor(Color.parseColor("#065FB3"), Color.parseColor("#138DAF"))
        setBubbleLayout<TestDialog>(bubbleView)

        val rootView: View = LayoutInflater.from(context).inflate(R.layout.dialog_clean_way, null)
        setDialogContentView<TestDialog>(rootView)

        setClickedView<TestDialog>(view)

    }


}