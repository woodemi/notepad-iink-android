package com.myscript.notepad_iink

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.woodemi.notepad.NotepadScanResult
import io.woodemi.notepad.NotepadScanner
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions

const val REQ_PERM_LOCATION = 0

val locationPermissions = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)

class MainActivity : AppCompatActivity() {

    private lateinit var notepadScanner: NotepadScanner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.start_scan).setOnClickListener { notepadScanner.startScan() }
        findViewById<Button>(R.id.stop_scan).setOnClickListener { notepadScanner.stopScan() }
        findViewById<RecyclerView>(R.id.list).adapter = scanResultAdapter

        notepadScanner = NotepadScanner(this)
        notepadScanner.callback = scanCallback

        if (!EasyPermissions.hasPermissions(this, *locationPermissions)) {
            EasyPermissions.requestPermissions(this, "TODO rationale", REQ_PERM_LOCATION, *locationPermissions)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    @AfterPermissionGranted(REQ_PERM_LOCATION)
    private fun onLocationGranted() {
        Toast.makeText(this, "Location permission granted", Toast.LENGTH_SHORT).show()
    }

    private val scanCallback = object : NotepadScanner.Callback {
        override fun onScanResult(result: NotepadScanResult) {
            val currentList = scanResultAdapter.currentList
            if (!currentList.any { it.deviceId == result.deviceId }) {
                scanResultAdapter.submitList(currentList + result)
            }
        }
    }

    private val scanResultAdapter =
        object : ListAdapter<NotepadScanResult, ScanResultViewHolder>(ScanResultDiffCallback()) {
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): ScanResultViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                return ScanResultViewHolder(inflater.inflate(android.R.layout.simple_list_item_2, parent, false))
            }

            override fun onBindViewHolder(holder: ScanResultViewHolder, position: Int) {
                val item = getItem(position)
                holder.title.text = "${item.name}(${item.rssi})"
                holder.subtitle.text = "${item.deviceId}"
                holder.itemView.setOnClickListener {
                    val intent = Intent(it.context, NotepadDetailActivity::class.java)
                        .putExtra(EXTRA_SCAN_RESULT, item)
                    startActivity(intent)
                }
            }

        }

    class ScanResultDiffCallback: DiffUtil.ItemCallback<NotepadScanResult>() {
        override fun areItemsTheSame(
            oldItem: NotepadScanResult,
            newItem: NotepadScanResult
        ): Boolean = oldItem.deviceId == newItem.deviceId

        override fun areContentsTheSame(
            oldItem: NotepadScanResult,
            newItem: NotepadScanResult
        ): Boolean = oldItem.rssi == newItem.rssi

    }

    class ScanResultViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val title = itemView.findViewById<TextView>(android.R.id.text1)
        val subtitle = itemView.findViewById<TextView>(android.R.id.text2)
    }
}
