package com.test.landvibe.company;

import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.loopj.android.http.PersistentCookieStore;
import com.test.landvibe.company.common.CheckRegId;
import com.test.landvibe.company.simplechat.SimplePrepareActivity;

/**
 * 관리자하게 문의하기에 대한 액티비티
 */
public class QuestionActivity extends AppCompatActivity  {

    private Toolbar toolbar;

    public PersistentCookieStore cookieStore;

    private CheckRegId checkRegid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company_question);

        toolbar = (Toolbar) findViewById(R.id.toolbar_question);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        checkRegid = new CheckRegId();

        cookieStore = new PersistentCookieStore(this);
        checkRegid.checkRegid(getString(R.string.URL), cookieStore.getCookies().get(0).getValue(), QuestionActivity.this);
     }


    @Override
    public void onBackPressed() {
            super.onBackPressed();
    }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {


            switch (item.getItemId()) {
                case android.R.id.home:
                    // NavUtils.navigateUpFromSameTask(this);
                    finish();
                    return true;
            }

            return super.onOptionsItemSelected(item);
        }

    @Override
    protected void onRestart() {
        checkRegid.checkRegid(getString(R.string.URL), cookieStore.getCookies().get(0).getValue(), QuestionActivity.this);
        super.onRestart();
    }

}

