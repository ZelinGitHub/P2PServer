package com.zelin.p2pserver.p2p

import android.net.wifi.p2p.WifiP2pManager.EXTRA_DISCOVERY_STATE
import android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_DISCOVERY_STARTED
import android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_DISCOVERY_STOPPED
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.activity.ComponentActivity
import com.zelin.p2pserver.PermissionManager.REQUEST_CODE_CREATE_GROUP
import com.zelin.p2pserver.PermissionManager.REQUEST_CODE_PEERS_DISCOVERY
import com.zelin.p2pserver.PermissionManager.REQUEST_CODE_REQUEST_DEVICE_INFO
import com.zelin.p2pserver.R
import com.zelin.p2pserver.SocketManager
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.EasyPermissions.PermissionCallbacks

class P2pTestActivity : ComponentActivity(), PermissionCallbacks {

    private var iv: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "onCreate()")
        setContentView(R.layout.activity_p2p)
        iv = findViewById(R.id.iv)
        registerReceivers()
        P2pServerManager.init(this)
        P2pServerManager.createGroup(this)
    }


    override fun onResume() {
        super.onResume()
        Log.i(TAG, "onResume()")
        P2pServerManager.beginPeersDiscovery(this)

    }


    override fun onPause() {
        super.onPause()
        Log.i(TAG, "onPause()")
        P2pServerManager.removeGroup()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "onDestroy()")
        P2pServerManager.deInit()
        unRegisterReceivers()
    }


    private fun registerReceivers() {
        P2pBroadcastReceiver.registerP2pReceiver(this)
        P2pBroadcastReceiver.mOnWIFI_P2P_THIS_DEVICE_CHANGED_ACTION = {
            P2pServerManager.beginRequestDeviceInfo(this) {
                SocketManager.acceptConnectAndData(this) {
                    Log.i(TAG, "acceptConnectAndData() call setImageBitmap()")
                    iv?.setImageBitmap(it)
                }
            }
        }
        P2pBroadcastReceiver.mOnWIFI_P2P_DISCOVERY_CHANGED_ACTION = {
            Log.i(TAG, "WIFI_P2P_DISCOVERY_CHANGED_ACTION 设备发现状态改变 bundle: $it")
            it?.let {
                if (it.getInt(EXTRA_DISCOVERY_STATE) == WIFI_P2P_DISCOVERY_STOPPED) {
                    Log.i(TAG, "WIFI_P2P_DISCOVERY_CHANGED_ACTION 设备发现结束, call beinPeersDiscovery 继续设备发现")
                    P2pServerManager.beginPeersDiscovery(this)
                } else if (it.getInt(EXTRA_DISCOVERY_STATE) == WIFI_P2P_DISCOVERY_STARTED) {
                    Log.i(TAG, "WIFI_P2P_DISCOVERY_CHANGED_ACTION 设备发现开始")
                }
            }
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

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        Log.i(TAG, "onPermissionsGranted()")
        when (requestCode) {
            REQUEST_CODE_CREATE_GROUP -> {
                P2pServerManager.createGroup(this)
            }

            REQUEST_CODE_PEERS_DISCOVERY -> {
                P2pServerManager.beginPeersDiscovery(this)
            }

            REQUEST_CODE_REQUEST_DEVICE_INFO -> {
                P2pServerManager.beginRequestDeviceInfo(this) {
                    SocketManager.acceptConnectAndData(this) {
                        iv?.setImageBitmap(it)
                    }
                }
            }
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        Log.e(TAG, "onPermissionsDenied() 用户拒绝了权限")
        finish()
    }


    companion object {
        private const val TAG = "peerServer/P2pTestActivity"
    }
}

