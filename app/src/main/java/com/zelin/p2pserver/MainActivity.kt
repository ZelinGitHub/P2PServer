package com.zelin.p2pserver

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
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.EasyPermissions.PermissionCallbacks

class MainActivity : ComponentActivity(){



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "onCreate()")
        setContentView(R.layout.activity_main)
    }



    companion object {
        private const val TAG = "peerServer/MainActivity"
    }
}

