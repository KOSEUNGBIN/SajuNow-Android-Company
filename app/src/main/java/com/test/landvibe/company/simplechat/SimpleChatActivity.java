package com.test.landvibe.company.simplechat;

/**
 * Created by 고승빈 on 2016-02-22.
 */

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.test.landvibe.company.R;
import com.test.landvibe.company.chat.ChatActivity;
import com.test.landvibe.company.chat.ChatService;
import com.test.landvibe.company.common.CheckRegId;
import com.test.landvibe.company.login.LoginActivity;
import com.test.landvibe.company.util.DeEncrypter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLDecoder;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.impl.cookie.BasicClientCookie;

import static android.support.v4.content.WakefulBroadcastReceiver.startWakefulService;

public class SimpleChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";

    private MenuItem actionSettings;
    private long history_no;
    private long company_no;
    private boolean end_yn;
    private PersistentCookieStore cookieStore;
    private AsyncHttpClient client = new AsyncHttpClient();
    private String[] result;
    private TextView question1_tv;
    private TextView question2_tv;
    private TextView question3_tv;
    private TextView answer1_tv;
    private TextView answer2_tv;
    private TextView answer3_tv;

    private CheckRegId checkRegid;

            @Override
            protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_simple_chat);
                question1_tv = (TextView) findViewById(R.id.question1_tv);
                question2_tv = (TextView) findViewById(R.id.question2_tv);
                question3_tv = (TextView) findViewById(R.id.question3_tv);
                answer1_tv = (TextView)findViewById(R.id.answer1_tv);
                answer2_tv = (TextView)findViewById(R.id.answer2_tv);
                answer3_tv = (TextView)findViewById(R.id.answer3_tv);
                Intent intent = getIntent();

                history_no = intent.getIntExtra("history_no", 0);
                actionSettings = (MenuItem) findViewById(R.id.action_settings);

                setTitle(intent.getStringExtra("name") + "님과의 상담");

                checkRegid = new CheckRegId();

                ////////////////////////////쿠키 load, 복호화//////////////////////////////////////////////////////////////////////////
                cookieStore = new PersistentCookieStore(this);
                DeEncrypter deEncrypter = new DeEncrypter();
                Log.d("error", "token : " + URLDecoder.decode(cookieStore.getCookies().get(0).getValue()));
                String token = deEncrypter.decrypt(URLDecoder.decode(cookieStore.getCookies().get(0).getValue()));
                result = token.split("\\?");
                company_no = Integer.parseInt(result[0]);
                client.addHeader("Cookie", cookieStore.getCookies().get(0).getValue());

                checkRegid.checkRegid(getString(R.string.URL), cookieStore.getCookies().get(0).getValue(), SimpleChatActivity.this);
                ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

                RequestParams param = new RequestParams();
                client.post(getString(R.string.URL) + "/history/" + history_no, param, new JsonHttpResponseHandler() {
//            URL 에 +history_no 추가

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        super.onSuccess(statusCode, headers, response);

                        try {
                            Log.d("error",  response.getJSONObject("result").toString());
                            JSONArray msgList = response.getJSONObject("result").getJSONArray("msgList");
                            String[] question = msgList.getJSONObject(0).getString("message").split("`c!4~D]s");
                            String[] answer = msgList.getJSONObject(1).getString("message").split("`c!4~D]s");
                            question1_tv.setText(question[0]);
                            question2_tv.setText(question[1]);
                            question3_tv.setText(question[2]);
                            answer1_tv.setText(answer[0]);
                            answer2_tv.setText(answer[1]);
                            answer3_tv.setText(answer[2]);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }




                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        Log.d("error", "Chat_ListInsert_Fail");

                    }
                });

                // ChatActivity의 상단 툴바의 정의
                Toolbar toolbar = (Toolbar) findViewById(R.id.chat_toolbar);
                setSupportActionBar(toolbar);

                // ChatActivity의 상단 툴바의 뒤로가기 버튼을 보여준다.
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            }
    @Override
    protected void onRestart() {
        checkRegid.checkRegid(getString(R.string.URL), cookieStore.getCookies().get(0).getValue(), SimpleChatActivity.this);
        super.onRestart();
    }

    @Override
    public void onBackPressed() {

        // ChatActivity에서 뒤로가기로 나갈 시 채팅 Count 값을 0으로 초기화한다.
        RequestParams params = new RequestParams();



        client.post(getString(R.string.URL) + "/history/init/company/" + history_no, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.d("error", "Chat Count Initialize to 0 - Success");
                finish();
                overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);


            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.d("error", "Chat Count Initialize to 0 - Failed");

            }
        });

        super.onBackPressed();
        super. overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);


    }



    // ChatActivity에서 상단 뒤로가기 버튼을 눌렀을 시
    //  onBackPressed() 함수를 호출한다.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // 새로운 intent를 푸쉬 받았을 떄
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        //intent로 hitory_no와 message, 채팅방안에서 화면이 켜져있는지의 유무를 받아옴
        String msg = intent.getStringExtra("msge");
        String sender_name = intent.getStringExtra("sender_name");
        int broadcast_history_no = intent.getIntExtra("broadcast_history_no", 0);

        Log.d("error", "ChatActivity : Receive Message : " + msg + " History No : " + broadcast_history_no );


            Intent serviceIntent = new Intent(this, ChatService.class);
            serviceIntent.putExtra("STATUS", "MESSAGE");
            serviceIntent.putExtra("broadcast_history_no", broadcast_history_no);
            serviceIntent.putExtra("sender_name", sender_name);
            startWakefulService(this, serviceIntent);



    }

    String convertTimeFormat(String original){

        // 2016-05-04 10:25:24.0
        String hour = original.substring(11,13);
        String result = "";
        int hour_ = Integer.parseInt(hour);
        if (hour_ > 12)
        {
            if(hour_ == 12)
                ;
            else
                hour_ -= 12;
            result += "오후 " + hour_;
        }
        else
        {
            result += "오전 " + hour_;
        }
        result = result + original.substring(13,16);

        return result;
    }



}
