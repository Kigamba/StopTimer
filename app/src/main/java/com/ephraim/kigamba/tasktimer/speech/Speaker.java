package com.ephraim.kigamba.tasktimer.speech;

/**
 * Created by Kigamba (nek.eam@gmail.com) on 07-June-2020
 */

import android.content.Context;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;

import static android.speech.tts.TextToSpeech.QUEUE_ADD;

import android.speech.tts.UtteranceProgressListener;

import java.util.ArrayList;
import java.util.Iterator;

import timber.log.Timber;

public class Speaker extends UtteranceProgressListener {

    private TextToSpeech textToSpeech;

    private ArrayList<String> speakingQueue = new ArrayList<>();
    private boolean isInitialised = false;

    public Speaker(Context context) {
        textToSpeech = new TextToSpeech(context, new OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    Timber.e("TTS Initialised successfully");
                    isInitialised = true;
                    processQueue();

                    textToSpeech.setOnUtteranceProgressListener(Speaker.this);
                } else {
                    Timber.e("TTS Initialisation failed");
                }
            }
        });

    }

    public void speak(String text) {
        speakingQueue.add(text);
        processQueue();
    }

    protected void processQueue() {
        if (isInitialised) {
            Iterator<String> queueIterator = speakingQueue.iterator();

            while (queueIterator.hasNext()) {
                String speechItem = queueIterator.next();
                if (Build.VERSION.SDK_INT >= 21) {
                    textToSpeech.speak(speechItem, QUEUE_ADD, null, speechItem);
                } else {
                    textToSpeech.speak(speechItem, QUEUE_ADD, null);
                }

                queueIterator.remove();
            }
        }
    }

    @Override
    public void onDone(String p0) {
        Timber.e("TTS onDone: %s", p0);
    }

    @Override
    public void onError(String p0) {
        Timber.e("TTS onError : %s", p0);
    }

    @Override
    public void onStart(String p0) {
        Timber.e("TTS onStart: %s", p0);
    }

    public void stop() {
        textToSpeech.stop();
        textToSpeech.shutdown();
    }

}