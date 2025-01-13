package com.zelin.p2pserver.p2p

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.util.Log
import android.widget.Toast
import com.zelin.p2pserver.PermissionManager
import com.zelin.p2pserver.PermissionManager.REQUEST_CODE_CREATE_GROUP
import com.zelin.p2pserver.PermissionManager.REQUEST_CODE_PEERS_DISCOVERY
import com.zelin.p2pserver.PermissionManager.REQUEST_CODE_REQUEST_DEVICE_INFO

object P2pServerManager {


    private var mWifiP2pManager: WifiP2pManager? = null
    private var mWifiP2pChannel: WifiP2pManager.Channel? = null
    private const val TAG = "peerServer/P2pServerManager"


    fun init(context: Context) {
        Log.i(TAG, "init()")
        mWifiP2pManager = context.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager?
        mWifiP2pChannel = mWifiP2pManager?.initialize(context, context.mainLooper, null)
    }


    fun deInit() {
        Log.i(TAG, "deInit()")
        mWifiP2pManager = null
        mWifiP2pChannel = null
    }


    //先开启Group，使当前设备成为GroupOwner
    //服务端设备做GroupOwner，客户端设备可以拿到GroupOwner的IP地址，从而发送数据
    @SuppressLint("MissingPermission")
    fun createGroup(activity: Activity) {
        Log.i(TAG, "createGroup()")
        if (!PermissionManager.isHavePermissions(activity, REQUEST_CODE_CREATE_GROUP)) {
            Log.e(TAG, "createGroup() 权限不足")
            return
        }
        if (mWifiP2pChannel != null) {
            mWifiP2pManager?.createGroup(mWifiP2pChannel, null)
        }
    }

    fun removeGroup() {
        Log.i(TAG, "removeGroup()")
        if (mWifiP2pChannel != null) {
            mWifiP2pManager?.removeGroup(mWifiP2pChannel, null)
        }
    }


    //开启设备发现
    //服务端和客户端都需要开启设备发现
    @SuppressLint("MissingPermission")
    fun beginPeersDiscovery(activity: Activity) {
        Log.i(TAG, "beginPeersDiscovery()")

        if (!PermissionManager.isHavePermissions(activity, REQUEST_CODE_PEERS_DISCOVERY)) {
            Log.e(TAG, "beginPeersDiscovery() 权限不足")
            return
        }
        Log.i(TAG, "beginPeersDiscovery() call WifiP2pManager.discoverPeers()")
        Toast.makeText(activity, "开始设备发现", Toast.LENGTH_SHORT).show()
        if (mWifiP2pChannel != null) {
            mWifiP2pManager?.discoverPeers(mWifiP2pChannel, null)
        }
    }


    @SuppressLint("MissingPermission")
    fun beginRequestDeviceInfo(
        activity: Activity,
        onWifiP2pDeviceResponse: ((wifiP2pDevice: WifiP2pDevice) -> Unit)?,
    ) {
        Log.i(TAG, "beginRequestDeviceInfo()")
        if (!PermissionManager.isHavePermissions(activity, REQUEST_CODE_REQUEST_DEVICE_INFO)) {
            Log.e(TAG, "beginRequestDeviceInfo() 权限不足")
            return
        }
        Log.i(TAG, "beginRequestDeviceInfo() call WifiP2pManager.requestDeviceInfo()")
        mWifiP2pChannel?.let {
            mWifiP2pManager?.requestDeviceInfo(
                it
            ) {
                //新的p2p设备信息
                    wifiP2pDevice ->
                Log.i(
                    TAG,
                    "beginRequestDeviceInfo() requestDeviceInfo() wifiP2pDevice: $wifiP2pDevice"
                )
                //本地
                //wifiP2pDevice:
                // Device: Redmi K40S
                //deviceAddress: 02:00:00:00:00:00
                //primary type: 10-0050F204-5
                //secondary type: null
                //wps: 0
                //grpcapab: 0
                //devcapab: 0
                //status: 3
                //wfdInfo: null
                //vendorElements: null
                //设置界面显示的mac地址：
                //f8:ab:82:d9:3a:1e
                //设置界面显示的蓝牙地址：
                //f8:ab:82:d8:01:9e
                wifiP2pDevice?.apply {
                    onWifiP2pDeviceResponse?.invoke(this)
                } ?: { Log.e(TAG, "beginRequestDeviceInfo() wifiP2pDevice is null! ") }
            }
        }
    }

}