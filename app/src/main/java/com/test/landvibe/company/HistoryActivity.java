package com.test.landvibe.company;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.test.landvibe.company.chat.ChatActivity;
import com.test.landvibe.company.chat.ChatService;
import com.test.landvibe.company.common.CheckRegId;
import com.test.landvibe.company.login.LoginActivity;
import com.test.landvibe.company.simplechat.SimpleChatActivity;
import com.test.landvibe.company.simplechat.SimplePrepareActivity;
import com.test.landvibe.company.util.DeEncrypter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.impl.cookie.BasicClientCookie;

import static android.support.v4.content.WakefulBroadcastReceiver.startWakefulService;


/**
 * Created by 고승빈 on 2016-02-22.
 */

public class HistoryActivity extends AppCompatActivity {


    private int company_no;
    private int history_no;
    private PersistentCookieStore cookieStore;
    private  String[] result;

    AsyncHttpClient client = new AsyncHttpClient();
    private Toolbar toolbar;
    private DrawerLayout drawer;

    private CheckRegId checkRegid;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company_history);


        checkRegid = new CheckRegId();

        cookieStore = new PersistentCookieStore(this);
        client.addHeader("Cookie", cookieStore.getCookies().get(0).getValue());
        DeEncrypter deEncrypter = new DeEncrypter();
        String token = deEncrypter.decrypt(URLDecoder.decode(cookieStore.getCookies().get(0).getValue()));
        result = token.split("\\?");
        company_no = Integer.parseInt(result[0]);
        Log.d("error", "company_no : " + company_no);

        checkRegid.checkRegid(getString(R.string.URL), cookieStore.getCookies().get(0).getValue(), HistoryActivity.this);

        history_listview();

        toolbar = (Toolbar) findViewById(R.id.toolbar_history);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


    }



    private class historyListAdapter extends BaseAdapter {

        JSONArray jsonArray = null;
        JSONObject jsonObject = null;
        Context context = null;
        LayoutInflater inflater = null;

        public historyListAdapter(Context context, JSONObject jsonObject) {
            try {
                this.context = context;
                this.jsonArray = jsonObject.getJSONObject("result").getJSONArray("list");
                this.inflater = LayoutInflater.from(this.context);
            } catch (JSONException e) {
                e.printStackTrace();
            }


        }

        //  chatListAdapter 뷰홀더
        class ViewHolder {
            ImageView history_image;
            TextView history_userName;
            TextView message;
            TextView message_time;
            TextView history_notread;
            TextView select_history;
        }


        @Override
        public int getCount() {
            if (jsonArray.length() != 0)
                return jsonArray.length();
            else
                return 0;
        }

        @Override
        public Object getItem(int position) {
            try {
                jsonObject = jsonArray.getJSONObject(position);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return jsonObject;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View layoutView = convertView;
            ViewHolder viewHolder = null;


            if (layoutView == null) {

                // 뷰,뷰홀더 초기화
                inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                layoutView = inflater.inflate(R.layout.history_listview_view, null);
                viewHolder = new ViewHolder();
                viewHolder.history_image = (ImageView) layoutView.findViewById(R.id.history_image);
                viewHolder.history_userName = (TextView) layoutView.findViewById(R.id.history_userName);
                viewHolder.message_time = (TextView) layoutView.findViewById(R.id.message_time);
                viewHolder.history_notread = (TextView) layoutView.findViewById(R.id.history_notread);
                viewHolder.message = (TextView) layoutView.findViewById(R.id.history_chatmessage);
                viewHolder.select_history = (TextView) layoutView.findViewById(R.id.select_history);

                // 뷰 저장
                layoutView.setTag(viewHolder);

            } else {
                // 뷰 재사용

                viewHolder = (ViewHolder) layoutView.getTag();

            }


            try {

                int ex = jsonArray.getJSONObject(position).getInt("company_not_read");

                Date to = new Date();
                SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                try {
                    to = transFormat.parse(jsonArray.getJSONObject(position).getJSONObject("msg").getString("send_date"));
                } catch (ParseException e) {
                    e.printStackTrace();
                }



                viewHolder.message.setText(jsonArray.getJSONObject(position).getJSONObject("msg").getString("message"));

                switch (jsonArray.getJSONObject(position).getInt("select_history")){
                    case 0 : {
                        viewHolder.select_history.setText("전문사주");
                        viewHolder.select_history.setTextColor(Color.parseColor("#8A0829"));
                        break;
                    }
                    case 1 : {
                        viewHolder.message.setText("");

                        viewHolder.select_history.setText("간단사주");
                        viewHolder.select_history.setTextColor(Color.parseColor("#2E2EFE"));
                        break;
                    }
                    case 2 : {
                        viewHolder.select_history.setText("관상손금");
                        viewHolder.select_history.setTextColor(Color.parseColor("#F6E3CE"));
                        break;
                    }
                    case 3 : {
                        viewHolder.select_history.setText("해몽");
                        viewHolder.select_history.setTextColor(Color.parseColor("#AC58FA"));
                        break;
                    }
                    case 4 : {
                        viewHolder.select_history.setText("작명");
                        viewHolder.select_history.setTextColor(Color.parseColor("#A4A4A4"));
                        break;
                    }
                    default: {
                        break;
                    }
                }

                viewHolder.history_userName.setText(jsonArray.getJSONObject(position).getString("name"));

                viewHolder.message_time.setText(calculateTime(to));

                if (!jsonArray.getJSONObject(position).getBoolean("end_yn")) {
                    viewHolder.history_notread.setText("" + ex);
                    viewHolder.history_notread.setBackgroundResource(R.drawable.message_counter);
                    //viewHolder.history_notread.setBackgroundColor(Color.parseColor("#c0392b"));
                    viewHolder.history_notread.setTextSize(20);
                } else {
                    Log.d("error", "positiion : " + position);
                    // viewHolder.history_notread.setText("상담 종료됨 " + jsonArray.getJSONObject(position).getString("end_date").substring(0, 10));
                    viewHolder.history_notread.setText("상담 종료됨");
                    //viewHolder.history_notread.setBackgroundColor(Color.parseColor("#2c3e50"));
                    viewHolder.history_notread.setBackgroundResource(R.drawable.message_terminated);
                    viewHolder.history_notread.setTextSize(8);

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }


            layoutView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent;
                    try {
                        if(jsonArray.getJSONObject(position).getInt("select_history") != 1) {
                             intent = new Intent(HistoryActivity.this, ChatActivity.class);
                        }
                        else
                        {
                            if(jsonArray.getJSONObject(position).getBoolean("end_yn"))
                            {
                                intent = new Intent(HistoryActivity.this, SimpleChatActivity.class);
                            }
                            else
                            {
                                intent = new Intent(HistoryActivity.this, SimplePrepareActivity.class);
                            }
                        }
                        history_no = jsonArray.getJSONObject(position).getInt("history_no");
                        int select_history  =  jsonArray.getJSONObject(position).getInt("select_history");

                        updateRegistId(company_no, history_no);
                        intent.putExtra("history_no", history_no);
                        intent.putExtra("end_yn", jsonArray.getJSONObject(position).getBoolean("end_yn"));
                        intent.putExtra("name", jsonArray.getJSONObject(position).getString("name"));
                        intent.putExtra("select_history",select_history);
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            });


            final int historyDeletePosition = position;
            final ViewHolder finalViewHolderDelete = viewHolder;
            layoutView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(HistoryActivity.this);
                    builder.setTitle("상담내역 삭제 확인");

                    try {
                        String historyDelete_endDate = jsonArray.getJSONObject(historyDeletePosition).getString("end_date");
                        Log.d("error", "" + historyDelete_endDate);
                        if (historyDelete_endDate.equals("null")) {
                            history_no = jsonArray.getJSONObject(historyDeletePosition).getInt("history_no");
                            final boolean end_yn = jsonArray.getJSONObject(historyDeletePosition).getBoolean("end_yn");
                            final String historyName = jsonArray.getJSONObject(historyDeletePosition).getString("name");

                            builder.setMessage(finalViewHolderDelete.history_userName.getText().toString() + "님 과 상담 중입니다.\n해당 상담내역을 삭제할 수 없습니다.\n채팅방으로 들어가시겠습니까?")
                                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(final DialogInterface dialog, int which) {


//                            report_check = jsonArray.getJSONObject(historyPosition).getBoolean("is_report_alarmed");

                                            Intent intent = new Intent(HistoryActivity.this, ChatActivity.class);


                                            updateRegistId(company_no, history_no);
                                            intent.putExtra("history_no", history_no);
                                            intent.putExtra("end_yn", end_yn);
                                            intent.putExtra("name", historyName);
                                            startActivity(intent);
                                            overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);

                                            dialog.dismiss();


                                        }
                                    })
                                    .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            history_listview();
                                        }
                                    });
                        } else {


                            String historyDelete_endDate_1[] = historyDelete_endDate.split("-");
                            String historyDelete_endDate_2[] = historyDelete_endDate_1[2].split(" ");
                            String historyDelete_endDate_3[] = historyDelete_endDate_2[1].split(":");


                            builder.setMessage(finalViewHolderDelete.history_userName.getText().toString() + " 역술인과 " + historyDelete_endDate_1[0] + "년 " + historyDelete_endDate_1[1] + "월 " + historyDelete_endDate_2[0] + "일\n" + historyDelete_endDate_3[0] + "시 " + historyDelete_endDate_3[1] + "분에 끝난 상담내역을 삭제하시겠습니까?")
                                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(final DialogInterface dialog, int which) {

                                            try {
                                                int historyDeleteNo = jsonArray.getJSONObject(historyDeletePosition).getInt("history_no");

                                                RequestParams historyDelete_param = new RequestParams();
                                                client.post(getString(R.string.URL) + "/history/delete/company/" + historyDeleteNo, historyDelete_param, new AsyncHttpResponseHandler() {
                                                    @Override
                                                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
//                                                        Toast.makeText(HistoryActivity.this,
//                                                                "History Delete Success", Toast.LENGTH_LONG)
//                                                                .show();
                                                        dialog.dismiss();
                                                        history_listview();
                                                    }

                                                    @Override
                                                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                                        Toast.makeText(HistoryActivity.this,
                                                                "오류가 발생하였습니다.\n" +
                                                                        "다시 시도해주세요.",
                                                                Toast.LENGTH_LONG).show();
                                                        history_listview();

                                                    }
                                                });
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }


                                        }
                                    })
                                    .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            history_listview();
                                        }
                                    });
                        }
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    return false;
                }
            });


            return layoutView;
        }
    }


    private void history_listview() {

        RequestParams param = new RequestParams();
        final PersistentCookieStore cookieStore = new PersistentCookieStore(this);
        DeEncrypter deEncrypter = new DeEncrypter();

        String token = deEncrypter.decrypt(URLDecoder.decode(cookieStore.getCookies().get(0).getValue()));
        String[] result = token.split("\\?");
        int user_no = Integer.parseInt(result[0]);


        client.post(getString(R.string.URL) + "/history/alluser/" + user_no, param, new JsonHttpResponseHandler() {


            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);

//                try {
////                    Toast.makeText(HistoryActivity.this,
////                            "안읽은 총개수 : " + response.getJSONObject("result").getLong("NotReadAll"),
////                            Toast.LENGTH_LONG).show();
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }

                historyListAdapter listAdapter = new historyListAdapter(HistoryActivity.this, response);
                ListView history_list = (ListView) findViewById(R.id.history_list);
                history_list.setAdapter(listAdapter);

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d("error", "History_ListInsert_Fail");
                if (statusCode == 200) {
                    //  200  ==>  서버에 접근은 하였으나, 인증과정에서 Token 값이 바뀌었기 때문에 인증이 되지않는 경우 이다.
                    Toast.makeText(getApplicationContext(), "다른 디바이스에서 해당 계정으로 로그인 되었습니다.\n 본 디바이스는 로그아웃 됩니다.", Toast.LENGTH_LONG).show();
                    checkRegid.logout(HistoryActivity.this);
                } else {
                    Toast.makeText(getApplicationContext(), "오류가 발생하였습니다.\n" +
                            "다시 시도해주세요." + responseString, Toast.LENGTH_LONG).show();
                    //  그외의 에러 코드가 들어온다.
                }
                Toast.makeText(HistoryActivity.this, "오류가 발생하였습니다.\n" +
                        "다시 시도해주세요.", Toast.LENGTH_LONG).show();

            }
        });
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
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // 처음 시작할 때 history list를 초기화 해준다. 또한,
    // 화면이 꺼져있다가 켜질때와 GCM Receiver가 올 때 reflesh를 시켜준다
    @Override
    protected void onRestart() {
        checkRegid.checkRegid(getString(R.string.URL), cookieStore.getCookies().get(0).getValue(), HistoryActivity.this);
        history_listview();
        super.onRestart();
    }

    // 새로운 intent를 푸쉬 받았을 때 호출된다.
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        Log.d("error", "HistoryActivity : Receive Signer : " );

        // reflesh를 시켜준다.
        history_listview();

        // ChatService에 history no을 담은 인텐트를 넘긴다.
        Intent serviceIntent = new Intent(getApplicationContext(), ChatService.class);
        serviceIntent.putExtras(intent);
        startWakefulService(getApplicationContext(), serviceIntent);
    }


    private void updateRegistId(int company_no, long history_no) {
        RequestParams params = new RequestParams();

        params.put("company_no", company_no);
        params.put("history_no", history_no);


        client.post(getString(R.string.URL) + "/history/update/company", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
//                Toast.makeText(HistoryActivity.this,
//                        "RegistId Update Success", Toast.LENGTH_LONG)
//                        .show();
//

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(HistoryActivity.this,
                        "오류가 발생하였습니다.\n" +
                                "다시 시도해주세요.",
                        Toast.LENGTH_LONG).show();

            }
        });

    }

    private static class TIME_MAXIMUM
    {
        public static final int SEC = 60;
        public static final int MIN = 60;
        public static final int HOUR = 24;
        public static final int DAY = 30;
        public static final int MONTH = 12;
    }

    public String calculateTime(Date date)
    {

        long curTime = System.currentTimeMillis();
        long regTime = date.getTime();
        long diffTime = (curTime - regTime) / 1000;

        String msg = null;

        if (diffTime < TIME_MAXIMUM.SEC)
        {
            // sec
            msg = diffTime + "초전";
        }
        else if ((diffTime /= TIME_MAXIMUM.SEC) < TIME_MAXIMUM.MIN)
        {
            // min
            System.out.println(diffTime);

            msg = diffTime + "분전";
        }
        else if ((diffTime /= TIME_MAXIMUM.MIN) < TIME_MAXIMUM.HOUR)
        {
            // hour
            msg = (diffTime ) + "시간전";
        }
        else if ((diffTime /= TIME_MAXIMUM.HOUR) < TIME_MAXIMUM.DAY)
        {
            // day
            msg = (diffTime ) + "일전";
        }
        else if ((diffTime /= TIME_MAXIMUM.DAY) < TIME_MAXIMUM.MONTH)
        {
            // day
            msg = (diffTime ) + "달전";
        }
        else
        {
            msg = (diffTime) + "년전";
        }

        return msg;
    }



}