# BlurBubbleView
背景虚化的气泡布局，继承 FrameLayout，自定义气泡样式，可以开启背景模糊效果，

android 12 之后背景模糊可以使用 [RenderEffect](https://developer.android.google.cn/reference/android/graphics/RenderEffect?hl=en)

[![](https://jitpack.io/v/eknow314/BlurBubbleView.svg)](https://jitpack.io/#eknow314/BlurBubbleView)


### 依赖配置

```groovy
allprojects {
    repositories {
        maven { url 'https://www.jitpack.io' }
    }
}

dependencies {
    implementation 'com.github.eknow314:BlurBubbleView:+'
}
```

#### 如果需要使用背景模糊效果，在项目的`defaultConfig`里面打开 renderscript
```groovy
android {
    defaultConfig {

        renderscriptTargetApi 21
        renderscriptSupportModeEnabled true
        
    }
}
```

---

### 基本使用

布局代码
```
<com.eknow.blurbubble.BlurBubbleView
        android:id="@+id/blurBubbleView"
        android:layout_width="200dp"
        android:layout_height="150dp"
        android:layout_marginTop="30dp"
        app:bbv_arrowAt="left"
        app:bbv_arrowLength="15dp"
        app:bbv_arrowPosition="25dp"
        app:bbv_arrowWidth="10dp"
        app:bbv_blur="false"
        app:bbv_blurRadius="2"
        app:bbv_color="#3D0067CA"
        app:bbv_gradientOrientation="horizontal"
        app:bbv_gradientColor1="#CC138DAF"
        app:bbv_gradientColor0="#CC065FB3"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent" />
```

如果需要开启背景模糊效果，设置
```
app:bbv_blur="true"
```
并且获取需要背景模糊的布局层，再讲layout设置为被虚化对象
``` java
blurBubbleView?.blurredView = layout
```
注意，这里需要 layout 的宽高不小于 blurBubbleView 的宽高


---

### 全部属性

| 属性 | 功能 | 默认值 |
| --- | --- | --- |
| bbv_color | 气泡颜色 | Color.WHITE |
| bbv_borderColor | 边框颜色 | Color.BLACK |
| bbv_borderSize | 边框大小 | 0dp |
| bbv_padding | 气泡内容区域边距 | 10dp |
| bbv_radius | 气泡圆角 | 10dp |
| bbv_leftTopRadius | 气泡左上圆角 | 默认跟随 bbv_radius |
| bbv_rightTopRadius | 气泡右上圆角 | 同上 |
| bbv_leftBottomRadius | 气泡左下圆角 | 同上 |
| bbv_rightBottomRadius | 气泡右下圆角 | 同上 |
| bbv_arrowAt | 箭头所在边 | left |
| bbv_arrowPosition | 箭头在边上的绝对位置 | 30dp |
| bbv_arrowWidth | 箭头在边上的宽度，平行于边的宽度 | 14dp |
| bbv_arrowLength | 箭头在边上的长度，垂直于边的长度 | 12dp |
| bbv_shadowColor | 气泡阴影颜色 | Color.GRAY |
| bbv_shadowRadius | 气泡阴影圆角 | 5dp |
| bbv_shadowX | 气泡阴影 X 轴偏移量 | 1dp |
| bbv_shadowY | 气泡阴影 Y 轴偏移量 | 1dp |
| bbv_gradientOrientation | 气泡渐变方向 | horizontal |
| bbv_gradientColor0 | 气泡渐变颜色0 | Color.TRANSPARENT |
| bbv_gradientColor1 | 气泡渐变颜色1 | Color.TRANSPARENT |
| bbv_blur | 气泡是否开启背景模糊效果 | false |
| bbv_blurRadius | 气泡背景模糊程度，最大 25 | 15 |
