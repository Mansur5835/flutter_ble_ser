package com.example.flutter_ble_ser

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.ParcelUuid
import android.util.Log
import androidx.annotation.RequiresApi
import io.flutter.plugin.common.MethodChannel
import java.util.Random
import java.util.UUID


@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
@SuppressLint("MissingPermission", "ServiceCast")
class BleService(context: Context, mResult: MethodChannel.Result)  {


    var context: Context;
    var bluetoothManager: BluetoothManager;
    var mResult: MethodChannel.Result;

    init {
        this.context = context;
        this.mResult = mResult;
        this.bluetoothManager =
            context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager;
    }


    private val SERVICE_UUID = UUID.fromString("D61217B3-292C-4EAE-B761-0B2B0D1DA4E9")

    private val CHARACTER_UUID = UUID.fromString("1184EB5C-6A12-478A-976B-C84C3E542989")

    private var server: BluetoothGattServer? = null

    private val BLUETOOTH_ADAPTER_NAME: String =
        "Tenga24";


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun startScanner() {
//        startAdvertiser();
        startBluetoothScan();
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun startBluetoothScan() {
        val filter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(SERVICE_UUID))
            .build()

        val settings: ScanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()


        // Start scanning with BluetoothLeScanner
        if (bluetoothManager.adapter.isEnabled) {
            println("scan started")
            bluetoothManager.adapter.bluetoothLeScanner.startScan(
                null,
                settings,
                scanCallback
            )
        }
    }


    private val scanCallback: ScanCallback = @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            println("->>>>succes: ${result.device.name} ---->> ${result.device.address} ");
            mResult.success(result.device.name);
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


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun startAdvertiser() {

        val settings = AdvertiseSettings.Builder()
            .setConnectable(true)
            .setTimeout(1000 * 5) // 1 min
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
            .build()


        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(false)
            .addServiceUuid(ParcelUuid(SERVICE_UUID))
            .build()


        val scanResponse = AdvertiseData.Builder()
            .setIncludeDeviceName(true)
            .build()

        println("->>>> AdvertiseData: $BLUETOOTH_ADAPTER_NAME")

        bluetoothManager.adapter.name = Random().nextInt().toString()

        bluetoothManager.adapter.bluetoothLeAdvertiser
            .startAdvertising(settings, data, scanResponse, advertiseCallback)
    }


    private val advertiseCallback = @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            println("->>>>onStartSuccess: $settingsInEffect ")
            super.onStartSuccess(settingsInEffect)
            openGattServer()
        }

        override fun onStartFailure(errorCode: Int) {
            println("->>>>onStartFailure: $errorCode ")
            super.onStartFailure(errorCode)
        }
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private fun openGattServer() {
        context.getSystemService(Context.BLUETOOTH_SERVICE)?.let {
            it as BluetoothManager
            if (server?.getService(SERVICE_UUID) == null) {
                println("->>>>openGattServer:")
                server = it.openGattServer(context, serverCallback)
                val gattService = BluetoothGattService(
                    SERVICE_UUID,
                    BluetoothGattService.SERVICE_TYPE_PRIMARY
                )

                val characteristic = BluetoothGattCharacteristic(
                    CHARACTER_UUID,
                    BluetoothGattCharacteristic.PROPERTY_READ,
                    BluetoothGattCharacteristic.PERMISSION_READ or BluetoothGattCharacteristic.PERMISSION_WRITE
                )
                characteristic.addDescriptor(
                    BluetoothGattDescriptor(
                        UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"),
                        BluetoothGattDescriptor.PERMISSION_WRITE
                    )
                )
                characteristic.value = "name is Mansur".toByteArray()
                gattService.addCharacteristic(characteristic)
                server?.addService(gattService)

            } else {
                val characteristic =
                    server?.getService(SERVICE_UUID)?.getCharacteristic(CHARACTER_UUID)
                val isUpdate =
                    characteristic?.setValue("name is Mansur".toByteArray())
            }
        }
    }


    private val serverCallback = @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    object : BluetoothGattServerCallback() {

        override fun onCharacteristicReadRequest(
            device: BluetoothDevice?,
            requestId: Int,
            offset: Int,
            characteristic: BluetoothGattCharacteristic?
        ) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic)
            server?.sendResponse(
                device,
                requestId,
                BluetoothGatt.GATT_SUCCESS,
                offset,
                characteristic?.value
            )
        }
    }

    private val bluetoothGattCallback = @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    object : BluetoothGattCallback() {
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            println("->>>>> read $gatt")

            if (BluetoothGatt.GATT_SUCCESS == status) {
                val service = gatt?.getService(SERVICE_UUID)
                val characteristic = service?.getCharacteristic(CHARACTER_UUID)
//                gatt.writeCharacteristic();
                val read = gatt?.readCharacteristic(characteristic)
                println("->>>>> read $read")
            }

            super.onServicesDiscovered(gatt, status)
        }

        @Deprecated("Deprecated in Java")
        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            Handler(Looper.getMainLooper()).post {
                characteristic?.value?.decodeToString()?.toLongOrNull()?.let {
                    println("->>>>> onCharacteristicRead ")
                    gatt?.disconnect()
//                    bluetoothLeAdvertiser.stopAdvertising(advertiseCallback)
                    server?.clearServices()
                    server?.close()
                }
            }
            super.onCharacteristicRead(gatt, characteristic, status)
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            gatt.readCharacteristic(characteristic)
            println("->>>>> onCharacteristicChanged ")
            super.onCharacteristicChanged(gatt, characteristic, value)
        }

        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    println("->>>>> STATE_CONNECTED ")
                    gatt?.discoverServices()

                }

                BluetoothProfile.STATE_DISCONNECTED -> {
                    println("->>>>> STATE_DISCONNECTED ")
                }

                BluetoothProfile.STATE_CONNECTING -> {
                    println("->>>>> STATE_CONNECTING ")
                }

                BluetoothProfile.STATE_DISCONNECTING -> {
                    Log.d("BluetoothDevice", "STATE_DISCONNECTING ")
                    println("->>>>> STATE_DISCONNECTING ")

                }
            }

            super.onConnectionStateChange(gatt, status, newState)

        }
    }



}