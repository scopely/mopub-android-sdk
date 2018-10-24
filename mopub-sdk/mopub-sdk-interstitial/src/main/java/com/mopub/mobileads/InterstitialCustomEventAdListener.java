package com.mopub.mobileads;

public interface InterstitialCustomEventAdListener {
    void onCustomEventInterstitialAttempted(String customEventClassName);
    void onCustomEventInterstitialFailed(String customEventClassName);
}
