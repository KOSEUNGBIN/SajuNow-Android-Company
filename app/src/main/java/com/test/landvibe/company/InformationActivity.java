package com.test.landvibe.company;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.loopj.android.http.PersistentCookieStore;
import com.test.landvibe.company.common.CheckRegId;

/**
 * 공지사항 액티비티
 */
public class InformationActivity extends AppCompatActivity  {

    private Toolbar toolbar;

    public PersistentCookieStore cookieStore;
    private CheckRegId checkRegid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company_information);

        WebView WebView01 = (WebView) findViewById(R.id.webView);
        WebView01.setWebViewClient(new WebViewClient());

        WebSettings webSettings = WebView01.getSettings();
        webSettings.setJavaScriptEnabled(true);

        WebView01.loadUrl("http://saju.oursoccer.co.kr/webview/inform/company");

        checkRegid = new CheckRegId();

        toolbar = (Toolbar) findViewById(R.id.toolbar_information);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        cookieStore = new PersistentCookieStore(this);
        checkRegid.checkRegid(getString(R.string.URL), cookieStore.getCookies().get(0).getValue(), InformationActivity.this);
    }

    @Override
    public void onBackPressed() {
            super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        switch (item.getItemId()) {
            case android.R.id.home:
                // NavUtils.navigateUpFromSameTask(this);
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onRestart() {
        checkRegid.checkRegid(getString(R.string.URL), cookieStore.getCookies().get(0).getValue(), InformationActivity.this);
        super.onRestart();
    }

}
