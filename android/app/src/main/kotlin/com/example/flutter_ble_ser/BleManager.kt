package com.example.flutter_ble_ser

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothDevice.TRANSPORT_LE
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothGattService.SERVICE_TYPE_PRIMARY
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
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodChannel
import java.util.Collections
import java.util.Random
import java.util.UUID


@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
@SuppressLint("MissingPermission", "ServiceCast")
class BleManager(private val context: Context, private val methodChannel: MethodChannel?) {
    private val handler = Handler(Looper.getMainLooper())
    private var eventSink: EventChannel.EventSink? = null

    private var bluetoothManager: BluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager;
    private val listDevises = Collections.synchronizedList(mutableListOf<BluetoothDevice>())


    private val BLUETOOTH_ADAPTER_NAME = "Tenge24";

    private var CHARACTER_UUID = UUID.fromString("402aec02-cd45-4e5f-b2cc-35beb0960b2c");
    private var SERVICE_UUID: UUID? = null;

    private var bleGatt: BluetoothGatt? = null;
    private var server: BluetoothGattServer? = null


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun startAdvertiser(uuid: String?) {

        if (uuid != null) {
            SERVICE_UUID = UUID.fromString(uuid);
        }


        val settings = AdvertiseSettings.Builder()
                .setConnectable(true)
                .setTimeout(1000 * 3 * 60) // 180000
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

        bluetoothManager.adapter.name = BLUETOOTH_ADAPTER_NAME

        bluetoothManager.adapter.bluetoothLeAdvertiser
                .startAdvertising(settings, data, scanResponse, advertiseCallback)
    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public fun startBluetoothScan(service: String?) {

        if (SERVICE_UUID == null) {
            SERVICE_UUID = UUID.fromString(service);
        }

        val filter = ScanFilter.Builder()
                .setServiceUuid(ParcelUuid(SERVICE_UUID))
                .build()
        val settings: ScanSettings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build()

        // Start scanning with BluetoothLeScanner
        if (bluetoothManager.adapter.isEnabled) {
            println("scan started")
            bluetoothManager.adapter.bluetoothLeScanner?.startScan(
                    listOf(filter),
                    settings,
                    scanCallback
            )
        }
    }

    private fun openGattServer() {
        server = bluetoothManager.openGattServer(context, serverCallback)
        writeChar(null);
    }


    @RequiresApi(Build.VERSION_CODES.M)
    fun connectToDevice(address: String?) {
        val device = listDevises.find { it.address == address };
        println("---> connecting to $device")
        device?.connectGatt(context, false, gattCallBack)
    }

    fun readChar() {
        val service = bleGatt?.getService(SERVICE_UUID)
        if (service != null) {
            val characteristic = service.getCharacteristic(CHARACTER_UUID)
            println("characteristic $characteristic")
            val read = bleGatt?.readCharacteristic(characteristic)
            println("isRead $read")
        }
    }


    fun writeChar(data: String?) {
        if (server?.getService(SERVICE_UUID) == null) {
            val gattService = BluetoothGattService(SERVICE_UUID, SERVICE_TYPE_PRIMARY)
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
            characteristic.value = data?.toByteArray()
            gattService.addCharacteristic(characteristic)
            server?.addService(gattService)
            invokeMethodUIThread("write", true)
        } else {
            val characteristic =
                    server?.getService(SERVICE_UUID)?.getCharacteristic(CHARACTER_UUID)
            val updta = characteristic?.setValue(data?.toByteArray())
            println("---- isupdate $updta");
        }

    }


    fun onCansel() {
        bleGatt?.disconnect()
        server?.clearServices()
        server?.close()
        listDevises.clear();
    }

    private val advertiseCallback = @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            println("->>>>onStartSuccess: $settingsInEffect ")
            openGattServer();
            super.onStartSuccess(settingsInEffect)
        }

        override fun onStartFailure(errorCode: Int) {
            println("->>>>onStartFailure: $errorCode ")
            super.onStartFailure(errorCode)
        }
    }


    private val serverCallback = object : BluetoothGattServerCallback() {
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

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            val isContain = listDevises.find { it.address == result.device.address } != null
            if (!isContain) {
                listDevises.add(result.device)
                val hashMap: HashMap<String, Any?> = HashMap();
                hashMap["id"] = result.device?.address;
                hashMap["name"] = result.device?.name;
                hashMap["isConnected"] = false;
                hashMap["uuid"] = result.device?.uuids;
                invokeMethodUIThread("scanResult", hashMap)
            }
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

    private val gattCallBack = object : BluetoothGattCallback() {

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            if (BluetoothGatt.GATT_SUCCESS == status) {
                bleGatt = gatt;
            }
        }

        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    println("gatt state STATE_CONNECTED")
                    gatt?.discoverServices()
                    invokeMethodUIThread("connectToDevise", "connected")
                }

                BluetoothProfile.STATE_DISCONNECTED -> {
                    println("gatt state STATE_DISCONNECTED")
                    invokeMethodUIThread("connectToDevise", "disconnected")
                }

                BluetoothProfile.STATE_CONNECTING -> {
                    println("gatt state STATE_CONNECTING")
                }

                BluetoothProfile.STATE_DISCONNECTING -> {
                    println("gatt state STATE_DISCONNECTING")
                }
            }
            super.onConnectionStateChange(gatt, status, newState)
        }

        @Deprecated("Deprecated in Java")
        override fun onCharacteristicRead(
                gatt: BluetoothGatt?,
                characteristic: BluetoothGattCharacteristic?,
                status: Int
        ) {

            Handler(Looper.getMainLooper()).post {

                characteristic?.value?.decodeToString()?.let {
                    println("----> value id :${it} ")
                    invokeMethodUIThread("read", it)

                }
            }

            super.onCharacteristicRead(gatt, characteristic, status)
        }


        override fun onCharacteristicWrite(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                status: Int
        ) {
            with(characteristic) {
                when (status) {
                    BluetoothGatt.GATT_SUCCESS -> {
                        println("<---> GATT_SUCCESS $value $uuid")
                    }

                    BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH -> {
                        println("<---> GATT_INVALID_ATTRIBUTE_LENGTH $value $uuid")
                    }

                    BluetoothGatt.GATT_WRITE_NOT_PERMITTED -> {
                        println("<---> GATT_WRITE_NOT_PERMITTED $value $uuid")
                    }

                    else -> {
                        println("<---> ERORR $value $uuid")
                    }
                }
            }
        }
    }


    private fun invokeMethodUIThread(method: String, data: Any?) {
        Handler(Looper.getMainLooper()).post {
            synchronized(Any()) {
                println("---> $method $methodChannel")
                methodChannel?.invokeMethod(method, data)
                        ?: println("invokeMethodUIThread: tried to call method on closed channel: $method")
            }
        }
    }


}

