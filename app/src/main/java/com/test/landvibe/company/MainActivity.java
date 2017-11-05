package com.test.landvibe.company;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.test.landvibe.company.profit.ProfitActivity;
import com.test.landvibe.company.common.CheckRegId;
import com.test.landvibe.company.config.ConfigActivity;
import com.test.landvibe.company.main.MainAdapter;
import com.test.landvibe.company.main.MainInfoFragment;
import com.test.landvibe.company.main.MainReportFragment;
import com.test.landvibe.company.profit.ProfitActivity;
import com.test.landvibe.company.profit.WithdrawActivity;
import com.test.landvibe.company.util.DeEncrypter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URLDecoder;

import cz.msebera.android.httpclient.Header;
import jp.wasabeef.glide.transformations.CropCircleTransformation;

/**
 * Main에 대한 액티비티
 */
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {


    private BackPressCloseSystem backPressCloseSystem;
    private TextView profile_nickname_tv;

    private TextView user_email;
    private TextView history_count;

    private ImageView iv_profile;

    private Switch switchMain;

    private Toolbar toolbar;
    private DrawerLayout drawer;
    private Menu nav_Menu;

    private int chatCount;
    private boolean chatPossibilty;
    private int historyPossibilityCount;
    final int ON_TOUCHABLE = 1;
    final int OFF_TOUCHABLE = 0;
    final int OFF_UNTOUCHABLE = 2;
    private int switch_status;
    public Context mContext;

    private PersistentCookieStore cookieStore;
    private String[] result;
    private int company_no;

    private AsyncHttpClient client_user = new AsyncHttpClient();
    private AsyncHttpClient client_guest = new AsyncHttpClient();

    private CheckRegId checkRegid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        backPressCloseSystem = new BackPressCloseSystem(this);

        checkRegid = new CheckRegId();

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        MainAdapter adapter = new MainAdapter(getSupportFragmentManager());
        adapter.addFragment(new MainInfoFragment(), "정보");
        adapter.addFragment(new MainReportFragment(), "리뷰");
        viewPager.setAdapter(adapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);


        ////////////////////////////쿠키 load, 복호화//////////////////////////////////////////////////////////////////////////

        cookieStore = new PersistentCookieStore(this);
        DeEncrypter deEncrypter = new DeEncrypter();
        Log.d("error", "token : " + URLDecoder.decode(cookieStore.getCookies().get(0).getValue()));
        String token = deEncrypter.decrypt(URLDecoder.decode(cookieStore.getCookies().get(0).getValue()));
        Log.d("error", "token length : " + token.length());
        result = token.split("\\?");
        company_no = Integer.parseInt(result[0]);
        Log.d("error", "company_no : " + company_no);
        client_user.addHeader("Cookie", cookieStore.getCookies().get(0).getValue());
        client_guest.addHeader("Cookie", "PASSWORD");

        checkRegid.checkRegid(getString(R.string.URL), cookieStore.getCookies().get(0).getValue(), MainActivity.this);
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////





    }

    @Override
    protected void onStart() {
        super.onStart();

        profile_nickname_tv = (TextView) this.findViewById(R.id.profile_nickname_tv);

        toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        mContext = getApplicationContext();

        switchMain = (Switch) findViewById(R.id.switch_main);

        iv_profile = (ImageView) findViewById(R.id.imageButton);
        NavigationView navigationView = (NavigationView) findViewById(R.id.main_nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        nav_Menu = navigationView.getMenu();

        // 액션바에 email,상담중인 개수 표현
        navigationView.setNavigationItemSelectedListener(this);
        final View header = navigationView.getHeaderView(0);

        final RatingBar ratingBar = (RatingBar) findViewById(R.id.rating);
        final TextView textStar = (TextView) findViewById(R.id.textStar);

        RequestParams params = new RequestParams();

        client_user.post(getString(R.string.URL) + "/history/consert/count/company/" + company_no, params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {


                try {
                    user_email = (TextView) header.findViewById(R.id.user_email);
                    history_count = (TextView) header.findViewById(R.id.history_count);

                    user_email.setText("" + result[1] + "님");

                    JSONObject resultObject = response.getJSONObject("result");

                    // 상담 중인 개수
                    long count = resultObject.getLong("count");

                    if(count>=0)
                        history_count.setText("상담중( " + count + " )");

                    //  공지 사항 최신판 여부
                    long information_new = resultObject.getLong("information_new");

                    if(information_new > 0)
                        nav_Menu.findItem(R.id.sidebar_company_inform).setTitle("공지사항       New");


                } catch (JSONException e) {
                    e.printStackTrace();
                }

                super.onSuccess(statusCode, headers, response);

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d("error", "Sidebar Consertting View Failed");
                Toast.makeText(MainActivity.this, "상담중 표시 실패", Toast.LENGTH_LONG).show();
                if (statusCode == 200) {
                    //  200  ==>  서버에 접근은 하였으나, 인증과정에서 Token 값이 바뀌었기 때문에 인증이 되지않는 경우 이다.
                    Toast.makeText(getApplicationContext(), "다른 디바이스에서 해당 계정으로 로그인 되었습니다.\n 본 디바이스는 로그아웃 됩니다.", Toast.LENGTH_LONG).show();
                    checkRegid.logout( MainActivity.this);
                } else {
                    Toast.makeText(getApplicationContext(), "오류가 발생하였습니다.\n" +
                            "다시 시도해주세요." + responseString, Toast.LENGTH_LONG).show();
                    //  그외의 에러 코드가 들어온다.
                }
            }
        });


        GlideUrl glideUrl = new GlideUrl(getString(R.string.URL) + "/company/image/" + company_no, new LazyHeaders.Builder()
                .addHeader("Cookie", "PASSWORD")
                .build());

        Glide.with(this)
                .load(glideUrl)
                .bitmapTransform(new CropCircleTransformation(this))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(iv_profile);


        RequestParams param = new RequestParams();


        client_guest.get(getString(R.string.URL) + "/company/join/report/" + company_no, param, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    double rate = (Double) (Math.ceil(response.getDouble("score_average") * 2) / 2);
                    profile_nickname_tv.setText(response.getString("nick_name"));
                    textStar.setText("" + rate);
                    ratingBar.setRating((float) response.getDouble("score_average"));
                    ratingBar.setIsIndicator(true);

                    chatCount = response.getInt("chat_count");
                    historyPossibilityCount = response.getInt("history_possibility_count");
                    boolean chatSwitch = response.getBoolean("chat_switch");
                    boolean chatPossibility = response.getBoolean("chat_possibility");
                    boolean chatPossibilityResult = response.getBoolean("chat_possibility_result");

                    if(chatPossibility)
                    {
                        if(chatSwitch)
                        {
                            switchMain.setChecked(true);
                            switch_status = ON_TOUCHABLE;
                        }
                        else
                        {
                            switchMain.setChecked(false);
                            switch_status = OFF_TOUCHABLE;
                        }
                    }
                    else
                    {
                        switchMain.setChecked(false);
                        switchMain.setClickable(false);
                        switch_status = OFF_UNTOUCHABLE;
                    }

                  /*  if (chatPossibilty) {
                        switchMain.setChecked(true);
                        switch_status = ON_TOUCHABLE;

                    } else if (chatCount >= historyPossibilityCount) {
                        switchMain.setChecked(false);
                        switchMain.setClickable(false);
                        switch_status = OFF_UNTOUCHABLE;
                    } else {
                        switchMain.setChecked(false);
                        switch_status = OFF_TOUCHABLE;
                    }*/

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                if (statusCode == 200) {
                    //  200  ==>  서버에 접근은 하였으나, 인증과정에서 Token 값이 바뀌었기 때문에 인증이 되지않는 경우 이다.
                    Toast.makeText(getApplicationContext(), "다른 디바이스에서 해당 계정으로 로그인 되었습니다.\n 본 디바이스는 로그아웃 됩니다.", Toast.LENGTH_LONG).show();
                    checkRegid.logout(MainActivity.this);
                } else {
                    Toast.makeText(getApplicationContext(), "오류가 발생하였습니다.\n" +
                            "다시 시도해주세요." + responseString, Toast.LENGTH_LONG).show();
                    //  그외의 에러 코드가 들어온다.
                }
            }
        });


        // switch에 대한 클릭리스너에 대한 내용

        switchMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(switchMain.isChecked()){
                    updateSwitchStatus(ON_TOUCHABLE);
//                    Toast.makeText(MainActivity.this, "상담가능 설정이 적용되었습니다.", Toast.LENGTH_LONG).show();
                }else{
                    updateSwitchStatus(OFF_TOUCHABLE);
//                    Toast.makeText(MainActivity.this, "상담불가 설정이 적용되었습니다.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    protected void onRestart() {
        checkRegid.checkRegid(getString(R.string.URL), cookieStore.getCookies().get(0).getValue(), MainActivity.this);
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Glide.clear((ImageView) findViewById(R.id.imageButton));
    }

    @Override
    public void onBackPressed() {
        backPressCloseSystem.onBackPressed();
        if (drawer.isDrawerOpen(GravityCompat.START))
            drawer.closeDrawer(GravityCompat.START);
    }

    // 휴대폰의 옵션메뉴 버튼에 대한 menu(sidebar_bottom_common)의 처리 부분
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu items for use in the action bar
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.main, menu);
//        return super.onCreateOptionsMenu(menu);
//    }

    // 옵션메뉴에 대한 이벤트 처리
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.logout) {
//
//            PersistentCookieStore cookieStore = new PersistentCookieStore(getApplicationContext());
//            updateRegistId();
//            cookieStore.clear();
//            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//            startActivity(intent);
//
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }


    // 터치에 대한의 메뉴창 처리 부분
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    // 터치에 대한의 메뉴창의 이벤트 처리 부분
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        return super.onContextItemSelected(item);
    }

    // "메뉴" 버튼 클릭시 나올 NavigationView, DrawerLayout을 정의한 함수
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
//        profile_user_id_tv = (TextView) this.findViewById(R.id.user_id);
//        profile_user_id_tv.setText(result[1]);
//
//        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
//
//            @SuppressWarnings("StatementWithEmptyBody")
//            @Override
//            public boolean onNavigationItemSelected(MenuItem item) {
//                // Handle navigation view item clicks here.
        int id = item.getItemId();
        Log.d("error", "in on NavigationItemSelected");

        // Handle the camera action
        switch (id) {
            case R.id.sidebar_company_inform: {
                Intent myIntent = new Intent(getApplicationContext(), InformationActivity.class);
//                myIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                drawer.closeDrawer(GravityCompat.START);
                startActivity(myIntent);
                overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
                break;

            }
            case R.id.sidebar_company_companyEditProfile: {
                Intent myIntent = new Intent(getApplicationContext(), EditProfileActivity.class);
//                myIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                drawer.closeDrawer(GravityCompat.START);
                startActivity(myIntent);
                overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
                break;
            }
            case R.id.sidebar_company_history: {
                Intent myIntent = new Intent(getApplicationContext(), HistoryActivity.class);
//                myIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                drawer.closeDrawer(GravityCompat.START);
                startActivity(myIntent);
                overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
                break;
            }
            case R.id.sidebar_company_offlineSchedule: {

                Intent myIntent = new Intent(getApplicationContext(), ScheduleActivity.class);
//                myIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                drawer.closeDrawer(GravityCompat.START);
                startActivity(myIntent);
                overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
                break;
            }
            case R.id.sidebar_company_profit: {
                Intent myIntent = new Intent(getApplicationContext(), ProfitActivity.class);
//                myIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                drawer.closeDrawer(GravityCompat.START);
                startActivity(myIntent);
                overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
                break;
            }
            case R.id.sidebar_company_withdraw: {
                Intent myIntent = new Intent(getApplicationContext(), WithdrawActivity.class);
//                myIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                drawer.closeDrawer(GravityCompat.START);
                startActivity(myIntent);
                overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
                break;
            }
            case R.id.sidebar_company_config: {
                // 설정시
                // 설정 화면으로 인텐트를 넘긴다.
                Intent myIntent = new Intent(getApplicationContext(),ConfigActivity.class);
//                myIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                drawer.closeDrawer(GravityCompat.START);
                startActivity(myIntent);
                overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
                break;
            }

        }
        return true;
    }




    public class BackPressCloseSystem {

        private long backKeyPressedTime = 0;
        private Toast toast;

        private Activity activity;

        public BackPressCloseSystem(Activity activity) {
            this.activity = activity;
        }

        public void onBackPressed() {

            if (isAfter2Seconds()) {
                backKeyPressedTime = System.currentTimeMillis();
                // ����ð��� �ٽ� �ʱ�ȭ

                toast = Toast.makeText(activity,
                        "앱을 종료하려면 뒤로가기 버튼을 한번 더 눌러주세요.",
                        Toast.LENGTH_SHORT);
                toast.show();

                return;
            }

            if (isBefore2Seconds()) {

                programShutdown();
                toast.cancel();
                PersistentCookieStore persistentCookieStore = new PersistentCookieStore(getApplicationContext());
                //persistentCookieStore.clear();
            }
        }

        private boolean isAfter2Seconds() {
            return System.currentTimeMillis() > backKeyPressedTime + 2000;
            // 2�� ������ ���
        }

        private boolean isBefore2Seconds() {
            return System.currentTimeMillis() <= backKeyPressedTime + 2000;
            // 2�ʰ� ������ �ʾ��� ���

        }

        private void programShutdown() {
            activity.moveTaskToBack(true);
            activity.finish();
            //     android.os.Process.killProcess(android.os.Process.myPid());
            //  System.exit(0);


        }

    }

    public void updateSwitchStatus(int chatPossibilty) {

        RequestParams param_possibility = new RequestParams();

        param_possibility.put("chat_switch", chatPossibilty);
        param_possibility.put("company_no", company_no);


        client_user.post(getString(R.string.URL) + "/company/update/possibility", param_possibility, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
//                Toast.makeText(MainActivity.this, "Possibility Update Success.", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(MainActivity.this, "오류가 발생하였습니다.\n" +
                        "다시 시도해주세요.", Toast.LENGTH_LONG).show();
            }
        });
    }

    public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;
        Boolean isError;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
            this.isError = false;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
                isError = true;
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            Log.e("Error", "" + isError);
            if (!isError) {

                bmImage.setImageBitmap(getCircularBitmap(result));
            } else {
                Glide.with(mContext).load(R.drawable.profile_default)
                        .bitmapTransform(new CropCircleTransformation(mContext))
                        .into(bmImage);
            }
        }
    }

    public static Bitmap getCircularBitmap(Bitmap bitmap) {
        Bitmap output;

        if (bitmap.getWidth() > bitmap.getHeight()) {
            output = Bitmap.createBitmap(bitmap.getHeight(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        } else {
            output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getWidth(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        float r = 0;

        if (bitmap.getWidth() > bitmap.getHeight()) {
            r = bitmap.getHeight() / 2;
        } else {
            r = bitmap.getWidth() / 2;
        }

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle(r, r, r, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

}
