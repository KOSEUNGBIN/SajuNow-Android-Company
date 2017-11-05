package com.test.landvibe.company.chat;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.test.landvibe.company.HistoryActivity;
import com.test.landvibe.company.R;
import com.test.landvibe.company.util.DeEncrypter;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLDecoder;

import cz.msebera.android.httpclient.Header;

//import static android.content.Intent.getIntent;

/**
 *  리시버에서 나눠진 STATUS로 수행할 작업을 실행
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class ChatService extends Service {
    private int history_no, temp=-1;
    private  String sender_name;
    private  int select_history;
    private boolean isSubscribe;

    private SharedPreferences alarmSwitch_SP;

    NotificationManager nm;
    Context  context;
    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    @Nullable

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    // ChatBroadcastReceiver 에서 보낸 Intent를 이곳에서 수신을 한다.
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){

        history_no = intent.getIntExtra("broadcast_history_no",0);
        sender_name = intent.getStringExtra("sender_name");
        isSubscribe = intent.getBooleanExtra("isSubscribe",false);
        select_history = intent.getIntExtra("select_history",0);

        alarmSwitch_SP = getSharedPreferences("alarmSwitch", MODE_PRIVATE);

        Log.d("error", "isSubscribe : " + isSubscribe +" / select_history : "+select_history);
        Log.d("error", "Call To Notification By GCM Message");

        if (alarmSwitch_SP.getBoolean("alarmSwitchCondition", false)) {
            // notification 함수 호출
            Notification();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy(){
        nm.cancel(history_no);
        temp =-1;
    }

    // Notification 함수 정의
 public void Notification() {

     AsyncHttpClient client = new AsyncHttpClient();
     RequestParams param = new RequestParams();


     ////////company_no을 받아오는 과정///////////////////////////////////////////////////////
     PersistentCookieStore cookieStore = new PersistentCookieStore(this);
     client.addHeader("Cookie",cookieStore.getCookies().get(0).getValue());
     DeEncrypter deEncrypter = new DeEncrypter();

     String token = deEncrypter.decrypt(URLDecoder.decode(cookieStore.getCookies().get(0).getValue()));
     String[] result = token.split("\\?");
     int user_no = Integer.parseInt(result[0]);
////////////////////////////////////////////////////////////////////////////////////////////

     client.post(getString(R.string.URL) + "/history/alluser/" + user_no, param, new JsonHttpResponseHandler() {
        //user_no에 등록된 history 정보를 가져옴
         long notreadmsg;

         @Override
         public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
             super.onSuccess(statusCode, headers, response);
             try {
                 notreadmsg = response.getJSONObject("result").getLong("NotReadAll");   //notificaion에 안읽은 메세지 갯수를 넣어줌
                 int not_read_msg = (int) notreadmsg;
                 nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                 Resources res = getResources();
                 PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                         new Intent(context, HistoryActivity.class), 0);

                 String notificationMessage;
                 String [] convertSeleteHistory = {"전문","간단","관상손금","해몽","작명"};
                 if(isSubscribe){
                         notificationMessage = sender_name+" 님에게서 "+convertSeleteHistory[select_history]+"사주 상담 신청이 왔습니다.";
                 }else{
                     notificationMessage = sender_name+" 님에게서 "+convertSeleteHistory[select_history]+" 메세지가 왔습니다.";
                 }


                 NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                         .setContentTitle("사주NOW")
                         .setContentText(notificationMessage)
                         .setTicker(notificationMessage)
                         .setSmallIcon(R.mipmap.ic_launcher)
                         .setLargeIcon(BitmapFactory.decodeResource(res,R.mipmap.ic_launcher))
                         .setContentIntent(pendingIntent)
                         .setAutoCancel(true)
                         .setWhen(System.currentTimeMillis())
                         .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS)
                         .setNumber(not_read_msg);

                 Notification n = builder.build();


                 ///////////////다른 채팅방에서 notification를 보내면 최신 notification만 띄어준다////////////
                 if (temp == -1) {
                     nm.notify(history_no, n);
                     temp = history_no;
                 } else if (history_no == temp) {
                     nm.notify(history_no, n);
                 } else if (history_no != temp && temp != -1) {
                     nm.cancel(temp);
                     nm.notify(history_no, n);
                     temp = history_no;
                 }
                 ///////////////////////////////////////////////////////////////////////////////////////////
             } catch (JSONException e) {
                 e.printStackTrace();
             }
         }

         @Override
         public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
             Log.d("error", "History_ListInsert_Fail");
         }
     });
 }
}