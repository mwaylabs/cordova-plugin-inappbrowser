# cordova-plugin-inappbrowser

This plugin adds a customizable in app browser to your cordova application.

```
 $window.webview.openWebView(success, failure, {
      iconColor: '#ffff00',
      backgroundColor: '#f00000',
      isPDF: false,
      url: 'http://mwaysolutions.com',
      urlEncoding: false,
      visibleAddress: false,
      editableAddress: false,
      navigationAtTop: false,
      icons:{
        backward: true,
        forward: true,
        refresh: true
      },
      iconsResources:{
        backward: 'file://main/assets/images/backward.png',
        forward: 'file://main/assets/images/forward.png',
        refresh: 'file://main/assets/images/refresh.png',
        close: 'file://main/assets/images/close.png'
      }
    });
```

## Installation
```
  $ cordova plugin add https://github.com/mwaylabs/cordova-plugin-inappbrowser.git
```

## Options
- All parameters are required except navigationAtTop (when not specified the navigation will show at the bottom), urlEncoding (when not specified the url will not be encoded), iconsResources (when not specified the default icons will be used) and iconColor (when not specified the icons will not be tinted).

- iconColor: Defines the color of the icons expectes a string with a colorcode e.g. '#000000'
- backgroundColor: Defines the backgroundcolor of the bar at the bottom of the webview, expectes a string with a colorcode e.g. '#FFFFFF'
- isPDF: Defines whether to show or hide the navigation buttons (next, back, reload) expects a boolean
- url: The url of the resource to show. expects a string with a valid url
- urlEncoding: Defines whether to encode or not the url (not encoding by default). Expects a boolean
- visibleAddress: (Only for Android) Defines whether if you can see the address url you are visiting or not. Expects a boolean.
- editableAddress: (Only for Android) Defines whether if you can edit the address url you are visiting or not. Expects a boolean.
- navigationAtTop: Defines whether to position the navigation at the top or at the bottom of screen (bottom by default). Expects a boolean
- icons: Expects an object { (ignored when 'isPDF' is true)
  - backward: Defines whether the backward button is shown or not. Expects a boolean.
  - forward: Defines whether the forward button is shown or not. Expects a boolean.
  - refresh: Defines whether the refresh button is shown or not. Expects a boolean.
},
- iconsResources: Expects an object { (ignored when 'isPDF' is true and it will be tinted if there is a value in 'iconColor')
  - backward: Defines the icon for the backward button. Expects either resource files, files from the device storage or assets from within the *www* folder.
  - forward: Defines the icon for the forward button. Expects either resource files, files from the device storage or assets from within the *www* folder.
  - refresh: Defines the icon for the refresh button. Expects either resource files, files from the device storage or assets from within the *www* folder.
  - close: Only for iOS. Defines the icon for the close button. Expects either resource files, files from the device storage or assets from within the *www* folder.
}

### Examples of "iconsResources"

#### Using native app resources
Each app has a resource folder, e.g. the _res_ folder for Android apps or the _Resources_ folder for iOS apps. The following example shows how to use the app icon from within the app's resource folder.

```
    backward:'res://ic_back_icon', //=> res/drawable/ic_back_icon.png (Android)
    close:'res://ic_nav_close.png' //=> Resources/ic_nav_close (iOS)
```

#### Using assets from the www folder
The path to the files must be defined relative from the root of the mobile web app folder, which is located under the _www_ folder.

```
    backward:'file://www/main/assets/images/logo.png', //=> www/main/assets/images/logo.png (Android)
    refresh:'file://img/logo.png' //=> www/img/logo.png (iOS)
```

#### Using files from the device storage
The path to the files must be defined absolute from the root of the file system.

```
    backward:'file:///sdcard/Pictures/icon.png', //=> (Android)
    refresh:'file:///icon.png' //=> (iOS)
```

### Supported Platforms
- iOS 7+
- Android 4.4+

### License
Code licensed under MIT. Docs under Apache 2. PhoneGap is a trademark of Adobe.
