// Copyright 2018-2019 Twitter, Inc.
// Licensed under the MoPub SDK License Agreement
// http://www.mopub.com/legal/sdk-license-agreement/

package com.mopub.mobileads;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.mopub.common.AdFormat;
import com.mopub.common.AdReport;
import com.mopub.common.MoPub;
import com.mopub.common.logging.MoPubLog;
import com.mopub.common.util.ManifestUtils;
import com.mopub.common.util.Reflection;
import com.mopub.common.util.Visibility;
import com.mopub.mobileads.factories.AdViewControllerFactory;

import java.util.Map;
import java.util.TreeMap;

import static com.mopub.common.logging.MoPubLog.AdLogEvent.CLICKED;
import static com.mopub.common.logging.MoPubLog.AdLogEvent.CUSTOM;
import static com.mopub.common.logging.MoPubLog.AdLogEvent.DID_DISAPPEAR;
import static com.mopub.common.logging.MoPubLog.AdLogEvent.LOAD_ATTEMPTED;
import static com.mopub.common.logging.MoPubLog.AdLogEvent.LOAD_FAILED;
import static com.mopub.common.logging.MoPubLog.AdLogEvent.LOAD_SUCCESS;
import static com.mopub.common.logging.MoPubLog.AdLogEvent.SHOW_ATTEMPTED;
import static com.mopub.common.logging.MoPubLog.AdLogEvent.SHOW_FAILED;
import static com.mopub.common.logging.MoPubLog.AdLogEvent.SHOW_SUCCESS;
import static com.mopub.common.logging.MoPubLog.SdkLogEvent.ERROR;
import static com.mopub.mobileads.MoPubErrorCode.ADAPTER_NOT_FOUND;

public class MoPubView extends FrameLayout {
    public interface BannerAdListener {
        public void onBannerLoaded(MoPubView banner);
        public void onBannerFailed(MoPubView banner, MoPubErrorCode errorCode);
        public void onBannerClicked(MoPubView banner);
        public void onBannerExpanded(MoPubView banner);
        public void onBannerCollapsed(MoPubView banner);
    }

    public interface BannerCustomEventAdListener {
        void onCustomEventBannerAttempted(String customEventClassName);
        void onCustomEventBannerFailed(String customEventClassName, MoPubErrorCode errorCode);
    }

    private static final String CUSTOM_EVENT_BANNER_ADAPTER_FACTORY =
            "com.mopub.mobileads.factories.CustomEventBannerAdapterFactory";

    @Nullable
    protected AdViewController mAdViewController;
    // mCustomEventBannerAdapter must be a CustomEventBannerAdapter
    protected Object mCustomEventBannerAdapter;

    private Context mContext;
    private int mScreenVisibility;
    private BroadcastReceiver mScreenStateReceiver;

    private BannerAdListener mBannerAdListener;
    protected BannerCustomEventAdListener mBannerCustomEventAdListener;

    public MoPubView(Context context) {
        this(context, null);
    }

    public MoPubView(Context context, AttributeSet attrs) {
        super(context, attrs);

        ManifestUtils.checkWebViewActivitiesDeclared(context);

        mContext = context;
        mScreenVisibility = getVisibility();

        setHorizontalScrollBarEnabled(false);
        setVerticalScrollBarEnabled(false);

        mAdViewController = AdViewControllerFactory.create(context, this);
        registerScreenStateBroadcastReceiver();
    }

    private void registerScreenStateBroadcastReceiver() {
        mScreenStateReceiver = new BroadcastReceiver() {
            public void onReceive(final Context context, final Intent intent) {
                if (!Visibility.isScreenVisible(mScreenVisibility) || intent == null) {
                    return;
                }

                final String action = intent.getAction();

                if (Intent.ACTION_USER_PRESENT.equals(action)) {
                    setAdVisibility(View.VISIBLE);
                } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                    setAdVisibility(View.GONE);
                }
            }
        };

        final IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        mContext.registerReceiver(mScreenStateReceiver, filter);
    }

    private void unregisterScreenStateBroadcastReceiver() {
        try {
            mContext.unregisterReceiver(mScreenStateReceiver);
        } catch (Exception IllegalArgumentException) {
            MoPubLog.log(CUSTOM, "Failed to unregister screen state broadcast receiver (never registered).");
        }
    }

    public void loadAd() {
        if (mAdViewController != null) {
            MoPubLog.log(LOAD_ATTEMPTED);
            mAdViewController.loadAd();
        }
    }

    /*
     * Tears down the ad view: no ads will be shown once this method executes. The parent
     * Activity's onDestroy implementation must include a call to this method.
     */
    public void destroy() {
        MoPubLog.log(CUSTOM, "Destroy() called");
        unregisterScreenStateBroadcastReceiver();
        removeAllViews();

        if (mAdViewController != null) {
            mAdViewController.cleanup();
            mAdViewController = null;
        }

        if (mCustomEventBannerAdapter != null) {
            invalidateAdapter();
            mCustomEventBannerAdapter = null;
        }
    }

    private void invalidateAdapter() {
        if (mCustomEventBannerAdapter != null) {
            try {
                new Reflection.MethodBuilder(mCustomEventBannerAdapter, "invalidate")
                        .setAccessible()
                        .execute();
            } catch (Exception e) {
                MoPubLog.log(ERROR, "Error invalidating adapter", e);
            }
        }
    }

    @NonNull
    Integer getAdTimeoutDelay(int defaultValue) {
        if (mAdViewController == null) {
            return defaultValue;
        }
        return mAdViewController.getAdTimeoutDelay(defaultValue);
    }

    protected boolean loadFailUrl(@NonNull final MoPubErrorCode errorCode) {
        if (mBannerCustomEventAdListener != null) {
            mBannerCustomEventAdListener.onCustomEventBannerFailed(mAdViewController.getCustomEventClassName(), errorCode);
        }
        if (mAdViewController == null) {
            return false;
        }
        return mAdViewController.loadFailUrl(errorCode);
    }

    protected void loadCustomEvent(String customEventClassName, Map<String, String> serverExtras) {
        if (mAdViewController == null) {
            return;
        }
        if (TextUtils.isEmpty(customEventClassName)) {
            MoPubLog.log(CUSTOM, "Couldn't invoke custom event because the server did not specify one.");
            loadFailUrl(ADAPTER_NOT_FOUND);
            return;
        }

        if (mCustomEventBannerAdapter != null) {
            invalidateAdapter();
        }

        MoPubLog.log(CUSTOM, "Loading custom event adapter.");
        if (mBannerCustomEventAdListener != null) {
            mBannerCustomEventAdListener.onCustomEventBannerAttempted(customEventClassName);
        }

        if (Reflection.classFound(CUSTOM_EVENT_BANNER_ADAPTER_FACTORY)) {
            try {
                final Class<?> adapterFactoryClass = Class.forName(CUSTOM_EVENT_BANNER_ADAPTER_FACTORY);
                mCustomEventBannerAdapter = new Reflection.MethodBuilder(null, "create")
                        .setStatic(adapterFactoryClass)
                        .addParam(MoPubView.class, this)
                        .addParam(String.class, customEventClassName)
                        .addParam(Map.class, serverExtras)
                        .addParam(long.class, mAdViewController.getBroadcastIdentifier())
                        .addParam(AdReport.class, mAdViewController.getAdReport())
                        .execute();
                new Reflection.MethodBuilder(mCustomEventBannerAdapter, "loadAd")
                        .setAccessible()
                        .execute();
            } catch (Exception e) {
                MoPubLog.log(ERROR, "Error loading custom event", e);
            }
        } else {
            MoPubLog.log(CUSTOM, "Could not load custom event -- missing banner module");
        }
    }

    protected void registerClick() {
        if (mAdViewController != null) {
            mAdViewController.registerClick();

            // Let any listeners know that an ad was clicked
            adClicked();
        }
    }

    protected void trackNativeImpression() {
        MoPubLog.log(CUSTOM, "Tracking impression for native adapter.");
        if (mAdViewController != null) mAdViewController.trackImpression();
    }

    @Override
    protected void onWindowVisibilityChanged(final int visibility) {
        // Ignore transitions between View.GONE and View.INVISIBLE
        if (Visibility.hasScreenVisibilityChanged(mScreenVisibility, visibility)) {
            mScreenVisibility = visibility;
            setAdVisibility(mScreenVisibility);
        }
    }

    private void setAdVisibility(final int visibility) {
        if (mAdViewController == null) {
            return;
        }

        if (Visibility.isScreenVisible(visibility)) {
            mAdViewController.resumeRefresh();
        } else {
            mAdViewController.pauseRefresh();
        }
    }

    protected void adLoaded() {
        MoPubLog.log(LOAD_SUCCESS);
        if (mBannerAdListener != null) {
            mBannerAdListener.onBannerLoaded(this);
        }
    }

    protected void adFailed(MoPubErrorCode errorCode) {
        MoPubLog.log(LOAD_FAILED, errorCode.getIntCode(), errorCode);
        if (mBannerAdListener != null) {
            mBannerAdListener.onBannerFailed(this, errorCode);
        }
    }

    protected void adPresentedOverlay() {
        if (mBannerAdListener != null) {
            mBannerAdListener.onBannerExpanded(this);
        }
    }

    protected void adClosed() {
        MoPubLog.log(DID_DISAPPEAR);
        if (mBannerAdListener != null) {
            mBannerAdListener.onBannerCollapsed(this);
        }
    }

    protected void adClicked() {
        MoPubLog.log(CLICKED);
        if (mBannerAdListener != null) {
            mBannerAdListener.onBannerClicked(this);
        }
    }

    protected void creativeDownloaded() {
        if (mAdViewController != null) {
            mAdViewController.creativeDownloadSuccess();
        }
        adLoaded();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void setAdUnitId(String adUnitId) {
        if (mAdViewController != null) mAdViewController.setAdUnitId(adUnitId);
    }

    public String getAdUnitId() {
        return (mAdViewController != null) ? mAdViewController.getAdUnitId() : null;
    }

    public void setKeywords(String keywords) {
        if (mAdViewController != null) mAdViewController.setKeywords(keywords);
    }

    public String getKeywords() {
        return (mAdViewController != null) ? mAdViewController.getKeywords() : null;
    }

    public void setUserDataKeywords(String userDataKeywords) {
        if (mAdViewController != null && MoPub.canCollectPersonalInformation()) {
            mAdViewController.setUserDataKeywords(userDataKeywords);
        }
    }

    public String getUserDataKeywords() {
        return (mAdViewController != null && MoPub.canCollectPersonalInformation()) ? mAdViewController.getUserDataKeywords() : null;
    }

    public void setLocation(Location location) {
        if (mAdViewController != null && MoPub.canCollectPersonalInformation()) {
            mAdViewController.setLocation(location);
        }
    }

    public Location getLocation() {
        return (mAdViewController != null && MoPub.canCollectPersonalInformation()) ? mAdViewController.getLocation() : null;
    }

    public int getAdWidth() {
        return (mAdViewController != null) ? mAdViewController.getAdWidth() : 0;
    }

    public int getAdHeight() {
        return (mAdViewController != null) ? mAdViewController.getAdHeight() : 0;
    }

    @Nullable
    public AdReport getAdReport() {
        return mAdViewController != null ? mAdViewController.getAdReport() : null;
    }

    public Activity getActivity() {
        return (Activity) mContext;
    }

    public void setBannerAdListener(BannerAdListener listener) {
        mBannerAdListener = listener;
    }

    public void setBannerCustomEventAdListener(BannerCustomEventAdListener listener) {
        mBannerCustomEventAdListener = listener;
    }

    public BannerAdListener getBannerAdListener() {
        return mBannerAdListener;
    }

    public void setLocalExtras(Map<String, Object> localExtras) {
        if (mAdViewController != null) mAdViewController.setLocalExtras(localExtras);
    }

    public Map<String, Object> getLocalExtras() {
        if (mAdViewController != null) {
            return mAdViewController.getLocalExtras();
        }
        return new TreeMap<String, Object>();
    }

    public void setAutorefreshEnabled(boolean enabled) {
        if (mAdViewController != null) {
            mAdViewController.setShouldAllowAutoRefresh(enabled);
        }
    }

    void pauseAutorefresh() {
        if (mAdViewController != null) {
            mAdViewController.pauseRefresh();
        }
    }

    void resumeAutorefresh() {
        if (mAdViewController != null) {
            mAdViewController.resumeRefresh();
        }
    }

    void expand() {
        if (mAdViewController != null) {
            mAdViewController.expand();
        }
    }

    void collapse() {
        if (mAdViewController != null) {
            mAdViewController.collapse();
        }
    }

    public boolean getAutorefreshEnabled() {
        if (mAdViewController != null) return mAdViewController.getCurrentAutoRefreshStatus();
        else {
            MoPubLog.log(CUSTOM, "Can't get autorefresh status for destroyed MoPubView. " +
                    "Returning false.");
            return false;
        }
    }

    public void setAdContentView(View view) {
        MoPubLog.log(SHOW_ATTEMPTED);
        if (mAdViewController != null) {
            mAdViewController.setAdContentView(view);
            MoPubLog.log(SHOW_SUCCESS);
        } else {
            MoPubLog.log(SHOW_FAILED);
        }
    }

    public void setTesting(boolean testing) {
        if (mAdViewController != null) mAdViewController.setTesting(testing);
    }

    public boolean getTesting() {
        if (mAdViewController != null) return mAdViewController.getTesting();
        else {
            MoPubLog.log(CUSTOM, "Can't get testing status for destroyed MoPubView. " +
                    "Returning false.");
            return false;
        }
    }

    public void forceRefresh() {
        if (mCustomEventBannerAdapter != null) {
            invalidateAdapter();
            mCustomEventBannerAdapter = null;
        }

        if (mAdViewController != null) {
            mAdViewController.forceRefresh();
        }
    }

    AdViewController getAdViewController() {
        return mAdViewController;
    }

    public AdFormat getAdFormat() {
        return AdFormat.BANNER;
    }

    /**
     * @deprecated As of release 4.4.0
     */
    @Deprecated
    public void setTimeout(int milliseconds) {
    }

    @Deprecated
    public String getResponseString() {
        return null;
    }

    @Deprecated
    public String getClickTrackingUrl() {
        return null;
    }
}
