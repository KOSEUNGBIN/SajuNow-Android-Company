package com.test.landvibe.company.common;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.test.landvibe.company.login.LoginActivity;
import com.test.landvibe.company.util.DeEncrypter;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLDecoder;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.impl.cookie.BasicClientCookie;

public class CheckRegId {

    private AsyncHttpClient client = new AsyncHttpClient();
    // 쿠키의 결과를 담을 변수
    private String[] result;
    private int company_no;
    private PersistentCookieStore cookieStore;

    public void checkRegid(String URL, final String userkey, final Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> Info = am.getRunningTasks(1);
        final ComponentName name = Info.get(0).topActivity;


        DeEncrypter deEncrypter = new DeEncrypter();
        Log.d("error", "token : " + URLDecoder.decode(userkey));
        String token = deEncrypter.decrypt(URLDecoder.decode(userkey));
        result = token.split("\\?");
        company_no = Integer.parseInt(result[0]);


        RequestParams params_checkId = new RequestParams();

        params_checkId.put("company_no", String.valueOf(company_no));
        params_checkId.put("userkey", userkey);
        client.addHeader("Cookie", userkey);
        client.post(URL + "/company/compare/regid", params_checkId, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    Log.d("login", "-----------------------------------------------");
                    Log.d("login", "" + name.getClassName());
                    Log.d("login", "result : " + response.getString("result"));
                    Log.d("login", "compare regid : " + response.getInt("code"));
                    Log.d("login", "-----------------------------------------------");
                    if (response.getString("result").equals("block")) {
                        Toast.makeText(context, "계정이 차단되어 로그아웃 됩니다.\n 고객센터에 문의해주세요.", Toast.LENGTH_LONG).show();
                        logout(context);
                    } else
                        ;

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                super.onSuccess(statusCode, headers, response);

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                if (statusCode == 200) {

                    Log.d("checkRegId", "onFailure ");
                    Log.d("checkRegId", "statusCode : "+ statusCode);

                    //  200  ==>  서버에 접근은 하였으나, 인증과정에서 Token 값이 바뀌었기 때문에 인증이 되지않는 경우 이다.
                    Toast.makeText(context, "다른 디바이스에서 해당 계정으로 로그인 되었습니다.\n 본 디바이스는 로그아웃 됩니다.", Toast.LENGTH_LONG).show();
                    logout(context);
                } else {
                    Toast.makeText(context, "오류가 발생하였습니다.\n" +
                            "다시 시도해주세요." + responseString, Toast.LENGTH_LONG).show();
                    //  그외의 에러 코드가 들어온다.
                }
            }
        });

        Log.d("checkRegId", "-------------------------------------------------");
    }

    public void logout( final Context context) {

        cookieStore = new PersistentCookieStore(context);
        cookieStore.clear();
        BasicClientCookie newCookie = new BasicClientCookie("email_cookie", "login");
        newCookie.setVersion(1);
        newCookie.setDomain("{Server Domain}");
        newCookie.setPath("/");
        newCookie.setValue(result[1]);
        cookieStore.addCookie(newCookie);
        Intent intent = new Intent(context, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);

    }
}


