package com.zelin.p2pserver.p2p

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.p2p.WifiP2pManager
import android.os.Bundle
import android.util.Log


class P2pBroadcastReceiver(
) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.i(TAG, "onReceive()")
        val action = intent?.action
        when (action) {
            //一次设备发现开始或结束
            WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION->{
                Log.i(TAG, "onReceive() WIFI_P2P_DISCOVERY_CHANGED_ACTION")
                mOnWIFI_P2P_DISCOVERY_CHANGED_ACTION?.invoke(intent.extras)
            }
            //设备详细信息改变
            WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                Log.i(TAG, "onReceive() WIFI_P2P_THIS_DEVICE_CHANGED_ACTION")
                mOnWIFI_P2P_THIS_DEVICE_CHANGED_ACTION?.invoke(intent.extras)
            }
        }
    }


    companion object {
        private const val TAG = "peerServer/WiFiDirectBroadcastReceiver"
        private val mReceiver: BroadcastReceiver = P2pBroadcastReceiver()
        var mOnWIFI_P2P_THIS_DEVICE_CHANGED_ACTION: ((bundle: Bundle?) -> Unit)? = null
        var mOnWIFI_P2P_DISCOVERY_CHANGED_ACTION: ((bundle: Bundle?) -> Unit)? = null

        fun registerP2pReceiver(context: Activity) {
            Log.i(TAG, "registerP2pReceiver()")
            val mIntentFilter = IntentFilter()
            mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
            mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION)
            mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
            mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
            mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
            context.registerReceiver(mReceiver, mIntentFilter);
        }

        fun unRegisterP2pReceiver(context: Context) {
            Log.i(TAG, "unRegisterP2pReceiver()")
            context.unregisterReceiver(mReceiver);
        }

    }
}