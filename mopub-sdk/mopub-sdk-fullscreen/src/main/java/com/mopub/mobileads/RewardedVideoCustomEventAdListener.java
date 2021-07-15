package com.mopub.mobileads;

import com.mopub.network.ImpressionData;

public interface RewardedVideoCustomEventAdListener {
    void onCustomEventRewardedVideoAttempted(String adUnitId, String customEventClass, String lineItemId);
    void onCustomEventRewardedVideoAttemptSucceeded(String adUnitId, String creativeId, ImpressionData impressionData);
    void onCustomEventRewardedVideoFailed(String adUnitId, MoPubErrorCode errorCode, ImpressionData impressionData);
}