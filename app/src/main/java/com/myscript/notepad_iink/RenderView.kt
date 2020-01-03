package com.myscript.notepad_iink

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.myscript.iink.IRenderTarget
import com.myscript.iink.Renderer
import com.myscript.iink.uireferenceimplementation.LayerView
import java.util.*

class RenderView(context: Context) : FrameLayout(context), IRenderTarget {
    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int = 0) : this(context)

    override fun onFinishInflate() {
        super.onFinishInflate()
        for (i in 0 until childCount) {
            (getChildAt(i) as LayerView).setCustomTypefaces(App.typefaceMap)
        }
    }

    override fun invalidate(renderer: Renderer, layers: EnumSet<IRenderTarget.LayerType>) {
        invalidate(renderer, 0, 0, width, height, layers)
    }

    override fun invalidate(
        renderer: Renderer,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        layers: EnumSet<IRenderTarget.LayerType>
    ) {
        for (i in 0 until childCount) {
            val layerView = getChildAt(i) as LayerView
            if (layers.contains(layerView.type)) {
                layerView.update(renderer, x, y, width, height)
            }
        }
    }
}