package it.skarafaz.mercury.help;

import android.graphics.Color;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import butterknife.Bind;
import butterknife.ButterKnife;
import it.skarafaz.mercury.R;
import it.skarafaz.mercury.infrastructure.MercuryActivity;

public class HelpActivity extends MercuryActivity {
    private static final String INDEX_URL = "file:///android_asset/help/index.html";
    @Bind(R.id.webview)
    protected WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_help);
        ButterKnife.bind(this);

        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl(INDEX_URL);
    }
}
