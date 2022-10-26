package com.kongregate.mobile.burritobi

import android.app.Application
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.onesignal.OneSignal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Appja : Application(){

    companion object {
        const val AF_DEV_KEY = "yMgFzYRw5PGCtanQ5paeti"
        const val jsoupCheck = "1v8v"
        const val ONESIGNAL_APP_ID = "c9cf7497-3ac8-40a7-8f93-0b38407c15e7"

        val linkAppsCheckPart1 = "http://ruby"
        val linkAppsCheckPart2 = "monkey.xyz/apps.txt"


        val odone = "sub_id_1="

        var MAIN_ID: String? = ""
        var C1: String? = "c11"
        var D1: String? = "d11"
        var CH: String? = "check"

    }

    override fun onCreate() {
        super.onCreate()

        GlobalScope.launch(Dispatchers.IO) {
            applyDeviceId(context = applicationContext)
        }
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE)
        // OneSignal Initialization
        OneSignal.initWithContext(this)
        OneSignal.setAppId(ONESIGNAL_APP_ID)
    }

    private suspend fun applyDeviceId(context: Context) {
        val advertisingInfo = Adv(context)
        val idInfo = advertisingInfo.getAdvertisingId()

        val prefs = getSharedPreferences("SP", MODE_PRIVATE)
        val editor = prefs.edit()

        editor.putString(MAIN_ID, idInfo)
        editor.apply()
    }

}

class Adv (context: Context) {
    private val adInfo = AdvertisingIdClient(context.applicationContext)

    suspend fun getAdvertisingId(): String =
        withContext(Dispatchers.IO) {
            adInfo.start()
            val adIdInfo = adInfo.info
            Log.d("getAdvertisingId = ", adIdInfo.id.toString())
            adIdInfo.id
        }
}