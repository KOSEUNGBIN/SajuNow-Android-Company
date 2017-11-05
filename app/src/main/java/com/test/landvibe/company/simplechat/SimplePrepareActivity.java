package com.test.landvibe.company.simplechat;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.test.landvibe.company.R;
import com.test.landvibe.company.common.CheckRegId;
import com.test.landvibe.company.util.DeEncrypter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import cz.msebera.android.httpclient.Header;

/**
 * Created by user on 2016-01-30.
 */
public class SimplePrepareActivity extends AppCompatActivity{
    private String DIVISOR = "`c!4~D]s";
    private int m_advertisementCount = 3;
    private ViewPager m_advertisementViewPager;
    private PagerAdapter m_advertisementPagerAdapter;
    private int m_currentPosition;
    private Toolbar toolbar;
    private DrawerLayout drawer;
    private PersistentCookieStore cookieStore;
    private String[] result;
    private String[] question;
    private int company_no;
    private int history_no;
    private String user_name;
    private AsyncHttpClient client_user = new AsyncHttpClient();


    private CheckRegId checkRegid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        history_no = intent.getIntExtra("history_no", 0);
        user_name = intent.getStringExtra("name");
        setContentView(R.layout.activity_simple_prepare);
      //  final CirclePageIndicator advertisementCirclePageIndicator = (CirclePageIndicator) findViewById(R.id.advertisement_indicator);
        toolbar = (Toolbar) findViewById(R.id.toolbar_simple_prepare);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        checkRegid = new CheckRegId();

        cookieStore = new PersistentCookieStore(this);
        DeEncrypter deEncrypter = new DeEncrypter();
        String token = deEncrypter.decrypt(URLDecoder.decode(cookieStore.getCookies().get(0).getValue()));
        result = token.split("\\?");
        company_no = Integer.parseInt(result[0]);
        Log.d("error", "company_no : " + company_no);
        client_user.addHeader("Cookie", cookieStore.getCookies().get(0).getValue());

        checkRegid.checkRegid(getString(R.string.URL), cookieStore.getCookies().get(0).getValue(), SimplePrepareActivity.this);

        RequestParams param = new RequestParams();
        client_user.post(getString(R.string.URL) + "/history/" + history_no, param, new JsonHttpResponseHandler() {
//            URL 에 +history_no 추가

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.d("error", response.toString());
                try {
                    JSONArray jsonarray = response.getJSONObject("result").getJSONArray("msgList");
                    question = jsonarray.getJSONObject(0).getString("message").split("`c!4~D]s");
                    m_advertisementViewPager = (ViewPager) findViewById(R.id.question_pager);
                    m_advertisementPagerAdapter = new AdvertisementPagerAdapter(getSupportFragmentManager(),question);
                    m_advertisementViewPager.setAdapter(m_advertisementPagerAdapter);

                //    advertisementCirclePageIndicator.setViewPager(m_advertisementViewPager);
                    TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
                    tabLayout.setupWithViewPager(m_advertisementViewPager);



                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {



            }
        });

        final Button insertButton = (Button)findViewById(R.id.simple_prepare_insert_btn);

        insertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertButton.setClickable(false);
                boolean isCorrect = true;
                try{
                List<Fragment> allFragments = getSupportFragmentManager().getFragments();
                QuestionPageFragment fragment = (QuestionPageFragment)allFragments.get(0);
                QuestionPageFragment fragment2 = (QuestionPageFragment)allFragments.get(1);
                QuestionPageFragment fragment3 = (QuestionPageFragment)allFragments.get(2);

                    String[] answers = { fragment.getEditText(), fragment2.getEditText(), fragment3.getEditText()};

                    for (String answer : answers) {
                        if(answer.equals(""))
                        {
                            isCorrect =false;
                            Toast.makeText(SimplePrepareActivity.this,
                                    "모든 질문에 대한 답변을 입력해야 합니다.", Toast.LENGTH_LONG)
                                    .show();
                            insertButton.setClickable(true);
                            break;
                        }
                    }
                    if(isCorrect)
                    {
                        SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                        String time = sdfNow.format(new Date(System.currentTimeMillis()));

                        RequestParams params = new RequestParams();
                        params.put("history_no", history_no);
                        params.put("sender", 12);
                        params.put("send_date", time);
                        params.put("message", answers[0]+ DIVISOR + answers[1] + DIVISOR +answers[2]);

                        client_user.post(getString(R.string.URL) + "/chat/terminate/simple", params, new AsyncHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                Intent intent;
                                    intent = new Intent(SimplePrepareActivity.this, SimpleChatActivity.class);
                                    intent.putExtra("history_no", history_no);
                                    intent.putExtra("name", user_name);
                                    startActivity(intent);
                                    finish();
                                    overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);


                                insertButton.setClickable(true);

                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                insertButton.setClickable(true);
                            }
                        });
//                        Toast.makeText(SimplePrepareActivity.this,
//                                "insert 실행", Toast.LENGTH_LONG)
//                                .show();
                    }
                }
                catch (Exception e)
                {
                    Toast.makeText(SimplePrepareActivity.this,
                            "모든 질문에 대한 답변을 입력해야 합니다.", Toast.LENGTH_LONG)
                            .show();
                }

            }
        });





    }

    private class AdvertisementPagerAdapter extends FragmentPagerAdapter {

        private  String[] question;
        private  String[] title = {"질문1" ,"질문2","질문3" };

        public AdvertisementPagerAdapter(FragmentManager supportFragmentManager, String[] question) {
            super(supportFragmentManager);
            this.question = question;
        }

        @Override
        public Fragment getItem(int iPageNumber) {
            return QuestionPageFragment.create(iPageNumber,question[iPageNumber]);
        }

        @Override
        public int getCount() {
            return m_advertisementCount;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return title[position];
        }
    }


    // 터치에 대한의 메뉴창 처리 부분
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    // 터치에 대한의 메뉴창의 이벤트 처리 부분
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        checkRegid.checkRegid(getString(R.string.URL), cookieStore.getCookies().get(0).getValue(), SimplePrepareActivity.this);
    }

    private void updateRegistId(int company_no, long history_no) {
        RequestParams params = new RequestParams();

        params.put("company_no", company_no);
        params.put("history_no", history_no);


        client_user.post(getString(R.string.URL) + "/history/update/company", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
//                Toast.makeText(SimplePrepareActivity.this,
//                        "RegistId Update Success", Toast.LENGTH_LONG)
//                        .show();


            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(SimplePrepareActivity.this,
                        "오류가 발생하였습니다.\n" +
                                "다시 시도해주세요.",
                        Toast.LENGTH_LONG).show();

            }
        });

    }



}