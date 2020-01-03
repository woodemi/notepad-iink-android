package com.myscript.notepad_iink

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.woodemi.notepad.*

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
                    this@NotepadDetailActivity.notepadClient = null
                }
                is ConnectionState.Connected -> {
                    this@NotepadDetailActivity.notepadClient = notepadClient
                }
            }
            runOnUiThread {
                Toast.makeText(this@NotepadDetailActivity, "$state", Toast.LENGTH_SHORT).show()
            }
        }
    }
}