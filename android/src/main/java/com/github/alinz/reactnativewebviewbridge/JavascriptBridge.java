package com.github.alinz.reactnativewebviewbridge;

import com.github.alinz.reactnativewebviewbridge.events.TopMessageEvent;

import android.webkit.WebView;
import android.webkit.JavascriptInterface;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.common.SystemClock;
import com.facebook.react.uimanager.UIManagerModule;
import com.facebook.react.uimanager.events.EventDispatcher;

class JavascriptBridge {
  private WebView webView;

  public JavascriptBridge(WebView webView) {
    this.webView = webView;
  }

  @JavascriptInterface
  public void send(String message) {
    ReactContext context = (ReactContext)this.webView.getContext();
    final EventDispatcher mEventDispatcher = context.getNativeModule(UIManagerModule.class).getEventDispatcher();

    WritableMap event = Arguments.createMap();
    event.putDouble("target", this.webView.getId());
    event.putString("message", message);

    mEventDispatcher.dispatchEvent(
      new TopMessageEvent(
          this.webView.getId(),
          SystemClock.nanoTime(),
          event));
  }
}
