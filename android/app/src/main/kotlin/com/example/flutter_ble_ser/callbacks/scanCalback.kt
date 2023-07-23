package com.example.flutter_ble_ser.callbacks

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.EventChannel.EventSink

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
@SuppressLint("MissingPermission", "ServiceCast")
class MyScanCallBack(eventSink: EventChannel.EventSink) : ScanCallback() {
    val eventSink: EventChannel.EventSink;

    init {
        this.eventSink = eventSink;
    }

    override fun onScanResult(callbackType: Int, result: ScanResult) {
        super.onScanResult(callbackType, result)

        eventSink.success(result.device.name);
        println("->>>>succes: ${result.device.name} ---->> ${result.device.address} ");

//            val device = result.device.let {
//                println("->>>>succes: ${it.name} ---->> ${it.address} ");
//                mResult.success(it.name);
////                bluetoothManager.adapter.bluetoothLeScanner.stopScan(this);
////                it.connectGatt(context, true, bluetoothGattCallback)
//            }
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

