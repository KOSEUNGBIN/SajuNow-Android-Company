package com.test.landvibe.company;


import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.test.landvibe.company.common.CheckRegId;
import com.test.landvibe.company.simplechat.SimplePrepareActivity;
import com.test.landvibe.company.util.DeEncrypter;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;

/**
 * 오프라인 일정관리에 대한 액티비티
 */

public class ScheduleActivity extends AppCompatActivity  {

    private TextView dateText;
    private Calendar calendar;
    private Calendar tempCalendar;
    private Button submit_btn;
    private String r_day = null;
    private String r_day_set[];
    private ImageButton goToMenu_btn;
    private int currentColor = 0xFF666666;
    private ImageView date_left_arrow;
    private ImageView date_right_arrow;
    private ToggleButton slot_9_button;
    private ToggleButton slot_10_button;
    private ToggleButton slot_11_button;
    private ToggleButton slot_12_button;
    private ToggleButton slot_13_button;
    private ToggleButton slot_14_button;
    private ToggleButton slot_15_button;
    private ToggleButton slot_16_button;
    private ToggleButton slot_17_button;
    private ToggleButton slot_18_button;
    private ToggleButton slot_19_button;

    private int company_no;

    private Toolbar toolbar;
    private DrawerLayout drawer;
    private PersistentCookieStore cookieStore;
    private AsyncHttpClient client = new AsyncHttpClient();


    SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");

    private CheckRegId checkRegid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company_schedule);
        tempCalendar =Calendar.getInstance();


        date_left_arrow = (ImageView) findViewById(R.id.schedule_left_arrow);
        date_right_arrow = (ImageView) findViewById(R.id.schedule_right_arrow);
        slot_9_button = (ToggleButton) findViewById(R.id.slot_9_btn);
        slot_10_button = (ToggleButton) findViewById(R.id.slot_10_btn);
        slot_11_button = (ToggleButton) findViewById(R.id.slot_11_btn);
        slot_12_button = (ToggleButton) findViewById(R.id.slot_12_btn);
        slot_13_button = (ToggleButton) findViewById(R.id.slot_13_btn);
        slot_14_button = (ToggleButton) findViewById(R.id.slot_14_btn);
        slot_15_button = (ToggleButton) findViewById(R.id.slot_15_btn);
        slot_16_button = (ToggleButton) findViewById(R.id.slot_16_btn);
        slot_17_button = (ToggleButton) findViewById(R.id.slot_17_btn);
        slot_18_button = (ToggleButton) findViewById(R.id.slot_18_btn);
        slot_19_button = (ToggleButton) findViewById(R.id.slot_19_btn);

        toolbar = (Toolbar) findViewById(R.id.toolbar_schedule);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        checkRegid = new CheckRegId();

        ////////////////////////////쿠키 load, 복호화//////////////////////////////////////////////////////////////////////////
        cookieStore = new PersistentCookieStore(this);
        DeEncrypter deEncrypter = new DeEncrypter();
        Log.d("error", "token : " + URLDecoder.decode(cookieStore.getCookies().get(0).getValue()));
        String token = deEncrypter.decrypt(URLDecoder.decode(cookieStore.getCookies().get(0).getValue()));
        String[] result = token.split("\\?");
        company_no = Integer.parseInt(result[0]);


        checkRegid.checkRegid(getString(R.string.URL), cookieStore.getCookies().get(0).getValue(), ScheduleActivity.this);
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        client.addHeader("Cookie",cookieStore.getCookies().get(0).getValue());
        slot_9_button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (slot_9_button.isChecked()) {
                    slot_9_button.setText("상담가능");
                } else {
                    slot_9_button.setText("예약중");
                }
            }
        });
        slot_10_button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (slot_10_button.isChecked()) {
                    slot_10_button.setText("상담가능");
                } else {
                    slot_10_button.setText("예약중");
                }
            }
        });
        slot_11_button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (slot_11_button.isChecked()) {
                    slot_11_button.setText("상담가능");
                } else {
                    slot_11_button.setText("예약중");
                }
            }
        });
        slot_12_button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (slot_12_button.isChecked()) {
                    slot_12_button.setText("상담가능");
                } else {
                    slot_12_button.setText("예약중");
                }
            }
        });
        slot_13_button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (slot_13_button.isChecked()) {
                    slot_13_button.setText("상담가능");
                } else {
                    slot_13_button.setText("예약중");
                }
            }
        });
        slot_14_button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (slot_14_button.isChecked()) {
                    slot_14_button.setText("상담가능");
                } else {
                    slot_14_button.setText("예약중");
                }
            }
        });
        slot_15_button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (slot_15_button.isChecked()) {
                    slot_15_button.setText("상담가능");
                } else {
                    slot_15_button.setText("예약중");
                }
            }
        });
        slot_16_button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (slot_16_button.isChecked()) {
                    slot_16_button.setText("상담가능");
                } else {
                    slot_16_button.setText("예약중");
                }
            }
        });
        slot_17_button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (slot_17_button.isChecked()) {
                    slot_17_button.setText("상담가능");
                } else {
                    slot_17_button.setText("예약중");
                }
            }
        });
        slot_18_button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (slot_18_button.isChecked()) {
                    slot_18_button.setText("상담가능");
                } else {
                    slot_18_button.setText("예약중");
                }
            }
        });
        slot_19_button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (slot_19_button.isChecked()) {
                    slot_19_button.setText("상담가능");
                } else {
                    slot_19_button.setText("예약중");
                }
            }
        });

        date_left_arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                tempCalendar = Calendar.getInstance(); //오늘
                if(calendar.after(tempCalendar)) {
                    calendar.add(Calendar.DAY_OF_YEAR, -1);
                    updateDateText();
                }else
                {
                    Toast.makeText(ScheduleActivity.this,
                            "일정은 오늘을 기준으로 14일 후 까지 설정 가능합니다.",
                            Toast.LENGTH_LONG).show();

                }
                tempCalendar.clear();
            }
        });
        date_right_arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tempCalendar = Calendar.getInstance(); //오늘
                tempCalendar.add(Calendar.DAY_OF_YEAR, 13);
                if(calendar.before(tempCalendar)) {
                    calendar.add(Calendar.DAY_OF_YEAR, 1);
                    updateDateText();
                }
                else{
                    Toast.makeText(ScheduleActivity.this,
                            "일정은 오늘을 기준으로 14일 후 까지 설정 가능합니다.",
                            Toast.LENGTH_LONG).show();
                }
                tempCalendar.clear();
            }
        });

        dateText = (TextView) findViewById(R.id.myReservation_date_tv);
        dateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(ScheduleActivity.this, dateSetListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        submit_btn = (Button) findViewById(R.id.submit_btn);
        submit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                RequestParams params = new RequestParams();
                params.put("company_no", company_no);
                params.put("date", dateText.getText());
                params.put("slot_09", slot_9_button.isChecked());
                params.put("slot_10", slot_10_button.isChecked());
                params.put("slot_11", slot_11_button.isChecked());
                params.put("slot_12", slot_12_button.isChecked());
                params.put("slot_13", slot_13_button.isChecked());
                params.put("slot_14", slot_14_button.isChecked());
                params.put("slot_15", slot_15_button.isChecked());
                params.put("slot_16", slot_16_button.isChecked());
                params.put("slot_17", slot_17_button.isChecked());
                params.put("slot_18", slot_18_button.isChecked());
                params.put("slot_19", slot_19_button.isChecked());


                Log.d("error", dateText.getText().toString());
                client.post(getString(R.string.URL) + "/schedule/insert",
                        params, new AsyncHttpResponseHandler() {
                            @Override
                            public void onSuccess(int i, Header[] headers, byte[] bytes) {

                                Intent refresh = new Intent(getApplicationContext(), ScheduleActivity.class);

                                Bundle bundle = new Bundle();
                                bundle.putInt("year", calendar.get(Calendar.YEAR));
                                bundle.putInt("month", calendar.get(Calendar.MONTH));
                                bundle.putInt("day", calendar.get(Calendar.DAY_OF_MONTH));
//                                Toast.makeText( ScheduleActivity.this, "일정이 수정되었습니다.", Toast.LENGTH_SHORT).show();
                                Log.d("error", "I'm on the Success");
                                refresh.putExtras(bundle);
                                startActivity(refresh);
                                overridePendingTransition(0, 0);
                                ScheduleActivity.this.finish();


                            }

                            @Override
                            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {

                                Log.d("error", "I'm on the Failure");

                            }
                        });

            }
        });

        calendar = Calendar.getInstance();

        Bundle bundle = getIntent().getExtras();
        if (bundle == null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            dateText.setText(dateFormat.format(calendar.getTime()));
        } else

        {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Log.d("error", "in create date text" + bundle.getString("date"));
            calendar.set(Calendar.YEAR, bundle.getInt("year"));
            calendar.set(Calendar.MONTH, bundle.getInt("month"));
            calendar.set(Calendar.DAY_OF_MONTH, bundle.getInt("day"));
            dateText.setText(dateFormat.format(calendar.getTime()));
        }


        RequestParams params = new RequestParams();
        params.put("company_no", company_no);
        Log.d("error", dateText.getText().toString());
        params.put("date", dateText.getText());
        client.post(getString(R.string.URL) + "/schedule/select",
                params, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers,
                                          JSONObject response) {
                        super.onSuccess(statusCode, headers, response);
                        Log.d("error", response.toString());
                        try {
                            slot_9_button.setChecked(response.getBoolean("slot_09"));
                            slot_10_button.setChecked(response.getBoolean("slot_10"));
                            slot_11_button.setChecked(response.getBoolean("slot_11"));
                            slot_12_button.setChecked(response.getBoolean("slot_12"));
                            slot_13_button.setChecked(response.getBoolean("slot_13"));
                            slot_14_button.setChecked(response.getBoolean("slot_14"));
                            slot_15_button.setChecked(response.getBoolean("slot_15"));
                            slot_16_button.setChecked(response.getBoolean("slot_16"));
                            slot_17_button.setChecked(response.getBoolean("slot_17"));
                            slot_18_button.setChecked(response.getBoolean("slot_18"));
                            slot_19_button.setChecked(response.getBoolean("slot_19"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers,
                                          String responseString, Throwable throwable) {
                        super.onFailure(statusCode, headers, responseString, throwable);
                        Log.d("error", "get onFailure");
                    }
                });
    }

    private DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, monthOfYear);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            Log.d("error", "year : " + year + " month : " + monthOfYear + " day : " + dayOfMonth);
            updateDateText();
        }
    };

    private void updateDateText() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        dateText.setText(dateFormat.format(calendar.getTime()));
        Intent refresh = new Intent(this, ScheduleActivity.class);

        Bundle bundle = new Bundle();
        bundle.putInt("year", calendar.get(Calendar.YEAR));
        bundle.putInt("month", calendar.get(Calendar.MONTH));
        bundle.putInt("day", calendar.get(Calendar.DAY_OF_MONTH));
        Log.d("error", "in update date text" + bundle.getString("date"));
        refresh.putExtras(bundle);
        startActivity(refresh);
        overridePendingTransition(0, 0);

        this.finish(); //

        Log.d("error", "finsh??");
        //onCreate(null);
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
                // NavUtils.navigateUpFromSameTask(this);
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onRestart() {
        checkRegid.checkRegid(getString(R.string.URL), cookieStore.getCookies().get(0).getValue(), ScheduleActivity.this);
        super.onRestart();
    }


}
