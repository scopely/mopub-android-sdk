package com.mopub.mobileads;

public interface RewardedVideoCustomEventAdListener {
    void onCustomEventRewardedVideoAttempted(String adUnitId, String customEventClass);
    void onCustomEventRewardedVideoAttemptSucceeded(String adUnitId, String creativeId, String lineItemId);
    void onCustomEventRewardedVideoFailed(String adUnitId, MoPubErrorCode errorCode);
}
