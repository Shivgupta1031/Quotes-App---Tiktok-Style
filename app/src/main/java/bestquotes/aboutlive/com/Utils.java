package bestquotes.aboutlive.com;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;

import bestquotes.aboutlive.com.R;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

public class Utils {

    private static Dialog loadingDialog;

    public static void showAdsLoading(Context context, boolean cancelable, boolean cancelOnTouchOutside) {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            cancelLoading();
        }
        loadingDialog = new Dialog(context);
        loadingDialog.setContentView(R.layout.ads_loading_view);
        try {
            loadingDialog.getWindow().setDimAmount(0);
            loadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        } catch (Exception e) {
            Log.d(Variables.Companion.getTAG(), "showLoading: " + e.getMessage());
        }
        loadingDialog.setCanceledOnTouchOutside(cancelOnTouchOutside);
        loadingDialog.setCancelable(cancelable);
        loadingDialog.show();
    }

    public static void cancelLoading() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            try {
                loadingDialog.cancel();
                loadingDialog = null;
            } catch (Exception e) {
                Log.d(Variables.Companion.getTAG(), "cancelLoading: " + e.getMessage());
            }
        }
    }

    public interface InterstitialAdsCallback {
        void onAdLoaded();

        void onAdFailedToLoad();

        void onAdDismissed();
    }

    public static void loadBannerAd(Activity activity, LinearLayout layout) {
        if (Variables.getSHOW_ADMOB_ADS()) {
            com.google.android.gms.ads.AdView admobAdView = new com.google.android.gms.ads.AdView(activity);
            admobAdView.setAdSize(com.google.android.gms.ads.AdSize.BANNER);
            admobAdView.setAdUnitId(Variables.Companion.getBANNER_ID());
            AdRequest adRequest = new AdRequest.Builder().build();
            admobAdView.loadAd(adRequest);
            admobAdView.setAdListener(new com.google.android.gms.ads.AdListener() {
                @Override
                public void onAdFailedToLoad(@NonNull @NotNull LoadAdError loadAdError) {
                    super.onAdFailedToLoad(loadAdError);
                    Log.d(Variables.Companion.getTAG(), "onAdFailedToLoad: " + loadAdError.getMessage());
                    layout.setVisibility(View.GONE);
                }

                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    Log.d(Variables.Companion.getTAG(), "onAdLoaded: ");
                    layout.setVisibility(View.VISIBLE);
                }
            });
            layout.addView(admobAdView);
        }else {
            layout.setVisibility(View.GONE);
        }
    }

    public static void showInterstitialAdWithLoading(Activity activity, InterstitialAdsCallback callback) {
        if (Variables.getSHOW_ADMOB_ADS()) {
            Utils.showAdsLoading(activity, false, false);
            AdRequest intersAdRequest = new AdRequest.Builder().build();
            com.google.android.gms.ads.interstitial.InterstitialAd.load(
                    activity,
                    Variables.Companion.getINTERSTITIAL_ID(),
                    intersAdRequest,
                    new InterstitialAdLoadCallback() {
                        @Override
                        public void onAdLoaded(@NonNull @NotNull com.google.android.gms.ads.interstitial.InterstitialAd interstitialAd) {
                            super.onAdLoaded(interstitialAd);
                            Log.d(Variables.Companion.getTAG(), "onAdLoaded: ");
                            if (callback != null) {
                                callback.onAdLoaded();
                            }
                            Utils.cancelLoading();
                            interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                                @Override
                                public void onAdFailedToShowFullScreenContent(@NonNull @NotNull AdError adError) {
                                    super.onAdFailedToShowFullScreenContent(adError);
                                    Log.d(Variables.Companion.getTAG(), "onAdFailedToShowFullScreenContent: " + adError);
                                    if (callback != null) {
                                        callback.onAdFailedToLoad();
                                    }
                                }

                                @Override
                                public void onAdDismissedFullScreenContent() {
                                    super.onAdDismissedFullScreenContent();
                                    Log.d(Variables.Companion.getTAG(), "onAdDismissedFullScreenContent: ");
                                    if (callback != null) {
                                        callback.onAdDismissed();
                                    }
                                }
                            });
                            interstitialAd.show(activity);
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull @NotNull LoadAdError loadAdError) {
                            super.onAdFailedToLoad(loadAdError);
                            Log.d(Variables.Companion.getTAG(), "onAdFailedToLoad: " + loadAdError.getMessage());
                            Utils.cancelLoading();
                            if (callback != null) {
                                callback.onAdFailedToLoad();
                            }
                        }
                    }
            );
        }
    }

}
