package bestquotes.aboutlive.com

import android.app.Application
import android.graphics.BitmapFactory
import android.util.Log
import com.androidnetworking.AndroidNetworking
import com.facebook.ads.AudienceNetworkAds

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        AudienceNetworkAds.initialize(this)

        val options: BitmapFactory.Options = BitmapFactory.Options()
        options.inPurgeable = true
        options.inPreferQualityOverSpeed = true
        AndroidNetworking.setBitmapDecodeOptions(options)
        AndroidNetworking.enableLogging()
        AndroidNetworking.initialize(applicationContext)
        AndroidNetworking.setConnectionQualityChangeListener { currentConnectionQuality, currentBandwidth ->
            Log.d(
                Variables.TAG,
                "onChange: currentConnectionQuality : $currentConnectionQuality currentBandwidth : $currentBandwidth"
            )
        }

    }
}