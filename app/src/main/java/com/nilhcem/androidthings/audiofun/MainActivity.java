package com.nilhcem.androidthings.audiofun;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;
import java.util.List;

public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int RESULT_SPEECH = 1;
    private static final String BUTTON_GPIO = "BCM21";

    private Gpio button;
    private TtsSpeaker tts;

    private final GpioCallback buttonCallback = new GpioCallback() {
        @Override
        public boolean onGpioEdge(Gpio gpio) {
            startSpeechToTextActivity();
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tts = new TtsSpeaker(this);

        try {
            button = new PeripheralManagerService().openGpio(BUTTON_GPIO);
            button.setDirection(Gpio.DIRECTION_IN);
            button.setEdgeTriggerType(Gpio.EDGE_FALLING);
            button.registerGpioCallback(buttonCallback);
        } catch (IOException e) {
            Log.w(TAG, "Unable to setup button", e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_SPEECH && resultCode == RESULT_OK && data != null) {
            List<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String spokenText = result.get(0);
            Log.i(TAG, "Text: " + spokenText);
            tts.say(spokenText);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tts.onDestroy();

        try {
            button.unregisterGpioCallback(buttonCallback);
            button.close();
        } catch (IOException e) {
            Log.e(TAG, "Unable to close button", e);
        }
    }

    private void startSpeechToTextActivity() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");

        try {
            startActivityForResult(intent, RESULT_SPEECH);
        } catch (ActivityNotFoundException a) {
            Log.e(TAG, "Your device does not support Speech to Text");
        }
    }
}
