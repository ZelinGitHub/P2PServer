package com.zelin.p2pserver

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.ServerSocket
import java.net.Socket


object SocketManager {

    private const val PORT = 7236
    private const val TAG = "peerServer/SocketManager"

    private var mIsAccepting: Boolean = false

    //监听连接，监听成功后获取request数据
    fun acceptConnectAndData(context: Activity, onBitmapReceived: ((bitmap: Bitmap) -> Unit)?) {
        Log.i(TAG, "acceptConnectAndData() mIsAccepting: $mIsAccepting")
        if (mIsAccepting) {
            return
        }
        mIsAccepting = true
        //开启子线程
        CoroutineScope(Dispatchers.IO).launch {
            try {
                var inputStream: InputStream? = null
                var socket: Socket? = null
                val serverSocket = ServerSocket(PORT)
                var length: Int
                //字节码输出流
                val response = ByteArrayOutputStream()
                try {
                    //切换到主线程
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "等待客户端连接", Toast.LENGTH_SHORT).show()
                    }
                    Log.i(TAG, "acceptConnectAndData() call serverSocket.accept()")
                    //监听连接
                    socket = serverSocket.accept()
                    //切换到主线程
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "接收图像数据", Toast.LENGTH_SHORT).show()
                    }
                    inputStream = socket.getInputStream()
                    val buffer = ByteArray(1024)
                    Log.i(TAG, "realConnectAndSendData() Reading DATA begin")
                    //读取图像数据
                    while ((inputStream.read(buffer).also { length = it }) != -1) {
                        Log.i(TAG, "realConnectAndSendData() 数据接收 length: $length")
                        //存储图像数据
                        response.write(buffer, 0, length);
                        Log.i(TAG, "acceptConnectAndData() buffer: $buffer")
                    }
                    //图像数据的字节码
                    val imageData = response.toByteArray()
                    Log.i(TAG, "acceptConnectAndData() imageData: $imageData")
                    val bitmap: Bitmap? =
                        BitmapFactory.decodeByteArray(imageData, 0, imageData.size);
                    Log.i(TAG, "acceptConnectAndData() call socket.shutdownInput()")
                    socket.shutdownInput()
                    //切换到主线程
                    withContext(Dispatchers.Main) {
                        bitmap?.apply {
                            Toast.makeText(context, "图像接收完成", Toast.LENGTH_SHORT).show()
                            onBitmapReceived?.invoke(this)
                        } ?: { Log.e(TAG, "acceptConnectAndData() bitmap is null! ") }
                    }
                } finally {
                    mIsAccepting = false
                    response.close()
                    inputStream?.close()
                    socket?.close()
                    serverSocket.close()
                }
            } catch (e: Exception) {
                Log.e(TAG, e.toString())
            }
        }
    }


}