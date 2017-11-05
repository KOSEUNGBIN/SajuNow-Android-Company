package com.test.landvibe.company.profit;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.formatter.XAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.mobsandgeeks.saripaar.QuickRule;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.mobsandgeeks.saripaar.annotation.Order;
import com.test.landvibe.company.R;
import com.test.landvibe.company.common.CheckRegId;
import com.test.landvibe.company.util.DeEncrypter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLDecoder;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import cz.msebera.android.httpclient.Header;

/**
 * Created by 고승빈 on 2016-06-21.
 */
public class WithdrawActivity extends AppCompatActivity implements View.OnClickListener, Validator.ValidationListener {
    private Toolbar toolbar;

    // company_no 선언, 쿠키 내용을 담을 변수 선언
    private int company_no;
    private String[] result;

    // company의 margin , 출금 가능한 금액
    private long margin;
    private long withdraw_possibillity_pay;

    // 쿠키
    private PersistentCookieStore cookieStore;
    // httpclient 선언
    private AsyncHttpClient client = new AsyncHttpClient();

    private CheckRegId checkRegid;

    private TextView withdraw_detail_tx;
    private Button withdraw_btn;

    // dialog 내의 view
    private android.support.v7.widget.AppCompatTextView withdraw_textview;

    @Order(1)
    @NotEmpty(message = "출금할 금액을 적어주세요.")
    private android.support.v7.widget.AppCompatEditText withdraw_edittext;
    private android.support.v7.widget.AppCompatTextView withdraw_textview_result;
    private Button withdraw_btn_cancel;
    private Button withdraw_btn_confirm;

    private Dialog dialog;

    // Validation 선언
    private Validator validator;

    private List<BarChart> barChart;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_withdraw);

        toolbar = (Toolbar) findViewById(R.id.toolbar_withdraw);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ////////////////////////////쿠키 load, 복호화//////////////////////////////////////////////////////////////////////////

        cookieStore = new PersistentCookieStore(this);
        DeEncrypter deEncrypter = new DeEncrypter();
        Log.d("error", "token : " + URLDecoder.decode(cookieStore.getCookies().get(0).getValue()));
        String token = deEncrypter.decrypt(URLDecoder.decode(cookieStore.getCookies().get(0).getValue()));
        Log.d("error", "token length : " + token.length());
        result = token.split("\\?");
        company_no = Integer.parseInt(result[0]);

        client.addHeader("Cookie", cookieStore.getCookies().get(0).getValue());

        checkRegid = new CheckRegId();
        checkRegid.checkRegid(getString(R.string.URL), cookieStore.getCookies().get(0).getValue(), WithdrawActivity.this);
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        withdraw_detail_tx = (TextView) findViewById(R.id.withdraw_detail_tx);
        withdraw_btn = (Button) findViewById(R.id.withdraw_btn);
        withdraw_btn.setOnClickListener(this);
        withdraw_btn.setClickable(false);

        profitMonthCompany();

        dialog = new Dialog(WithdrawActivity.this);
        dialog.setTitle("출금 요청서");
        dialog.setContentView(R.layout.withdraw_dialog);

        // dialog 내의 view
        withdraw_textview = (android.support.v7.widget.AppCompatTextView) dialog.findViewById(R.id.withdraw_textview);
        withdraw_edittext = (android.support.v7.widget.AppCompatEditText) dialog.findViewById(R.id.withdraw_edittext);
        withdraw_textview_result = (android.support.v7.widget.AppCompatTextView) dialog.findViewById(R.id.withdraw_textview_result);
        withdraw_btn_cancel = (Button) dialog.findViewById(R.id.withdraw_btn_cancel);
        withdraw_btn_confirm = (Button) dialog.findViewById(R.id.withdraw_btn_confirm);
        withdraw_btn_cancel.setOnClickListener(this);
        withdraw_btn_confirm.setOnClickListener(this);


        /*withdraw_edittext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.d("num111", "s : " + s + " / start : " + start + " / count : " + count + " / after : " + after);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().isEmpty()) {
                    double company_pay = Math.ceil((Integer.parseInt(s.toString())) * (1 - (margin * 0.01)));
                    double admin_pay = Math.floor((Integer.parseInt(s.toString())) * (margin * 0.01));
                    withdraw_textview_result.setText("출금액 : " + s + " = 입금되는 금액(" + company_pay + ") + 수수료 (" + admin_pay + ")");
                    Log.d("num2222", "s : " + s + " / start : " + start + " / before : " + before + " / count : " + count);
                } else {
                    withdraw_textview_result.setText("출금액 : " + 0 + " = 입금되는 금액(" + 0 + ") + 수수료 (" + 0 + ")");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });*/

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                withdraw_edittext.setText("");
            }
        });

        // validation 사용
        validator = new Validator(this);
        validator.setValidationListener(this);
        validator.put(withdraw_edittext, new AllowEvenNumbersRule(0));


        // barchart 선언
        barChart = new ArrayList<BarChart>();
        barChart.add((BarChart) findViewById(R.id.barChart_payment));
        barChart.add((BarChart) findViewById(R.id.barChart_exchange));
        barChart.add((BarChart) findViewById(R.id.barChart_count));

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onRestart() {
        checkRegid.checkRegid(getString(R.string.URL), cookieStore.getCookies().get(0).getValue(), WithdrawActivity.this);
        profitMonthCompany();
        super.onRestart();
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.withdraw_btn: {

                dialog.show();
                break;
            }
            case R.id.withdraw_btn_cancel: {
                dialog.dismiss();
                break;
            }
            case R.id.withdraw_btn_confirm: {
                validator.validate(false);
                break;
            }
        }
    }

    @Override
    public void onValidationSucceeded() {
        RequestParams exchange_insert_param = new RequestParams();
        exchange_insert_param.put("company_no", company_no);
        exchange_insert_param.put("exchange_amount", withdraw_edittext.getText());
        client.post(getString(R.string.URL) + "/profit/exchange/insert", exchange_insert_param, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                withdraw_edittext.setText("");
                dialog.dismiss();

                withdraw_btn.setClickable(false);
                profitMonthCompany();

//                Toast.makeText(WithdrawActivity.this,
//                        "Withdraw Insert Success", Toast.LENGTH_LONG)
//                        .show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(WithdrawActivity.this,
                        "오류가 발생하였습니다.\n" +
                                "다시 시도해주세요.",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        for (ValidationError error : errors) {
            View view = error.getView();
            String message = error.getCollatedErrorMessage(this);

            // Display error messages ;)
            if (view instanceof EditText) {
                ((EditText) view).setError(message);
            } else {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        }
    }

    // 사용자 지정 Validation 클래스
    public class AllowEvenNumbersRule extends QuickRule<View> {

        private int isCheck;

        // Override this constructor ONLY if you want sequencing.
        public AllowEvenNumbersRule(int sequence) {
            super(sequence);
        }

        @Override
        public boolean isValid(View view) {
            switch (view.getId()) {
                case R.id.withdraw_edittext: {
                    isCheck = 1;
                    return (withdraw_edittext.getText().toString().isEmpty() || (Integer.parseInt(withdraw_edittext.getText().toString()) <= withdraw_possibillity_pay));
                }
                default:
                    return false;
            }
        }

        @Override
        public String getMessage(Context context) {

            switch (isCheck) {
                case 1:
                    return "출금 가능 금액보다 적게 적어주세요.";
                default:
                    return "Validation Error";
            }

        }
    }

    private void profitMonthCompany() {
        RequestParams params = new RequestParams();

        client.post(getString(R.string.URL) + "/profit/month/company/" + company_no, params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);

                withdraw_btn.setClickable(true);

                try {
                    long payment_all = response.getJSONObject("result").getLong("payment_all");
                    long exchange_all = response.getJSONObject("result").getLong("exchange_all");
                    long waiting_amount = response.getJSONObject("result").getLong("waiting_amount");
                    withdraw_possibillity_pay = payment_all - exchange_all - waiting_amount;

                    margin = response.getJSONObject("result").getJSONObject("company").getLong("margin");
                    withdraw_detail_tx.setText(" 누적 매출 : " + payment_all + " 원" + "\n 누적 출금 : " + exchange_all + " 원" + "\n 출금 요청 중  : " + waiting_amount + " 원" + "\n 출금 가능 : " + withdraw_possibillity_pay + " 원");
                    withdraw_textview.setText("출금 가능 : " + withdraw_possibillity_pay + " 원");


                    //////////////////////////////////// barChart //////////////////////////////////////////

                    List<JSONArray> result_list = new ArrayList<>();
                    List<ArrayList<BarEntry>> data_list = new ArrayList<>();
                    ArrayList<String> labels = new ArrayList<>();
                    List<BarDataSet> barDataSet = new ArrayList<>();
                    List<BarData> barData = new ArrayList<>();
                    List<XAxis> xAxis = new ArrayList<>();
                    List<YAxis> yAxis = new ArrayList<>();

                    for (int i = -5; i < 1; i++) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.add(Calendar.MONTH, i);
                        labels.add(new SimpleDateFormat("yy-MM").format(calendar.getTime()));
                    }

                    for (int k = 0; k < 3; k++) {

                        result_list.add(response.getJSONObject("result").getJSONArray(getResources().getStringArray(R.array.withdraw_result_month_List)[k]));
                        data_list.add(new ArrayList<BarEntry>());

                        for (int j = 0; j < labels.size(); j++) {
                            int i = 0;
                            for (; i < result_list.get(k).length(); i++) {
                                if (labels.get(j).equals(result_list.get(k).getJSONObject(i).getString(getResources().getStringArray(R.array.withdraw_date_month_List)[k]).substring(2, 7))) {
                                    data_list.get(k).add(new BarEntry((float) result_list.get(k).getJSONObject(i).getLong(getResources().getStringArray(R.array.withdraw_sum_month_List)[k]), j));
                                    break;
                                }
                            }
                            if (i == result_list.get(k).length())
                                data_list.get(k).add(new BarEntry(0f, j));
                        }

                        barDataSet.add(new BarDataSet(data_list.get(k), getResources().getStringArray(R.array.withdraw_month_name_List)[k]));
                        barDataSet.get(k).setColors(ColorTemplate.COLORFUL_COLORS);

                        barData.add(new BarData(labels, barDataSet.get(k)));
                        barData.get(k).setValueTextSize(10f);
                        barData.get(k).setValueFormatter(new MyValueFormatter(k));

                        xAxis.add(barChart.get(k).getXAxis());
                        xAxis.get(k).setPosition(XAxis.XAxisPosition.BOTTOM);
                        xAxis.get(k).setTextSize(10f);
                        xAxis.get(k).setTextColor(Color.RED);
                        xAxis.get(k).setDrawAxisLine(false);
                        xAxis.get(k).setDrawGridLines(true);
                        xAxis.get(k).setSpaceBetweenLabels(2);
                        xAxis.get(k).setLabelsToSkip(0);
                        xAxis.get(k).setAvoidFirstLastClipping(true);
                        xAxis.get(k).setAxisLineWidth(0.5f);
                        xAxis.get(k).setValueFormatter(new MyValueFormatter(k));

                        yAxis.add(barChart.get(k).getAxisRight());
                        yAxis.get(k).setDrawLabels(false); // no axis labels
                        yAxis.get(k).setDrawAxisLine(false); // no axis line
                        yAxis.get(k).setDrawGridLines(false); // no grid lines
                        yAxis.get(k).setDrawZeroLine(true); // draw a zero line

                        barChart.get(k).animateY(2500);
                        barChart.get(k).setDescription("원/월");
                        barChart.get(k).setData(barData.get(k));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                if (statusCode == 200) {
                    //  200  ==>  서버에 접근은 하였으나, 인증과정에서 Token 값이 바뀌었기 때문에 인증이 되지않는 경우 이다.
                    Toast.makeText(getApplicationContext(), "다른 디바이스에서 해당 계정으로 로그인 되었습니다.\n 본 디바이스는 로그아웃 됩니다.", Toast.LENGTH_LONG).show();
                    checkRegid.logout(WithdrawActivity.this);
                } else {
                    Toast.makeText(getApplicationContext(), "오류가 발생하였습니다.\n" +
                            "다시 시도해주세요." + responseString, Toast.LENGTH_LONG).show();
                    //  그외의 에러 코드가 들어온다.
                }
            }
        });
    }

    private class MyValueFormatter implements ValueFormatter, XAxisValueFormatter {

        private DecimalFormat mFormat;
        private int count;

        public MyValueFormatter(int count) {
            mFormat = new DecimalFormat("###,###,##0"); // use one decimal
            this.count = count;
        }

        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            // write your logic here
            if (count < 2)
                return mFormat.format(value) + " 원"; // e.g. append a dollar-sign
            else
                return mFormat.format(value) + " 회"; // e.g. append a dollar-sign
        }


        @Override
        public String getXValue(String original, int index, ViewPortHandler viewPortHandler) {
            return original.substring(0, 2) + "년 " + (Integer.parseInt(original.substring(3, 5)) / 10 == 1 ? original.substring(3, 5) : original.substring(4, 5) + "월"); // e.g. append a dollar-sig
        }
    }
}
