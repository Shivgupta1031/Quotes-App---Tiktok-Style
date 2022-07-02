package bestquotes.aboutlive.com

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import bestquotes.aboutlive.com.databinding.ActivityMainBinding
import com.androidnetworking.error.ANError

import org.json.JSONArray
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.interfaces.JSONObjectRequestListener
import bestquotes.aboutlive.com.Variables.Companion.TAG
import org.json.JSONObject
import android.graphics.Color

import androidx.core.content.ContextCompat

import android.net.Uri

import android.view.Gravity
import androidx.core.content.res.ResourcesCompat
import bestquotes.aboutlive.com.R
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions

import com.skydoves.powermenu.MenuAnimation

import com.skydoves.powermenu.PowerMenuItem

import com.skydoves.powermenu.PowerMenu
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    var adapter: QuotesRVAdapter? = null
    var dataList: ArrayList<Any>? = ArrayList()
    private var offset: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loading.visibility = View.VISIBLE
        binding.nothingFoundTxt.visibility = View.GONE

        getData()

        val powerMenu = PowerMenu.Builder(this)
            .addItem(PowerMenuItem("Share App", false))
            .addItem(PowerMenuItem("Rate Us", false))
            .addItem(PowerMenuItem("Privacy Policy", false))
            .setAnimation(MenuAnimation.SHOWUP_TOP_RIGHT) // Animation start point (TOP | RIGHT).
            .setMenuRadius(10f) // sets the corner radius.
            .setMenuShadow(10f) // sets the shadow.
            .setTextColor(ContextCompat.getColor(this, R.color.white))
            .setTextGravity(Gravity.CENTER)
            .setTextTypeface(ResourcesCompat.getFont(this, R.font.main_font)!!)
            .setTextSize(18)
            .setSelectedTextColor(Color.WHITE)
            .setMenuColor(Color.WHITE)
            .setMenuColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
            .setSelectedMenuColor(ContextCompat.getColor(this, R.color.white))
            .setOnMenuItemClickListener { position, item ->
                when (position) {
                    0 -> {
                        val intent = Intent(Intent.ACTION_SEND)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        intent.putExtra(
                            Intent.EXTRA_TEXT,
                            "Download ${getString(R.string.app_name)} App At : https://play.google.com/store/apps/details?id=${packageName}"
                        )
                        intent.type = "text/plain"
                        startActivity(Intent.createChooser(intent, "Share Via"))
                    }
                    1 -> {
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data =
                            Uri.parse("https://play.google.com/store/apps/details?id=${packageName}")
                        startActivity(Intent.createChooser(intent, "Share Via"))
                    }
                    2 -> {
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse(Variables.PRIVACY_POLICY)
                        startActivity(Intent.createChooser(intent, "Share Via"))
                    }
                }
            }
            .build()

        binding.menuBtn.setOnClickListener {
            powerMenu.showAsDropDown(it)
        }

        binding.homeViewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position != 0 && position % Variables.SHOW_INTERSTITIAL_ADS_AFTER_QUOTE == 0) {
                    Utils.showInterstitialAdWithLoading(this@MainActivity,
                        object : Utils.InterstitialAdsCallback {
                            override fun onAdLoaded() {

                            }

                            override fun onAdFailedToLoad() {

                            }

                            override fun onAdDismissed() {

                            }
                        })
                }
            }
        })
    }

    private fun getData() {
        AndroidNetworking.get(Variables.AIRTABLE_QUOTES_TABLE_URL + offset)
            .setTag("GetData")
            .setPriority(Priority.LOW)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {

                override fun onResponse(response: JSONObject?) {
                    Log.d(TAG, "onResponse: $response")
                    if (response != null) {
                        try {
                            offset = response.getString("offset")
                        } catch (e: Exception) {
                            Log.d(TAG, "onResponse: $e")
                        }
                        Log.d(TAG, "onResponse: Offset $offset")
                        val records: JSONArray = response.getJSONArray("records")
                        for (i in 0 until records.length()) {
                            val fields = records.getJSONObject(i).getJSONObject("fields")
                            if (fields.length() > 0) {
                                val model = QuoteModel()
                                try {
                                    model.text = fields.getString("text")
                                } catch (e: Exception) {
                                    Log.d(TAG, "onResponse: $e")
                                }
                                try {
                                    model.image = fields.getString("image")
                                } catch (e: Exception) {
                                    Log.d(TAG, "onResponse: $e")
                                }
                                try {
                                    model.quoted_by = fields.getString("quoted_by")
                                } catch (e: Exception) {
                                    Log.d(TAG, "onResponse: $e")
                                }
                                dataList?.add(model)
                            }
                        }

                        if (dataList?.size!! > 0) {
                            dataList!!.reverse()
                            dataList!!.shuffle()

                            binding.loading.visibility = View.GONE
                            binding.nothingFoundTxt.visibility = View.GONE
                            binding.homeViewPager.visibility = View.VISIBLE
                            adapter = QuotesRVAdapter(this@MainActivity, dataList!!)
                            binding.homeViewPager.offscreenPageLimit = 2
                            binding.homeViewPager.orientation = ViewPager2.ORIENTATION_VERTICAL
//                            binding.homeViewPager.setPageTransformer(StackTransformer())
                            binding.homeViewPager.adapter = adapter
                        } else {
                            Toast.makeText(
                                this@MainActivity,
                                "Some Error Occurred",
                                Toast.LENGTH_SHORT
                            ).show()

                            binding.loading.visibility = View.GONE
                            binding.homeViewPager.visibility = View.GONE
                            binding.nothingFoundTxt.visibility = View.VISIBLE
                        }

                    } else {
                        Toast.makeText(this@MainActivity, "Some Error Occurred", Toast.LENGTH_SHORT)
                            .show()
                        binding.loading.visibility = View.GONE
                        binding.homeViewPager.visibility = View.GONE
                        binding.nothingFoundTxt.visibility = View.VISIBLE
                    }
                }

                override fun onError(anError: ANError?) {
                    Log.d(TAG, "onError: ${anError?.message}")
                    Toast.makeText(this@MainActivity, "Some Error Occurred", Toast.LENGTH_SHORT)
                        .show()
                    binding.loading.visibility = View.GONE
                    binding.homeViewPager.visibility = View.GONE
                    binding.nothingFoundTxt.visibility = View.VISIBLE
                }

            })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}