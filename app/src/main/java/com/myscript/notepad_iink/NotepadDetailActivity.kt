package com.myscript.notepad_iink

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.myscript.iink.*
import com.myscript.iink.uireferenceimplementation.FontMetricsProvider
import io.woodemi.notepad.*
import java.io.File
import java.util.*
import kotlin.math.min

const val widthDpi = 2610F
const val width = 14800
const val heightDpi = 2540F
const val height = 21000
const val MAX_PRESSURE = 512

const val EXTRA_SCAN_RESULT = "SCAN_RESULT"

private const val TAG = "NotepadDetailActivity"

class NotepadDetailActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var scanResult: NotepadScanResult

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notepad_detail)
        findViewById<Button>(R.id.connect).setOnClickListener(this)
        findViewById<Button>(R.id.set_mode).setOnClickListener(this)
        findViewById<Button>(R.id.disconnect).setOnClickListener(this)

        scanResult = intent.getParcelableExtra(EXTRA_SCAN_RESULT)

        NotepadConnector.callback = connectorCallback

        openIInk()
    }

    override fun onDestroy() {
        super.onDestroy()
        closeIInk()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.connect -> NotepadConnector.connect(this, scanResult)
            R.id.set_mode -> {
                notepadClient?.setMode(
                    NotepadMode.Sync,
                    { Log.d(TAG, "setMode complete") },
                    { Log.d(TAG, "setMode error $it") }
                )
            }
            R.id.disconnect -> NotepadConnector.disconnect()
        }
    }

    private var notepadClient: NotepadClient? = null

    private val connectorCallback = object : NotepadConnectorCallback {
        override fun onConnectionStateChange(
            notepadClient: NotepadClient,
            state: ConnectionState
        ) {
            when (state) {
                is ConnectionState.Disconnected -> {
                    this@NotepadDetailActivity.notepadClient?.callback = null
                    this@NotepadDetailActivity.notepadClient = null
                }
                is ConnectionState.Connected -> {
                    this@NotepadDetailActivity.notepadClient = notepadClient
                    this@NotepadDetailActivity.notepadClient?.callback = clientCallback
                }
            }
            runOnUiThread {
                Toast.makeText(this@NotepadDetailActivity, "$state", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val clientCallback = object : NotepadClient.Callback {
        var prePointer: NotePenPointer? = null

        override fun handleEvent(message: NotepadMessage) {
            Log.d(TAG, "handleEvent $message")
        }

        override fun handlePointer(list: List<NotePenPointer>) {
            val viewScale = renderer.viewScale
            for (pointer in list) {
                val pre = prePointer?.p ?: 0
                when {
                    pre <= 0 && pointer.p > 0 -> {
                        editor.pointerDown(
                            pointer.x * viewScale, pointer.y * viewScale,
                            -1, (pointer.p / MAX_PRESSURE).toFloat(),
                            PointerType.PEN, -1
                        )
                    }
                    pre > 0 && pointer.p > 0 -> {
                        editor.pointerMove(
                            pointer.x * viewScale, pointer.y * viewScale,
                            -1, (pointer.p / MAX_PRESSURE).toFloat(),
                            PointerType.PEN, -1
                        )
                    }
                    pre > 0 && pointer.p <= 0 -> {
                        editor.pointerUp(
                            pointer.x * viewScale, pointer.y * viewScale,
                            -1, (pointer.p / MAX_PRESSURE).toFloat(),
                            PointerType.PEN, -1
                        )
                    }
                }
                prePointer = pointer
            }
        }
    }

    private lateinit var renderer: Renderer
    private lateinit var editor: Editor
    private lateinit var contentPackage: ContentPackage

    private fun initRenderEditor() {
        renderer = App.engine.createRenderer(widthDpi, heightDpi, renderTarget)
        editor = App.engine.createEditor(renderer)
        editor.setFontMetricsProvider(
            FontMetricsProvider(
                resources.displayMetrics,
                App.typefaceMap
            )
        )

        val renderContainer = findViewById<FrameLayout>(R.id.render_container)
        renderContainer.post {
            val renderView = LayoutInflater.from(this)
                .inflate(R.layout.render_view_layout, renderContainer, false)
            renderer.viewScale = min(
                renderContainer.width / width.toDouble(),
                renderContainer.height / height.toDouble()
            ).toFloat()
            val layoutParams = FrameLayout.LayoutParams(
                (width * renderer.viewScale).toInt(),
                (height * renderer.viewScale).toInt(),
                Gravity.CENTER
            )
            renderView.setBackgroundColor(Color.parseColor("#FEFEFE"))
            renderContainer.addView(renderView, layoutParams)
            renderTarget.delegate = renderView as RenderView
        }
    }

    private fun openIInk() {
        initRenderEditor()

        contentPackage =
            App.engine.openPackage(File(cacheDir, "${Date()}.iink"), PackageOpenOption.CREATE)
        editor.part = contentPackage.createPart("Text")
    }

    private fun closeIInk() {
        contentPackage.save()
        contentPackage.close()
        editor.close()
        renderer.close()
    }

    private val renderTarget = object : IRenderTarget {
        var delegate: IRenderTarget? = null

        override fun invalidate(renderer: Renderer, layers: EnumSet<IRenderTarget.LayerType>) {
            Log.d(TAG, "invalidate(renderer: Renderer, layers: EnumSet<IRenderTarget.LayerType>)")
            delegate?.invalidate(renderer, layers)
        }

        override fun invalidate(
            renderer: Renderer,
            x: Int,
            y: Int,
            width: Int,
            height: Int,
            layers: EnumSet<IRenderTarget.LayerType>
        ) {
            Log.d(
                TAG,
                "invalidate(renderer: Renderer, x: Int, y: Int, width: Int, height: Int, layers: EnumSet<IRenderTarget.LayerType>)"
            )
            delegate?.invalidate(renderer, x, y, width, height, layers)
        }
    }
}