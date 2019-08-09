#import "FileSharePlugin.h"

@implementation FileSharePlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
    FlutterMethodChannel* channel = [FlutterMethodChannel
                                     methodChannelWithName:@"file_share"
                                     binaryMessenger:[registrar messenger]];
    FileSharePlugin* instance = [[FileSharePlugin alloc] init];
    [registrar addMethodCallDelegate:instance channel:channel];
}

- (void)handleMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
    if ([@"shareFile" isEqualToString:call.method]) {
        [self share:call result:result];
    } else {
        result(FlutterMethodNotImplemented);
    }
}

- (void)share:(FlutterMethodCall*)call result:(FlutterResult)result {
    
    NSString *titleArg = call.arguments[@"title"];
    NSString *pathToFile = call.arguments[@"pathToFile"];
    NSString *mimeType = call.arguments[@"mimeType"];
    
    NSData *data = [[NSData alloc] initWithContentsOfFile:pathToFile];
    //NSURL *url = [[NSURL alloc] initWithString:pathToFile]
    
    UIViewController *viewController = UIApplication.sharedApplication.keyWindow.rootViewController;

    UIActivityViewController *activityViewController = [[UIActivityViewController alloc] initWithActivityItems:@[data] applicationActivities:nil];
    
    [activityViewController.popoverPresentationController setSourceView:viewController.view];
    
    [viewController presentViewController:activityViewController animated:YES completion:^{
        
    }];
    
    result([@"iOS " stringByAppendingString:[[UIDevice currentDevice] systemVersion]]);
}

@end
