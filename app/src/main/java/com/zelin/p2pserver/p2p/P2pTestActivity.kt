package com.zelin.p2pserver.p2p

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.NetworkInfo
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager.EXTRA_DISCOVERY_STATE
import android.net.wifi.p2p.WifiP2pManager.EXTRA_NETWORK_INFO
import android.net.wifi.p2p.WifiP2pManager.EXTRA_WIFI_P2P_INFO
import android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_DISCOVERY_STARTED
import android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_DISCOVERY_STOPPED
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.zelin.p2pserver.PermissionManager.REQUEST_CODE_CREATE_GROUP
import com.zelin.p2pserver.PermissionManager.REQUEST_CODE_PEERS_DISCOVERY
import com.zelin.p2pserver.PermissionManager.REQUEST_CODE_REQUEST_DEVICE_INFO
import com.zelin.p2pserver.R
import com.zelin.p2pserver.SocketManager
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.EasyPermissions.PermissionCallbacks

//服务端只有三步：
//1、创建Group，成为owner
//2、开启设备发现
//3、等待客户端的socket连接，连接建立后后得到request数据（在设备详细信息改变、然后获取当前设备详细信息后进行）
class P2pTestActivity : ComponentActivity(), PermissionCallbacks {

    private var iv: ImageView? = null
    private var btn_create_group: Button? = null
    private var btn_start_socket: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "onCreate()")
        setContentView(R.layout.activity_p2p)
        iv = findViewById(R.id.iv)
        btn_create_group = findViewById(R.id.btn_create_group)
        btn_start_socket = findViewById(R.id.btn_start_socket)
        registerReceivers()
        //初始化
        P2pServerManager.init(this)
        btn_create_group?.setOnClickListener {
            Log.i(TAG,"onClick() click btn_create_group")
            //创建一个p2p组，当前设备作为组的owner（GO），谁创建Group，谁就是Owner。
            createGroup()
        }
        btn_start_socket?.setOnClickListener {
            Log.i(TAG,"onClick() click btn_start_socket")
            Toast.makeText(this,"启动Socket",Toast.LENGTH_SHORT).show()
            //监听连接请求，并得到request数据
            SocketManager.acceptConnectAndData(this) {
                Log.i(TAG, "acceptConnectAndData() call setImageBitmap()")
                iv?.setImageBitmap(it)
            }
        }
        //开设设备发现
        beginPeersDiscovery()
    }


    override fun onResume() {
        super.onResume()
        Log.i(TAG, "onResume()")

    }


    override fun onPause() {
        super.onPause()
        Log.i(TAG, "onPause()")

    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "onDestroy()")
        //可以调用WifiP2pManger.removeGroup方法来断开P2P连接
        P2pServerManager.removeGroup()
        P2pServerManager.deInit()
        unRegisterReceivers()
    }


    private fun registerReceivers() {
        P2pBroadcastReceiver.registerP2pReceiver(this)

        //广播：设备发现状态改变。保持设备发现状态
        P2pBroadcastReceiver.mOnWIFI_P2P_DISCOVERY_CHANGED_ACTION = {
            Log.i(TAG, "WIFI_P2P_DISCOVERY_CHANGED_ACTION 设备发现状态改变 bundle: $it")
            it?.let {
                if (it.getInt(EXTRA_DISCOVERY_STATE) == WIFI_P2P_DISCOVERY_STOPPED) {
                    Log.i(
                        TAG,
                        "WIFI_P2P_DISCOVERY_CHANGED_ACTION 设备发现结束, call beinPeersDiscovery 继续设备发现"
                    )
                    beginPeersDiscovery()
                } else if (it.getInt(EXTRA_DISCOVERY_STATE) == WIFI_P2P_DISCOVERY_STARTED) {
                    Log.i(TAG, "WIFI_P2P_DISCOVERY_CHANGED_ACTION 设备发现开始")
                }
            }
        }
        P2pBroadcastReceiver.mOnWIFI_P2P_CONNECTION_CHANGED_ACTION=on@{
            if(it==null){
                return@on
            }
            val networkInfo: NetworkInfo? =
                it.getParcelable<NetworkInfo>(EXTRA_NETWORK_INFO)
            Log.i(
                TAG,
                "onReceive() WIFI_P2P_CONNECTION_CHANGED_ACTION networkInfo: $networkInfo"
            )
            val wifiP2pInfo: WifiP2pInfo? =
                it.getParcelable<WifiP2pInfo>(EXTRA_WIFI_P2P_INFO)
            Log.i(
                TAG,
                "onReceive() WIFI_P2P_CONNECTION_CHANGED_ACTION wifiP2pInfo: $wifiP2pInfo"
            )
            Log.i(
                TAG,
                "onReceive() WIFI_P2P_CONNECTION_CHANGED_ACTION networkInfo?.isConnected: ${networkInfo?.isConnected}"
            )
        }

        //广播：当前设备的详细信息发生改变
        P2pBroadcastReceiver.mOnWIFI_P2P_THIS_DEVICE_CHANGED_ACTION = {
            beginRequestDeviceInfo()
        }
    }

    private fun unRegisterReceivers() {
        P2pBroadcastReceiver.mOnWIFI_P2P_THIS_DEVICE_CHANGED_ACTION = null
        P2pBroadcastReceiver.mOnWIFI_P2P_DISCOVERY_CHANGED_ACTION = null
        P2pBroadcastReceiver.unRegisterP2pReceiver(this)
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.i(TAG, "onRequestPermissionsResult()")
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    //权限通过的回调
    //通过requestCode来区分回调
    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        Log.i(TAG, "onPermissionsGranted()")
        when (requestCode) {
            //创建组的权限有了
            REQUEST_CODE_CREATE_GROUP -> {
                //再次创建Group（成为groupOwner）
                createGroup()
            }

            //请求设备发现的权限有了
            REQUEST_CODE_PEERS_DISCOVERY -> {
                //再次请求设备发现
                beginPeersDiscovery()
            }

            //请求设备详细信息有了
            REQUEST_CODE_REQUEST_DEVICE_INFO -> {
                //再次请求设备详细信息
                beginRequestDeviceInfo()
            }
        }
    }

    private fun beginRequestDeviceInfo() {
        //请求当前设备的详细信息
        P2pServerManager.beginRequestDeviceInfo(this) {
            //这个里面的信息不包含IP地址
                wifiP2pDevice ->
            //wifiP2pDevice实际上没有用到
            Log.d(TAG, "beginRequestDeviceInfo() wifiP2pDevice: $wifiP2pDevice")
        }
    }

    private fun beginPeersDiscovery() {
        P2pServerManager.beginPeersDiscovery(this)
    }

    private fun createGroup() {
        P2pServerManager.createGroup(this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        Log.e(TAG, "onPermissionsDenied() 用户拒绝了权限")
        finish()
    }


    companion object {
        private const val TAG = "peerServer/P2pTestActivity"
        fun start(context:Activity){
            val intent=Intent(context,P2pTestActivity::class.java)
            context.startActivity(intent)
        }
    }
}

