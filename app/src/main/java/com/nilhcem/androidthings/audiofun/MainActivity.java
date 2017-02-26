package com.nilhcem.androidthings.audiofun;

import android.app.Activity;
import android.os.Bundle;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity implements TtsSpeaker.Listener, PocketSphinx.Listener {

    private enum State {
        INITALIZING,
        LISTENING_TO_KEYPHRASE,
        CONFIRMING_KEYPHRASE,
        LISTENING_TO_ACTION,
        CONFIRMING_ACTION,
        TIMEOUT
    }

    private TtsSpeaker tts;
    private PocketSphinx pocketsphinx;
    private State state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tts = new TtsSpeaker(this, this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tts.onDestroy();
        pocketsphinx.onDestroy();
    }

    @Override
    public void onTtsInitialized() {
        // There's no runtime permissions on Android Things.
        // Otherwise, we would first have to ask for the Manifest.permission.RECORD_AUDIO
        pocketsphinx = new PocketSphinx(this, this);
    }

    @Override
    public void onTtsSpoken() {
        switch (state) {
            case INITALIZING:
            case CONFIRMING_ACTION:
            case TIMEOUT:
                state = State.LISTENING_TO_KEYPHRASE;
                pocketsphinx.startListeningToActivationPhrase();
                break;
            case CONFIRMING_KEYPHRASE:
                state = State.LISTENING_TO_ACTION;
                pocketsphinx.startListeningToAction();
                break;
        }
    }

    @Override
    public void onSpeechRecognizerReady() {
        state = State.INITALIZING;
        tts.say("I'm ready!");
    }

    @Override
    public void onActivationPhraseDetected() {
        state = State.CONFIRMING_KEYPHRASE;
        tts.say("Yup?");
    }

    @Override
    public void onTextRecognized(String recognizedText) {
        state = State.CONFIRMING_ACTION;

        String answer;
        String input = recognizedText == null ? "" : recognizedText;
        if (input.contains("tv")) {
            answer = "No, you need to work!";
        } else if (input.contains("time")) {
            DateFormat dateFormat = new SimpleDateFormat("HH mm", Locale.US);
            answer = "It is " + dateFormat.format(new Date());
        } else if (input.matches(".* joke")) {
            answer = "You are a joke.";
        } else if (input.contains("weather")) {
            answer = "Buy me some sensors, and I will tell you.";
        } else if (input.matches("how are you.*")) {
            answer = "Could not be worst with you.";
        } else {
            answer = "Sorry, I didn't understand your poor English.";
        }
        tts.say(answer);
    }

    @Override
    public void onTimeout() {
        state = State.TIMEOUT;
        tts.say("Timeout! You're too slow");
    }
}
