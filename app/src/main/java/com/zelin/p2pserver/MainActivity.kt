package com.zelin.p2pserver

import android.net.wifi.p2p.WifiP2pManager.EXTRA_DISCOVERY_STATE
import android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_DISCOVERY_STARTED
import android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_DISCOVERY_STOPPED
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.activity.ComponentActivity
import com.zelin.p2pserver.PermissionManager.REQUEST_CODE_CREATE_GROUP
import com.zelin.p2pserver.PermissionManager.REQUEST_CODE_PEERS_DISCOVERY
import com.zelin.p2pserver.PermissionManager.REQUEST_CODE_REQUEST_DEVICE_INFO
import com.zelin.p2pserver.p2p.P2pTestActivity
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.EasyPermissions.PermissionCallbacks

class MainActivity : ComponentActivity(){

    var btn_to_p2ptest: Button?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "onCreate()")
        setContentView(R.layout.activity_main)
        btn_to_p2ptest=findViewById(R.id.btn_to_p2ptest)
        btn_to_p2ptest?.setOnClickListener {
            P2pTestActivity.start(this)
        }
    }



    companion object {
        private const val TAG = "peerServer/MainActivity"
    }
}

