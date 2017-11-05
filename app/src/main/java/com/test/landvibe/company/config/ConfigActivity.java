package com.test.landvibe.company.config;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.test.landvibe.company.FaqActivity;
import com.test.landvibe.company.InformationActivity;
import com.test.landvibe.company.R;
import com.test.landvibe.company.common.CheckRegId;
import com.test.landvibe.company.login.LoginActivity;
import com.test.landvibe.company.login.SignUpClauseOneActivity;
import com.test.landvibe.company.login.SignUpClauseTwoActivity;
import com.test.landvibe.company.util.DeEncrypter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLDecoder;
import java.text.DateFormat;
import java.util.Date;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.impl.cookie.BasicClientCookie;


/**
 * Created by 고승빈 on 2016-05-21.
 */
public class ConfigActivity extends AppCompatActivity
        implements View.OnClickListener {

    // Toolbar 선언
    private Toolbar toolbar;
    // Switch 선언
    private Switch alarmSwitch;
    // SharedPreferences 선언
    private SharedPreferences alarmSwitch_SP;

    // 각 TextView에 대한 변수 선언
    private ViewGroup config_information_layout;
    private TextView config_information_new_textview;
    private TextView config_question_common_textview;
    private TextView config_question_email_textview;
    private TextView config_clause_1_textview;
    private TextView config_clause_2_textview;
    private TextView config_clause_license_textview;
    private TextView config_logout_textview;

    // company_no 선언, 쿠키 내용을 담을 변수 선언
    private int company_no;
    private String[] result;

    // 쿠키
    private PersistentCookieStore cookieStore;
    // httpclient 선언
    private AsyncHttpClient client_guest = new AsyncHttpClient();
    private AsyncHttpClient client_user = new AsyncHttpClient();

    private CheckRegId checkRegid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        // Toolbar를 사용
        toolbar = (Toolbar) findViewById(R.id.toolbar_config);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        // Switch 선언
        alarmSwitch = (Switch) findViewById(R.id.config_alarm_switch);

        // TextView 변수에 할당
        config_information_layout = (ViewGroup) findViewById(R.id.config_information_layout);
        config_information_new_textview = (TextView) findViewById(R.id.config_information_new_textview);
        config_question_common_textview = (TextView) findViewById(R.id.config_question_common_textview);
        config_question_email_textview = (TextView) findViewById(R.id.config_question_email_textview);
        config_clause_1_textview = (TextView) findViewById(R.id.config_clause_1_textview);
        config_clause_2_textview = (TextView) findViewById(R.id.config_clause_2_textview);
        config_clause_license_textview = (TextView) findViewById(R.id.config_clause_license_textview);
        config_logout_textview = (TextView) findViewById(R.id.config_logout_textview);

        // 선언한 TextView에 Listenner 선언
        config_information_layout.setOnClickListener(this);
        config_question_common_textview.setOnClickListener(this);
        config_question_email_textview.setOnClickListener(this);
        config_clause_1_textview.setOnClickListener(this);
        config_clause_2_textview.setOnClickListener(this);
        config_clause_license_textview.setOnClickListener(this);
        config_logout_textview.setOnClickListener(this);

        // httpclient header 값 설정
        client_guest.addHeader("Cookie", "PASSWORD");

        ////////////////////////////쿠키 load, 복호화//////////////////////////////////////////////////////////////////////////

        cookieStore = new PersistentCookieStore(this);
        DeEncrypter deEncrypter = new DeEncrypter();
        Log.d("error", "token : " + URLDecoder.decode(cookieStore.getCookies().get(0).getValue()));
        String token = deEncrypter.decrypt(URLDecoder.decode(cookieStore.getCookies().get(0).getValue()));
        Log.d("error", "token length : " + token.length());
        result = token.split("\\?");
        company_no = Integer.parseInt(result[0]);

        checkRegid = new CheckRegId();
        checkRegid.checkRegid(getString(R.string.URL), cookieStore.getCookies().get(0).getValue(), ConfigActivity.this);
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


        // SharedPreferences 선언
        alarmSwitch_SP = getSharedPreferences("alarmSwitch", MODE_PRIVATE);
        alarmSwitch.setChecked(alarmSwitch_SP.getBoolean("alarmSwitchCondition", false));

        // switch 터치에 대한 이벤트 처리
        alarmSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = alarmSwitch_SP.edit();
                editor.clear();
                editor.putBoolean("alarmSwitchCondition", isChecked);
                editor.commit();
                Log.d("isChecked",isChecked+"");
                Log.d("alarmSwitchCondition",alarmSwitch_SP.getBoolean("alarmSwitchCondition", false)+"");
                alarmSwitch.setChecked(isChecked);

            }
        });

        // 공지사항에서 오늘로 부터 기준 날짜안에 있는 공지사항을 모두 불러온다.
        RequestParams params = new RequestParams();
        client_user.addHeader("Cookie",cookieStore.getCookies().get(0).getValue());

        client_user.post(getString(R.string.URL) + "/company/question", params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    JSONArray result = response.getJSONArray("result");
                    if(result.length()>0){
                        config_information_new_textview.setVisibility(View.VISIBLE);
                    }
                    else
                        config_information_new_textview.setVisibility(View.GONE);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d("login", "compare regid"+ statusCode);
            }
        });


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.config_information_layout :
            {
                // 공지사항으로 이동
                Intent myIntent = new Intent(getApplicationContext(), InformationActivity.class);
                startActivity(myIntent);
                overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
                break;
            }
            case R.id.config_question_common_textview :
            {
                // 자주하는 질문
                Intent myIntent = new Intent(getApplicationContext(), FaqActivity.class);
                startActivity(myIntent);
                overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
                break;
            }
            case R.id.config_question_email_textview :
            {
                // 이메일 보내기
                Date date = new Date();

                Intent intent_email = new Intent(Intent.ACTION_SEND);
                intent_email.setType("plaine/text");
                intent_email.putExtra(Intent.EXTRA_EMAIL, new String[]{"{Email ID}@naver.com"}); // 보내는쪽 이메일
                intent_email.putExtra(Intent.EXTRA_SUBJECT, " 문의드립니다.  /    " + DateFormat.getDateInstance(DateFormat.FULL).format(date));
                startActivity(intent_email);

                break;
            }
            case R.id.config_clause_1_textview :
            {
                // 이용약관으로 이동
                Intent intent = new Intent(getApplicationContext(), SignUpClauseOneActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
                break;


            }
            case R.id.config_clause_2_textview :
            {
                // 개인정보 방침
                Intent intent = new Intent(getApplicationContext(), SignUpClauseTwoActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
                break;
            }
            case R.id.config_clause_license_textview :
            {
                // 오픈소스 라이선스
                break;
            }
            case R.id.config_logout_textview :
            {
                // 로그아웃시
                final AlertDialog.Builder builder = new AlertDialog.Builder(ConfigActivity.this);

                builder.setTitle("종료 확인 대화 상자")
                        .setMessage("앱을 종료 하시겠습니까?")
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                PersistentCookieStore cookieStore = new PersistentCookieStore(getApplicationContext());
                                Log.d("error", "Cookies 1 : " + cookieStore.getCookies().get(0).getName());

                                // registerId 업데이트 한다.
                                updateRegistId();

                                // 쿠키를 지우고 email으로 다시 초기화한다.
                                cookieStore.clear();
                                BasicClientCookie newCookie = new BasicClientCookie("email_cookie", "login");
                                newCookie.setVersion(1);
                                newCookie.setDomain("{Server Domain}");
                                newCookie.setPath("/");
                                newCookie.setValue(result[1]);
                                cookieStore.addCookie(newCookie);

                                // 로그인화면으로 intent 전환
                                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                                overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);


                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                AlertDialog dialog = builder.create();
                dialog.show();
                break;
            }
        }

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        checkRegid.checkRegid(getString(R.string.URL), cookieStore.getCookies().get(0).getValue(), ConfigActivity.this);
    }

    // 옵션메뉴에 대한 이벤트 처리
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
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
        super.onBackPressed();
    }

    // RegisterId 업데이트
    private void updateRegistId() {

        RequestParams params = new RequestParams();


        params.add("company_no", String.valueOf(company_no));
        params.add("company_reg_id", "LOGOUT");
        // DB - update 에 접근
        client_guest.post(getString(R.string.URL) + "/company/update/regid", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] bytes) {
                Log.d("error", "Regid update Success");
            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                Log.d("error", "Regid update fail..");


            }

        });

    }



}
