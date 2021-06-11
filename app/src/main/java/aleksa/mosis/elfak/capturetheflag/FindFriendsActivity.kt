package aleksa.mosis.elfak.capturetheflag

import android.app.Activity
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_find_friends.*
import java.io.IOException
import java.util.*


class FindFriendsActivity : AppCompatActivity() {

//    companion object {
//        var myUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
//        var bluetoothSocket: BluetoothSocket? = null
//        lateinit var progress: ProgressDialog
//        lateinit var bluetoothAdapter: BluetoothAdapter
//        var isConnected: Boolean = false
//        lateinit var address: String
//    }
//
//    private inner class ConnectThread(device: BluetoothDevice) : Thread() {
//
//        private val mmSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
//            device.createRfcommSocketToServiceRecord(MY_UUID)
//        }
//
//        public override fun run() {
//            // Cancel discovery because it otherwise slows down the connection.
//            bluetoothAdapter?.cancelDiscovery()
//
//            mmSocket?.let { socket ->
//                // Connect to the remote device through the socket. This call blocks
//                // until it succeeds or throws an exception.
//                socket.connect()
//
//                // The connection attempt succeeded. Perform work associated with
//                // the connection in a separate thread.
//                manageMyConnectedSocket(socket)
//            }
//        }
//
//        // Closes the client socket and causes the thread to finish.
//        fun cancel() {
//            try {
//                mmSocket?.close()
//            } catch (e: IOException) {
//                Log.e(TAG, "Could not close the client socket", e)
//            }
//        }
//    }
//
//    private inner class AcceptThread : Thread() {
//
//        private val mmServerSocket: BluetoothServerSocket? by lazy(LazyThreadSafetyMode.NONE) {
//            bluetoothAdapter?.listenUsingInsecureRfcommWithServiceRecord(NAME, MY_UUID)
//        }
//
//        override fun run() {
//            // Keep listening until exception occurs or a socket is returned.
//            var shouldLoop = true
//            while (shouldLoop) {
//                val socket: BluetoothSocket? = try {
//                    mmServerSocket?.accept()
//                } catch (e: IOException) {
//                    Log.e(TAG, "Socket's accept() method failed", e)
//                    shouldLoop = false
//                    null
//                }
//                socket?.also {
//                    manageMyConnectedSocket(it)
//                    mmServerSocket?.close()
//                    shouldLoop = false
//                }
//            }
//        }
//
//        // Closes the connect socket and causes the thread to finish.
//        fun cancel() {
//            try {
//                mmServerSocket?.close()
//            } catch (e: IOException) {
//                Log.e(TAG, "Could not close the connect socket", e)
//            }
//        }
//    }

    private var bluetoothAdapter: BluetoothAdapter? = null
    private lateinit var pairedDevices: Set<BluetoothDevice>
    private var foundList :MutableList<BluetoothDevice> = mutableListOf<BluetoothDevice>()
    private val REQUEST_ENABLE_BLUETOOTH = 1




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_friends)


        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if(bluetoothAdapter == null) {
            return
        }
        if(!bluetoothAdapter!!.isEnabled){
            var enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BLUETOOTH)
        }

        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
        pairedDevices?.forEach { device ->
            val deviceName = device.name
            val deviceHardwareAddress = device.address // MAC address
        }
        val adapter = ArrayAdapter(this@FindFriendsActivity,android.R.layout.simple_list_item_1, pairedDevices!!.distinct().toList())
        paired_device_list.adapter = adapter


        val filter = IntentFilter()
        filter.addAction(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)


        registerReceiver(receiver, filter)
        select_device_refresh.setOnClickListener {
            bluetoothAdapter!!.startDiscovery()

        }
        btn_visability.setOnClickListener {
            val requestCode = 1;
            val discoverableIntent: Intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
                putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
            }
            startActivityForResult(discoverableIntent, requestCode)
        }


    }
    private val receiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val action: String = intent.action as String
            when(action) {
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    progressBar_discover.visibility = View.VISIBLE
                    foundList.clear()
                }

                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice =
                        intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE) as BluetoothDevice
                    val deviceName = device.name
                    val deviceHardwareAddress = device.address // MAC address
                    foundList.add(device)
                    val adapter = ArrayAdapter(this@FindFriendsActivity,android.R.layout.simple_list_item_1, foundList.distinct().toList())
                    select_device_list.adapter = adapter
                }

                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    progressBar_discover.visibility = View.GONE
                }
            }

        }
    }

    override fun onDestroy() {
        unregisterReceiver(receiver)
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_ENABLE_BLUETOOTH) {
            if(resultCode == Activity.RESULT_OK){
                if(bluetoothAdapter!!.isEnabled){
                    //enabled
                }
                else {}// disabled

            }
        }else if(resultCode == Activity.RESULT_CANCELED){
            //enabling has been canceled
        }
    }

}