package com.test.landvibe.company.chat;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import java.net.URLDecoder;
import java.util.List;

/**
 * 푸쉬 메세지를 받는 Receiver 정의
 */
public class ChatBroadcastReceiver extends WakefulBroadcastReceiver {

    private PowerManager ppm;
    private static final String TAG = "ChatBroadcastReceiver";

    //서버에서 보낸 데이터 intent에 저장
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "action : " + action);

        if (action != null) {
            // 푸시 메시지 수신 시
            if (action.equals("com.google.android.c2dm.intent.RECEIVE")) {
                try {
                    Log.d("error", "Receive Message GCM ");

                    // intent로 받은 메시지를 문자열로 디코드한다.
                    String history_no_temp = URLDecoder.decode(intent.getStringExtra("history_no"), "UTF-8");
                    String msg = URLDecoder.decode(intent.getStringExtra("msg"), "UTF-8");
                    String sender_name = URLDecoder.decode(intent.getStringExtra("sender_name"), "UTF-8");
                    String select_history_temp = URLDecoder.decode(intent.getStringExtra("select_history"), "UTF-8");
                    String isSubscribe_temp = URLDecoder.decode(intent.getStringExtra("isSubscribe"), "UTF-8");
                    int history_number = Integer.parseInt(history_no_temp);
                    int select_history = Integer.parseInt(select_history_temp);
                    boolean isSubscribe = Boolean.parseBoolean(isSubscribe_temp);

                    // 해당 휴대폰이 켜져있는지와 TOP 액티비를 확인한다.
                    ppm = (PowerManager) context
                            .getSystemService(Context.POWER_SERVICE);
                    ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                    List<ActivityManager.RunningTaskInfo> Info = am.getRunningTasks(1);
                    ComponentName topActivity = Info.get(0).topActivity;

                    //현재 제일 위에 올라와있는 activity 이름을 초기화한다.
                    String topactivityname = topActivity.getClassName();


                    // 휴대폰의 화면이 켜져있는지 꺼져있는지를 확인한다.
                    if (ppm.isScreenOn()) {

                        Log.d("error", "Turn On Phone , Top Activity - " + topactivityname + " , History No : " + history_number);

                        // 화면이 켜져 있을 경우
                        // 현재 TOP Activity가 ChatActivity 또는 HistoryActivity 일 경우
                        if (topactivityname.equals("com.test.landvibe.company.chat.ChatActivity")||topactivityname.equals("com.test.landvibe.company.HistoryActivity")) {

                            // 메시지와 history no,을 인텐트에 담고 RedirectActivity에 인텐트를 넘긴다.
                            Intent chatIntent = new Intent(context, RedirectActivity.class);
                            chatIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            chatIntent.putExtra("msge", msg);
                            chatIntent.putExtra("sender_name", sender_name);
                            chatIntent.putExtra("topactivity_name", topactivityname);
                            chatIntent.putExtra("broadcast_history_no", history_number);
                            chatIntent.putExtra("select_history", select_history);
                            chatIntent.putExtra("isSubscribe",isSubscribe);

                            context.startActivity(chatIntent);
                        }
                        // 화면이 켜져 있을 경우
                        // 현재 TOP Activity가 ChatActivity,CompanyHistoryActivity이 아닐 경우
                        else {
                             // ChatService에 history no을 담은 인텐트를 넘긴다.
                            Intent serviceIntent = new Intent(context, ChatService.class);
                            serviceIntent.putExtra("STATUS", "MESSAGE");
                            serviceIntent.putExtra("sender_name", sender_name);
                            serviceIntent.putExtra("broadcast_history_no", history_number);
                            serviceIntent.putExtra("select_history", select_history);
                            serviceIntent.putExtra("isSubscribe",isSubscribe);
                            startWakefulService(context, serviceIntent);
                        }
                    } else {
                        // 화면이 꺼져 있을 경우
                        Log.d("error", "Turn Off Phone , Top Activity - " + topactivityname + " , History No : " + history_number);

                        // ChatService에 history no을 담은 인텐트를 넘긴다.
                        Intent serviceIntent = new Intent(context, ChatService.class);
                        serviceIntent.putExtra("STATUS", "MESSAGE");
                        serviceIntent.putExtra("broadcast_history_no", history_number);
                        serviceIntent.putExtra("sender_name", sender_name);
                        serviceIntent.putExtra("select_history", select_history);
                        serviceIntent.putExtra("isSubscribe",isSubscribe);
                        startWakefulService(context, serviceIntent);
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            } else {
                Log.d(TAG, "Unknown action : " + action);
            }
        } else {
            Log.d(TAG, "action is null.");
        }
    }
}



