package com.mschat.app;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
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
    private boolean mobileChatViewOpen = false;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(android.graphics.Color.rgb(85,117,255));
        getWindow().setNavigationBarColor(android.graphics.Color.BLACK);
        buildWebView();
        if (state == null) webView.loadUrl(HOME); else webView.restoreState(state);
    }

    private void buildWebView() {
        webView = new WebView(this);
        webView.setLayoutParams(new ViewGroup.LayoutParams(-1, -1));
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
        s.setUseWideViewPort(false);
        s.setLoadWithOverviewMode(false);
        s.setJavaScriptCanOpenWindowsAutomatically(true);
        s.setSupportMultipleWindows(false);
        s.setTextZoom(100);
        s.setDefaultFontSize(16);

        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);

        webView.setWebViewClient(new WebViewClient() {
            @Override public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                injectAndroidMobileUi(view);
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
                if (android.os.Build.VERSION.SDK_INT < 23) {
                    request.grant(request.getResources());
                    pendingPermissionRequest = null;
                    return;
                }

                boolean needsCam = false, needsMic = false;
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

    private void injectAndroidMobileUi(WebView view) {
        String script = "javascript:(function(){"
                + "if(window.__msAndroidUiV2)return;window.__msAndroidUiV2=true;"
                + "var s=document.createElement('style');s.id='msAndroidMobileOnlyStyle';s.textContent=`"
                + "html,body{width:100%!important;height:100%!important;margin:0!important;overflow:hidden!important;}"
                + "#chat{position:fixed!important;inset:0!important;width:100vw!important;height:100dvh!important;max-width:none!important;min-height:0!important;margin:0!important;border-radius:0!important;overflow:hidden!important;}"
                + "#sidebar{width:100%!important;min-width:0!important;height:100%!important;overflow:auto!important;padding:12px!important;}"
                + "#chatArea{display:none!important;width:100%!important;height:100%!important;min-width:0!important;}"
                + "html.msAndroidChatOpen #sidebar{display:none!important;}"
                + "html.msAndroidChatOpen #chatArea{display:flex!important;}"
                + "#messages{padding:12px!important;min-height:0!important;}"
                + "#message{font-size:16px!important;min-height:44px!important;}"
                + "#chatHeader{min-height:64px!important;padding:8px 10px!important;flex:none!important;}"
                + "#sendArea{flex:none!important;padding:8px!important;}"
                + "#androidBackButton{display:inline-flex!important;align-items:center;justify-content:center;width:40px;height:40px;border:0;border-radius:12px;background:rgba(255,255,255,.82);font-size:24px;margin-left:2px;}"
                + "#androidBackButton.hidden{display:none!important;}"
                + "#login{margin:20px auto!important;max-height:calc(100dvh - 40px)!important;overflow:auto!important;}"
                + "@media(max-width:760px){#sidebar{padding:10px!important;} .userItem,.groupItem{padding:12px!important;} .itemName{font-size:14px!important;} .itemStatus{font-size:11px!important;}}"
                + "`;document.head.appendChild(s);"
                + "function byId(x){return document.getElementById(x)};"
                + "function setOpen(open){document.documentElement.classList.toggle('msAndroidChatOpen',!!open);window.__msAndroidChatOpen=!!open;}"
                + "function addBack(){var h=byId('chatHeader');if(!h||byId('androidBackButton'))return;var b=document.createElement('button');b.id='androidBackButton';b.type='button';b.textContent='‹';b.title='بازگشت به گفتگوها';b.onclick=function(e){e.preventDefault();e.stopPropagation();setOpen(false);try{window.stopTyping&&window.stopTyping()}catch(_){}};h.insertBefore(b,h.firstChild);}"
                + "function ready(){var c=byId('chat');if(!c)return false;addBack();if(window.__msAndroidChatOpen!==true)setOpen(false);return true;}"
                + "document.addEventListener('click',function(e){var item=e.target.closest&&e.target.closest('.userItem,.groupItem,#savedMessagesButton');if(item){setTimeout(function(){addBack();setOpen(true)},60);} },true);"
                + "var obs=new MutationObserver(function(){if(ready())addBack();});obs.observe(document.documentElement,{childList:true,subtree:true});"
                + "ready();window.__msAndroidSetChatView=setOpen;"
                + "})();";
        try { view.evaluateJavascript(script, null); } catch (Exception ignored) {}
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] perms, int[] results) {
        super.onRequestPermissionsResult(requestCode, perms, results);
        if (requestCode == MEDIA_PERMS && pendingPermissionRequest != null) {
            boolean needsCam = false, needsMic = false;
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
        if (webView != null) {
            webView.evaluateJavascript("(function(){if(document.documentElement.classList.contains('msAndroidChatOpen')){document.documentElement.classList.remove('msAndroidChatOpen');window.__msAndroidChatOpen=false;return 'chat';} return 'other';})()", value -> {
                if ("\"other\"".equals(value)) {
                    if (webView.canGoBack()) webView.goBack(); else MainActivity.super.onBackPressed();
                }
            });
        } else super.onBackPressed();
    }

    @Override protected void onSaveInstanceState(Bundle out) { webView.saveState(out); super.onSaveInstanceState(out); }
    @Override protected void onDestroy() { if (webView != null) webView.destroy(); super.onDestroy(); }
}
