package org.solovyev.android.calculator;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class Broadcaster implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String ACTION_INIT = "org.solovyev.android.calculator.INIT";
    public static final String ACTION_EDITOR_STATE_CHANGED = "org.solovyev.android.calculator.EDITOR_STATE_CHANGED";
    public static final String ACTION_DISPLAY_STATE_CHANGED = "org.solovyev.android.calculator.DISPLAY_STATE_CHANGED";
    public static final String ACTION_THEME_CHANGED = "org.solovyev.android.calculator.THEME_CHANGED";
    @Nonnull
    private final Context context;
    @Nonnull
    private final Intents intents = new Intents();

    @Inject
    public Broadcaster(@Nonnull Application application, @Nonnull SharedPreferences preferences, @Nonnull Bus bus, @Nonnull Handler handler) {
        this.context = application;
        preferences.registerOnSharedPreferenceChangeListener(this);
        bus.register(this);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // we must update the widget when app starts
                sendInitIntent();
            }
        }, 100);
    }

    @Subscribe
    public void onEditorChanged(@Nonnull Editor.ChangedEvent e) {
        sendBroadcastIntent(ACTION_EDITOR_STATE_CHANGED);
    }

    @Subscribe
    public void onDisplayChanged(@Nonnull Display.ChangedEvent e) {
        sendBroadcastIntent(ACTION_DISPLAY_STATE_CHANGED);
    }

    @Subscribe
    public void onCursorMoved(@Nonnull Editor.CursorMovedEvent e) {
        sendBroadcastIntent(ACTION_EDITOR_STATE_CHANGED);
    }

    public void sendInitIntent() {
        sendBroadcastIntent(ACTION_INIT);
    }

    public void sendBroadcastIntent(@Nonnull String action) {
        context.sendBroadcast(intents.get(action));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (Preferences.Gui.theme.isSameKey(key) || Preferences.Widget.theme.isSameKey(key)) {
            sendBroadcastIntent(ACTION_THEME_CHANGED);
        }
    }

    private static final class Intents {
        @Nonnull
        private Map<String, Intent> map = new HashMap<>();

        @Nonnull
        Intent get(@Nonnull String action) {
            Intent intent = map.get(action);
            if (intent != null) {
                return intent;
            }
            intent = new Intent(action);
            map.put(action, intent);
            return intent;
        }
    }
}
