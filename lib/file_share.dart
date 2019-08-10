import 'dart:async';

import 'package:flutter/services.dart';

class FileShare {
  static const MethodChannel _channel = const MethodChannel('file_share');

  static Future<void> share(String pathToFile, String mimeType,
      {String title, String text}) async {
    await _channel.invokeMethod("shareFile", {
      "pathToFile": pathToFile,
      "mimeType": mimeType,
      "title": title,
      "text": text
    });
  }
}
