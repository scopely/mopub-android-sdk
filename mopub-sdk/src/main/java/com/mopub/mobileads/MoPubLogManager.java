package java.com.mopub.mobileads;

import java.util.HashMap;
import java.util.Map;

/**
 * Part of the Scopely™ Platform
 * © 2013 Scopely, Inc.
 */
public class MoPubLogManager {
    @SuppressWarnings("UnusedDeclaration")
    private static final String TAG = MoPubLogManager.class.getCanonicalName();

    public interface MoPubLogManagerDelegate {
        public void logMessage(String message);
    }

    private static Map<MoPubLogManagerDelegate, Integer> moPubLogManagerDelegateIntegerMap;

    private static Map<MoPubLogManagerDelegate, Integer> getMoPubLogManagerDelegateIntegerMap() {
        if(moPubLogManagerDelegateIntegerMap == null) {
            moPubLogManagerDelegateIntegerMap = new HashMap<MoPubLogManagerDelegate, Integer>();
        }
        return moPubLogManagerDelegateIntegerMap;
    }

    public static synchronized void registerMoPubLogManagerDelegate(MoPubLogManagerDelegate moPubLogManagerDelegate) {
        Map<MoPubLogManagerDelegate, Integer> moPubLogManagerDelegateIntegerMap = getMoPubLogManagerDelegateIntegerMap();
        if(!moPubLogManagerDelegateIntegerMap.containsKey(moPubLogManagerDelegate)) {
            moPubLogManagerDelegateIntegerMap.put(moPubLogManagerDelegate, 0);
        }
        moPubLogManagerDelegateIntegerMap.put(moPubLogManagerDelegate, moPubLogManagerDelegateIntegerMap.get(moPubLogManagerDelegate) + 1);
    }

    public static synchronized void unregisterMoPubLogManagerDelegate(MoPubLogManagerDelegate moPubLogManagerDelegate) {
        Map<MoPubLogManagerDelegate, Integer> moPubLogManagerDelegateIntegerMap = getMoPubLogManagerDelegateIntegerMap();
        if(!moPubLogManagerDelegateIntegerMap.containsKey(moPubLogManagerDelegate)) {
            throw new RuntimeException("trying to unregister an unregistered delegate.");
        }
        moPubLogManagerDelegateIntegerMap.put(moPubLogManagerDelegate, moPubLogManagerDelegateIntegerMap.get(moPubLogManagerDelegate) - 1);
        if(moPubLogManagerDelegateIntegerMap.get(moPubLogManagerDelegate) == 0) {
            moPubLogManagerDelegateIntegerMap.remove(moPubLogManagerDelegate);
        }
    }

    public static synchronized void logMessage(String message) {
        Map<MoPubLogManagerDelegate, Integer> moPubLogManagerDelegateIntegerMap = getMoPubLogManagerDelegateIntegerMap();
        for(MoPubLogManagerDelegate moPubLogManagerDelegate : moPubLogManagerDelegateIntegerMap.keySet()) {
            moPubLogManagerDelegate.logMessage(message);
        }
    }
}
