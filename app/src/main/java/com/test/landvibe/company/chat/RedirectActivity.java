package com.test.landvibe.company.chat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.test.landvibe.company.HistoryActivity;

/**
 * Created by jik on 2016-03-03.
 */

public class RedirectActivity extends Activity {
    //ChatBroadcastReceiver에서 넘어온 intent를 그대로 chatActivity로 보내준다,
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Log.d("error", "GCM Message Is Send To RedirectActivity - ");
        Intent intent = getIntent();

        // Top Activity가 ChatActivity로 intent를 넘긴다.
        if(intent.getStringExtra("topactivity_name").equals("com.test.landvibe.company.chat.ChatActivity"))
        {
            intent.setClass(RedirectActivity.this, ChatActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
        // Top Activity가 HistoryActivity intent를 넘긴다.
        else{
            intent.setClass(RedirectActivity.this, HistoryActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
    }


}
