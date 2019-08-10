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
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/**
 * FileSharePlugin
 */
public class FileSharePlugin implements MethodCallHandler, PluginRegistry.ActivityResultListener {

    private static final int REQUEST_CODE = 1337;
    private static final String PROVIDER_EXTENSION = ".fileprovider";

    /**
     * Plugin registration.
     */
    public static void registerWith(Registrar registrar) {
        final MethodChannel channel = new MethodChannel(registrar.messenger(), "file_share");
        channel.setMethodCallHandler(new FileSharePlugin(registrar));
    }

    private Registrar mRegistrar;
    private Result mResult;

    private FileSharePlugin(Registrar registrar) {
        mRegistrar = registrar;
        mRegistrar.addActivityResultListener(this);
    }

    @Override
    public void onMethodCall(MethodCall call, Result result) {
        mResult = result;
        if (call.method.equals("shareFile")) {
            share(call, result);
        } else {
            result.notImplemented();
        }
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode != REQUEST_CODE || mResult == null)
            return false;

        mResult.success(null);
        return true;
    }

    private void share(MethodCall call, Result result) {
        Args args = Args.fromCall(call);

        File fileToShare = new File(args.pathToFile);
        Uri fileUri;
        try {
            fileUri = FileProvider.getUriForFile(
                    mRegistrar.context(),
                    mRegistrar.activeContext().getPackageName() + PROVIDER_EXTENSION,
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
        mRegistrar.activity().startActivityForResult(chooserIntent, REQUEST_CODE);
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
