package com.example.flutter_ble_ser

import android.Manifest
import android.annotation.TargetApi
import android.bluetooth.le.ScanCallback
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel


class MainActivity : FlutterActivity(), MethodChannel.MethodCallHandler {
    lateinit var bleManager: BleManager;
    private var methodChannel: MethodChannel? = null
    private val NAMESPACE = "myChannel"


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        methodChannel = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, "$NAMESPACE/methods");
        methodChannel?.setMethodCallHandler(this)
        bleManager = BleManager(this, methodChannel);
    }


    private fun requestBluetoothPermissions() {
        val permissions = arrayOf(
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
        )


        var permissionRequestCode = 0
        for (permission in permissions) {
            permissionRequestCode++;
            if (ContextCompat.checkSelfPermission(
                            this,
                            permission
                    ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(this, permissions, permissionRequestCode)
                break
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            "start" -> {
                bleManager.startBluetoothScan(call.argument("service"))
                result.success(null);
            }

            "setAdvertise" -> {
                requestBluetoothPermissions();
                bleManager.startAdvertiser(call.argument("uuid"));
                result.success(null);
            }

            "connectToDevise" -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    bleManager.connectToDevice(call.argument("address"))
                    result.success(null);
                };
            }
            "write" -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    bleManager.writeChar(call.argument("data"))
                    result.success(null);
                };
            }

            "read" -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    bleManager.readChar()
                    result.success(null);
                };
            }
            "dispose" -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    bleManager.onCansel()
                    result.success(null);
                };
            }

            else -> {
                result.error("UNAVAILABLE", "Battery level not available.", null)
            }
        }
    }


}










