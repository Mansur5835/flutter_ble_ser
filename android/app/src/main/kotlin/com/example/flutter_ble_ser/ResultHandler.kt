package com.example.flutter_ble_ser

import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.os.Build
import androidx.annotation.RequiresApi
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.EventChannel

class ResultHandler(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) :
    EventChannel.StreamHandler {

    private var eventSink: EventChannel.EventSink? = null


    private val eventChannel = EventChannel(
        flutterPluginBinding.binaryMessenger,
        "myEventChannel"
    )

    init {
        eventChannel.setStreamHandler(this)
    }

    override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
        this.eventSink = eventSink
    }

    override fun onCancel(arguments: Any?) {
        this.eventSink = null
    }


    private val scanCallback: ScanCallback = @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    object : ScanCallback() {

        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
              result.device;

        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            println("->>>>error: ${errorCode} ")
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
            println("->>>>succesBatch: ${results} ")
        }
    }


}