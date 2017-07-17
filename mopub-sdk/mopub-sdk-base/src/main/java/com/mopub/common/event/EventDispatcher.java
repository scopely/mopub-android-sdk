package com.mopub.common.event;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.mopub.common.VisibleForTesting;
import com.mopub.common.logging.MoPubLog;

import java.util.Collection;

public class EventDispatcher {
    private final Collection<EventRecorder> mEventRecorders;
    private final Looper mLooper;
    private final Handler mMessageHandler;
    private final Handler.Callback mHandlerCallback;

    @VisibleForTesting
    EventDispatcher(Collection<EventRecorder> recorders, Looper looper) {
        mEventRecorders = recorders;
        mLooper = looper;
        mHandlerCallback = new Handler.Callback() {
            @Override
            public boolean handleMessage(final Message msg) {
                if (msg.obj instanceof BaseEvent) {
                    for (final EventRecorder recorder : mEventRecorders) {
                        recorder.record((BaseEvent) msg.obj);
                    }
                } else {
                    MoPubLog.d("EventDispatcher received non-BaseEvent message type.");
                }
                return true;
            }
        };
        mMessageHandler = new Handler(mLooper, mHandlerCallback);
    }

    public void dispatch(BaseEvent event) {
        Message.obtain(mMessageHandler, 0, event).sendToTarget();
    }

    @VisibleForTesting
    Collection<EventRecorder> getEventRecorders() {
        return mEventRecorders;
    }

    @VisibleForTesting
    Handler.Callback getHandlerCallback() {
        return mHandlerCallback;
    }

    public void registerEventRecorder(EventRecorder eventRecorder) {
        if(!mEventRecorders.contains(eventRecorder)){
            mEventRecorders.add(eventRecorder);
        }
    }

    public void unregisterEventRecorder(EventRecorder eventRecorder) {
        mEventRecorders.remove(eventRecorder);
    }
}
