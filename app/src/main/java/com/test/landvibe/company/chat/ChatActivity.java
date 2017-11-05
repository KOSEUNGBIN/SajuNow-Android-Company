package com.test.landvibe.company.chat;

/**
 * Created by 고승빈 on 2016-02-22.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.test.landvibe.company.HistoryActivity;
import com.test.landvibe.company.R;
import com.test.landvibe.company.common.CheckRegId;
import com.test.landvibe.company.util.DeEncrypter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cz.msebera.android.httpclient.Header;

import static android.support.v4.content.WakefulBroadcastReceiver.startWakefulService;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";

    private EditText msgEdit;
    private Button sendBtn;
    private ImageButton plusBtn;
    private ImageView chatImagelistLarge;
    private LinearLayout chatFrontLayout;
    private LinearLayout chatBoxLayout;
    private MenuItem actionSettings;
    Bitmap resized;

    private Button chatEndBtn;
    private ListView chat_list;
    private Intent intent;
    private long history_no;
    private long company_no;
    private boolean end_yn;
    private int select_history;

    private PersistentCookieStore cookieStore;
    private AsyncHttpClient client = new AsyncHttpClient();
    private String[] result;

    List<Object> list = new ArrayList<Object>();

    private int REQUEST_CAMERA = 0, SELECT_FILE = 1;

    private int imagePostion = 1;

    private CheckRegId checkRegid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Intent intent = getIntent();

        history_no = intent.getIntExtra("history_no", 0);
        select_history = intent.getIntExtra("select_history", -1);
        end_yn = intent.getBooleanExtra("end_yn", false);
        actionSettings = (MenuItem) findViewById(R.id.action_settings);

        // 채팅이 끝났지를 확인한다.
        if (end_yn) {
            // 채팅이 끝났을 경우, 메시지 보내는 레이아웃을 GONE 시킨다.
            chatBoxLayout = (LinearLayout) findViewById(R.id.chat_box_layout);
            chatBoxLayout.setVisibility(View.GONE);

        }
        String[] convertSeleteHistory = {"전문", "간단", "관상손금", "해몽", "작명"};
        setTitle(intent.getStringExtra("name") + "님과의 " + convertSeleteHistory[select_history] + " 상담");

        msgEdit = (EditText) findViewById(R.id.send_text);
        sendBtn = (Button) findViewById(R.id.send_btn);
        plusBtn = (ImageButton) findViewById(R.id.plus_btn);

        chatImagelistLarge = (ImageView) findViewById(R.id.chat_imagelist_large);
        chatFrontLayout = (LinearLayout) findViewById(R.id.chat_front_layout);

        chatEndBtn = (Button) findViewById(R.id.chat_end_btn);

        ////////////////////////////쿠키 load, 복호화//////////////////////////////////////////////////////////////////////////
        cookieStore = new PersistentCookieStore(this);
        DeEncrypter deEncrypter = new DeEncrypter();
        Log.d("error", "token : " + URLDecoder.decode(cookieStore.getCookies().get(0).getValue()));
        String token = deEncrypter.decrypt(URLDecoder.decode(cookieStore.getCookies().get(0).getValue()));
        result = token.split("\\?");
        company_no = Integer.parseInt(result[0]);
        client.addHeader("Cookie", cookieStore.getCookies().get(0).getValue());

        checkRegid = new CheckRegId();
        checkRegid.checkRegid(getString(R.string.URL), cookieStore.getCookies().get(0).getValue(), ChatActivity.this);
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


        // ChatActivity의 메시지 입력하는 레이아웃에서
        // 보내기 버튼을 눌렀을시  sendMsg() 함수를 호출한다. -> GCM으로 메시지를 보내는 함수
        sendBtn.setOnClickListener(new View.OnClickListener() {
            //메세지 보내기
            @Override
            public void onClick(View v) {

                //메세지가 없을 경우 안보냄
                if (msgEdit.getText().toString().length() != 0) {
                    sendBtn.setClickable(false);
                    sendMsg();
                }


            }

        });

        // ChatActivity의 메시지 입력하는 레이아웃에서
        // 플러스 버튼을 눌렀을시 selectImage() 함수를 호출한다. - > dialog를 띄우는 함수
        plusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();

            }
        });

        // ChatActivity의 메시지 Listd에서 이미지를 눌렀을 시
        // 기존의 레이아웃을 GONE 시키고 이미지를 VISIBLE 시킨다.
        chatImagelistLarge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chatFrontLayout.setVisibility(View.VISIBLE);

                chatImagelistLarge.setVisibility(View.GONE);
            }
        });

        chat_listview();

        // ChatActivity의 상단 툴바의 정의
        Toolbar toolbar = (Toolbar) findViewById(R.id.chat_toolbar);
        setSupportActionBar(toolbar);

        // ChatActivity의 상단 툴바의 뒤로가기 버튼을 보여준다.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // ChatActivity의 상단 툴바의 "상담종료" 버튼을 눌렀을시 실행한다.
        chatEndBtn.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {

                // 상담을 종료할 지에 대한 다이얼로그를 띄운다.
                final AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);

                builder.setTitle("상담 종료 확인 대화 상자")
                        .setMessage("상담을 종료 하시겠습니까?")
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                // 확인 버튼을 눌렀을 시
                                // 서버로 상담종료 메시지를 전달한다.
                                if (!end_yn) {

                                    RequestParams params = new RequestParams();

                                    params.put("history_no", history_no);
                                    // 우선 10으로 해놓고 , 바꿀수도 있음
                                    params.put("sender", 10);
                                    params.put("company_no", company_no);

                                    client.post(getString(R.string.URL) + "/chat/terminate", params, new AsyncHttpResponseHandler() {
                                        @Override
                                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
//                                            Toast.makeText(ChatActivity.this,
//                                                    "상담 종료 성공", Toast.LENGTH_LONG)
//                                                    .show();

                                            chatBoxLayout = (LinearLayout) findViewById(R.id.chat_box_layout);
                                            chatBoxLayout.setVisibility(View.GONE);
                                            onBackPressed();


                                        }

                                        @Override
                                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                            Toast.makeText(ChatActivity.this,
                                                    "오류가 발생하였습니다.\n다시 시도해주세요.",
                                                    Toast.LENGTH_LONG).show();

                                        }
                                    });
                                } else {
                                    Intent intend = new Intent(ChatActivity.this, HistoryActivity.class);
                                    intend.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                    startActivity(intend);
                                }

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

            }
        });


    }

    // 처음 시작할 때 Chat list를 초기화 해준다. 또한,
    // 화면이 꺼져있다가 켜질때와 GCM Receiver가 올 때 reflesh를 시켜준다
    @Override
    protected void onRestart() {
        checkRegid.checkRegid(getString(R.string.URL), cookieStore.getCookies().get(0).getValue(), ChatActivity.this);

        // History No에 대한 메시지 List를 모두 보여주는 chat_listview() 함수 호출한다.
        chat_listview();
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
        super.overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);


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

    // Message GCM을 보내는 함수
    private void sendMsg() {
        //현재시간
        SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String time = sdfNow.format(new Date(System.currentTimeMillis()));


        RequestParams params = new RequestParams();
        params.put("history_no", history_no);
        params.put("sender", 10);
        params.put("send_date", time);
        params.put("select_history", select_history);


        if (10 == 10)  // 보내는 메시지 내용이 Text일 경우
            params.put("message", msgEdit.getText());
        else            // 보내는 메시지 내용이 Image일 경우
            params.put("message", "image");


        View view;
        Date dNow = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd hh:mm");

        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.chat_listview_view, null);
        TextView chat_textlist_me = (TextView) view.findViewById(R.id.chat_textlist_me);
        chat_textlist_me.setVisibility(View.VISIBLE);
        chat_textlist_me.setText(msgEdit.getText().toString());
        TextView send_time = (TextView) view.findViewById(R.id.send_time);
        send_time.setText(convertTimeFormat(ft.format(dNow)));
        RelativeLayout.LayoutParams saveLayoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        saveLayoutParams.addRule(RelativeLayout.LEFT_OF, chat_textlist_me.getId());
        saveLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        send_time.setLayoutParams(saveLayoutParams);
        chat_list.addFooterView(view);

        list.add(view);
        msgEdit.setText("");


        client.post(getString(R.string.URL) + "/chat/insert", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
//                Toast.makeText(ChatActivity.this,
//                        "메세지 보내기 성공 ㅎ", Toast.LENGTH_LONG)
//                        .show();

                //  chat_listview();
                sendBtn.setClickable(true);

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(ChatActivity.this,
                        "오류가 발생하였습니다.\n" +
                                "다시 시도해주세요.",
                        Toast.LENGTH_LONG).show();

            }
        });
    }

    // 새로운 intent를 푸쉬 받았을 떄
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        //intent로 hitory_no와 message, 채팅방안에서 화면이 켜져있는지의 유무를 받아옴
        String msg = intent.getStringExtra("msge");
        int broadcast_history_no = intent.getIntExtra("broadcast_history_no", 0);

        Log.d("error", "ChatActivity : Receive Message : " + msg + " History No : " + broadcast_history_no);

        // 해당 채팅방에 있을경우,  chat_listview()를 재호출 한다.
        if (broadcast_history_no == history_no) {
            chat_listview();

        }
        // 다른 채팅방에 있을 경우, Notification을 울리기 위해 ChatService로 Intent를 넘긴다.
        else {
            Intent serviceIntent = new Intent(this, ChatService.class);
            serviceIntent.putExtras(intent);
            startWakefulService(this, serviceIntent);

        }


    }

    private class chatListAdapter extends BaseAdapter {

        private JSONArray jsonArray = null;
        private JSONObject jsonObject = null;
        private Context context = null;
        private LayoutInflater inflater = null;
        private int count = 0;

        public chatListAdapter(Context context, JSONObject jsonObject) {

            try {
                this.context = context;

                this.jsonObject = jsonObject.getJSONObject("result");
                this.jsonArray = this.jsonObject.getJSONArray("msgList");

                count = jsonArray.length();

                this.inflater = LayoutInflater.from(this.context);

            } catch (JSONException e) {
                e.printStackTrace();
            }


        }

        //  chatListAdapter 뷰홀더
        class ViewHolder {
            TextView chat_textlist_me;
            TextView chat_textlist_you;
            ImageView chat_imagelist_me;
            ImageView chat_imagelist_you;
            TextView send_time;
        }


        @Override
        public int getCount() {
            return count;
        }

        @Override
        public Object getItem(int position) {

            try {

                return jsonArray.getJSONObject(position);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View layoutView = convertView;
            ViewHolder viewHolder = null;
            chatImagelistLarge = (ImageView) findViewById(R.id.chat_imagelist_large);
            chatFrontLayout = (LinearLayout) findViewById(R.id.chat_front_layout);

            Log.d("getView", "getView 호출");


            if (layoutView == null) {

                // 뷰,뷰홀더 초기화
                inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                layoutView = inflater.inflate(R.layout.chat_listview_view, null);
                viewHolder = new ViewHolder();
                viewHolder.chat_textlist_me = (TextView) layoutView.findViewById(R.id.chat_textlist_me);
                viewHolder.chat_textlist_you = (TextView) layoutView.findViewById(R.id.chat_textlist_you);
                viewHolder.chat_imagelist_me = (ImageView) layoutView.findViewById(R.id.chat_imagelist_me);
                viewHolder.chat_imagelist_you = (ImageView) layoutView.findViewById(R.id.chat_imagelist_you);
                viewHolder.send_time = (TextView) layoutView.findViewById(R.id.send_time);


                // 뷰 저장
                layoutView.setTag(viewHolder);

            } else {
                // 뷰 재사용

                viewHolder = (ViewHolder) layoutView.getTag();

            }


            // sender id 가 '0'이면 Company , '1'이면 User
            try {


                int sender_id = jsonArray.getJSONObject(position).getInt("sender");
                Log.d("ImagePosition", "sender Id" + sender_id);
                switch (sender_id) {
                    case 0: // User Text 일 경우
                        viewHolder.chat_textlist_me.setVisibility(View.GONE);
                        viewHolder.chat_imagelist_me.setVisibility(View.GONE);
                        viewHolder.chat_imagelist_you.setVisibility(View.GONE);
                        viewHolder.chat_textlist_you.setVisibility(View.VISIBLE);
                        viewHolder.chat_textlist_you.setText(jsonArray.getJSONObject(position).getString("message"));
                        viewHolder.send_time.setText(convertTimeFormat(jsonArray.getJSONObject(position).getString("send_date")));

                        RelativeLayout.LayoutParams saveLayoutParams = new RelativeLayout.LayoutParams(
                                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                        saveLayoutParams.addRule(RelativeLayout.RIGHT_OF, viewHolder.chat_textlist_you.getId());
                        saveLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                        viewHolder.send_time.setLayoutParams(saveLayoutParams);
                        break;
                    case 1: // User Image 일 경우
                        viewHolder.chat_textlist_me.setVisibility(View.GONE);
                        viewHolder.chat_imagelist_me.setVisibility(View.GONE);
                        viewHolder.chat_textlist_you.setVisibility(View.GONE);
                        viewHolder.chat_imagelist_you.setVisibility(View.VISIBLE);
                        GlideUrl glideUrl = new GlideUrl(getString(R.string.URL) + "/history/image/" + history_no + "/" + jsonArray.getJSONObject(position).getLong("message_no"), new LazyHeaders.Builder()
                                .addHeader("Cookie", cookieStore.getCookies().get(0).getValue())
                                .build());

                        Glide.with(getApplicationContext())
                                .load(glideUrl)
                                .override(1000, 333)
                                .into(viewHolder.chat_imagelist_you);

                        viewHolder.send_time.setText(convertTimeFormat(jsonArray.getJSONObject(position).getString("send_date")));

                        RelativeLayout.LayoutParams saveLayoutParams2 = new RelativeLayout.LayoutParams(
                                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                        saveLayoutParams2.addRule(RelativeLayout.RIGHT_OF, viewHolder.chat_imagelist_you.getId());
                        saveLayoutParams2.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                        viewHolder.send_time.setLayoutParams(saveLayoutParams2);
                        break;
                    case 10: // Company Text 일 경우
                        viewHolder.chat_imagelist_me.setVisibility(View.GONE);
                        viewHolder.chat_imagelist_you.setVisibility(View.GONE);
                        viewHolder.chat_textlist_you.setVisibility(View.GONE);
                        viewHolder.chat_textlist_me.setVisibility(View.VISIBLE);
                        viewHolder.chat_textlist_me.setText(jsonArray.getJSONObject(position).getString("message"));
                        viewHolder.send_time.setText(convertTimeFormat(jsonArray.getJSONObject(position).getString("send_date")));

                        RelativeLayout.LayoutParams saveLayoutParams3 = new RelativeLayout.LayoutParams(
                                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                        saveLayoutParams3.addRule(RelativeLayout.LEFT_OF, viewHolder.chat_textlist_me.getId());
                        saveLayoutParams3.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                        viewHolder.send_time.setLayoutParams(saveLayoutParams3);

                        break;
                    case 11: // Company Image 일 경우
                        viewHolder.chat_textlist_me.setVisibility(View.GONE);
                        viewHolder.chat_imagelist_me.setVisibility(View.VISIBLE);
                        viewHolder.chat_textlist_you.setVisibility(View.GONE);
                        viewHolder.chat_imagelist_you.setVisibility(View.GONE);
                        Log.d("ImagePosition", "11 - " + imagePostion);

                        GlideUrl glideUrl2 = new GlideUrl(getString(R.string.URL) + "/history/image/" + history_no + "/" + jsonArray.getJSONObject(position).getLong("message_no"), new LazyHeaders.Builder()
                                .addHeader("Cookie", cookieStore.getCookies().get(0).getValue())
                                .build());
                        Glide.with(getApplicationContext()).load(glideUrl2)
                                .override(1000, 333)
                                .into(viewHolder.chat_imagelist_me);

                        viewHolder.send_time.setText(convertTimeFormat(jsonArray.getJSONObject(position).getString("send_date")));
                        RelativeLayout.LayoutParams saveLayoutParams4 = new RelativeLayout.LayoutParams(
                                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                        saveLayoutParams4.addRule(RelativeLayout.LEFT_OF, viewHolder.chat_imagelist_me.getId());
                        saveLayoutParams4.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                        viewHolder.send_time.setLayoutParams(saveLayoutParams4);

                        break;
                    default:
                        break;
                    //qbd
                }
            } catch (JSONException e) {

                Log.d("error", "Catched!!!!!!!!!!!!!!!");
                e.printStackTrace();
            }

            viewHolder.chat_imagelist_me.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Bitmap bmap = getViewBitmap(v);

                    chatImagelistLarge.setImageBitmap(bmap);
                    chatFrontLayout.setVisibility(View.GONE);

                    chatImagelistLarge.setVisibility(View.VISIBLE);


                }
            });

            viewHolder.chat_imagelist_you.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bitmap bmap = getViewBitmap(v);

                    chatImagelistLarge.setImageBitmap(bmap);
                    chatFrontLayout.setVisibility(View.GONE);

                    chatImagelistLarge.setVisibility(View.VISIBLE);


                }
            });


            return layoutView;
        }
    }

    public void chat_listview() {

        for (int i = 0; i < list.size(); i++) {
            chat_list.removeFooterView((View) list.get(i));
        }
        RequestParams param = new RequestParams();

        imagePostion = 1;

        // DB에서 해당 History No에 대한 Chat List를 모두 불러온다.
        client.post(getString(R.string.URL) + "/history/" + history_no, param, new JsonHttpResponseHandler() {
//            URL 에 +history_no 추가

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);

                try {

                    select_history = response.getJSONObject("result").getInt("select_history");
                    //  상담이 종료된 상태일 경우
                    // 상단 툴바의  "상담 종료"을 안보이게 한다.
                    if (response.getJSONObject("result").getBoolean("end_yn")) {
                        chatEndBtn.setVisibility(View.GONE);
                    }
                    //  상담이 종료되지 않은 상태일 경우
                    // 상단 툴바의  "상담 종료"을 보이게 한다.
                    else {
                        chatEndBtn.setVisibility(View.VISIBLE);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // Chat List를 Adapter에 부친다.
                chatListAdapter listAdapter = new chatListAdapter(ChatActivity.this, response);
                chat_list = (ListView) findViewById(R.id.chat_list);
                chat_list.setAdapter(listAdapter);
                chat_list.setSelection(listAdapter.getCount());


                Log.d("error", "Chat_List_Aception_Success");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d("error", "Chat_ListInsert_Fail");
                if (statusCode == 200) {
                    //  200  ==>  서버에 접근은 하였으나, 인증과정에서 Token 값이 바뀌었기 때문에 인증이 되지않는 경우 이다.
                    Toast.makeText(getApplicationContext(), "다른 디바이스에서 해당 계정으로 로그인 되었습니다.\n 본 디바이스는 로그아웃 됩니다.", Toast.LENGTH_LONG).show();
                    checkRegid.logout(ChatActivity.this);
                } else {
                    Toast.makeText(getApplicationContext(), "오류가 발생하였습니다.\n" +
                            "다시 시도해주세요." + responseString, Toast.LENGTH_LONG).show();
                    //  그외의 에러 코드가 들어온다.
                }
                Toast.makeText(ChatActivity.this, "오류가 발생하였습니다.\n" +
                        "다시 시도해주세요.", Toast.LENGTH_LONG).show();

            }
        });
    }


    // 사진 고르기 다이얼로그 실행
    private void selectImage() {
        final CharSequence[] items = {"Take Photo", "Choose from Library",
                "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    Log.d("error", "is REQUEST_CAMERA ");
                    startActivityForResult(intent, REQUEST_CAMERA);
                } else if (items[item].equals("Choose from Library")) {
                    Log.d("error", "is EXTERNAL_CONTENT_URI ");
                    Intent intent = new Intent(
                            Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(
                            Intent.createChooser(intent, "Select File"),
                            SELECT_FILE);
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    //
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE) {
                onSelectFromGalleryResult(data);
                Log.d("error", "is SELECT_FILE ");

            } else if (requestCode == REQUEST_CAMERA) {
                onCaptureImageResult(data);
                Log.d("error", "is REQUEST_CAMERA ");
            }
        }
    }

    //방금 찍힌 사진 표시
    private void onCaptureImageResult(Intent data) {

        Log.d("error", "onCaptureImage");


        Bitmap thumbnail;
        try {
            thumbnail = (Bitmap) data.getExtras().get("data");
        } catch (Exception e) {
            Intent intent_uri = new Intent();
            onSelectFromGalleryResult(intent_uri);
            return;
            //  thumbnail =(Bitmap) data.getData();
        }
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();

        BitmapFactory.Options options = new BitmapFactory.Options();
        if (thumbnail.getWidth() + thumbnail.getHeight() < 1000)
            thumbnail = Bitmap.createScaledBitmap(thumbnail, thumbnail.getWidth(), thumbnail.getHeight(), false);
        else if (thumbnail.getWidth() + thumbnail.getHeight() < 2000)
            thumbnail = Bitmap.createScaledBitmap(thumbnail, thumbnail.getWidth() / 2, thumbnail.getHeight() / 2, false);
        else
            thumbnail = Bitmap.createScaledBitmap(thumbnail, thumbnail.getWidth() / 3, thumbnail.getHeight() / 3, false);


        final File destination = new File(Environment.getExternalStorageDirectory(),
                System.currentTimeMillis() + ".jpg");

        FileOutputStream fo;
        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        if (thumbnail.getWidth() >= thumbnail.getHeight()) {
//
//            thumbnail = Bitmap.createBitmap(
//                    thumbnail,
//                    thumbnail.getWidth() / 2 - thumbnail.getHeight() / 2,
//                    0,
//                    thumbnail.getHeight(),
//                    thumbnail.getHeight()
//            );
//
//        } else {
//
//            thumbnail = Bitmap.createBitmap(
//                    thumbnail,
//                    0,
//                    thumbnail.getHeight() / 2 - thumbnail.getWidth() / 2,
//                    thumbnail.getWidth(),
//                    thumbnail.getWidth()
//            );
        SaveBitmapToFileCache(thumbnail, destination.getPath());
        //   thumbnail.recycle();
        thumbnail = null;

        File file = null;
        file = new File(destination.getPath());
        RequestParams params = new RequestParams();
        try {
            params.put("profile_picture", file);
            Log.d("error", "image is stored");

        } catch (FileNotFoundException e) {
            Log.d("error", e.toString());
        }

        client.post(getString(R.string.URL) + "/history/upload/image/company/" + history_no, params, new FileAsyncHttpResponseHandler(this) {

            @Override
            public void onFailure(int i, Header[] headers, Throwable throwable, File file) {
                Log.d("error", "fail!!");
            }

            @Override
            public void onSuccess(int i, Header[] headers, File file) {
                Log.d("error", file.getName() + ", " + file.getPath());
                Log.d("error", "success");
                destination.deleteOnExit();
                destination.delete();
//                    sendMsg(11);
                chat_listview();

            }


        });
    }


    //갤러리에서 선택한 사진 표시
    private void onSelectFromGalleryResult(Intent data) {


        Log.d("error", "onSelectFromGalleryResult : " + company_no);
        Uri selectedImageUri = data.getData();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        String[] projection = {MediaStore.MediaColumns.DATA};
        Cursor cursor;
        String selectedImagePath = "";
        try {
            cursor = managedQuery(selectedImageUri, projection, null, null,
                    null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            cursor.moveToFirst();

            selectedImagePath = cursor.getString(column_index);

        } catch (NullPointerException e) {
            selectedImagePath = getOriginalImagePath();
        }

        final File destination = new File(Environment.getExternalStorageDirectory(),
                System.currentTimeMillis() + ".jpg");
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inDither = true;


        BitmapFactory.decodeFile(selectedImagePath, options);
        Log.d("error", "outHeight : " + options.outHeight);
        Log.d("error", "outWidth : " + options.outWidth);

        int photoWidth = options.outWidth;
        int photoHeight = options.outHeight;

        int scaleFactor = Math.min(photoWidth / 333, photoHeight / 1000);

        options.inSampleSize = scaleFactor;
        options.inJustDecodeBounds = false;

        resized = BitmapFactory.decodeFile(selectedImagePath, options);
        //resized.recycle();
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(selectedImagePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        int exifOrientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        float exifDegree = exifOrientationToDegrees(exifOrientation);
        Log.d("error", "exifDegree : " + exifOrientation);
        Log.d("error", "exifDegree : " + exifDegree);
        resized = imgRotate(resized, exifDegree);

//        if (resized.getWidth() >= resized.getHeight()){
//
//            resized = Bitmap.createBitmap(
//                    resized,
//                    resized.getWidth()/2 - resized.getHeight()/2,
//                    0,
//                    resized.getHeight(),
//                    resized.getHeight()
//            );
//
//        }else{
//
//            resized = Bitmap.createBitmap(
//                    resized,
//                    0,
//                    resized.getHeight() / 2 - resized.getWidth() / 2,
//                    resized.getWidth(),
//                    resized.getWidth()
//            );
//        }


        resized.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

        FileOutputStream fo;
        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        SaveBitmapToFileCache(resized, destination.getPath());
        //resized.recycle();
        resized = null;


        File file = null;
        file = new File(destination.getPath());
        Log.d("error", destination.getPath());
        RequestParams params = new RequestParams();
        try {
            params.put("profile_picture", file);
            params.put("sender", 11);
            Log.d("error", "image is stored");

        } catch (FileNotFoundException e) {
            Log.d("error", e.toString());
        }


        client.post(getString(R.string.URL) + "/history/upload/image/company/" + history_no, params, new FileAsyncHttpResponseHandler(this) {

            @Override
            public void onFailure(int i, Header[] headers, Throwable throwable, File file) {
                Log.d("error", "fail!!");
            }

            @Override
            public void onSuccess(int i, Header[] headers, File file) {
                Log.d("error", file.getName() + ", " + file.getPath());
                Log.d("error", "success");
                destination.deleteOnExit();
                destination.delete();
//                    sendMsg(11);
                chat_listview();

            }


            /*public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d("error", "fail!!");

            }*/
        });
        Log.d("error", "end process");
//        ivImage.setImageBitmap(bm);
    }

    private Bitmap imgRotate(Bitmap bmp, float degree) {
        int width = bmp.getWidth();
        int height = bmp.getHeight();

        Matrix matrix = new Matrix();
        matrix.postRotate(degree);

        Bitmap resizedBitmap = Bitmap.createBitmap(bmp, 0, 0, width, height, matrix, true);
        // bmp.recycle();

        return resizedBitmap;
    }

    private void SaveBitmapToFileCache(Bitmap bitmap, String strFilePath) {

        File fileCacheItem = new File(strFilePath);
        OutputStream out = null;

        try {
            fileCacheItem.createNewFile();
            out = new FileOutputStream(fileCacheItem);

            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public int exifOrientationToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }

    public String getOriginalImagePath() {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection, null, null, null);
        int column_index_data = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToLast();

        return cursor.getString(column_index_data);
    }

    private Bitmap getViewBitmap(View v) {
        v.clearFocus();
        v.setPressed(false);

        boolean willNotCache = v.willNotCacheDrawing();
        v.setWillNotCacheDrawing(false);

        // Reset the drawing cache background color to fully transparent
        // for the duration of this operation
        int color = v.getDrawingCacheBackgroundColor();
        v.setDrawingCacheBackgroundColor(0);

        if (color != 0) {
            v.destroyDrawingCache();
        }
        v.buildDrawingCache();
        Bitmap cacheBitmap = v.getDrawingCache();

        Bitmap bitmap = Bitmap.createBitmap(cacheBitmap);

        // Restore the view
        v.destroyDrawingCache();
        v.setWillNotCacheDrawing(willNotCache);
        v.setDrawingCacheBackgroundColor(color);

        return bitmap;
    }

    String convertTimeFormat(String original) {

        // 2016-05-04 10:25:24.0
        String hour = original.substring(11, 13);
        String result = "";
        int hour_ = Integer.parseInt(hour);
        if (hour_ > 12) {
            if (hour_ == 12)
                ;
            else
                hour_ -= 12;
            result += "오후 " + hour_;
        } else {
            result += "오전 " + hour_;
        }
        result = result + original.substring(13, 16);

        return result;
    }
}
