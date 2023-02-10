import { NativeModules, Platform } from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-pcm-audio' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

const PcmAudio = NativeModules.PcmAudio
  ? NativeModules.PcmAudio
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );


export interface PCMAPlayerOptions  {
  sampleRate?: number;
  channelConfig?: 1 | 2;
  bitsPerSample?: 8 | 16;
  bufferSize?: number;
}

export interface PCMARecorderOptions  {
  sampleRate?: number;
  channelConfig?: 1 | 2;
  bitsPerSample?: 8 | 16;
  audioSource?: number;
  bufferSize?: number;
}

export interface PCMAudioProps {
  initializePCMPlayer: (options?: PCMAPlayerOptions) => void;
  initializePCMRecorder: (options?: PCMARecorderOptions) => void;
  startRecorder: () => void;
  stopRecorder: () => Promise<string>;
  startPlayer: () => void;
  stopPlayer: () => Promise<string>;
  on: (event: "data", callback: (data: string) => void) => void
}

export default PcmAudio as PCMAudioProps;
