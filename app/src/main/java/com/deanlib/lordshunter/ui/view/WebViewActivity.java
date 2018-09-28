package com.deanlib.lordshunter.ui.view;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.deanlib.lordshunter.R;
import com.deanlib.ootblite.utils.DLog;

import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class WebViewActivity extends AppCompatActivity {
    
    @BindView(R.id.tvAppTitle)
    TextView tvAppTitle;
    @BindView(R.id.webview)
    WebView webview;
    @BindView(R.id.pbLoading)
    ProgressBar pbLoading;

    String mUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//        }
        setContentView(R.layout.activity_webview);
        ButterKnife.bind(this);
        mUrl = getIntent().getStringExtra("url");
        init();
    }

    private void init(){

        if (!TextUtils.isEmpty(mUrl)) {
            if (!mUrl.startsWith("file:///") && !Pattern.matches("https?:\\/\\/.+",mUrl)){
                mUrl = "http://" + mUrl;
            }
            webview.setWebChromeClient(webChromeClient);
            webview.setWebViewClient(webViewClient);


            WebSettings webSettings = webview.getSettings();
            webSettings.setJavaScriptEnabled(false);

            //Uncaught TypeError: Cannot read property ‘getItem’ of null”
            webSettings.setDomStorageEnabled(true);//允许浏览器保存doom原型，这样js就可以调用这个方法了

            /**
             * LOAD_CACHE_ONLY: 不使用网络，只读取本地缓存数据
             * LOAD_DEFAULT: （默认）根据cache-control决定是否从网络上取数据。
             * LOAD_NO_CACHE: 不使用缓存，只从网络获取数据.
             * LOAD_CACHE_ELSE_NETWORK，只要本地有，无论是否过期，或者no-cache，都使用缓存中的数据。
             */
//            webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);//不使用缓存，只从网络获取数据.
            //Mozilla/5.0 (Linux; Android 7.0; AOSP on HammerHead Build/NRD90M; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/51.0.2704.91 Mobile Safari/537.36
            //Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.99 Mobile Safari/537.36
//            webSettings.setUserAgentString("Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.99 Mobile Safari/537.36");
            //支持屏幕缩放
//            webSettings.setSupportZoom(true);
//            webSettings.setBuiltInZoomControls(true);

            //不显示webview缩放按钮
//        webSettings.setDisplayZoomControls(false);

            webview.loadUrl(mUrl);
            //使用webview显示html代码
//        webView.loadDataWithBaseURL(null,"<html><head><title> 欢迎您 </title></head>" +
//                "<body><h2>使用webview显示 html代码</h2></body></html>", "text/html" , "utf-8", null);
            //加载asset文件夹下html
//        webView.loadUrl("file:///android_asset/test.html");
//            webview.addJavascriptInterface(this,"android");//添加js监听 这样html就能调用客户端
        }
    }

    @OnClick(R.id.layoutBack)
    public void onViewClicked() {
        finish();
    }

    //WebViewClient主要帮助WebView处理各种通知、请求事件
    private WebViewClient webViewClient = new WebViewClient(){
        @Override
        public void onPageFinished(WebView view, String url) {//页面加载完成

            String title = view.getTitle();
            if (!TextUtils.isEmpty(title)){
                tvAppTitle.setText(title);
            }else {
                tvAppTitle.setText(R.string.app_name);
            }
            pbLoading.setVisibility(View.GONE);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {//页面开始加载
            pbLoading.setVisibility(View.VISIBLE);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return false;
        }
    };

    //WebChromeClient主要辅助WebView处理Javascript的对话框、网站图标、网站title、加载进度等
    private WebChromeClient webChromeClient=new WebChromeClient(){
        //不支持js的alert弹窗，需要自己监听然后通过dialog弹窗
        @Override
        public boolean onJsAlert(WebView webView, String url, String message, JsResult result) {
            AlertDialog.Builder localBuilder = new AlertDialog.Builder(webView.getContext());
            localBuilder.setMessage(message).setPositiveButton(R.string.ok,null);
            localBuilder.setCancelable(false);
            localBuilder.create().show();

            //注意:
            //必须要这一句代码:result.confirm()表示:
            //处理结果为确定状态同时唤醒WebCore线程
            //否则不能继续点击按钮
            result.confirm();
            return true;
        }

        //获取网页标题
        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            DLog.i("网页标题:"+title);
        }

        //加载进度回调
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
//            pbLoading.setProgress(newProgress);
        }

    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        DLog.i("是否有上一个页面:"+webview.canGoBack());
        if (webview.canGoBack() && keyCode == KeyEvent.KEYCODE_BACK){//点击返回按钮的时候判断有没有上一页
            webview.goBack(); // goBack()表示返回webView的上一页面
            return true;
        }
        return super.onKeyDown(keyCode,event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //释放资源
        webview.destroy();
        webview=null;
    }
}
