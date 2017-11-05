package com.test.landvibe.company.login;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.test.landvibe.company.MainActivity;
import com.test.landvibe.company.R;
import com.test.landvibe.company.util.DeEncrypter;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLDecoder;

import cz.msebera.android.httpclient.Header;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{
private EditText id_et;
    private EditText pw_et;
    private Button sign_btn;
    private Button login_btn;
    private TokenBroadcastReceiver tokenReceiverService;
    private String reg_id;
    private static final String LOGOUT = "LOGOUT";
    private static final String DUPLICATED = "DUPLICATED";
    private static final String BLOCK = "BLOCK";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        id_et =  (EditText) findViewById(R.id.id_et);
        pw_et =  (EditText) findViewById(R.id.pw_et);
        pw_et.setHint("비밀번호");
        login_btn =  (Button) findViewById(R.id.login_btn);
        login_btn.setOnClickListener(this);
        sign_btn =  (Button) findViewById(R.id.signup_btn);
        sign_btn.setOnClickListener(this);

        tokenReceiverService = new TokenBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PreRegistrationIntentService.MY_ACTION);
        registerReceiver(tokenReceiverService, intentFilter);

        //Start our own service
        Intent intent = new Intent(this, PreRegistrationIntentService.class);
        startService(intent);



    }


    private class TokenBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            // TODO Auto-generated method stub

            reg_id = arg1.getStringExtra("token");
            Log.d("error", "reg_id in onReceive : "+ reg_id);

            unregisterReceiver(tokenReceiverService);


        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        final PersistentCookieStore myCookieStore = new PersistentCookieStore(this);
        if(myCookieStore.getCookies().size() > 0 && myCookieStore.getCookies().get(0).getName().equals("UserKey")){

            getInstanceIdToken(Integer.parseInt(new DeEncrypter().decrypt(URLDecoder.decode(myCookieStore.getCookies().get(0).getValue())).split("\\?")[0]));
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        }
        else if(myCookieStore.getCookies().size() == 0)
        {
            return;
        }
        else
            id_et.setText(myCookieStore.getCookies().get(0).getValue());
    }

    public void getInstanceIdToken(int company_no) {
        // Start IntentService to register this application with GCM.
        Intent intent = new Intent(this, RegistrationIntentService.class);
        intent.putExtra("company_no",company_no);
        startService(intent);
    }

    @Override
    public void onClick(View v) {
        if(v==login_btn)
        {
            Log.d("error", "reg_id in onClick :" + reg_id);
            AsyncHttpClient client = new AsyncHttpClient();
            RequestParams params = new RequestParams();
            final PersistentCookieStore myCookieStore = new PersistentCookieStore(this);
            myCookieStore.clear();
            client.setCookieStore(myCookieStore);
            client.addHeader("Cookie","PASSWORD");
            params.put("email", id_et.getText());
            params.put("password", pw_et.getText());
            params.put("company_reg_id", reg_id);
            client.post(getString(R.string.URL) + "/company/login",
                    params, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers,
                                              JSONObject response) {
                            super.onSuccess(statusCode, headers, response);
                            try {
                                if(response.getString("company_reg_id").equals(LOGOUT)) {

                                    Toast.makeText(
                                            LoginActivity.this,
                                            response.getString("nick_name")
                                                    + "님 안녕하세요", Toast.LENGTH_SHORT)
                                            .show();
                                    getInstanceIdToken(response.getInt("company_no"));
                                    Log.d("error", "success : " + myCookieStore.getCookies().toString());
                                    // onResume();
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
                                    finish();
                                }else if (response.getString("company_reg_id").equals(DUPLICATED)) {

                                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                                    builder.setTitle("주의")
                                            .setMessage( "다른 디바이스에서 사용중인 계정입니다.\n기존의 디바이스를 로그아웃 시키고 이 디바이스를 사용하려면 확인을 눌러주세요")
                                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {

                                                    duplicateLogin();
                                                    dialog.dismiss();

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

                                    Toast.makeText(
                                            LoginActivity.this,
                                            "현재 활성화된 다른 계정이 존재합니다.", Toast.LENGTH_SHORT)
                                            .show();
                                }
                                else if (response.getString("company_reg_id").equals(BLOCK)) {
                                    Toast.makeText(
                                            LoginActivity.this,
                                            "해당 계정은 차단된 상태입니다.\n고객센터에 문의해주세요", Toast.LENGTH_SHORT)
                                            .show();
                                }
                            } catch (Exception e) {
                                Toast.makeText(LoginActivity.this,
                                        "로그인 중 오류가 발생하였습니다.\n다시 시도해주세요.", Toast.LENGTH_LONG)
                                        .show();
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers,
                                              String responseString, Throwable throwable) {
                            super.onFailure(statusCode, headers,
                                    responseString, throwable);
                            Toast.makeText(LoginActivity.this,
                                    "아이디 또는 비밀번호를 다시 확인하세요.\n사주Now에 등록되지 않은 아이디이거나, 아이디 또는 비밀번호를 잘못 입력하셨습니다.",
                                    Toast.LENGTH_LONG).show();
                        }
                    });

        }
        else if(v==sign_btn)
        {
            Intent myIntent = new Intent(getApplicationContext(), SignUpActivity.class);
            startActivity(myIntent);

        }

    }

    public void duplicateLogin(){
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        final PersistentCookieStore myCookieStore = new PersistentCookieStore(this);
        myCookieStore.clear();
        client.setCookieStore(myCookieStore);
        client.addHeader("Cookie","PASSWORD");
        params.put("email", id_et.getText());
        params.put("password", pw_et.getText());

        client.post(getString(R.string.URL) + "/company/change/userkey", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.d("login", "duplicate login success"+ statusCode);
                try {
                    getInstanceIdToken(response.getInt("user_no"));

                    Toast.makeText(
                            LoginActivity.this,
                            response.getString("nick_name")
                                    + "님 안녕하세요", Toast.LENGTH_SHORT)
                            .show();

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
                        finish();


                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                Log.d("login", "duplicate login fail"+ statusCode + throwable.getMessage());

//                Toast.makeText(
//                        LoginActivity.this,
//
//                                "duplicate login fail"+ statusCode + throwable.getMessage(), Toast.LENGTH_SHORT)
//                        .show();
            }
        });
    }
}
