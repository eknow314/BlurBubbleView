package com.eknow.blurbubble.demo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatImageView
import com.eknow.blurbubble.BlurBubbleView

class MainActivity : AppCompatActivity() {

    private var layout: FrameLayout? = null
    private var blurBubbleView: BlurBubbleView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        layout = findViewById(R.id.layout)
        blurBubbleView = findViewById(R.id.blurBubbleView)

        blurBubbleView?.blurredView = layout
    }

    override fun onDestroy() {
        blurBubbleView?.recycle()
        super.onDestroy()
    }
}