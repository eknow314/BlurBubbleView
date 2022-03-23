package com.eknow.blurbubble.demo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatImageView
import com.eknow.blurbubble.BlurBubbleView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val layout: FrameLayout = findViewById(R.id.layout)
        val blurBubbleView: BlurBubbleView = findViewById(R.id.blurBubbleView)

        blurBubbleView.blurredView = layout
    }
}