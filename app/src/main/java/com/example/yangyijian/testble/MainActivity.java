package com.example.yangyijian.testble;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private String TAG = "BluetoothRecord";
    private static final int RECORDER_SAMPLERATE = 8000;
    private static final int RECORDER_CHANNELS_IN = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_CHANNELS_OUT = AudioFormat.CHANNEL_OUT_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private static final int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;

    private int bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE, RECORDER_CHANNELS_IN,
            RECORDER_AUDIO_ENCODING) * 3;
    private AudioRecord recorder = null;
    private Thread recordingThread = null;
    private boolean isRecording = false;

    private Button start_button, stop_button;

    private byte audioBuffer[] = new byte[bufferSize];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        start_button = findViewById(R.id.button1);
        stop_button = findViewById(R.id.button2);
        start_button.setOnClickListener(this);
        stop_button.setOnClickListener(this);
    }

    private void startRecording() {
        if (bufferSize == AudioRecord.ERROR_BAD_VALUE) {
            Log.e(TAG, "Bad Value for ");
        }
        if (bufferSize == AudioRecord.ERROR) {
            Log.e(TAG, "Bad Value for operties");
        }
        // Initialize Audio Recorder.
        recorder = new AudioRecord(AUDIO_SOURCE, RECORDER_SAMPLERATE, RECORDER_CHANNELS_IN,
                RECORDER_AUDIO_ENCODING, bufferSize);
        // Starts recording from the AudioRecord instance.
        recorder.startRecording();
        isRecording = true;
        recordingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                //writeAudioDataToFile();
                processAudioData();
            }
        }, "AudioRecorder Thread");
        recordingThread.start();
    }

    private void processAudioData(){
        while (isRecording) {
            // gets the voice output from microphone to byte format
            recorder.read(audioBuffer, 0, bufferSize);
        }
    }

    private void writeAudioDataToFile() {
        //Write the output audio in byte
        String filePath = "/sdcard/8k16bitMono.pcm";
        byte saudioBuffer[] = new byte[bufferSize];
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(filePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        while (isRecording) {
            // gets the voice output from microphone to byte format
            recorder.read(saudioBuffer, 0, bufferSize);
            try {
                //  writes the data to file from buffer stores the voice buffer
                os.write(saudioBuffer, 0, bufferSize);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopRecording() throws IOException {
        //  stops the recording activity
        if (null != recorder) {
            isRecording = false;
            recorder.stop();
            recorder.release();
            recorder = null;
            recordingThread = null;
            showAduipData();
            Log.d(TAG, "stopRecording: " +bufferSize);
            //PlayShortAudioFileViaAudioTrack("/sdcard/8k16bitMono.pcm");
        }
    }

    private void showAduipData(){
        String str = "";
        for(int i=0;i<bufferSize;++i){
            str += audioBuffer[i] + " ";
        }
        Log.d(TAG, str);
    }

    private void PlayShortAudioFileViaAudioTrack(String filePath) throws IOException {
        // We keep temporarily filePath globally as we have only two sample sounds now..
        if (filePath == null) {
            return;
        }
        //Reading the file..
        // for ex. path= "/sdcard/samplesound.pcm" or "/sdcard/samplesound.wav"
        File file = new File(filePath);
        byte[] byteData = new byte[(int) file.length()];
        Log.d(TAG, (int) file.length() + "");
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            in.read(byteData);
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // Set and push to audio track..
        int intSize = android.media.AudioTrack.getMinBufferSize(RECORDER_SAMPLERATE, RECORDER_CHANNELS_OUT, RECORDER_AUDIO_ENCODING);
        Log.d(TAG, intSize + "");
        AudioTrack at = new AudioTrack(AudioManager.STREAM_MUSIC, RECORDER_SAMPLERATE, RECORDER_CHANNELS_OUT, RECORDER_AUDIO_ENCODING, intSize, AudioTrack.MODE_STREAM);
        if (at != null) {
            at.play();
            // Write the byte array to the track
            at.write(byteData, 0, byteData.length);
            at.stop();
            at.release();
        } else {
            Log.d(TAG, "audio track is not initialised ");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button1: {
                startRecording();
                Log.d(TAG, "onClick: ");
                break;
            }
            case R.id.button2: {
                try {
                    stopRecording();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
            default:
        }
    }
}
