//
//  WebViewController.m
//  Cordova WebView Plugin
//
//  Created by Marcus Koehler on 27.10.14.
//
//

#import "WebViewController.h"
#import <WebKit/WebKit.h>

#define RGBCOLOR(rgbValue) [UIColor \
colorWithRed:	((float)((rgbValue & 0x00FF0000) >> 16))/0xFF \
green:			((float)((rgbValue & 0x0000FF00) >>  8))/0xFF \
blue:			((float)((rgbValue & 0x000000FF) >>  0))/0xFF \
alpha:			1.0 \
]

@interface WebViewController (private) <WKUIDelegate, WKNavigationDelegate>
@end

@implementation WebViewController

- (id)initWithOptions: (NSDictionary*) _options
{
	if ((self = [super init]) != nil)
	{
		options = _options;
		self.edgesForExtendedLayout = UIRectEdgeNone;
	}
	return self;
}

- (void)loadView
{
	[super loadView];

	UIColor *bgColor = [self colorWithRGBHexString:[options objectForKey:@"backgroundColor"]];
	UIColor *iconColor = [self colorWithRGBHexString:[options objectForKey:@"iconColor"]];

	bool isPDF = [[options objectForKey:@"isPDF"] boolValue];

	self.view.backgroundColor = [UIColor blackColor];

	UIView * bottomView = [[UIView alloc] initWithFrame:CGRectMake(0, self.view.frame.size.height-44, self.view.frame.size.width, 44)];
	bottomView.autoresizingMask = UIViewAutoresizingFlexibleTopMargin | UIViewAutoresizingFlexibleWidth;
	bottomView.backgroundColor = bgColor;
	[self.view addSubview:bottomView];

	bool navigationAtTop = false;
	if ([options objectForKey:@"navigationAtTop"] != nil)
		navigationAtTop = [[options objectForKey:@"navigationAtTop"] boolValue];

	bool showBackwardButton = true;
	bool showForwardButton = true;
	bool showRefreshButton = true;
	
	NSDictionary *icons = [options objectForKey:@"icons"];
	if (icons != nil)
	{
		if ([icons objectForKey:@"backward"] != nil)
			showBackwardButton = [[icons objectForKey:@"backward"] boolValue];
		
		if ([icons objectForKey:@"forward"] != nil)
			showForwardButton = [[icons objectForKey:@"forward"] boolValue];
		
		if ([icons objectForKey:@"refresh"] != nil)
			showRefreshButton = [[icons objectForKey:@"refresh"] boolValue];
	}
	
	UIView *middleView = [[UIView alloc] initWithFrame:CGRectMake((bottomView.frame.size.width - 100)/2, 0, 100, bottomView.frame.size.height)];
	middleView.autoresizingMask = UIViewAutoresizingFlexibleWidth;
	middleView.backgroundColor = [UIColor clearColor];
	[bottomView addSubview:middleView];
	
	NSDictionary *iconsResources = [options objectForKey:@"iconsResources"];
	UIImage *backwardIcon = nil;
	UIImage *forwardIcon = nil;
	UIImage *refreshIcon = nil;
	UIImage *closeIcon = nil;
	
	if (iconsResources != nil)
	{
		if ([iconsResources objectForKey:@"backward"] != nil)
		{
			NSString *backwardPath = [iconsResources objectForKey:@"backward"];
			NSData *backwardIconData = [self getDataForAttachmentPath:backwardPath];
			//NSData *backwardIconData = UIImagePNGRepresentation([self getDataForAttachmentPath:backwardPath]);
			backwardIcon = [UIImage imageWithData:backwardIconData];
		}
		if ([iconsResources objectForKey:@"forward"] != nil)
		{
			NSString *forwardPath = [iconsResources objectForKey:@"forward"];
			NSData *forwardIconData = [self getDataForAttachmentPath:forwardPath];
			//NSData *forwardIconData = UIImagePNGRepresentation([self getDataForAttachmentPath:forwardPath]);
			forwardIcon = [UIImage imageWithData:forwardIconData];
		}
		if ([iconsResources objectForKey:@"refresh"] != nil)
		{
			NSString *refreshPath = [iconsResources objectForKey:@"refresh"];
			NSData *refreshIconData = [self getDataForAttachmentPath:refreshPath];
			//NSData *refreshIconData = UIImagePNGRepresentation([self getDataForAttachmentPath:refreshPath]);
			refreshIcon = [UIImage imageWithData:refreshIconData];
		}
		if ([iconsResources objectForKey:@"close"] != nil)
		{
			NSString *closePath = [iconsResources objectForKey:@"close"];
			NSData *closeIconData = [self getDataForAttachmentPath:closePath];
			//NSData *closeIconData = UIImagePNGRepresentation([self getDataForAttachmentPath:closePath]);
			closeIcon = [UIImage imageWithData:closeIconData];
		}
	}
	
	UIButton *closeButton = [[UIButton alloc] initWithFrame:CGRectMake(10, (bottomView.frame.size.height-40)/2, 40, 40)];
	[closeButton addTarget:self action:@selector(onClose) forControlEvents:UIControlEventTouchUpInside];
	if (closeIcon == nil)
	{
		[closeButton setImage:[self tintedImageWithColor:iconColor image:[UIImage imageNamed:@"ic_nav_close"]]forState:UIControlStateNormal];
		closeButton.tintColor = iconColor;
	}
	else
	{
		if (iconColor != nil)
		{
			[closeButton setImage:[self tintedImageWithColor:iconColor image:closeIcon]forState:UIControlStateNormal];
			closeButton.tintColor = iconColor;
		}
		else
		{
			[closeButton setImage:closeIcon forState:UIControlStateNormal];
		}
	}
	closeButton.backgroundColor = [UIColor clearColor];
	[bottomView addSubview:closeButton];
	
	if (!isPDF)
	{
		backButton = [[UIButton alloc] initWithFrame:CGRectMake(0, (middleView.frame.size.height-40)/2, 40, 40)];
		[backButton addTarget:self action:@selector(onBack) forControlEvents:UIControlEventTouchUpInside];
		if (backwardIcon == nil)
		{
			[backButton setImage:[self tintedImageWithColor:iconColor image:[UIImage imageNamed:@"ic_nav_back"]]forState:UIControlStateNormal];
		}
		else
		{
			if (iconColor != nil)
				[backButton setImage:[self tintedImageWithColor:iconColor image:backwardIcon]forState:UIControlStateNormal];
			else
				[backButton setImage:backwardIcon forState:UIControlStateNormal];
		}
		backButton.enabled = NO;
		[backButton setHidden:!showBackwardButton];
		[middleView addSubview: backButton];

		forwardButton = [[UIButton alloc] initWithFrame:CGRectMake(60, (middleView.frame.size.height-40)/2, 40, 40)];
		[forwardButton addTarget:self action:@selector(onForward) forControlEvents:UIControlEventTouchUpInside];
		forwardButton.autoresizingMask = UIViewAutoresizingFlexibleLeftMargin;
		if (forwardIcon == nil)
		{
			[forwardButton setImage:[self tintedImageWithColor:iconColor image:[UIImage imageNamed:@"ic_nav_forward"]]forState:UIControlStateNormal];
		}
		else
		{
			if (iconColor != nil)
				[forwardButton setImage:[self tintedImageWithColor:iconColor image:forwardIcon]forState:UIControlStateNormal];
			else
				[forwardButton setImage:forwardIcon forState:UIControlStateNormal];
		}
		forwardButton.enabled = NO;
		[forwardButton setHidden:!showForwardButton];
		[middleView addSubview: forwardButton];

		refreshButton = [[UIButton alloc] initWithFrame:CGRectMake(bottomView.frame.size.width-50, (bottomView.frame.size.height-40)/2, 40, 40)];
		[refreshButton addTarget:self action:@selector(onRefresh) forControlEvents:UIControlEventTouchUpInside];
		if (refreshIcon == nil)
		{
			[refreshButton setImage:[self tintedImageWithColor:iconColor image:[UIImage imageNamed:@"ic_nav_refresh"]]forState:UIControlStateNormal];
		}
		else
		{
			if (iconColor != nil)
				[refreshButton setImage:[self tintedImageWithColor:iconColor image:refreshIcon]forState:UIControlStateNormal];
			else
				[refreshButton setImage:refreshIcon forState:UIControlStateNormal];
		}
		[refreshButton setHidden:!showRefreshButton];
		[bottomView addSubview:refreshButton];
	}


	webView = [WKWebView new];
	webView.backgroundColor = [UIColor whiteColor];
	webView.UIDelegate = self;
    webView.navigationDelegate = self;

	ai = [[UIActivityIndicatorView alloc] initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleGray];
	ai.center = CGPointMake(webView.bounds.size.width/2, (webView.bounds.size.height/2));
	ai.autoresizingMask = (UIViewAutoresizingFlexibleLeftMargin | UIViewAutoresizingFlexibleRightMargin | UIViewAutoresizingFlexibleBottomMargin | UIViewAutoresizingFlexibleTopMargin);
	[webView addSubview:ai];
    
    NSArray *arrangedSubviews;
    UIView *topSpace = [UIView new];
    topSpace.backgroundColor = bottomView.backgroundColor;
    
    UIView *bottomSpace = [UIView new];
    bottomSpace.backgroundColor = bottomView.backgroundColor;
    
    if (navigationAtTop)
    {
        arrangedSubviews = @[topSpace, bottomView, webView];
    } else {
        arrangedSubviews = @[topSpace, webView, bottomView, bottomSpace];
    }
    
    UIStackView *stack = [[UIStackView alloc] initWithArrangedSubviews:arrangedSubviews];
    stack.axis = UILayoutConstraintAxisVertical;
    [self.view addSubview:stack];
    stack.translatesAutoresizingMaskIntoConstraints = NO;
    [bottomView.heightAnchor constraintEqualToConstant:44].active = YES;
    
    [topSpace.heightAnchor constraintEqualToAnchor:self.topLayoutGuide.heightAnchor].active = YES;
    
    if (bottomSpace.superview)
    {
        [bottomSpace.heightAnchor constraintEqualToAnchor:self.bottomLayoutGuide.heightAnchor].active = YES;
    }
    
    [stack.leadingAnchor constraintEqualToAnchor:self.view.leadingAnchor].active = YES;
    [stack.trailingAnchor constraintEqualToAnchor:self.view.trailingAnchor].active = YES;
    [stack.topAnchor constraintEqualToAnchor:self.view.topAnchor].active = YES;
    [stack.bottomAnchor constraintEqualToAnchor:self.view.bottomAnchor].active = YES;
    
}

- (NSData*) getDataForAttachmentPath:(NSString*)path
{
	if ([path hasPrefix:@"file:///"])
	{
		return [self dataForAbsolutePath:path];
	}
	else if ([path hasPrefix:@"res:"])
	{
		return [self dataForResource:path];
	}
	else if ([path hasPrefix:@"file://"])
	{
		return [self dataForAsset:path];
	}

	NSFileManager* fileManager = [NSFileManager defaultManager];

	if (![fileManager fileExistsAtPath:path]){
		NSLog(@"File not found: %@", path);
	}

	return [fileManager contentsAtPath:path];
}

/**
 * Retrieves the data for an absolute attachment path.
 *
 * @param path
 * An absolute file path.
 * @return
 * The data for the attachment.
 */
- (NSData*) dataForAbsolutePath:(NSString*)path
{
	NSFileManager* fileManager = [NSFileManager defaultManager];
	NSString* absPath;

	absPath = [path stringByReplacingOccurrencesOfString:@"file://"
											  withString:@""];

	if (![fileManager fileExistsAtPath:absPath])
	{
		NSLog(@"File not found: %@", absPath);
	}

	NSData* data = [fileManager contentsAtPath:absPath];

	return data;
}

/**
 * Retrieves the data for a resource path.
 *
 * @param path
 * A relative file path.
 * @return
 * The data for the attachment.
 */
- (NSData*) dataForResource:(NSString*)path
{
	NSFileManager* fileManager = [NSFileManager defaultManager];
	NSString* absPath;

	NSBundle* mainBundle = [NSBundle mainBundle];
	NSString* bundlePath = [[mainBundle bundlePath]
							stringByAppendingString:@"/"];

	absPath = [path pathComponents].lastObject;

	absPath = [bundlePath stringByAppendingString:absPath];

	if (![fileManager fileExistsAtPath:absPath]){
		NSLog(@"File not found: %@", absPath);
	}

	NSData* data = [fileManager contentsAtPath:absPath];

	return data;
}

/**
 * Retrieves the data for a asset path.
 *
 * @param path
 * A relative www file path.
 * @return
 * The data for the attachment.
 */
- (NSData*) dataForAsset:(NSString*)path
{
	NSFileManager* fileManager = [NSFileManager defaultManager];
	NSString* absPath;

	NSBundle* mainBundle = [NSBundle mainBundle];
	NSString* bundlePath = [[mainBundle bundlePath]
							stringByAppendingString:@"/"];

	absPath = [path stringByReplacingOccurrencesOfString:@"file:/"
											  withString:@"www"];

	absPath = [bundlePath stringByAppendingString:absPath];

	if (![fileManager fileExistsAtPath:absPath]){
		NSLog(@"File not found: %@", absPath);
	}

	NSData* data = [fileManager contentsAtPath:absPath];

	return data;
}

- (BOOL)prefersStatusBarHidden {
	return YES;
}

- (void) viewWillAppear:(BOOL)animated
{
	[super viewWillAppear:animated];
	[UIApplication sharedApplication].statusBarHidden = true;
	NSString *urlString = [options objectForKey:@"url"];

	if ([options objectForKey:@"urlEncoding"] != nil)
	{
		bool encodeURL = [[options objectForKey:@"urlEncoding"] boolValue];
		if (encodeURL)
			urlString = [[options objectForKey:@"url"] stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding];
	}
    
    NSURL *url = [NSURL URLWithString:urlString];
    if (!url.isFileURL)
    {
        [webView loadRequest:[NSURLRequest requestWithURL:url]];
    }
    else
    {
        NSURL *root = [NSBundle mainBundle].resourceURL;
        url = [root URLByAppendingPathComponent:url.path];
        NSAssert([[NSFileManager defaultManager] fileExistsAtPath:url.path], @"");
        [webView loadFileURL:url allowingReadAccessToURL:[url URLByDeletingLastPathComponent]];
    }
}

- (void) viewWillDisappear:(BOOL)animated
{
	[super viewWillDisappear:animated];
	[UIApplication sharedApplication].statusBarHidden = false;
}

#pragma mark -
#pragma mark Webview Delegate

- (void)webView:(WKWebView *)webView decidePolicyForNavigationAction:(WKNavigationAction *)navigationAction decisionHandler:(void (^)(WKNavigationActionPolicy))decisionHandler {

	forwardButton.enabled = webView.canGoForward;
	backButton.enabled = webView.canGoBack;

    decisionHandler(WKNavigationActionPolicyAllow);
}

- (void)webView:(WKWebView *)webView didStartProvisionalNavigation:(WKNavigation *)navigation {
    [ai startAnimating];
}

- (void)webView:(WKWebView *)webView didCommitNavigation:(WKNavigation *)navigation {
    [ai startAnimating];
}

- (void)webView:(WKWebView *)webView didFinishNavigation:(WKNavigation *)navigation {
    [ai stopAnimating];
}

- (void)webView:(WKWebView *)webView didFailNavigation:(WKNavigation *)navigation withError:(NSError *)error {
    [ai stopAnimating];
}

- (void)webView:(WKWebView *)webView didFailProvisionalNavigation:(WKNavigation *)navigation withError:(NSError *)error {
    [ai stopAnimating];
}

#pragma mark -

- (void) onClose
{
	[self dismissViewControllerAnimated:YES completion:nil];
}

- (void) onRefresh
{
	[webView reload];
}

- (void) onForward
{
	if (webView.canGoForward)
		[webView goForward];
}

- (void) onBack
{
	if (webView.canGoBack)
		[webView goBack];
}

#pragma mark -
#pragma mark Helper

- (UIColor*)colorWithRGBHexString:(NSString *)stringToConvert
{
	unsigned hexNum;

	if ([stringToConvert hasPrefix:@"#"])
		stringToConvert = [stringToConvert stringByReplacingOccurrencesOfString:@"#" withString:@"0x"];

	NSScanner *scanner = [NSScanner scannerWithString:stringToConvert];
	if (![scanner scanHexInt:&hexNum]) return nil;
	return [self colorWithRGBHex:hexNum];
}

- (UIColor*)colorWithRGBHex:(UInt32)hex
{
	int r = (hex >> 16) & 0xFF;
	int g = (hex >> 8) & 0xFF;
	int b = (hex) & 0xFF;

	return [UIColor colorWithRed:r/255.0f green:g/255.0f blue:b/255.0f alpha:1.0];
}

- (UIImage*)tintedImageWithColor:(UIColor*)tintColor image: (UIImage*) image
{
	CGSize imgSize = image.size;
	CGRect imgRect = CGRectMake(0, 0, imgSize.width, imgSize.height);
	UIGraphicsBeginImageContextWithOptions(imgSize, false, image.scale);
	CGContextRef context = UIGraphicsGetCurrentContext();

	// draw alpha-mask
	[image drawInRect:imgRect blendMode:kCGBlendModeNormal alpha:1.0];

	// draw tint color, preserving alpha values of original image
	CGContextSetBlendMode(context, kCGBlendModeSourceIn);
	[tintColor setFill];
	CGContextFillRect(context, imgRect);

	UIImage* tintedImage = UIGraphicsGetImageFromCurrentImageContext();
	UIGraphicsEndImageContext();

	return tintedImage;
}

@end
