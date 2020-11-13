package com.mopub.mobileads;

import com.mopub.network.ImpressionData;

public interface InterstitialCustomEventAdListener {
    void onCustomEventInterstitialAttempted(MoPubInterstitial interstitial, String customEventClassName, String lineItemId);
    void onCustomEventInterstitialAttemptSucceeded(MoPubInterstitial interstitial, String creativeId, ImpressionData impressionData);
    void onCustomEventInterstitialFailed(MoPubInterstitial interstitial, MoPubErrorCode errorCode);
}