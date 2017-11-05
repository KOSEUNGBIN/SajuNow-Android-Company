package com.test.landvibe.company.login;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mobsandgeeks.saripaar.QuickRule;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Checked;
import com.mobsandgeeks.saripaar.annotation.Email;
import com.mobsandgeeks.saripaar.annotation.Length;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.mobsandgeeks.saripaar.annotation.Order;
import com.mobsandgeeks.saripaar.annotation.Password;
import com.test.landvibe.company.R;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpResponse;


public class SignUpActivity extends AppCompatActivity implements View.OnClickListener, Validator.ValidationListener {

    @Order(1)
    @NotEmpty(message = "이메일을 입력해주세요.")
    @Email(message = "유효하지 않은 이메일 형식입니다.")
    private EditText signup_email_et;
    @Order(2)
    @NotEmpty(message = "패스워드를 입력해주세요.")
    @Length(max = 12)
    @Password(min = 6, scheme = Password.Scheme.ALPHA_NUMERIC_SYMBOLS, message = "유효하지 않은 패스워드입니다.\n 6 ~ 12 이내의 숫자+문자+특수문자로 작성해 주세요.")
    private EditText signup_pw_et;
    @Order(3)
    @NotEmpty(message = "이름을 입력해주세요.")
    private EditText signup_nickname_et;
    @Order(3)
    @NotEmpty(message = "약력을 입력해주세요.")
    @Length(min = 10, max = 400, message = "글자수는 10 ~ 400 이내입니다.")
    private EditText signup_experience_et;
    @Order(4)
    @NotEmpty(message = "인삿말을 입력해주세요.")
    @Length(min = 10, max = 400, message = "글자수는 10 ~ 400 이내입니다.")
    private EditText signup_greeting_et;
    @Order(5)
    @NotEmpty(message = "자기소개를 입력해주세요.")
    @Length(min = 10, max = 400, message = "글자수는 10 ~ 400 이내입니다.")
    private EditText signup_introduce_et;
    @Order(6)
    @NotEmpty(message = "전문분야을 입력해주세요.")
    @Length(min = 10, max = 400, message = "글자수는 10 ~ 400 이내입니다.")
    private EditText signup_professional_category_et;
    private CheckBox signup_simple_chat_cb;

    // 회원가입에서 "우편번호","상세주소" edittext에 대한 선언
    @Order(7)
    @NotEmpty(message = "상세주소를 입력해주세요.")
    private EditText signup_home_address_ed;
    @Order(8)
    @NotEmpty(message = "우편번호를 입력해주세요.")
    @Length(min = 5, max = 7, message = "우편번호는 5 ~ 7 이내입니다.")
    private EditText signup_post_address_ed;
    @Order(9)
    private CheckBox signup_category_1_cb;
    @Order(10)
    private CheckBox signup_category_2_cb;
    @Order(11)
    private CheckBox signup_category_3_cb;
    @Order(13)
    @Checked(message = "이용약관을 선택해 주세요.")
    private CheckBox clause_one_agree_cb;
    @Checked(message = "개인정보 취급을 선택해 주세요.")
    @Order(14)
    private CheckBox clause_two_agree_cb;

    // 가입 - 취소 버튼
    private Button signup_ok_btn;
    private Button signup_cancle_btn;

    private AsyncHttpClient client = new AsyncHttpClient();

//    private int agreeone = 0;
//    private int agreetwo = 0;

    // 주소 검색 api key
    private String Key = "{우체국 API KEY}";

    // 해당 이메일을 서버에서 비교하기 전에, 앱에서 비교하기 위한 변수
    private boolean isEmailComplex = true;
    private boolean isProblemEmail;

    // 중복확인 버튼
    private Button signup_email_complex_btn;

    // 카테고리를 선택한 개수를 센다.
    private int categoryConut = 0;

    // 다이얼로그 안에서의 주소 키워드 검색에 의한 주소 리스트를 뿌려준다.
    private ListView dialog_addressList;
    // 회원가입에서 "우편번호검색" 버튼에 대한 선언
    private Button signup_address_search_btn;

    // 이용약관 이미지버튼 뷰
    private ImageButton clause_one_imgbtn;
    private ImageButton clause_two_imgbtn;

    // 주소 어댑터
    private ArrayAdapter<String> addressListAdapter;
    // 사용자가 입력한 주소
    private String putAddress;
    // 우체국으로부터 반환 받은 우편주소 리스트
    private ArrayList<String> addressSearchResultArr = new ArrayList<String>();

    // Time - 상담 가능 시간을 체크하는 TextView
    private TextView signupTime_1_text;
    private TextView signupTime_2_text;
    private TextView signupTime_3_text;
    private TextView signupTime_4_text;

    // TimePicker 변수 선언
    private TimePickerDialog timePickerDialog;
    // TimePickerDialog에서의 시간 내용을 변경해서 저장하기 위한 변수
    private String reservationTime;

    // Validation 선언
    private Validator validator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        client.addHeader("Cookie", "PASSWORD");

        // 회원가입에서의 모든 EditText에 대한 선언
        signup_email_et = (EditText) findViewById(R.id.signup_email_et);
        signup_pw_et = (EditText) findViewById(R.id.signup_pw_et);
        signup_nickname_et = (EditText) findViewById(R.id.signup_nickname_et);
        signup_experience_et = (EditText) findViewById(R.id.signup_experience_et);
        signup_greeting_et = (EditText) findViewById(R.id.signup_greeting_et);
        signup_introduce_et = (EditText) findViewById(R.id.signup_introduce_et);
        signup_professional_category_et = (EditText) findViewById(R.id.signup_professional_category_et);

        // 간단 사주 체크박스에 대한 선언
        signup_simple_chat_cb = (CheckBox) findViewById(R.id.signup_simple_chat_cb);

        // 이용약관의 이미지 버튼에 대한 선언
        clause_one_imgbtn = (ImageButton) findViewById(R.id.clause_one_imgbtn);
        clause_two_imgbtn = (ImageButton) findViewById(R.id.clause_two_imgbtn);
        clause_one_imgbtn.setOnClickListener(this);
        clause_two_imgbtn.setOnClickListener(this);

        // 이용약관 체크박스에 대한 선언
        clause_one_agree_cb = (CheckBox) findViewById(R.id.clause_one_agree_cb);
        clause_two_agree_cb = (CheckBox) findViewById(R.id.clause_two_agree_cb);
        clause_one_agree_cb.setOnClickListener(this);
        clause_two_agree_cb.setOnClickListener(this);

        // 이메일 중복에 대한 선언
        signup_email_complex_btn = (Button) findViewById(R.id.signup_email_complex_btn);
        signup_email_complex_btn.setOnClickListener(this);

        // 가입 - 취소 에 대한 선언
        signup_ok_btn = (Button) findViewById(R.id.signup_ok_btn);
        signup_cancle_btn = (Button) findViewById(R.id.signup_cancle_btn);
        signup_ok_btn.setOnClickListener(this);
        signup_cancle_btn.setOnClickListener(this);

        // 회원가입에서 "우편번호검색" 버튼에 대한 선언
        signup_address_search_btn = (Button) findViewById(R.id.signup_address_search_btn);
        signup_address_search_btn.setOnClickListener(this);

        // 회원가입 화면에서 우편번호 , 상세주소가 담겨질 editText 이다.
        signup_home_address_ed = (EditText) findViewById(R.id.signup_home_address_ed);
        signup_post_address_ed = (EditText) findViewById(R.id.signup_post_address_ed);


        signupTime_1_text = (TextView) findViewById(R.id.signup_time_1_tx);
        signupTime_2_text = (TextView) findViewById(R.id.signup_time_2_tx);
        signupTime_3_text = (TextView) findViewById(R.id.signup_time_3_tx);
        signupTime_4_text = (TextView) findViewById(R.id.signup_time_4_tx);
        signupTime_1_text.setOnClickListener(this);
        signupTime_2_text.setOnClickListener(this);
        signupTime_3_text.setOnClickListener(this);
        signupTime_4_text.setOnClickListener(this);


        signup_category_1_cb = (CheckBox) findViewById(R.id.signup_category_1_cb);
        signup_category_2_cb = (CheckBox) findViewById(R.id.signup_category_2_cb);
        signup_category_3_cb = (CheckBox) findViewById(R.id.signup_category_3_cb);
        signup_category_1_cb.setOnClickListener(this);
        signup_category_2_cb.setOnClickListener(this);
        signup_category_3_cb.setOnClickListener(this);


        signup_email_et.requestFocus();// 이메일 edittext에 제일 먼저 포커스를 준다

        // validation 사용
        validator = new Validator(this);
        validator.setValidationListener(this);
        validator.put(signup_pw_et, new AllowEvenNumbersRule(0));
        validator.put(signup_category_3_cb, new AllowEvenNumbersRule(0));
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.signup_ok_btn: {


                if (isEmailComplex) {
                    Toast.makeText(this, "이메일 중복확인을 해주세요.", Toast.LENGTH_SHORT).show();
                    break;
                }
                validator.validate(true);
                break;
            }
            case R.id.signup_cancle_btn: {
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
                break;
            }
            case R.id.clause_one_imgbtn: {
                Intent intent = new Intent(SignUpActivity.this, SignUpClauseOneActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
                break;
            }
            case R.id.clause_two_imgbtn: {
                Intent intent = new Intent(SignUpActivity.this, SignUpClauseTwoActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
                break;
            }
            // timePicker
            case R.id.signup_time_1_tx: {
                signUpTimePicker(signupTime_1_text);
                break;
            }
            case R.id.signup_time_2_tx: {
                signUpTimePicker(signupTime_2_text);
                break;
            }

            case R.id.signup_time_3_tx: {
                signUpTimePicker(signupTime_3_text);
                break;
            }
            case R.id.signup_time_4_tx: {
                signUpTimePicker(signupTime_4_text);
                break;
            }

            // category
            case R.id.signup_category_1_cb: {
                checkCategoryCheckBox(signup_category_1_cb);
                break;
            }

            case R.id.signup_category_2_cb: {
                checkCategoryCheckBox(signup_category_2_cb);
                break;
            }
            case R.id.signup_category_3_cb: {
                checkCategoryCheckBox(signup_category_3_cb);
                break;
            }
            // 이메일 중복
            case R.id.signup_email_complex_btn: {
                isProblemEmail = false;

                validator.validateTill(signup_email_et, false);

                if(!isProblemEmail) {
                String companyEmail_EditText = signup_email_et.getText().toString().trim();
                RequestParams params = new RequestParams();
                params.put("email", companyEmail_EditText);
                client.post(getString(R.string.URL) + "/company/complex", params, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        super.onSuccess(statusCode, headers, response);
                        try {
                            if (response.getBoolean("result")) {
                                Log.d("error", "SignUp ID Not Complexed");
                                signup_email_complex_btn.setText("VALID Email");
                                signup_email_complex_btn.setBackgroundColor(0xff000000);
                                isEmailComplex = false;

                            } else {
                                Log.d("error", "SignUp ID Complexed");
                                signup_email_complex_btn.setText("INVALID Email");
                                signup_email_complex_btn.setBackgroundColor(0xffD8D8D8);
                                isEmailComplex = true;

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers,
                                          String responseString, Throwable throwable) {
                        super.onFailure(statusCode, headers,
                                responseString, throwable);
                    }
                });
                }
                break;
            }

            // 주소 찾기
            case R.id.signup_address_search_btn: {

                Button dialog_signup_address_number_btn;
                final TextView dialog_signup_address_number_tx;
                final EditText dialog_signup_address_number_ed;
                final Button dialog_signup_address_number_search_btn;

                Button dialog_signup_address_street_btn;
                final TextView dialog_signup_address_street_tx;
                final EditText dialog_signup_address_street_ed;
                final Button dialog_signup_address_street_search_btn;

                final Dialog dialog = new Dialog(SignUpActivity.this);
                dialog.setTitle("주소 입력");
                dialog.setContentView(R.layout.activity_sign_up_address);

                // 지번우편번호 찾기 - > 다이얼로그에서 처리
                dialog_signup_address_number_btn = (Button) dialog.findViewById(R.id.signup_address_number_btn);
                dialog_signup_address_number_tx = (TextView) dialog.findViewById(R.id.signup_address_number_tx);
                dialog_signup_address_number_ed = (EditText) dialog.findViewById(R.id.signup_address_number_ed);
                dialog_signup_address_number_search_btn = (Button) dialog.findViewById(R.id.signup_address_number_search_btn);

                // 도로명우편번호 ->다이얼로그에서 처리
                dialog_signup_address_street_btn = (Button) dialog.findViewById(R.id.signup_address_street_btn);
                dialog_signup_address_street_tx = (TextView) dialog.findViewById(R.id.signup_address_street_tx);
                dialog_signup_address_street_ed = (EditText) dialog.findViewById(R.id.signup_address_street_ed);
                dialog_signup_address_street_search_btn = (Button) dialog.findViewById(R.id.signup_address_street_search_btn);

                // 다이얼로그 안에서 검색 버튼을 눌렀을 시 해당 주소를 찾아 리스트형태로 뿌려준다.
                dialog_addressList = (ListView) dialog.findViewById(R.id.addressList);


                //다이얼로그 안에서 지번 우편번호 버튼을 눌렀을 시 도로명 우편번호에 대한 view 들은 모두 GONE하고 지번 우편번호에 대한 view들만 보여준다.
                dialog_signup_address_number_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog_signup_address_street_tx.setVisibility(View.GONE);
                        dialog_signup_address_street_ed.setVisibility(View.GONE);
                        dialog_signup_address_street_search_btn.setVisibility(View.GONE);

                        dialog_signup_address_number_tx.setVisibility(View.VISIBLE);
                        dialog_signup_address_number_ed.setVisibility(View.VISIBLE);
                        dialog_signup_address_number_search_btn.setVisibility(View.VISIBLE);

                        dialog_signup_address_number_search_btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                getAddress(dialog_signup_address_number_ed.getText().toString(), 1);
                            }
                        });


                    }
                });

                //다이얼로그 안에서 도로면 우편번호 버튼을 눌렀을 시 지번 우편번호에 대한 view 들은 모두 GONE하고 도로명 우편번호에 대한 view들만 보여준다.
                dialog_signup_address_street_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        dialog_signup_address_number_tx.setVisibility(View.GONE);
                        dialog_signup_address_number_ed.setVisibility(View.GONE);
                        dialog_signup_address_number_search_btn.setVisibility(View.GONE);

                        dialog_signup_address_street_tx.setVisibility(View.VISIBLE);
                        dialog_signup_address_street_ed.setVisibility(View.VISIBLE);
                        dialog_signup_address_street_search_btn.setVisibility(View.VISIBLE);

                        dialog_signup_address_street_search_btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                getAddress(dialog_signup_address_street_ed.getText().toString(), 2);
                            }
                        });
                    }
                });

                // 다이얼로그에 리스트중 하나를 선택하면 해당 내용을 가공하여 , 다이얼로그를 끄고 회원가입에 상세주소와 우편번호 edittext에 해당 가공한 내용을 넣는다.
                dialog_addressList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String address = (String) parent.getItemAtPosition(position);


                        String[] address_detail = address.split("\n");
                        String[] address_detail_post = address_detail[1].split(" ");


                        signup_home_address_ed.setText(address_detail[0]);
                        signup_post_address_ed.setText(address_detail_post[1]);


                        dialog.dismiss();


                    }
                });
                dialog.show();
                break;
            }

        }
    }

    private void getAddress(String kAddress, int a) {
        putAddress = kAddress;
        if (a == 1)
            new GetAddressDataTask_1().execute();
        else if (a == 2)
            new GetAddressDataTask_2().execute();
        else
            ;
    }

    // 주소 찾기를 위한 클래스 -- 구주소
    private class GetAddressDataTask_1 extends AsyncTask<String, Void, HttpResponse> {
        @Override
        protected HttpResponse doInBackground(String... urls) {
            HttpResponse response = null;
            final String apiurl = "http://biz.epost.go.kr/KpostPortal/openapi";

            ArrayList<String> addressInfo = new ArrayList<String>();

            HttpURLConnection conn = null;
            try {
                StringBuffer sb = new StringBuffer(3);
                sb.append(apiurl);
                sb.append("?regkey=" + Key + "&target=post&query=");
                sb.append(URLEncoder.encode(putAddress, "EUC-KR"));
                String query = sb.toString();

                URL url = new URL(query);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("accept-language", "ko");

                DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                byte[] bytes = new byte[4096];
                InputStream in = conn.getInputStream();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                while (true) {
                    int red = in.read(bytes);
                    if (red < 0)
                        break;
                    baos.write(bytes, 0, red);
                }
                String xmlData = baos.toString("utf-8");
                baos.close();
                in.close();
                conn.disconnect();

                Document doc = docBuilder.parse(new InputSource(new StringReader(xmlData)));
                Element el = (Element) doc.getElementsByTagName("itemlist").item(0);
                for (int i = 0; i < ((Node) el).getChildNodes().getLength(); i++) {
                    Node node = ((Node) el).getChildNodes().item(i);
                    if (!node.getNodeName().equals("item")) {
                        continue;
                    }
                    String address = node.getChildNodes().item(1).getFirstChild().getNodeValue();
                    String post = node.getChildNodes().item(3).getFirstChild().getNodeValue();
                    Log.w("jaeha", "address = " + address);
                    addressInfo.add(address + "\n우편번호: " + post.substring(0, 3) + "-" + post.substring(3));
                }

                addressSearchResultArr = addressInfo;
                publishProgress();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (conn != null)
                        conn.disconnect();
                } catch (Exception e) {
                }
            }

            return response;
        }


        @Override
        protected void onProgressUpdate(Void... values) {
            // TODO Auto-generated method stub
            super.onProgressUpdate(values);

            String[] addressStrArray = new String[addressSearchResultArr.size()];
            addressStrArray = addressSearchResultArr.toArray(addressStrArray);

            addressListAdapter = new ArrayAdapter<String>(SignUpActivity.this, android.R.layout.simple_list_item_1, addressStrArray);
            dialog_addressList.setAdapter(addressListAdapter);


        }
    }

    // 주소 찾기를 위한 클래스 -- 신주소
    private class GetAddressDataTask_2 extends AsyncTask<String, Void, HttpResponse> {
        @Override
        protected HttpResponse doInBackground(String... urls) {
            HttpResponse response = null;
            final String apiurl = "http://biz.epost.go.kr/KpostPortal/openapi";

            ArrayList<String> addressInfo = new ArrayList<String>();

            HttpURLConnection conn = null;
            try {
                StringBuffer sb = new StringBuffer(3);
                sb.append(apiurl);
                sb.append("?regkey=" + Key + "&target=postNew&query=");
                sb.append(URLEncoder.encode(putAddress, "EUC-KR"));
                String query = sb.toString();

                URL url = new URL(query);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("accept-language", "ko");

                DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                byte[] bytes = new byte[4096];
                InputStream in = conn.getInputStream();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                while (true) {
                    int red = in.read(bytes);
                    if (red < 0)
                        break;
                    baos.write(bytes, 0, red);
                }
                String xmlData = baos.toString("utf-8");
                baos.close();
                in.close();
                conn.disconnect();

                Document doc = docBuilder.parse(new InputSource(new StringReader(xmlData)));
                Element el = (Element) doc.getElementsByTagName("itemlist").item(0);
                for (int i = 0; i < ((Node) el).getChildNodes().getLength(); i++) {
                    Node node = ((Node) el).getChildNodes().item(i);
                    if (!node.getNodeName().equals("item")) {
                        continue;
                    }
                    String address = node.getChildNodes().item(3).getFirstChild().getNodeValue();
                    String post = node.getChildNodes().item(1).getFirstChild().getNodeValue();
                    Log.w("jaeha", "address = " + address);
                    addressInfo.add(address + "\n우편번호: " + post);
                }

                addressSearchResultArr = addressInfo;
                publishProgress();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (conn != null)
                        conn.disconnect();
                } catch (Exception e) {
                }
            }

            return response;
        }


        @Override
        protected void onProgressUpdate(Void... values) {
            // TODO Auto-generated method stub
            super.onProgressUpdate(values);

            String[] addressStrArray = new String[addressSearchResultArr.size()];
            addressStrArray = addressSearchResultArr.toArray(addressStrArray);

            addressListAdapter = new ArrayAdapter<String>(SignUpActivity.this, android.R.layout.simple_list_item_1, addressStrArray);
            dialog_addressList.setAdapter(addressListAdapter);


        }
    }

    // 카테고리 체크시 호출되는 함수
    private void checkCategoryCheckBox(CheckBox cb) {
        if (categoryConut >= 2) {
            if (cb.isChecked()) {
                cb.setChecked(false);
                Toast.makeText(SignUpActivity.this,
                        "카테고리를 2개 이상 체크할 수 없습니다. ",
                        Toast.LENGTH_LONG).show();
            } else
                categoryConut--;

        } else {
            if (cb.isChecked())
                categoryConut++;
            else
                categoryConut--;
        }
    }

    // TimePicker를 터치시 호출되는 함수
    private void signUpTimePicker(final TextView timeView) {
        TimePickerDialog.OnTimeSetListener callBack = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                reservationTime = leftPad(hourOfDay) + ":" + leftPad(minute);
                timeView.setText(reservationTime);
            }
        };
        timePickerDialog = new TimePickerDialog(SignUpActivity.this, callBack, 8, 00, true);
        timePickerDialog.show();

    }

    // signUpTimePicker() 함수에서 호출되는 함수
    private String leftPad(int num) {
        String val = null;
        if (num < 10) {
            val = "0" + num;
        } else {
            val = "" + num;
        }
        return val;
    }

    // Validation 성공시 호출되는 함수
    @Override
    public void onValidationSucceeded() {
        RequestParams param = new RequestParams();

        String companySignUpEmail_EditText = signup_email_et.getText().toString().trim();
        String companySignUpPassWord_EditText = signup_pw_et.getText().toString().trim();
        String companySignUpName_EditText = signup_nickname_et.getText().toString().trim();
        String companySignUpHomeAddress_EditText = signup_home_address_ed.getText().toString().trim();
        String companySignUpPostAddress_EditText = signup_post_address_ed.getText().toString().trim();
        String companySignUpExperience_EditText = signup_experience_et.getText().toString().trim();
        String companySignUpGreeting_EditText = signup_greeting_et.getText().toString().trim();
        String companySignUpIntroduce_EditText = signup_introduce_et.getText().toString().trim();
        String companySignUpProfessionalCategory_EditText = signup_professional_category_et.getText().toString().trim();

        Log.d("gg", "Successed");
        String categoryCheckbox = "";
        categoryCheckbox += signup_category_1_cb.isChecked() + "?";
        categoryCheckbox += signup_category_2_cb.isChecked() + "?";
        categoryCheckbox += signup_category_3_cb.isChecked();


        Log.d("email ", companySignUpEmail_EditText);
        Log.d("password ", companySignUpPassWord_EditText);
        Log.d("nick_name ", companySignUpName_EditText);
        Log.d("address ", companySignUpHomeAddress_EditText);
        Log.d("experience ", companySignUpExperience_EditText);
        Log.d("greeting ", companySignUpGreeting_EditText);
        Log.d("introduce ", companySignUpIntroduce_EditText);
        Log.d("company_reg_id ", "LOGOUT");
        Log.d("possible_time ", "평일: " + signupTime_1_text.getText().toString() + " ~ " + signupTime_2_text.getText().toString() + ", 주말: " + signupTime_3_text.getText().toString() + " ~ " + signupTime_4_text.getText().toString());
        Log.d("categoryCheckbox ", "" + categoryCheckbox);
        Log.d("category_detail ", "" + companySignUpProfessionalCategory_EditText);
        Log.d("history_simple ", "" + signup_simple_chat_cb.isChecked());


        param.put("email", companySignUpEmail_EditText);
        param.put("password", companySignUpPassWord_EditText);
        param.put("nick_name", companySignUpName_EditText);
        param.put("address", companySignUpHomeAddress_EditText);
        param.put("experience", companySignUpExperience_EditText);
        param.put("greeting", companySignUpGreeting_EditText);
        param.put("introduce", companySignUpIntroduce_EditText);
        param.put("company_reg_id", "LOGOUT");
        param.put("possible_time", "평일: " + signupTime_1_text.getText().toString() + " ~ " + signupTime_2_text.getText().toString() + ", 주말: " + signupTime_3_text.getText().toString() + " ~ " + signupTime_4_text.getText().toString());
        param.put("category_list", categoryCheckbox);

        // ------  새로 추가할 칼럼 ----  (전문 분야 , 간단 사주 여부)
        param.put("category_detail", companySignUpProfessionalCategory_EditText);
        param.put("history_simple", signup_simple_chat_cb.isChecked());

        client.post(getString(R.string.URL) + "/company/insert", param, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    if (response.getBoolean("result")) {
                        Log.d("signup", "회원가입 Success");

                        // 회원가입이 성공시 SharedPreference에 알림음 설정을 True로 설정
                        SharedPreferences alarmSwitch = getSharedPreferences("alarmSwitch", MODE_PRIVATE);
                        SharedPreferences.Editor editor = alarmSwitch.edit();

                        editor.putBoolean("alarmSwitchCondition", true);
                        editor.commit();
                        Toast.makeText(SignUpActivity.this,
                                "로그인해주세요.", Toast.LENGTH_LONG)
                                .show();
//                            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
//                            startActivity(intent);
                        finish();
                    } else {
                        Log.d("signup", "회원가입 Faulure");
                        isEmailComplex = true;
                        signup_email_et.requestFocus();
                        signup_email_complex_btn.setBackgroundColor(0xffD8D8D8);
                        signup_email_complex_btn.setText("이메일 중복확인");
                        Toast.makeText(SignUpActivity.this,
                                "Email 중복 확인을 다시 해주세요.", Toast.LENGTH_LONG)
                                .show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d("signup", "회원가입 Faulure");
                Toast.makeText(SignUpActivity.this,
                        "회원가입 중 오류가 발생하였습니다.", Toast.LENGTH_LONG)
                        .show();
            }
        });

    }

    // Validation 실패시 호출되는 함수
    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        for (ValidationError error : errors) {
            View view = error.getView();
            String message = error.getCollatedErrorMessage(this);

            // Display error messages ;)
            if (view instanceof EditText) {
                ((EditText) view).setError(message);
                if(view.getId() == R.id.signup_email_et)
                {
                    signup_email_complex_btn.setText("INVALID Email");
                    isEmailComplex = true;
                    isProblemEmail = true;
                }
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
                case R.id.signup_pw_et: {
                    isCheck = 1;
                    return !signup_pw_et.getText().toString().trim().matches(".*\\s.*");
                }
                case R.id.signup_category_1_cb:
                case R.id.signup_category_2_cb:
                case R.id.signup_category_3_cb: {
                    isCheck = 2;
                    return categoryConut == 0 ? false : true;
                }
                default:
                    return true;
            }
        }

        @Override
        public String getMessage(Context context) {

            switch (isCheck) {
                case 1:
                    return "패스워드에 빈칸이 존재합니다.";
                case 2:
                    return "적어도 하나 이상의 카테고리를 선택해야 합니다.";
                default:
                    return "Validation Error";
            }

        }
//        @Override
//        public boolean isValid(EditText editText) {
//
//        }
    }

}
