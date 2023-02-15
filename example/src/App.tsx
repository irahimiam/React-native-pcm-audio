import * as React from 'react';

import {
  StyleSheet,
  View,
  Text,
  NativeEventEmitter,
  NativeModules,
  TouchableOpacity,
  PermissionsAndroid, ScrollView
} from 'react-native';
import PCMAudio from 'react-native-pcm-audio';
import { Buffer } from "buffer";

export default function App() {
  const [messages, setMessages] = React.useState([])

  const getData = async () => {
    // @ts-ignore
    // PCMAudio.sayHello("Arash Rahimi, khobi?")

    const eventEmitter = new NativeEventEmitter(NativeModules.PCMAudio);

    eventEmitter.addListener("data", (data) => {
      const chunk = Buffer.from(data, 'base64');
      setMessages([JSON.stringify(chunk)])
    })


    const granted = await PermissionsAndroid.request(
      // @ts-ignore
      PermissionsAndroid.PERMISSIONS.RECORD_AUDIO ,
      {
        title: "Cool Photo App Audio Permission",
        message: "Cool shit app needs access to your mic ",
        buttonNeutral: "Ask Me Later",
        buttonNegative: "Cancel",
        buttonPositive: "OK"
      }
    );

    if (granted) {
    //  const eventEmitter = new NativeEventEmitter(NativeModules.PCMAudio);

      PCMAudio.initializePCMRecorder({
        bufferSize: 1024
      });
      PCMAudio.startRecorder();

/*      eventEmitter.addListener("data", (data) => {
        setMessage(JSON.stringify(data))
      }) */
    }
  }


  return (
    <View style={styles.container}>
      <TouchableOpacity onPress={() => getData()}>
        <Text>
          press to get data
        </Text>
      </TouchableOpacity>
      <ScrollView style={{height: 300, width: '100%'}}>
        {messages.map((m: any) => (
          <Text>{m}</Text>
        ))}
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  box: {
    width: 60,
    height: 60,
    marginVertical: 20,
  },
});
