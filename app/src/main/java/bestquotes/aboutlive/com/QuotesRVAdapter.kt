package bestquotes.aboutlive.com

import android.Manifest
import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.drawToBitmap
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import bestquotes.aboutlive.com.Variables.Companion.TAG
import bestquotes.aboutlive.com.databinding.NativeAdItemLayoutBinding
import bestquotes.aboutlive.com.databinding.QuoteItemLayoutBinding
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.*

class QuotesRVAdapter(var context: Activity, var data: ArrayList<Any>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_QUOTE = 0
        private const val TYPE_NATIVE_AD = 1
    }

    private var androidColors: IntArray = context.resources.getIntArray(R.array.translucent_colors)

    override fun getItemViewType(position: Int): Int {
        return if (Variables.SHOW_ADS && Variables.SHOW_ADMOB_ADS && position != 0 && position % Variables.SHOW_NATIVE_ADS_AFTER_QUOTE == 0) {
            TYPE_NATIVE_AD
        } else {
            TYPE_QUOTE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_NATIVE_AD -> {
                val binding = NativeAdItemLayoutBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                NativeAdsViewHolder(binding)
            }
            else -> {
                val binding =
                    QuoteItemLayoutBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                QuotesViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            TYPE_NATIVE_AD -> {
                val adLoader: AdLoader = AdLoader.Builder(context, Variables.NATIVE_ID)
                    .forNativeAd { nativeAd ->
                        Log.d(TAG, "loadNativeAd: ")
                        val template: TemplateView =
                            (holder as NativeAdsViewHolder).binding.nativeTemplateView
                        template.setNativeAd(nativeAd)
                        val randomAndroidColor = androidColors[Random().nextInt(androidColors.size)]
                        (holder as NativeAdsViewHolder).binding.adRootView.setBackgroundColor(
                            randomAndroidColor
                        )
                    }
                    .withAdListener(object : AdListener() {
                        override fun onAdFailedToLoad(adError: LoadAdError) {
                            Log.d(
                                TAG,
                                "Native Ad, onAdFailedToLoad: ${adError.message}, Code = ${adError.code} "
                            )
                        }

                        override fun onAdLoaded() {
                            super.onAdLoaded()
                            Log.d(TAG, "Native Ad, onAdLoaded: ")
                        }
                    })
                    .withNativeAdOptions(
                        NativeAdOptions.Builder()
                            .build()
                    )
                    .build()

                adLoader.loadAd(
                    AdRequest.Builder().build()
                )

            }
            else -> {
                val quoteView = holder as QuotesViewHolder
                val data = data[position] as QuoteModel

                if (data.image != null && data.image!!.isNotEmpty()) {
                    Glide.with(context)
                        .asBitmap()
                        .load(data.image)
                        .placeholder(R.color.colorPrimaryDark)
                        .error(R.color.colorPrimaryDark)
                        .transform(BlurTransformation(context))
                        .into(quoteView.binding.fullImg)

                    Glide.with(context).load(data.image).placeholder(R.color.colorPrimary)
                        .error(R.color.colorPrimary)
                        .listener(object : RequestListener<Drawable> {
                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any?,
                                target: Target<Drawable>?,
                                isFirstResource: Boolean
                            ): Boolean {
                                quoteView.binding.loading.visibility = View.GONE
                                return false
                            }

                            override fun onResourceReady(
                                resource: Drawable?,
                                model: Any?,
                                target: Target<Drawable>?,
                                dataSource: DataSource?,
                                isFirstResource: Boolean
                            ): Boolean {
                                quoteView.binding.loading.visibility = View.GONE
                                return false
                            }
                        })
                        .into(quoteView.binding.smallImg)
                } else {
                    quoteView.binding.loading.visibility = View.GONE
                    val randomAndroidColor = androidColors[Random().nextInt(androidColors.size)]
                    quoteView.binding.fullImg.setBackgroundColor(randomAndroidColor)
                    quoteView.binding.smallImg.setBackgroundColor(randomAndroidColor)
                }

                quoteView.binding.quoteTxt.text = data.text

                val randomAndroidColor = androidColors[Random().nextInt(androidColors.size)]
                quoteView.binding.quotedByTxt.setBackgroundColor(randomAndroidColor)

                if (!data.quoted_by.isNullOrEmpty()) {
                    quoteView.binding.quotedByTxt.visibility = View.VISIBLE
                    quoteView.binding.quotedByTxt.text = "~${data.quoted_by}"
                } else {
                    quoteView.binding.quotedByTxt.visibility = View.GONE
                }

                quoteView.binding.copyImg.setOnClickListener {
                    val clipboard: ClipboardManager? =
                        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
                    val clip = if (!data.quoted_by.isNullOrEmpty()) {
                        ClipData.newPlainText(
                            "quote",
                            "${data.text}  ~${data.quoted_by}"
                        )
                    } else {
                        ClipData.newPlainText("quote", data.text)
                    }
                    clipboard?.setPrimaryClip(clip)
                    Toast.makeText(context, "Copied", Toast.LENGTH_LONG).show()
                    Utils.showInterstitialAdWithLoading(context, null)
                }

                quoteView.binding.shareImg.setOnClickListener {
                    share(quoteView.binding.mainContainer.drawToBitmap(Bitmap.Config.ARGB_8888))
                }

                quoteView.binding.downloadImg.setOnClickListener {
                    if (checkIfAlreadyHavePermission()) {
                        download(quoteView.binding.mainContainer.drawToBitmap(Bitmap.Config.ARGB_8888))
                    } else {
                        Toast.makeText(
                            context,
                            "Please Give Permissions And Try Again.",
                            Toast.LENGTH_SHORT
                        ).show()
                        ActivityCompat.requestPermissions(
                            context,
                            arrayOf(
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                            ),
                            101
                        )
                    }
                }
            }
        }
    }

    private fun share(bitmap: Bitmap) {
        //Generating a file name
        val filename = "${System.currentTimeMillis()}.jpg"

        //Output stream
        var fos: OutputStream? = null

        val image = File(context.externalCacheDir, filename)
        if (image.exists()) {
            image.delete()
        }
        fos = FileOutputStream(image)
        fos.use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)

            val intent = Intent(Intent.ACTION_SEND)

            val imageUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                image
            )

            Log.d(TAG, "share: $imageUri")

            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtra(
                Intent.EXTRA_TEXT,
                "Download ${context.getString(R.string.app_name)} App At : https://play.google.com/store/apps/details?id=${context.packageName}"
            )
            intent.putExtra(Intent.EXTRA_STREAM, imageUri)
            intent.type = "image/jpg"
            context.startActivity(Intent.createChooser(intent, "Share Via"))
        }
    }

    private fun download(bitmap: Bitmap) {
        //Generating a file name
        val filename = "${context.getString(R.string.app_name)}_${
            System.currentTimeMillis().toString().substring(5)
        }.jpg"

        //Output stream
        var fos: OutputStream? = null

        //For devices running android >= Q
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            //getting the contentResolver
            context?.contentResolver?.also { resolver ->

                //Content resolver will process the contentvalues
                val contentValues = ContentValues().apply {

                    //putting file information in content values
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }

                //Inserting the contentValues to contentResolver and getting the Uri
                val imageUri: Uri? =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

                //Opening an outputstream with the Uri that we got
                fos = imageUri?.let { resolver.openOutputStream(it) }
            }
        } else {
            //These for devices running on android < Q
            //So I don't think an explanation is needed here
            val imagesDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDir, filename)
            fos = FileOutputStream(image)
        }

        if (fos != null) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            Toast.makeText(context, "Saved To Pictures", Toast.LENGTH_LONG).show()
            Utils.showInterstitialAdWithLoading(context, null)
        } else {
            Toast.makeText(context, "Failed To Download", Toast.LENGTH_LONG).show()
        }
    }

    private fun checkIfAlreadyHavePermission(): Boolean {
        val result =
            ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        return result == PackageManager.PERMISSION_GRANTED
    }

    override fun getItemCount(): Int = data.size

}