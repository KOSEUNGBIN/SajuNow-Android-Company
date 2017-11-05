package com.test.landvibe.company.main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.test.landvibe.company.util.DeEncrypter;
import com.test.landvibe.company.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLDecoder;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Administrator on 2016-02-23.
 */
public class MainInfoFragment extends Fragment
{
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_main_introduce, container, false);
        final TextView profile_hello_sentense_tv = (TextView) view.findViewById(R.id.profile_hello_sentense_tv);
        final TextView profile_introduce_sentense_tv = (TextView) view.findViewById(R.id.profile_introduce_sentense_tv);
        final TextView profile_time_sentense_tv = (TextView) view.findViewById(R.id.profile_time_sentense_tv);
        final TextView profile_address_sentense_tv = (TextView) view.findViewById(R.id.profile_address_sentense_tv);

        ///////////////////////company_no 획득/////////////////////////////////
        PersistentCookieStore cookieStore = new PersistentCookieStore(getActivity());
        DeEncrypter deEncrypter = new DeEncrypter();
        Log.d("error", "token : " + URLDecoder.decode(cookieStore.getCookies().get(0).getValue()));
        String token = deEncrypter.decrypt(URLDecoder.decode(cookieStore.getCookies().get(0).getValue()));
        String[] result = token.split("\\?");
        int company_no = Integer.parseInt(result[0]);
/////////////////////////////////////////////////////////////////////////////////////
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams param = new RequestParams();

        client.addHeader("Cookie", "PASSWORD");


        client.get(getString(R.string.URL) + "/company/join/report/" + company_no, param, new JsonHttpResponseHandler() {
           //해당 company에 등록된 report를 모두 불러온다.
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    String result[] = response.getString("possible_time").split("\\,");
                    profile_hello_sentense_tv.setText(response.getString("greeting"));
                    profile_introduce_sentense_tv.setText(response.getString("introduce"));
                    try {
                        profile_time_sentense_tv.setText(" "+result[0] +"\n" +result[1]);
                    }
                   catch (ArrayIndexOutOfBoundsException e)
                   {
                       ;
                   }
                    profile_address_sentense_tv.setText(response.getString("address"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
            }
        });



        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
    }
}

