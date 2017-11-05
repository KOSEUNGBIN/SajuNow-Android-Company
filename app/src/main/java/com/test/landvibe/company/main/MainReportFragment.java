package com.test.landvibe.company.main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.test.landvibe.company.util.DeEncrypter;
import com.test.landvibe.company.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLDecoder;
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;
/**
 * Created by Administrator on 2016-02-23.
 */
public class MainReportFragment extends Fragment
{
    private AsyncHttpClient client = new AsyncHttpClient();
    private RequestParams param = new RequestParams();
    private JSONObject m_obj;
    private JSONArray m_orders;
    private MainReportAdapter m_adapter;
    private ArrayList<JSONObject> m_viewitem;
    private int company_no;
    private ListView profile_review_sentense_lv;

    private ViewGroup m_rootView;


    @Nullable

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        m_viewitem = new ArrayList<>();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_main_report, container, false);
        profile_review_sentense_lv = (ListView) view.findViewById(R.id.report_listview);


        /////////////////company_no받아오기////////////////////////
        PersistentCookieStore cookieStore = new PersistentCookieStore(getActivity());
        DeEncrypter deEncrypter = new DeEncrypter();
        String token = deEncrypter.decrypt(URLDecoder.decode(cookieStore.getCookies().get(0).getValue()));
        String[] result = token.split("\\?");
        company_no = Integer.parseInt(result[0]);
    ////////////////////////////////////////////////////////////////////////

        client.addHeader("Cookie", "PASSWORD");
        client.get(getString(R.string.URL) + "/company/join/report/" + company_no, param, new JsonHttpResponseHandler() {
        //company에 등록된 모든 report를 불러온다
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    Log.d("error", "통신성공");

                    m_orders = response.getJSONArray("companyToReport");
                    for(int i=0;i<m_orders.length();i++)
                    {
                        m_obj = m_orders.getJSONObject(i);
                        m_viewitem.add(m_obj);      //JSONObject를 list에 삽입
                    }
                    Log.d("error", ""+m_orders);
                    m_adapter = new MainReportAdapter(getActivity(), R.layout.activity_profile_review, m_viewitem);     //정보가 삽입된 list를 adapter에 삽입
                    profile_review_sentense_lv.setAdapter(m_adapter);       //listview에 adapter를 set

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