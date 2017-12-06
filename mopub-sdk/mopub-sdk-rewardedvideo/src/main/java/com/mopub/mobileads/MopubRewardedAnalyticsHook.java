package com.mopub.mobileads;

import java.util.Map;
import java.util.Set;

/**
 * Part of the Scopely™ Platform
 * © 2017 Scopely, Inc.
 */

public interface MopubRewardedAnalyticsHook {
    <T extends CustomEventRewardedAd> void onRewardedVideoLoadSuccess(Class<T> customEventClass, String thirdPartyId, Set<String> adUnitIds);
    <T extends CustomEventRewardedAd> void onRewardedVideoLoadFailure(Class<T> customEventClass, String thirdPartyId, MoPubErrorCode errorCode, Set<String> adUnitIds);
    <T extends CustomEventRewardedAd> void onLoadCustomEvent(Class<T> customEvenClass, String adUnitId, Map<String, Object> localExtras, Map<String, String> serverExtras);
}
