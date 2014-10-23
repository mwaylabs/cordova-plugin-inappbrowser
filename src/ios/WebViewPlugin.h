//
//  CordovaPushPlugin.h
//  PushLibrary
//
//  Created by Marcus Koehler on 27.01.14.
//  Copyright (c) 2014 M-Way Solutions GmbH. All rights reserved.
//

#import <Cordova/CDVPlugin.h>

@interface WebViewPlugin : CDVPlugin
- (void)openDocument:(CDVInvokedUrlCommand*)command;
@end
