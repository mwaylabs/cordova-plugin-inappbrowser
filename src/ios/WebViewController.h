//
//  DocumentViewController.h
//  mobileCRM
//
//  Created by Marcus Koehler on 12.02.14.
//
//

#import <UIKit/UIKit.h>

@interface WebViewController : UIViewController
{
    UIWebView *webView;
    UIActivityIndicatorView *ai;
    NSString *url;
    NSString *mimeType;
}

- (id)initWithUrl: (NSString*) _url name: (NSString*) name mimeType: (NSString*) _mimeType;

@end
