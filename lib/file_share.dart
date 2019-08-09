import 'dart:async';

import 'package:flutter/services.dart';

class FileShare {
  static const MethodChannel _channel = const MethodChannel('file_share');

  static Future<void> share(String pathToFile, String mimeType, {title}) async {
    await _channel.invokeMethod("shareFile",
        {"pathToFile": pathToFile, "title": title, "mimeType": mimeType});
  }
}
