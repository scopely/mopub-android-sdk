// Copyright 2018-2019 Twitter, Inc.
// Licensed under the MoPub SDK License Agreement
// http://www.mopub.com/legal/sdk-license-agreement/

package com.mopub.mobileads;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.View;

import com.mopub.common.test.support.SdkTestRunner;
import com.mopub.common.util.Reflection;
import com.mopub.common.util.test.support.ShadowReflection;
import com.mopub.mobileads.test.support.TestAdViewControllerFactory;
import com.mopub.mobileads.test.support.TestCustomEventBannerAdapterFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.mopub.mobileads.MoPubErrorCode.ADAPTER_NOT_FOUND;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SdkTestRunner.class)
@Config(shadows = {ShadowReflection.class})
public class MoPubViewTest {
    private MoPubView subject;
    private Map<String,String> paramsMap = new HashMap<String, String>();
    private CustomEventBannerAdapter customEventBannerAdapter;
    private AdViewController adViewController;
    private Activity context;

    @Before
    public void setup() {
        context = spy(Robolectric.buildActivity(Activity.class).create().get());
        subject = new MoPubView(context);
        customEventBannerAdapter = TestCustomEventBannerAdapterFactory.getSingletonMock();
        reset(customEventBannerAdapter);
        adViewController = TestAdViewControllerFactory.getSingletonMock();
    }

    @Test
    public void screenStateBroadcastReceiver_withActionUserPresent_shouldUnpauseRefresh() throws Exception {
        broadcastIntent(new Intent(Intent.ACTION_USER_PRESENT));

        verify(adViewController).resumeRefresh();
    }

    @Test
    public void screenStateBroadcastReceiver_withActionScreenOff_shouldPauseRefersh() throws Exception {
        broadcastIntent(new Intent(Intent.ACTION_SCREEN_OFF));

        verify(adViewController).pauseRefresh();
    }

    @Test
    public void screenStateBroadcastReceiver_withNullIntent_shouldDoNothing() throws Exception {
        broadcastIntent(null);

        verify(adViewController, never()).pauseRefresh();
        verify(adViewController, never()).resumeRefresh();
    }

    @Test
    public void screenStateBroadcastReceiver_withRandomIntent_shouldDoNothing() throws Exception {
        broadcastIntent(new Intent(Intent.ACTION_BATTERY_LOW));

        verify(adViewController, never()).pauseRefresh();
        verify(adViewController, never()).resumeRefresh();
    }

    @Test
    public void screenStateBroadcastReceiver_whenAdInBackground_shouldDoNothing() throws Exception {
        subject.onWindowVisibilityChanged(View.INVISIBLE);
        reset(adViewController);

        broadcastIntent(new Intent(Intent.ACTION_USER_PRESENT));
        verify(adViewController, never()).resumeRefresh();

        broadcastIntent(new Intent(Intent.ACTION_SCREEN_OFF));
        verify(adViewController, never()).pauseRefresh();
    }

    @Test
    public void screenStateBroadcastReceiver_afterOnDestroy_shouldDoNothing() throws Exception {
        subject.destroy();

        broadcastIntent(new Intent(Intent.ACTION_USER_PRESENT));
        verify(adViewController, never()).resumeRefresh();

        broadcastIntent(new Intent(Intent.ACTION_SCREEN_OFF));
        verify(adViewController, never()).pauseRefresh();
    }

    @Test
    public void onWindowVisibilityChanged_fromVisibleToInvisible_shouldPauseRefresh() throws Exception {
        // Default visibility is View.VISIBLE
        subject.onWindowVisibilityChanged(View.INVISIBLE);

        verify(adViewController).pauseRefresh();
        verify(adViewController, never()).resumeRefresh();
    }


    @Test
    public void onWindowVisibilityChanged_fromInvisibleToVisible_shouldUnpauseRefresh() throws Exception {
        subject.onWindowVisibilityChanged(View.INVISIBLE);
        reset(adViewController);

        subject.onWindowVisibilityChanged(View.VISIBLE);

        verify(adViewController, never()).pauseRefresh();
        verify(adViewController).resumeRefresh();
    }

    @Test
    public void onWindowVisibilityChanged_fromVisibleToVisible_shouldDoNothing() throws Exception {
        // Default visibility is View.VISIBLE
        subject.onWindowVisibilityChanged(View.VISIBLE);

        verify(adViewController, never()).pauseRefresh();
        verify(adViewController, never()).resumeRefresh();
    }

    @Test
    public void onWindowVisibilityChanged_fromInvisibleToGone_shouldDoNothing() throws Exception {
        subject.onWindowVisibilityChanged(View.INVISIBLE);
        reset(adViewController);

        subject.onWindowVisibilityChanged(View.GONE);

        verify(adViewController, never()).pauseRefresh();
        verify(adViewController, never()).resumeRefresh();
    }

    @Test
    public void onWindowVisibilityChanged_fromGoneToInvisible_shouldDoNothing() throws Exception {
        subject.onWindowVisibilityChanged(View.GONE);
        reset(adViewController);

        subject.onWindowVisibilityChanged(View.INVISIBLE);

        verify(adViewController, never()).pauseRefresh();
        verify(adViewController, never()).resumeRefresh();
    }

    @Test
    public void setAutorefreshEnabled_withRefreshTrue_shouldForwardToAdViewController() throws Exception {
        subject.setAutorefreshEnabled(true);

        verify(adViewController).setShouldAllowAutoRefresh(true);
    }

    @Test
    public void setAutorefreshEnabled_withRefreshFalse_shouldForwardToAdViewController() throws Exception {
        subject.setAutorefreshEnabled(false);

        verify(adViewController).setShouldAllowAutoRefresh(false);
    }
    
    @Test
    public void creativeDownloaded_shouldCreativeDownloadSuccess() {
        subject.creativeDownloaded();

        verify(adViewController).creativeDownloadSuccess();
    }

    @Test
    public void loadCustomEvent_shouldInitializeCustomEventBannerAdapter() throws Exception {
        subject.loadCustomEvent("name", paramsMap);

        assertThat(TestCustomEventBannerAdapterFactory.getLatestMoPubView()).isEqualTo(subject);
        assertThat(TestCustomEventBannerAdapterFactory.getLatestClassName()).isEqualTo("name");
        assertThat(TestCustomEventBannerAdapterFactory.getLatestClassData()).isEqualTo(paramsMap);

        verify(customEventBannerAdapter).loadAd();
    }

    @Test
    public void loadCustomEvent_whenParamsMapIsNull_shouldCallLoadFailUrl() throws Exception {
        subject.loadCustomEvent(null, null);

        verify(adViewController).loadFailUrl(eq(ADAPTER_NOT_FOUND));
        verify(customEventBannerAdapter, never()).invalidate();
        verify(customEventBannerAdapter, never()).loadAd();
    }

    @Test
    public void loadCustomEvent_withTwoCalls_shouldInvalidateAdapterOnce() throws Exception {
        subject.loadCustomEvent("name", paramsMap);
        subject.loadCustomEvent("name", paramsMap);

        verify(customEventBannerAdapter).invalidate();
    }

    @Test
    public void forceRefresh_withCallToLoadCustomEvent_shouldInvalidateAdapter() throws Exception {
        subject.loadCustomEvent("name", paramsMap);
        subject.forceRefresh();

        verify(customEventBannerAdapter).invalidate();
    }

    @Test
    public void loadCustomEvent_withoutBannerModule_shouldNotLoadAd() throws Exception {
        ShadowReflection.setNextClassNotFound(true);

        subject.loadCustomEvent("name", paramsMap);

        verify(customEventBannerAdapter, never()).loadAd();
    }

    @Test
    public void forceRefresh_withoutBannerModule_withCallToLoadCustomEvent_shouldNotInvalidateAdapter() throws Exception {
        ShadowReflection.setNextClassNotFound(true);

        subject.loadCustomEvent("name", paramsMap);
        subject.forceRefresh();

        verify(customEventBannerAdapter, never()).invalidate();
    }

    @Test
    public void forceRefresh_withoutBannerModule_withCallToLoadCustomEvent_shouldForceRefreshAdViewController() throws Exception {
        ShadowReflection.setNextClassNotFound(true);

        subject.loadCustomEvent("name", paramsMap);
        subject.forceRefresh();

        verify(adViewController).forceRefresh();
    }

    @Test
    public void invalidateAdapter_withReflection_shouldExist() throws Exception {
        assertThat(Reflection.getDeclaredMethodWithTraversal(CustomEventBannerAdapter.class,
                "invalidate")).isNotNull();
    }

    @Test
    public void loadAd_withoutRequestedAdSize_shouldSetRequestedAdSizeToZeroZero() throws Exception {
        subject.loadAd();
        final Point point = new Point(0, 0);
        verify(adViewController).setRequestedAdSize(point);
    }

    @Test
    public void loadAd_withAdSize50Height_withFhdScreen_withDensityOf1_shouldResolveWithHeight50() {
        final float density = 1.0f;
        final MoPubView.MoPubAdSize adSize = MoPubView.MoPubAdSize.HEIGHT_50;

        // Set the expected screen dimensions
        final Resources spyResources = spy(context.getResources());
        final DisplayMetrics mockDisplayMetrics = mock(DisplayMetrics.class);
        mockDisplayMetrics.widthPixels = 1080;
        mockDisplayMetrics.heightPixels = 1920;
        mockDisplayMetrics.density = density;
        when(spyResources.getDisplayMetrics()).thenReturn(mockDisplayMetrics);
        when(context.getResources()).thenReturn(spyResources);

        final Point point = new Point(0, (int)(adSize.toInt() * density));
        subject.loadAd(adSize);
        verify(adViewController).setRequestedAdSize(point);
    }

    @Test
    public void loadAd_withAdSize90Height_withFhdScreen_withDensityOf1_shouldResolveWithHeight90() {
        final float density = 1.0f;
        final MoPubView.MoPubAdSize adSize = MoPubView.MoPubAdSize.HEIGHT_90;

        // Set the expected screen dimensions
        final Resources spyResources = spy(context.getResources());
        final DisplayMetrics mockDisplayMetrics = mock(DisplayMetrics.class);
        mockDisplayMetrics.widthPixels = 1080;
        mockDisplayMetrics.heightPixels = 1920;
        mockDisplayMetrics.density = density;
        when(spyResources.getDisplayMetrics()).thenReturn(mockDisplayMetrics);
        when(context.getResources()).thenReturn(spyResources);

        final Point point = new Point(0, (int)(adSize.toInt() * density));
        subject.loadAd(adSize);
        verify(adViewController).setRequestedAdSize(point);
    }

    @Test
    public void loadAd_withAdSize250Height_withFhdScreen_withDensityOf2_shouldResolveWithHeight500() {
        final float density = 2.0f;
        final MoPubView.MoPubAdSize adSize = MoPubView.MoPubAdSize.HEIGHT_250;

        // Set the expected screen dimensions
        final Resources spyResources = spy(context.getResources());
        final DisplayMetrics mockDisplayMetrics = mock(DisplayMetrics.class);
        mockDisplayMetrics.widthPixels = 1080;
        mockDisplayMetrics.heightPixels = 1920;
        mockDisplayMetrics.density = density;
        when(spyResources.getDisplayMetrics()).thenReturn(mockDisplayMetrics);
        when(context.getResources()).thenReturn(spyResources);

        final Point point = new Point(0, (int)(adSize.toInt() * density));
        subject.loadAd(adSize);
        verify(adViewController).setRequestedAdSize(point);
    }

    @Test
    public void loadAd_withAdSize280Height_withFhdScreen_withDensityOf3_shouldResolveWithHeight840() {
        final float density = 3.0f;
        final MoPubView.MoPubAdSize adSize = MoPubView.MoPubAdSize.HEIGHT_280;

        // Set the expected screen dimensions
        final Resources spyResources = spy(context.getResources());
        final DisplayMetrics mockDisplayMetrics = mock(DisplayMetrics.class);
        mockDisplayMetrics.widthPixels = 1080;
        mockDisplayMetrics.heightPixels = 1920;
        mockDisplayMetrics.density = density;
        when(spyResources.getDisplayMetrics()).thenReturn(mockDisplayMetrics);
        when(context.getResources()).thenReturn(spyResources);

        final Point point = new Point(0, (int)(adSize.toInt() * density));
        subject.loadAd(adSize);
        verify(adViewController).setRequestedAdSize(point);
    }

    private void broadcastIntent(final Intent intent) {
        final List<ShadowApplication.Wrapper> wrappers = ShadowApplication.getInstance().getRegisteredReceivers();

        for (final ShadowApplication.Wrapper wrapper : wrappers) {
            wrapper.broadcastReceiver.onReceive(context, intent);
        }
    }
}
