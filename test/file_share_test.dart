import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:file_share/file_share.dart';

void main() {
  const MethodChannel channel = MethodChannel('file_share');

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('share throws an exception if there are any errors', () async {
    //expect(await FileShare.share(""), throwsException);
  });
}
