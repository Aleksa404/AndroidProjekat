import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.ContentValues
import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset
import java.util.*



class BluetoothService {
    var NAME ="veza"
    val uuid: UUID = UUID.fromString("8989063a-c9af-463a-b3f1-f21d9b2b827b")
     var mInsecureAcceptThread: AcceptThread? = null
     var mConnectThread: ConnectThread? = null
     var mConnectedThread: ConnectedThread? = null



    fun startThread(){
        Log.d(ContentValues.TAG, "start")

        if(mConnectThread != null) {
            mConnectThread?.cancel()
            mConnectThread = null
        }
        if(mInsecureAcceptThread == null){
            mInsecureAcceptThread = AcceptThread()
            mInsecureAcceptThread?.start()
        }
    }

   inner class AcceptThread() : Thread() {

        private val bluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        private val mmServerSocket: BluetoothServerSocket? by lazy(LazyThreadSafetyMode.NONE) {
            bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(NAME, uuid)
        }

        override fun run() {
            // Keep listening until exception occurs or a socket is returned.
            Log.d(ContentValues.TAG, "accept thread run ")
            var shouldLoop = true
            while (shouldLoop) {
                val socket: BluetoothSocket? = try {
                    mmServerSocket?.accept()
                } catch (e: IOException) {
                    Log.e(ContentValues.TAG, "Socket's accept() method failed", e)
                    shouldLoop = false
                    null
                }
                socket?.also {
                    Log.d(ContentValues.TAG, "ACCEPTED A SOCKET CONNECTION")
                    // manageMyConnectedSocket(it)
                    mmServerSocket?.close()
                    shouldLoop = false
                }
            }
        }

        // Closes the connect socket and causes the thread to finish.
        fun cancel() {
            try {
                mmServerSocket?.close()
            } catch (e: IOException) {
                Log.e(ContentValues.TAG, "Could not close the connect socket", e)
            }
        }
    }


  inner class ConnectThread(device: BluetoothDevice) : Thread() {


        private val mmSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
              device.createRfcommSocketToServiceRecord(uuid)
        }

        private val bluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

         override fun run() {
            Log.d(ContentValues.TAG, "connect thread run ")
            bluetoothAdapter.cancelDiscovery()

            mmSocket?.let { socket ->
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                try {
                    socket.connect()
                    Log.d(ContentValues.TAG, " connect thread SOCKET CONNECTED ")
                } catch (e: java.lang.Exception) {
                    Log.d(ContentValues.TAG, e.printStackTrace().toString())
                }


                // The connection attempt succeeded. Perform work associated with
                // the connection in a separate thread.
                // manageMyConnectedSocket(socket)
            }
        }

        // Closes the client socket and causes the thread to finish.
        fun cancel() {
            try {
                mmSocket?.close()
            } catch (e: IOException) {
                Log.e(ContentValues.TAG, "Could not close the client socket", e)
            }
        }
    }

   inner class ConnectedThread(socket: BluetoothSocket) : Thread() {

        private var mmSocket: BluetoothSocket = socket
        private var mmInStream: InputStream?
        private var mmOutStream: OutputStream?

        init {
            var tmpIn: InputStream? = null
            var tmpOut: OutputStream? = null

            try {
                tmpIn = mmSocket.inputStream
                tmpOut = mmSocket.outputStream
            } catch (e: IOException) {

            }
            mmInStream = tmpIn
            mmOutStream = tmpOut
        }

        override fun run() {
            var buffer: ByteArray = ByteArray(1024)
            var bytes: Int

            while (true) {
                try {
                    bytes = mmInStream!!.read(buffer)
                    var incomingMessage: String = String(buffer, 0, bytes)
                    Log.d(ContentValues.TAG, "inputStream: " + incomingMessage)
                } catch (e: IOException) {
                    break
                }
            }
        }

        fun write(bytes: ByteArray) {
            var text: String = String(bytes, Charset.defaultCharset())
            try {
                mmOutStream!!.write(bytes)
            } catch (e: IOException) {

            }
        }

        fun cancel() {
            try {
                mmSocket.close()
            } catch (e: IOException) {

            }
        }

        fun connected(mmSocket: BluetoothSocket, device: BluetoothDevice) {

            mConnectedThread = ConnectedThread (mmSocket)
            mConnectedThread?.start()

        }
    }
}