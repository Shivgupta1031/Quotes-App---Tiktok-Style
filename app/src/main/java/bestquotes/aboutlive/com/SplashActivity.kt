package bestquotes.aboutlive.com

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import bestquotes.aboutlive.com.Variables.Companion.TAG
import bestquotes.aboutlive.com.databinding.ActivitySplashBinding
import com.google.android.gms.ads.MobileAds
import com.onesignal.OneSignal
import org.json.JSONArray
import org.json.JSONObject

class SplashActivity : AppCompatActivity() {

    lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getSettings()
    }

    private fun getSettings() {
        AndroidNetworking.get(Variables.AIRTABLE_SETTINGS_TABLE_URL)
            .setTag("GetSettings")
            .setPriority(Priority.LOW)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    Log.d(TAG, "onResponse: $response")
                    if (response != null) {
                        val records: JSONArray = response.getJSONArray("records")

                        if (records.length() > 0) {
                            val fields = records.getJSONObject(0).getJSONObject("fields")

                            Variables.SHOW_ADMOB_ADS = fields.getInt("Show Admob Ads") == 1
                            Variables.ONESIGNAL_APP_ID = fields.getString("OneSignal App ID")
                            Variables.ADMOB_APP_ID = fields.getString("Admob App ID")
                            Variables.BANNER_ID = fields.getString("Banner Ad ID")
                            Variables.INTERSTITIAL_ID = fields.getString("Interstitial Ad ID")
                            Variables.NATIVE_ID = fields.getString("Native Ad ID")
                            Variables.SHOW_NATIVE_ADS_AFTER_QUOTE =
                                fields.getInt("Show Native Ad After Quotes")
                            Variables.SHOW_INTERSTITIAL_ADS_AFTER_QUOTE =
                                fields.getInt("Show Interstitial Ad After Quotes")
                            Variables.PRIVACY_POLICY = fields.getString("Privacy Policy")
                        } else {
                            Toast.makeText(
                                this@SplashActivity,
                                "We Are Unable To Import Settings",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        try {
                            val ai =
                                packageManager.getApplicationInfo(
                                    packageName,
                                    PackageManager.GET_META_DATA
                                )
                            val bundle = ai.metaData
                            val myApiKey =
                                bundle.getString("com.google.android.gms.ads.APPLICATION_ID")
                            Log.d(TAG, "Name Found: $myApiKey")
                            ai.metaData.putString(
                                "com.google.android.gms.ads.APPLICATION_ID",
                                Variables.ADMOB_APP_ID
                            ) //you can replace your key APPLICATION_ID here
                            val ApiKey =
                                bundle.getString("com.google.android.gms.ads.APPLICATION_ID")
                            Log.d(TAG, "ReNamed Found: $ApiKey")
                        } catch (e: PackageManager.NameNotFoundException) {
                            Log.d(TAG, "Failed to load meta-data, NameNotFound: " + e.message)
                        } catch (e: NullPointerException) {
                            Log.d(TAG, "Failed to load meta-data, NullPointer: " + e.message)
                        } catch (e: Exception) {
                            Log.d(TAG, "Error $e")
                        }
                        MobileAds.initialize(
                            applicationContext
                        ) { initializationStatus ->
                            val statusMap = initializationStatus.adapterStatusMap
                            for (adapterClass in statusMap.keys) {
                                val status = statusMap[adapterClass]
                                Log.d(
                                    TAG, String.format(
                                        "Adapter name: %s, Description: %s, Latency: %d",
                                        adapterClass, status?.description, status?.latency
                                    )
                                )
                            }
                        }

                        OneSignal.setLogLevel(
                            OneSignal.LOG_LEVEL.VERBOSE,
                            OneSignal.LOG_LEVEL.NONE
                        );

                        OneSignal.initWithContext(applicationContext)
                        OneSignal.setAppId(Variables.ONESIGNAL_APP_ID)

//                        Handler(Looper.getMainLooper()).postDelayed({
                        val intent = Intent(this@SplashActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
//                        }, 200)
                    }
                }

                override fun onError(anError: ANError?) {
                    Log.d(TAG, "onError: ${anError?.message}")
                    Toast.makeText(
                        this@SplashActivity,
                        "We Are Unable To Import Settings",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

}