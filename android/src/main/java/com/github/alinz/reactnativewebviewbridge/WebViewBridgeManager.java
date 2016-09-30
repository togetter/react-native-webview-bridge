package com.github.alinz.reactnativewebviewbridge;

import com.github.alinz.reactnativewebviewbridge.events.TopMessageEvent;

import android.webkit.WebView;
import android.webkit.WebSettings;

import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.views.webview.ReactWebViewManager;
import com.facebook.react.common.MapBuilder;

import java.util.Map;

import javax.annotation.Nullable;

public class WebViewBridgeManager extends ReactWebViewManager {
  private static final String REACT_CLASS = "RCTWebViewBridge";

  public static final int COMMAND_INJECT_BRIDGE_SCRIPT = 100;
  public static final int COMMAND_SEND_TO_BRIDGE = 101;

  @Override
  public String getName() {
    return REACT_CLASS;
  }

  @Override
  public @Nullable Map<String, Integer> getCommandsMap() {
    Map<String, Integer> commandsMap = super.getCommandsMap();

    commandsMap.put("injectBridgeScript", COMMAND_INJECT_BRIDGE_SCRIPT);
    commandsMap.put("sendToBridge", COMMAND_SEND_TO_BRIDGE);

    return commandsMap;
  }

  @Override
  public void receiveCommand(WebView root, int commandId, @Nullable ReadableArray args) {
    super.receiveCommand(root, commandId, args);

    switch (commandId) {
      case COMMAND_INJECT_BRIDGE_SCRIPT:
        injectBridgeScript(root);
        break;
      case COMMAND_SEND_TO_BRIDGE:
        sendToBridge(root, args.getString(0));
        break;
      default:
        //do nothing!!!!
    }
  }

  private void sendToBridge(WebView root, String message) {
    //root.loadUrl("javascript:(function() {\n" + script + ";\n})();");
    String script = "WebViewBridge.onMessage('" + message + "');";
    WebViewBridgeManager.evaluateJavascript(root, script);
  }

  @Override
  protected WebView createViewInstance(ThemedReactContext reactContext) {
    WebView root = super.createViewInstance(reactContext);
    // httpsのページでhttpを許可する by http://qiita.com/intemous9/items/2797ca533bad4c68a225
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
        // Andoird L未満と同じ設定にする「MIXED_CONTENT_ALWAYS_ALLOW」
        root.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
    }
    root.addJavascriptInterface(new JavascriptBridge(root), "WebViewBridgeAndroid");
    return root;
  }

  @Override
  public void onDropViewInstance(WebView root) {
    root.removeJavascriptInterface("WebViewBridgeAndroid");
    super.onDropViewInstance(root);
  }

  @Override
  public @Nullable Map getExportedCustomDirectEventTypeConstants() {
    return MapBuilder.of(
      TopMessageEvent.EVENT_NAME,
        MapBuilder.of("registrationName", "onMessage")
    );
  }

  private void injectBridgeScript(WebView root) {
    // this code needs to be executed everytime a url changes.
    WebViewBridgeManager.evaluateJavascript(root, ""
            + "(function() {"
            + "if (window.WebViewBridge) return;"
            + "var customEvent = document.createEvent('Event');"
            + "var WebViewBridge = {"
              + "send: function(message) { WebViewBridgeAndroid.send(message); },"
              + "onMessage: function() {}"
            + "};"
            + "window.WebViewBridge = WebViewBridge;"
            + "customEvent.initEvent('WebViewBridge', true, true);"
            + "document.dispatchEvent(customEvent);"
            + "}());");
  }

  static private void evaluateJavascript(WebView root, String javascript) {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
      root.evaluateJavascript(javascript, null);
    } else {
      root.loadUrl("javascript:" + javascript);
    }
  }
}
