import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() {
  runApp(const MyApp());
}

const platform = MethodChannel('audio_channel');

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Audio Test',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.deepPurple),
        useMaterial3: true,
      ),
      home: const MyHomePage(title: 'Audio Test'),
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({super.key, required this.title});
  final String title;
  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  bool isRecording = false;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor: Theme.of(context).colorScheme.inversePrimary,
        title: Text(widget.title),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            MaterialButton(
              onPressed: () {
                callNativeRecord(!isRecording);
                setState(() {
                  isRecording = !isRecording;
                });
              } ,
              child: Text( isRecording ? "Stop Recording" : "Start Recording"),
            ),
          ],
        ),
      ),
    );
  }

  Future<void> callNativeRecord(bool shouldRecord) async {
    try {
      if(shouldRecord) {
        await platform.invokeMethod('startRecording');
      } else {
        await platform.invokeMethod('stopRecording');
      }
    } catch (e) {
      print('Error calling native method: $e');
    }
  }
}
