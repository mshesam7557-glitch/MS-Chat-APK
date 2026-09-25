package com.mschat.app;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {
    private static final String HOME = "https://ms-chat-307k.onrender.com/";
    private static final int FILE_CHOOSER = 1001;
    private static final int MEDIA_PERMS = 1002;

    private WebView webView;
    private ValueCallback<Uri[]> fileCallback;
    private PermissionRequest pendingPermissionRequest;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.rgb(85, 117, 255));
        getWindow().setNavigationBarColor(Color.BLACK);
        buildWebView();
        webView.clearCache(true);
        if (state != null) {
            webView.clearHistory();
        }
        webView.loadUrl(HOME);
    }

    private void buildWebView() {
        webView = new WebView(this);
        webView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        webView.setFocusable(true);
        webView.setFocusableInTouchMode(true);
        webView.requestFocus(View.FOCUS_DOWN);
        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        setContentView(webView);

        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setBuiltInZoomControls(false);
        s.setDisplayZoomControls(false);
        s.setSupportZoom(false);
        s.setUseWideViewPort(false);
        s.setLoadWithOverviewMode(false);
        s.setJavaScriptCanOpenWindowsAutomatically(true);
        s.setSupportMultipleWindows(false);
        s.setCacheMode(WebSettings.LOAD_NO_CACHE);
        s.setLoadsImagesAutomatically(true);
        s.setTextZoom(100);
        s.setDefaultFontSize(16);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            s.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        }

        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);

        webView.setWebViewClient(new WebViewClient() {
            @Override public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                view.setFocusable(true);
                view.setFocusableInTouchMode(true);
                injectAndroidWebFixes(view);
            }

            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest req) {
                Uri u = req.getUrl();
                if ("http".equals(u.getScheme()) || "https".equals(u.getScheme())) return false;
                try { startActivity(new Intent(Intent.ACTION_VIEW, u)); } catch (Exception ignored) {}
                return true;
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override public void onPermissionRequest(final PermissionRequest request) {
                pendingPermissionRequest = request;
                if (Build.VERSION.SDK_INT < 23) {
                    request.grant(request.getResources());
                    pendingPermissionRequest = null;
                    return;
                }

                boolean needsCam = false;
                boolean needsMic = false;
                for (String resource : request.getResources()) {
                    if (PermissionRequest.RESOURCE_VIDEO_CAPTURE.equals(resource)) needsCam = true;
                    if (PermissionRequest.RESOURCE_AUDIO_CAPTURE.equals(resource)) needsMic = true;
                }

                boolean cam = !needsCam || checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
                boolean mic = !needsMic || checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
                if (cam && mic) {
                    request.grant(request.getResources());
                    pendingPermissionRequest = null;
                    return;
                }

                java.util.ArrayList<String> permissions = new java.util.ArrayList<>();
                if (needsCam && !cam) permissions.add(Manifest.permission.CAMERA);
                if (needsMic && !mic) permissions.add(Manifest.permission.RECORD_AUDIO);
                requestPermissions(permissions.toArray(new String[0]), MEDIA_PERMS);
            }

            @Override public boolean onShowFileChooser(WebView v, ValueCallback<Uri[]> cb, FileChooserParams params) {
                if (fileCallback != null) fileCallback.onReceiveValue(null);
                fileCallback = cb;
                Intent i = params.createIntent();
                i.addCategory(Intent.CATEGORY_OPENABLE);
                try {
                    startActivityForResult(i, FILE_CHOOSER);
                } catch (Exception e) {
                    fileCallback = null;
                    return false;
                }
                return true;
            }
        });
    }


    private void injectAndroidWebFixes(WebView view) {
        String js = "javascript:(function(){"
                + "try{"
                + "var style=document.getElementById('mschatAndroidFixes');"
                + "if(!style){style=document.createElement('style');style.id='mschatAndroidFixes';document.head.appendChild(style);}"
                // Keep the working background fix. No layout or click interception is added here.
                + "style.textContent=\"html,body{margin:0!important;width:100%!important;min-height:100%!important;}html{background-color:#dfe2ea!important;background-image:linear-gradient(rgba(6,14,50,.20),rgba(18,8,60,.22)),url('background.png')!important;background-repeat:no-repeat!important;background-position:center top!important;background-size:cover!important;background-attachment:scroll!important;}body{min-height:100dvh!important;background:transparent!important;background-image:none!important;overflow-x:hidden!important;-webkit-tap-highlight-color:transparent!important;}\";"
                // The web version already owns the mobile UI. We only hide a bottom nav while
                // the login panel is visible, using visibility/opacity so there is no blinking.
                + "var navTargets=[];"
                + "function isLoginVisible(){var login=document.getElementById('login');if(!login)return false;var c=getComputedStyle(login),r=login.getBoundingClientRect();return c.display!=='none'&&c.visibility!=='hidden'&&r.width>0&&r.height>0;}"
                + "function looksLikeNavText(t){t=(t||'').replace(/\\s+/g,' ').trim();return /(پروفایل|گروه.?ها|چت.?ها|پشتیبان|پنتر)/.test(t)&&t.length<100;}"
                + "function findNav(){var found=new Set();document.querySelectorAll('body *').forEach(function(el){var t=(el.innerText||'').replace(/\\s+/g,' ').trim();if(!looksLikeNavText(t))return;var r=el.getBoundingClientRect(),c=getComputedStyle(el);if(r.bottom<innerHeight-8||r.top<innerHeight*0.62||r.width<innerWidth*.60||r.height<45||r.height>220)return;var p=el;for(var i=0;i<6&&p;i++,p=p.parentElement){var pr=p.getBoundingClientRect(),pc=getComputedStyle(p);if((pc.position==='fixed'||pc.position==='sticky')&&pr.bottom>=innerHeight-10&&pr.top>innerHeight*.60&&pr.width>innerWidth*.60&&pr.height<240){found.add(p);break;}}});navTargets=Array.from(found);}"
                + "function applyNav(){var login=isLoginVisible();navTargets.forEach(function(el){if(!el.isConnected)return;if(login){el.style.setProperty('visibility','hidden','important');el.style.setProperty('opacity','0','important');el.style.setProperty('pointer-events','none','important');}else{el.style.removeProperty('visibility');el.style.removeProperty('opacity');el.style.removeProperty('pointer-events');}});}"
                + "function refreshNav(){findNav();applyNav();}"
                + "refreshNav();"
                + "setTimeout(refreshNav,250);setTimeout(refreshNav,700);setTimeout(refreshNav,1500);"
                + "var login=document.getElementById('login');if(login)new MutationObserver(function(){applyNav();}).observe(login,{attributes:true,attributeFilter:['style','class']});"
                + "if(document.body)new MutationObserver(function(m){var added=false;m.forEach(function(x){if(x.addedNodes&&x.addedNodes.length)added=true;});if(added){setTimeout(refreshNav,0);}}).observe(document.body,{childList:true,subtree:true});"
                + "window.addEventListener('resize',function(){setTimeout(refreshNav,50)});"
                + "}catch(e){console.warn('MS__Chat Android fixes',e)}})();";
        view.evaluateJavascript(js, null);
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] perms, int[] results) {
        super.onRequestPermissionsResult(requestCode, perms, results);
        if (requestCode == MEDIA_PERMS && pendingPermissionRequest != null) {
            boolean needsCam = false;
            boolean needsMic = false;
            for (String resource : pendingPermissionRequest.getResources()) {
                if (PermissionRequest.RESOURCE_VIDEO_CAPTURE.equals(resource)) needsCam = true;
                if (PermissionRequest.RESOURCE_AUDIO_CAPTURE.equals(resource)) needsMic = true;
            }
            boolean ok = (!needsCam || checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
                    && (!needsMic || checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED);
            if (ok) pendingPermissionRequest.grant(pendingPermissionRequest.getResources());
            else pendingPermissionRequest.deny();
            pendingPermissionRequest = null;
        }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_CHOOSER && fileCallback != null) {
            Uri[] result = WebChromeClient.FileChooserParams.parseResult(resultCode, data);
            fileCallback.onReceiveValue(result);
            fileCallback = null;
        }
    }

    @Override public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }

    @Override protected void onSaveInstanceState(Bundle out) {
        if (webView != null) webView.saveState(out);
        super.onSaveInstanceState(out);
    }

    @Override protected void onDestroy() {
        if (webView != null) webView.destroy();
        super.onDestroy();
    }
}
