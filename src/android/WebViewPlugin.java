
package com.mwaysolutions.cordova.webviewplugin;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.LOG;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;


@SuppressLint("SetJavaScriptEnabled")
public class WebViewPlugin extends CordovaPlugin {

    protected static final String LOG_TAG           = "WebViewPlugin";
    private static final String   EXIT_EVENT        = "exit";
    private static final String   BACKGROUND_COLOR  = "backgroundColor";
    private static final String   ICON_COLOR        = "iconColor";
    private static final String   IS_PDF            = "isPDF";
    private static final String   URL               = "url";
    private static final String   VISIBLE_ADDRESS   = "visibleAddress";
    private static final String   EDITABLE_ADDRESS  = "editableAddress";
    private static final String   ICONS             = "icons";
    private static final String   ICONS_RESOURCES   = "iconsResources";
    private static final String   ICON_BACKWARD     = "backward";
    private static final String   ICON_FORWARD      = "forward";
    private static final String   ICON_REFRESH      = "refresh";
    private static final String   ICON_CLOSE        = "close";
    private static final String   LOAD_START_EVENT  = "loadstart";
    private static final String   LOAD_STOP_EVENT   = "loadstop";
    private static final String   LOAD_ERROR_EVENT  = "loaderror";
    private static final int      ICON_COLOR_NULL   = -14;

    private InAppBrowserDialog    dialog;
    private WebView               inAppWebView;
    private EditText              edittext;
    private CallbackContext       callbackContext;
    private boolean               showLocationBar   = true;
    private final boolean         clearAllCache     = false;
    private final boolean         clearSessionCache = false;
    private String                mUrl;
    private int                   mBgColor;
    private int                   mIconColor;
    private boolean               mIsPDF;
    private boolean               mIsVisible;
    private boolean               mIsEditable;
    private boolean               mIsBackward;
    private boolean               mIsForward;
    private boolean               mIsRefresh;
    private String                mCloseIcon;
    private String                mBackwardIcon;
    private String                mForwardIcon;
    private String                mRefreshIcon;

    @Override
    public boolean execute(final String action, final JSONArray args, final CallbackContext callbackContext) throws JSONException {

        if (action.equals("openWebView")) {
            this.callbackContext = callbackContext;
            if (args != null) {
                this.cordova.getActivity().runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        String result = "";
                        try {
                            result = WebViewPlugin.this.showWebPage(args);
                        } catch (final JSONException e) {
                            e.printStackTrace();
                        }
                        final PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, result);
                        pluginResult.setKeepCallback(true);
                        callbackContext.sendPluginResult(pluginResult);
                    }
                });
            } else if (action.equals("exit")) {
                this.closeDialog();
            } else {
                return false;
            }
        } else {
            return false;
        }
        return true;
    }

    /**
     * Closes the dialog
     */
    public void closeDialog() {
        final WebView childView = this.inAppWebView;
        // The JS protects against multiple calls, so this should happen only when
        // closeDialog() is called by other native code.
        if (childView == null) {
            return;
        }
        this.cordova.getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                childView.setWebViewClient(new WebViewClient() {

                    // NB: wait for about:blank before dismissing
                    @Override
                    public void onPageFinished(final WebView view, final String url) {
                        if (WebViewPlugin.this.dialog != null) {
                            WebViewPlugin.this.dialog.dismiss();
                        }
                    }
                });
                // NB: From SDK 19: "If you call methods on WebView from any thread
                // other than your app's UI thread, it can cause unexpected results."
                // http://developer.android.com/guide/webapps/migrating.html#Threads
                childView.loadUrl("about:blank");
            }
        });

        try {
            final JSONObject obj = new JSONObject();
            obj.put("type", EXIT_EVENT);
            this.sendUpdate(obj, false);
        } catch (final JSONException ex) {
            Log.d(LOG_TAG, "Should never happen");
        }
    }

    /**
     * Checks to see if it is possible to go back one page in history, then does so.
     */
    private void goBack() {
        if (this.inAppWebView.canGoBack()) {
            this.inAppWebView.goBack();
        }
    }

    /**
     * Checks to see if it is possible to go forward one page in history, then does so.
     */
    private void goForward() {
        if (this.inAppWebView.canGoForward()) {
            this.inAppWebView.goForward();
        }
    }

    /**
     * Navigate to the new page
     *
     * @param url to load
     */
    private void navigate(final String url) {
        final InputMethodManager imm = (InputMethodManager) this.cordova.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(this.edittext.getWindowToken(), 0);

        if (!url.startsWith("http") && !url.startsWith("file:")) {
            this.inAppWebView.loadUrl("http://" + url);
        } else {
            this.inAppWebView.loadUrl(url);
        }
        this.inAppWebView.requestFocus();
    }

    /**
     * Should we show the location bar?
     *
     * @return boolean
     */
    private boolean getShowLocationBar() {
        return this.showLocationBar;
    }

    private WebViewPlugin getWebViewPlugin() {
        return this;
    }

    /**
     * Display a new browser with the specified URL.
     *
     * @param url           The url to load.
     * @param jsonObject
     * @throws JSONException
     */
    public String showWebPage(final JSONArray json) throws JSONException {
        // Determine if we should hide the location bar.
        this.showLocationBar = true;
        this.mIsVisible = false;
        this.mIsEditable = false;
        this.mIconColor = ICON_COLOR_NULL;
        final JSONObject features = json.getJSONObject(0);
        if (features != null) {
            final String iColor = features.getString(ICON_COLOR);
            if (iColor != null) {
                this.mIconColor = Color.parseColor(iColor.replaceAll("\\s+", ""));
            }
            final String bColor = features.getString(BACKGROUND_COLOR);
            if (bColor != null) {
                this.mBgColor = Color.parseColor(bColor.replaceAll("\\s+", ""));
            }
            final String u = features.getString(URL);
            if (u != null) {
                this.mUrl = u.replaceAll("\\s+", "");
            }
            if (features.has(IS_PDF)) {
                final boolean iPDF = features.getBoolean(IS_PDF);
                this.mIsPDF = iPDF;
            }
            if (features.has(VISIBLE_ADDRESS)) {
                final boolean vText = features.getBoolean(VISIBLE_ADDRESS);
                this.mIsVisible = vText;
            }
            if (features.has(EDITABLE_ADDRESS)) {
                final boolean eText = features.getBoolean(EDITABLE_ADDRESS);
                this.mIsEditable = eText;
            }
            final JSONObject jo = features.getJSONObject(ICONS);
            if (jo.has(ICON_BACKWARD)) {
                final boolean iBack = jo.getBoolean(ICON_BACKWARD);
                this.mIsBackward = iBack;
            }
            if (jo.has(ICON_FORWARD)) {
                final boolean iForw = jo.getBoolean(ICON_FORWARD);
                this.mIsForward = iForw;
            }
            if (jo.has(ICON_REFRESH)) {
                final boolean iRefresh = jo.getBoolean(ICON_REFRESH);
                this.mIsRefresh = iRefresh;
            }
            final JSONObject jor = features.getJSONObject(ICONS_RESOURCES);
            if (jor != null) {
                if (jor.has(ICON_CLOSE)) {
                    final String iClose = jor.getString(ICON_CLOSE);
                    this.mCloseIcon = iClose;
                }
                if (jor.has(ICON_BACKWARD)) {
                    final String iBack = jor.getString(ICON_BACKWARD);
                    this.mBackwardIcon = iBack;
                }
                if (jor.has(ICON_FORWARD)) {
                    final String iForw = jor.getString(ICON_FORWARD);
                    this.mForwardIcon = iForw;
                }
                if (jor.has(ICON_REFRESH)) {
                    final String iRefresh = jor.getString(ICON_REFRESH);
                    this.mRefreshIcon = iRefresh;
                }
            }
        }
        final CordovaWebView thatWebView = this.webView;

        // Create dialog in new thread
        final Runnable runnable = new Runnable() {

            /**
             * Convert our DIP units to Pixels
             *
             * @return int
             */
            private int dpToPixels(final int dipValue) {
                final int value = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                        dipValue,
                        WebViewPlugin.this.cordova.getActivity().getResources().getDisplayMetrics()
                        );

                return value;
            }

            @Override
            @SuppressLint("NewApi")
            public void run() {
                // Let's create the main dialog
                WebViewPlugin.this.dialog = new InAppBrowserDialog(WebViewPlugin.this.cordova.getActivity(), android.R.style.Theme_NoTitleBar);
                WebViewPlugin.this.dialog.getWindow().getAttributes().windowAnimations = android.R.style.Animation_Dialog;
                WebViewPlugin.this.dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                WebViewPlugin.this.dialog.setCancelable(true);
                WebViewPlugin.this.dialog.setInAppBroswer(WebViewPlugin.this.getWebViewPlugin());

                // Main container layout
                final LinearLayout main = new LinearLayout(WebViewPlugin.this.cordova.getActivity());
                main.setOrientation(LinearLayout.VERTICAL);

                // Toolbar layout
                final RelativeLayout toolbar = new RelativeLayout(WebViewPlugin.this.cordova.getActivity());
                toolbar.setBackgroundColor(WebViewPlugin.this.mBgColor);//android.graphics.Color.LTGRAY);
                toolbar.setLayoutParams(new RelativeLayout.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, this.dpToPixels(44)));
                toolbar.setPadding(this.dpToPixels(2), this.dpToPixels(2), this.dpToPixels(2), this.dpToPixels(2));
                toolbar.setHorizontalGravity(Gravity.LEFT);
                toolbar.setVerticalGravity(Gravity.TOP);

                // Action Button Container layout
                final RelativeLayout actionButtonContainer = new RelativeLayout(WebViewPlugin.this.cordova.getActivity());
                actionButtonContainer.setLayoutParams(new RelativeLayout.LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                        android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
                actionButtonContainer.setHorizontalGravity(Gravity.LEFT);
                actionButtonContainer.setVerticalGravity(Gravity.CENTER_VERTICAL);
                actionButtonContainer.setId(1);

                // Back button
                final Button back = new Button(WebViewPlugin.this.cordova.getActivity());
                final RelativeLayout.LayoutParams backLayoutParams = new RelativeLayout.LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT);
                backLayoutParams.addRule(RelativeLayout.ALIGN_LEFT);
                back.setLayoutParams(backLayoutParams);
                back.setContentDescription("Back Button");
                back.setId(2);
                final Resources activityRes = WebViewPlugin.this.cordova.getActivity().getResources();
                Drawable backIcon = null;
                if (WebViewPlugin.this.mBackwardIcon != null) {
                    try {
                        backIcon = Drawable.createFromStream(WebViewPlugin.this.cordova.getActivity().getAssets().open(WebViewPlugin.this.mBackwardIcon), null);
                    } catch (final IOException e) {
                        backIcon = null;
                    }
                    if (backIcon == null) {
                        final int backRId = activityRes.getIdentifier(WebViewPlugin.this.mBackwardIcon, "drawable",
                                WebViewPlugin.this.cordova.getActivity().getPackageName());
                        try {
                            backIcon = activityRes.getDrawable(backRId);
                        } catch (final NotFoundException e) {
                            backIcon = null;
                        }
                    }
                }
                if (backIcon == null) {
                    final int backResId = activityRes.getIdentifier("ic_action_previous_item", "drawable",
                            WebViewPlugin.this.cordova.getActivity().getPackageName());
                    backIcon = activityRes.getDrawable(backResId);
                }
                if (WebViewPlugin.this.mIconColor != ICON_COLOR_NULL) {
                    backIcon.setColorFilter(WebViewPlugin.this.mIconColor, Mode.MULTIPLY);//
                }
                if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    back.setBackgroundDrawable(backIcon);
                } else {
                    back.setBackground(backIcon);
                }
                back.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(final View v) {
                        WebViewPlugin.this.goBack();
                    }
                });

                // Forward button
                final Button forward = new Button(WebViewPlugin.this.cordova.getActivity());
                final RelativeLayout.LayoutParams forwardLayoutParams = new RelativeLayout.LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT);
                forwardLayoutParams.addRule(RelativeLayout.RIGHT_OF, 2);
                forward.setLayoutParams(forwardLayoutParams);
                forward.setContentDescription("Forward Button");
                forward.setId(3);
                Drawable fwdIcon = null;
                if (WebViewPlugin.this.mForwardIcon != null) {
                    try {
                        fwdIcon = Drawable.createFromStream(WebViewPlugin.this.cordova.getActivity().getAssets().open(WebViewPlugin.this.mForwardIcon), null);
                    } catch (final IOException e) {
                        fwdIcon = null;
                    }
                    if (fwdIcon == null) {
                        final int fwdRId = activityRes.getIdentifier(WebViewPlugin.this.mForwardIcon, "drawable",
                                WebViewPlugin.this.cordova.getActivity().getPackageName());
                        try {
                            fwdIcon = activityRes.getDrawable(fwdRId);
                        } catch (final NotFoundException e) {
                            fwdIcon = null;
                        }
                    }
                }
                if (fwdIcon == null) {
                    final int fwdResId = activityRes.getIdentifier("ic_action_next_item", "drawable", WebViewPlugin.this.cordova.getActivity().getPackageName());
                    fwdIcon = activityRes.getDrawable(fwdResId);
                }
                if (WebViewPlugin.this.mIconColor != ICON_COLOR_NULL) {
                    fwdIcon.setColorFilter(WebViewPlugin.this.mIconColor, Mode.MULTIPLY);//
                }
                if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    forward.setBackgroundDrawable(fwdIcon);
                } else {
                    forward.setBackground(fwdIcon);
                }
                forward.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(final View v) {
                        WebViewPlugin.this.goForward();
                    }
                });

                // Edit Text Box
                WebViewPlugin.this.edittext = new EditText(WebViewPlugin.this.cordova.getActivity());
                final RelativeLayout.LayoutParams textLayoutParams = new RelativeLayout.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT);
                textLayoutParams.addRule(RelativeLayout.RIGHT_OF, 1);
                textLayoutParams.addRule(RelativeLayout.LEFT_OF, 5);
                WebViewPlugin.this.edittext.setLayoutParams(textLayoutParams);
                WebViewPlugin.this.edittext.setId(4);
                WebViewPlugin.this.edittext.setSingleLine(true);
                WebViewPlugin.this.edittext.setText(WebViewPlugin.this.mUrl);//url);
                WebViewPlugin.this.edittext.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
                WebViewPlugin.this.edittext.setSelectAllOnFocus(true);
                WebViewPlugin.this.edittext.setImeOptions(EditorInfo.IME_ACTION_GO);
                if (!WebViewPlugin.this.mIsEditable) {
                    WebViewPlugin.this.edittext.setInputType(InputType.TYPE_NULL);
                    WebViewPlugin.this.edittext.setSelectAllOnFocus(false);
                }
                WebViewPlugin.this.edittext.setOnKeyListener(new View.OnKeyListener() {

                    @Override
                    public boolean onKey(final View v, final int keyCode, final KeyEvent event) {
                        // If the event is a key-down event on the "enter" button
                        if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                            WebViewPlugin.this.navigate(WebViewPlugin.this.edittext.getText().toString());
                            return true;
                        }
                        return false;
                    }
                });

                // WebView
                WebViewPlugin.this.inAppWebView = new WebView(WebViewPlugin.this.cordova.getActivity());
                WebViewPlugin.this.inAppWebView.setLayoutParams(new LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT));
                WebViewPlugin.this.inAppWebView.setWebChromeClient(new InAppChromeClient(thatWebView));
                final WebViewClient client = new WebViewPluginClient(thatWebView, WebViewPlugin.this.edittext);
                WebViewPlugin.this.inAppWebView.setWebViewClient(client);
                final WebSettings settings = WebViewPlugin.this.inAppWebView.getSettings();
                settings.setJavaScriptEnabled(true);
                settings.setJavaScriptCanOpenWindowsAutomatically(true);
                settings.setBuiltInZoomControls(true);
                settings.setPluginState(android.webkit.WebSettings.PluginState.ON);

                final Button refresh = new Button(WebViewPlugin.this.cordova.getActivity());
                final RelativeLayout.LayoutParams refreshLayoutParams = new RelativeLayout.LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT);
                refreshLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                refresh.setLayoutParams(refreshLayoutParams);
                refresh.setContentDescription("Refresh Button");
                refresh.setId(5);
                Drawable refreshIcon = null;
                if (WebViewPlugin.this.mRefreshIcon != null) {
                    try {
                        refreshIcon = Drawable.createFromStream(WebViewPlugin.this.cordova.getActivity().getAssets().open(WebViewPlugin.this.mRefreshIcon),
                                null);
                    } catch (final IOException e) {
                        refreshIcon = null;
                    }
                    if (refreshIcon == null) {
                        final int refreshRId = activityRes.getIdentifier(WebViewPlugin.this.mRefreshIcon, "drawable",
                                WebViewPlugin.this.cordova.getActivity().getPackageName());
                        try {
                            refreshIcon = activityRes.getDrawable(refreshRId);
                        } catch (final NotFoundException e) {
                            refreshIcon = null;
                        }
                    }
                }
                if (refreshIcon == null) {
                    final int refreshResId = activityRes.getIdentifier("ic_action_refresh", "drawable",
                            WebViewPlugin.this.cordova.getActivity().getPackageName());
                    refreshIcon = activityRes.getDrawable(refreshResId);
                }
                if (WebViewPlugin.this.mIconColor != ICON_COLOR_NULL) {
                    refreshIcon.setColorFilter(WebViewPlugin.this.mIconColor, Mode.MULTIPLY);//
                }
                if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN)
                {
                    refresh.setBackgroundDrawable(refreshIcon);
                }
                else
                {
                    refresh.setBackground(refreshIcon);
                }
                refresh.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(final View v) {
                        WebViewPlugin.this.inAppWebView.reload();
                    }
                });

                //Toggle whether this is enabled or not!
                final Bundle appSettings = WebViewPlugin.this.cordova.getActivity().getIntent().getExtras();
                final boolean enableDatabase = appSettings == null ? true : appSettings.getBoolean("WebViewPluginStorageEnabled", true);
                if (enableDatabase) {
                    final String databasePath = WebViewPlugin.this.cordova.getActivity().getApplicationContext().getDir("WebViewPluginDB", Context.MODE_PRIVATE).getPath();
                    settings.setDatabasePath(databasePath);
                    settings.setDatabaseEnabled(true);
                }
                settings.setDomStorageEnabled(true);

                if (WebViewPlugin.this.clearAllCache) {
                    CookieManager.getInstance().removeAllCookie();
                } else if (WebViewPlugin.this.clearSessionCache) {
                    CookieManager.getInstance().removeSessionCookie();
                }

                WebViewPlugin.this.inAppWebView.loadUrl(WebViewPlugin.this.mUrl);//url);
                WebViewPlugin.this.inAppWebView.setId(6);
                WebViewPlugin.this.inAppWebView.getSettings().setLoadWithOverviewMode(true);
                WebViewPlugin.this.inAppWebView.getSettings().setUseWideViewPort(true);
                WebViewPlugin.this.inAppWebView.requestFocus();
                WebViewPlugin.this.inAppWebView.requestFocusFromTouch();

                // Add the back and forward buttons to our action button container layout
                if (WebViewPlugin.this.mIsBackward) {
                    actionButtonContainer.addView(back);
                }
                if (WebViewPlugin.this.mIsForward) {
                    actionButtonContainer.addView(forward);
                }
                // Add the views to our toolbar
                if (!WebViewPlugin.this.mIsPDF) {
                    toolbar.addView(actionButtonContainer);
                }
                if (WebViewPlugin.this.mIsVisible) {
                    toolbar.addView(WebViewPlugin.this.edittext);
                }
                if ((!WebViewPlugin.this.mIsPDF) && (WebViewPlugin.this.mIsRefresh)) {
                    toolbar.addView(refresh);
                }
                main.addView(toolbar);

                // Add our webview to our main view/layout
                main.addView(WebViewPlugin.this.inAppWebView);

                final WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                lp.copyFrom(WebViewPlugin.this.dialog.getWindow().getAttributes());
                lp.width = android.view.ViewGroup.LayoutParams.MATCH_PARENT;
                lp.height = android.view.ViewGroup.LayoutParams.MATCH_PARENT;

                WebViewPlugin.this.dialog.setContentView(main);
                WebViewPlugin.this.dialog.show();
                WebViewPlugin.this.dialog.getWindow().setAttributes(lp);
            }
        };
        this.cordova.getActivity().runOnUiThread(runnable);
        return "";
    }

    /**
     * Create a new plugin success result and send it back to JavaScript
     *
     * @param obj a JSONObject contain event payload information
     */
    private void sendUpdate(final JSONObject obj, final boolean keepCallback) {
        this.sendUpdate(obj, keepCallback, PluginResult.Status.OK);
    }

    /**
     * Create a new plugin result and send it back to JavaScript
     *
     * @param obj a JSONObject contain event payload information
     * @param status the status code to return to the JavaScript environment
     */
    private void sendUpdate(final JSONObject obj, final boolean keepCallback, final PluginResult.Status status) {
        if (this.callbackContext != null) {
            final PluginResult result = new PluginResult(status, obj);
            result.setKeepCallback(keepCallback);
            this.callbackContext.sendPluginResult(result);
            if (!keepCallback) {
                this.callbackContext = null;
            }
        }
    }

    /**
     * The webview client receives notifications about appView
     */
    public class WebViewPluginClient extends WebViewClient {

        EditText       edittext;
        CordovaWebView webView;

        /**
         * Constructor.
         *
         * @param mContext
         * @param edittext
         */
        public WebViewPluginClient(final CordovaWebView webView, final EditText mEditText) {
            this.webView = webView;
            this.edittext = mEditText;
        }

        /**
         * Notify the host application that a page has started loading.
         *
         * @param view          The webview initiating the callback.
         * @param url           The url of the page.
         */
        @Override
        public void onPageStarted(final WebView view, final String url, final Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            String newloc = "";
            if (url.startsWith("http:") || url.startsWith("https:") || url.startsWith("file:")) {
                newloc = url;
            }
            // If dialing phone (tel:5551212)
            else if (url.startsWith(WebView.SCHEME_TEL)) {
                try {
                    final Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse(url));
                    WebViewPlugin.this.cordova.getActivity().startActivity(intent);
                } catch (final android.content.ActivityNotFoundException e) {
                    LOG.e(LOG_TAG, "Error dialing " + url + ": " + e.toString());
                }
            }

            else if (url.startsWith("geo:") || url.startsWith(WebView.SCHEME_MAILTO) || url.startsWith("market:")) {
                try {
                    final Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    WebViewPlugin.this.cordova.getActivity().startActivity(intent);
                } catch (final android.content.ActivityNotFoundException e) {
                    LOG.e(LOG_TAG, "Error with " + url + ": " + e.toString());
                }
            }
            // If sms:5551212?body=This is the message
            else if (url.startsWith("sms:")) {
                try {
                    final Intent intent = new Intent(Intent.ACTION_VIEW);

                    // Get address
                    String address = null;
                    final int parmIndex = url.indexOf('?');
                    if (parmIndex == -1) {
                        address = url.substring(4);
                    }
                    else {
                        address = url.substring(4, parmIndex);

                        // If body, then set sms body
                        final Uri uri = Uri.parse(url);
                        final String query = uri.getQuery();
                        if (query != null) {
                            if (query.startsWith("body=")) {
                                intent.putExtra("sms_body", query.substring(5));
                            }
                        }
                    }
                    intent.setData(Uri.parse("sms:" + address));
                    intent.putExtra("address", address);
                    intent.setType("vnd.android-dir/mms-sms");
                    WebViewPlugin.this.cordova.getActivity().startActivity(intent);
                } catch (final android.content.ActivityNotFoundException e) {
                    LOG.e(LOG_TAG, "Error sending sms " + url + ":" + e.toString());
                }
            }
            else {
                newloc = "http://" + url;
            }

            if (!newloc.equals(this.edittext.getText().toString())) {
                this.edittext.setText(newloc);
            }

            try {
                final JSONObject obj = new JSONObject();
                obj.put("type", LOAD_START_EVENT);
                obj.put("url", newloc);

                WebViewPlugin.this.sendUpdate(obj, true);
            } catch (final JSONException ex) {
                Log.d(LOG_TAG, "Should never happen");
            }
        }

        @Override
        public void onPageFinished(final WebView view, final String url) {
            super.onPageFinished(view, url);

            try {
                final JSONObject obj = new JSONObject();
                obj.put("type", LOAD_STOP_EVENT);
                obj.put("url", url);

                WebViewPlugin.this.sendUpdate(obj, true);
            } catch (final JSONException ex) {
                Log.d(LOG_TAG, "Should never happen");
            }
        }

        @Override
        public void onReceivedError(final WebView view, final int errorCode, final String description, final String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);

            try {
                final JSONObject obj = new JSONObject();
                obj.put("type", LOAD_ERROR_EVENT);
                obj.put("url", failingUrl);
                obj.put("code", errorCode);
                obj.put("message", description);

                WebViewPlugin.this.sendUpdate(obj, true, PluginResult.Status.ERROR);
            } catch (final JSONException ex) {
                Log.d(LOG_TAG, "Should never happen");
            }
        }
    }
}
