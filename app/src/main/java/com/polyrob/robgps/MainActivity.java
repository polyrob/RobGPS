package com.polyrob.robgps;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.webkit.WebView;

public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WebView webView = (WebView)findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true); // enable javascript

        webView.loadUrl("http://robbiescheidt.pythonanywhere.com/");


        Intent intent = new Intent(this, GPSService.class);
        startService(intent);
    }
}
