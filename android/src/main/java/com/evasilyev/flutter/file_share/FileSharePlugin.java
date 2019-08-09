package com.evasilyev.flutter.file_share;

import android.content.Intent;
import android.net.Uri;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import java.io.File;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/**
 * FileSharePlugin
 */
public class FileSharePlugin implements MethodCallHandler {

    private static Registrar registrar;

    /**
     * Plugin registration.
     */
    public static void registerWith(Registrar registrar) {
        FileSharePlugin.registrar = registrar;
        final MethodChannel channel = new MethodChannel(registrar.messenger(), "file_share");
        channel.setMethodCallHandler(new FileSharePlugin());
    }

    @Override
    public void onMethodCall(MethodCall call, Result result) {
        if (call.method.equals("shareFile")) {
            share(call, result);
        } else {
            result.notImplemented();
        }
    }

    private void share(MethodCall call, Result result) {
        Args args = Args.fromCall(call);

        File fileToShare = new File(args.pathToFile);
        Uri fileUri;
        try {
            fileUri = FileProvider.getUriForFile(
                    registrar.context(),
                    registrar.activeContext().getPackageName() + ".fileprovider",
                    fileToShare
            );
        } catch (IllegalArgumentException e) {
            result.error("Error", e.getMessage(), null);
            return;
        }

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        shareIntent.setType(args.mimeType);

        Intent chooserIntent = Intent.createChooser(shareIntent, args.title);
        if (registrar.activity() != null) {
            registrar.activity().startActivity(chooserIntent);
        } else {
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            registrar.context().startActivity(chooserIntent);
        }

        result.success(null);
    }

    private static class Args {
        final String title;
        final String pathToFile;
        final String mimeType;

        private Args(String pathToFile, String mimeType, @Nullable String title) {
            this.pathToFile = pathToFile;
            this.title = title;
            this.mimeType = mimeType;
        }

        static Args fromCall(MethodCall call) {
            String pathToFile = getArg(call, "pathToFile");
            String mimeType = getArg(call, "mimeType");
            String title = getArg(call, "title");
            return new Args(pathToFile, mimeType, title);
        }

        private static<T> T getArg(MethodCall call, String argKey) {
            if (!call.hasArgument(argKey)) {
                throw new IllegalArgumentException("Missing argument: " + argKey);
            }
            return call.argument(argKey);
        }
    }
}
