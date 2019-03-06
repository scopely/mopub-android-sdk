package com.mopub.mobileads;

public interface InterstitialCustomEventAdListener {
    void onCustomEventInterstitialAttempted(MoPubInterstitial interstitial, String customEventClassName);
    void onCustomEventInterstitialAttemptSucceeded(MoPubInterstitial interstitial, String creativeId, String lineItemId);
    void onCustomEventInterstitialFailed(MoPubInterstitial interstitial, MoPubErrorCode errorCode);
}
