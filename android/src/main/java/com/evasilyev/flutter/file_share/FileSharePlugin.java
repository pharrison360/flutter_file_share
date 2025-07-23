package com.evasilyev.flutter.file_share;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import java.io.File;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

/** FileSharePlugin */
public class FileSharePlugin implements FlutterPlugin, MethodCallHandler, ActivityAware, PluginRegistry.ActivityResultListener {

    private static final int REQUEST_CODE = 1337;
    private static final String PROVIDER_EXTENSION = ".fileprovider";

    private MethodChannel channel;
    private Context context;
    private Activity activity;
    private Result pendingResult;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        context = flutterPluginBinding.getApplicationContext();
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "file_share");
        channel.setMethodCallHandler(this);
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
        channel = null;
        context = null;
    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        activity = binding.getActivity();
        binding.addActivityResultListener(this);
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
        activity = null;
    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
        activity = binding.getActivity();
        binding.addActivityResultListener(this);
    }

    @Override
    public void onDetachedFromActivity() {
        activity = null;
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        if (call.method.equals("shareFile")) {
            pendingResult = result;
            share(call, result);
        } else {
            result.notImplemented();
        }
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != REQUEST_CODE || pendingResult == null)
            return false;

        pendingResult.success(null);
        pendingResult = null;
        return true;
    }

    private void share(MethodCall call, Result result) {
        Args args = Args.fromCall(call);
        File fileToShare = new File(args.pathToFile);

        Uri fileUri;
        try {
            fileUri = FileProvider.getUriForFile(
                    context,
                    context.getPackageName() + PROVIDER_EXTENSION,
                    fileToShare
            );
        } catch (IllegalArgumentException e) {
            result.error("Error", e.getMessage(), null);
            return;
        }

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        shareIntent.setType(args.mimeType);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        Intent chooserIntent = Intent.createChooser(shareIntent, args.title);
        activity.startActivityForResult(chooserIntent, REQUEST_CODE);
    }

    private static class Args {
        final String title;
        final String pathToFile;
        final String mimeType;

        private Args(String pathToFile, String mimeType, String title) {
            this.pathToFile = pathToFile;
            this.mimeType = mimeType;
            this.title = title;
        }

        static Args fromCall(MethodCall call) {
            String pathToFile = getArg(call, "pathToFile");
            String mimeType = getArg(call, "mimeType");
            String title = getArg(call, "title");
            return new Args(pathToFile, mimeType, title);
        }

        private static <T> T getArg(MethodCall call, String argKey) {
            if (!call.hasArgument(argKey)) {
                throw new IllegalArgumentException("Missing argument: " + argKey);
            }
            return call.argument(argKey);
        }
    }
}
