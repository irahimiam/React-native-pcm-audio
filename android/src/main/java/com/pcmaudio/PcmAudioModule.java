package com.pcmaudio;

import androidx.annotation.NonNull;

import android.util.Base64;
import android.media.AudioTrack;
import android.media.AudioManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.module.annotations.ReactModule;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.modules.core.DeviceEventManagerModule;


@ReactModule(name = PcmAudioModule.NAME)
public class PcmAudioModule extends ReactContextBaseJavaModule {
  public static final String NAME = "PcmAudio";

  private final ReactApplicationContext reactContext;
  private DeviceEventManagerModule.RCTDeviceEventEmitter eventEmitter;

  private AudioTrack track;
  private AudioRecord recorder;

  private boolean isRecording;

  private int playerSampleRateInHz;
  private int playerChannelConfig;
  private int playerAudioFormat;
  private int playerBufferSize;

  private int recorderSampleRateInHz;
  private int recorderChannelConfig;
  private int recorderAudioFormat;
  private int recorderAudioSource;
  private int recorderBufferSize;

  private Promise stopRecordingPromise;


  public PcmAudioModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
  }

  @Override
  @NonNull
  public String getName() {
    return NAME;
  }

  @ReactMethod
  public void initializePCMPlayer(ReadableMap options) {
    try {
      playerSampleRateInHz = 44100;
      if (options.hasKey("sampleRate")) {
        playerSampleRateInHz = options.getInt("sampleRate");
      }

      playerChannelConfig = AudioFormat.CHANNEL_OUT_MONO;
      if (options.hasKey("channelConfig")) {
        playerChannelConfig = options.getInt("channelConfig");
      }

      playerAudioFormat = AudioFormat.ENCODING_PCM_16BIT;
      if (options.hasKey("bitsPerSample")) {
        if (options.getInt("bitsPerSample") == 8) {
          playerAudioFormat = AudioFormat.ENCODING_PCM_8BIT;
        }
      }

      playerBufferSize = 1024;
      if (options.hasKey("bufferSize")) {
        playerBufferSize = options.getInt("bufferSize");
      }

      track = new AudioTrack(AudioManager.STREAM_MUSIC, playerSampleRateInHz, playerChannelConfig, playerAudioFormat, playerBufferSize, AudioTrack.MODE_STREAM);
      track.play();
    } catch (Exception e) {
       e.printStackTrace();
    }
  }

  @ReactMethod
  public void initializePCMRecorder(ReadableMap options) {
    recorderSampleRateInHz = 44100;
    if (options.hasKey("sampleRate")) {
      recorderSampleRateInHz = options.getInt("sampleRate");
    }

    recorderChannelConfig = AudioFormat.CHANNEL_IN_MONO;
    if (options.hasKey("channelConfig")) {
      if (options.getInt("channelConfig") == 2) {
        recorderChannelConfig = AudioFormat.CHANNEL_IN_STEREO;
      }
    }

    recorderAudioFormat = AudioFormat.ENCODING_PCM_16BIT;
    if (options.hasKey("bitsPerSample")) {
      if (options.getInt("bitsPerSample") == 8) {
        recorderAudioFormat = AudioFormat.ENCODING_PCM_8BIT;
      }
    }

    recorderAudioSource = AudioSource.VOICE_RECOGNITION;
    if (options.hasKey("audioSource")) {
      recorderAudioSource = options.getInt("audioSource");
    }

    recorderBufferSize = AudioRecord.getMinBufferSize(recorderSampleRateInHz, recorderChannelConfig, recorderAudioFormat);
    if (options.hasKey("bufferSize")) {
      recorderBufferSize = options.getInt("bufferSize");
    }

    isRecording = false;
    eventEmitter = reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class);

    recorder = new AudioRecord(recorderAudioSource, recorderSampleRateInHz, recorderChannelConfig, recorderAudioFormat, recorderBufferSize);
  }

  @ReactMethod
  public void startPlayer(ReadableMap buffer, Promise promise) {
    ReadableArray data = buffer.getArray("data");

    int size = data.size();
    byte[] toWrite = new byte[size];
    try {
      for (int i = 0; i < size; i++) {
        toWrite[i] = (byte) (data.getInt(i));
      }
      track.write(toWrite, 0, size);
      promise.resolve(true);
    } catch (Exception e) {
      promise.reject("Play audio buffer error.", e);
    }
  }

  @ReactMethod
  public void startRecorder() {
    isRecording = true;
    recorder.startRecording();

    Thread recordingThread = new Thread(new Runnable() {
      public void run() {
        try {
          int bytesRead;
          String base64Data;
          byte[] buffer = new byte[recorderBufferSize];

          while (isRecording) {
            bytesRead = recorder.read(buffer, 0, buffer.length);

            if (bytesRead > 0) {
              try {
                base64Data = Base64.encodeToString(buffer, Base64.NO_WRAP);

                eventEmitter.emit("data", base64Data);
              } catch (Exception x) {
                x.printStackTrace();
              }
            }
          }

          recorder.stop();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });

    recordingThread.start();
  }


  @ReactMethod
  public void stopPlayer(Promise promise) {
    try {
      track.release();
      promise.resolve(true);
    } catch (Exception e) {
      promise.reject("Uninitialized pcm player error.", e);
    }
  }

  @ReactMethod
  public void stopRecorder(Promise promise) {
    isRecording = false;
    stopRecordingPromise = promise;
  }
}
