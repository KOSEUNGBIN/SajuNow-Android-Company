package com.test.landvibe.company;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.test.landvibe.company.common.CheckRegId;
import com.test.landvibe.company.util.DeEncrypter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;

import cz.msebera.android.httpclient.Header;
import jp.wasabeef.glide.transformations.CropCircleTransformation;

/**
 * 역술인 정보 변경에 대한 액티비티
 */
public class EditProfileActivity extends AppCompatActivity {

    private Button companyEdit_menu;
    private Button update_bt;
    private EditText greeting_et;
    private EditText introduce_et;
    private EditText histroy_introduce_et;
    private int company_no;
    public AsyncHttpClient client;
    public PersistentCookieStore cookieStore;
    public Context mContext;

    int REQUEST_CAMERA = 0, SELECT_FILE = 1;
    Button btnSelect;
    ImageView ivImage;

    private Toolbar toolbar;
    private DrawerLayout drawer;

    private CheckRegId checkRegid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company_edit_profile);
        btnSelect = (Button) findViewById(R.id.btnSelectPhoto);
        btnSelect.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                selectImage();
            }
        });
        ivImage = (ImageView) findViewById(R.id.ivImage);
        greeting_et = (EditText) findViewById(R.id.greeting_et);
        introduce_et = (EditText) findViewById(R.id.introduce_et);
        histroy_introduce_et = (EditText) findViewById(R.id.histroy_introduce_et);
        update_bt = (Button) findViewById(R.id.update_btn);

        toolbar = (Toolbar) findViewById(R.id.toolbar_company_edit);
        setSupportActionBar(toolbar);
        mContext = getApplicationContext();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        checkRegid = new CheckRegId();

        client = new AsyncHttpClient();
        cookieStore = new PersistentCookieStore(this);
        Log.d("error", "company token : " + cookieStore.getCookies().get(0).toString());
        client.addHeader("Cookie", cookieStore.getCookies().get(0).getValue());
        DeEncrypter deEncrypter = new DeEncrypter();

        checkRegid.checkRegid(getString(R.string.URL), cookieStore.getCookies().get(0).getValue(), EditProfileActivity.this);

        String token = deEncrypter.decrypt(URLDecoder.decode(cookieStore.getCookies().get(0).getValue()));
        String[] result = token.split("\\?");
        company_no = Integer.parseInt(result[0]);
        Log.d("error", "company_no : " + company_no);

        RequestParams params = new RequestParams();

        client.post(getString(R.string.URL) + "/company/" + company_no,
                params, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers,
                                          JSONObject response) {
                        super.onSuccess(statusCode, headers, response);
                        Log.d("error", "on Success : " + response.toString());
                        try {
                            Log.d("error", "on Success : " + response.getJSONObject("result").getString("greeting"));
                            greeting_et.setText(response.getJSONObject("result").getString("greeting"));
                            introduce_et.setText(response.getJSONObject("result").getString("introduce"));
                            histroy_introduce_et.setText(response.getJSONObject("result").getString("history_introduce"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers,
                                          String responseString, Throwable throwable) {
                        super.onFailure(statusCode, headers,
                                responseString, throwable);

                        Log.d("error", "on Failed  ");

                    }
                });


        update_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final RequestParams param = new RequestParams();
                param.add("greeting", String.valueOf(greeting_et.getText()));
                param.add("introduce", String.valueOf(introduce_et.getText()));
                param.add("history_introduce", String.valueOf(histroy_introduce_et.getText()));
                param.add("company_no", "" + company_no);
                // DB - update 에 접근
                client.post(getString(R.string.URL) + "/company/update", param, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int i, Header[] headers, byte[] bytes) {
//                        Toast.makeText(EditProfileActivity.this, "인삿말, 소개 변경 완료", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                        Log.d("error", "Friend_Update_DB_Fail");


                    }

                });


            }
        });

        GlideUrl glideUrl = new GlideUrl(getString(R.string.URL) + "/company/image/" + company_no, new LazyHeaders.Builder()
                .addHeader("Cookie", cookieStore.getCookies().get(0).getValue())
                .build());

        Glide.with(this)
                .load(glideUrl)
                .bitmapTransform(new CropCircleTransformation(this))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into((ImageView) findViewById(R.id.ivImage));


//        Glide.with(this).load(getString(R.string.URL) + "/company/image/" + company_no)
//                .into((ImageView) findViewById(R.id.ivImage));
//        Glide.with(this).load((ImageView) findViewById(R.id.ivImage))
//                .bitmapTransform(new CropCircleTransformation((BitmapPool) getApplicationContext()))
//                .into((ImageView) findViewById(R.id.ivImage));


    }

    @Override
    protected void onRestart() {
        checkRegid.checkRegid(getString(R.string.URL), cookieStore.getCookies().get(0).getValue(), EditProfileActivity.this);
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Glide.clear((ImageView) findViewById(R.id.ivImage));
    }

    // 사진 고르기 다이얼로그 실행
    private void selectImage() {
        final CharSequence[] items = {"Take Photo", "Choose from Library",
                "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    Log.d("error", "is REQUEST_CAMERA ");
                    startActivityForResult(intent, REQUEST_CAMERA);
                } else if (items[item].equals("Choose from Library")) {
                    Log.d("error", "is EXTERNAL_CONTENT_URI ");
                    Intent intent = new Intent(
                            Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(
                            Intent.createChooser(intent, "Select File"),
                            SELECT_FILE);
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    //
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE) {
                onSelectFromGalleryResult(data);
                Log.d("error", "is SELECT_FILE ");

            } else if (requestCode == REQUEST_CAMERA) {
                onCaptureImageResult(data);
                Log.d("error", "is REQUEST_CAMERA ");
            }
        }
    }

    //방금 찍힌 사진 표시
    private void onCaptureImageResult(Intent data) {
        Bitmap thumbnail;
        try {
            thumbnail = (Bitmap) data.getExtras().get("data");
        } catch (Exception e) {
            Intent intent_uri = new Intent();
            onSelectFromGalleryResult(intent_uri);
            return;
            //  thumbnail =(Bitmap) data.getData();
        }
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
        Log.d("error", "onCaptureImageResult : " + company_no);


        final File destination = new File(Environment.getExternalStorageDirectory(),
                System.currentTimeMillis() + ".jpg");

        FileOutputStream fo;
        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (thumbnail.getWidth() >= thumbnail.getHeight()) {

            thumbnail = Bitmap.createBitmap(
                    thumbnail,
                    thumbnail.getWidth() / 2 - thumbnail.getHeight() / 2,
                    0,
                    thumbnail.getHeight(),
                    thumbnail.getHeight()
            );

        } else {

            thumbnail = Bitmap.createBitmap(
                    thumbnail,
                    0,
                    thumbnail.getHeight() / 2 - thumbnail.getWidth() / 2,
                    thumbnail.getWidth(),
                    thumbnail.getWidth()
            );
            SaveBitmapToFileCache(thumbnail, destination.getPath());


            File file = null;
            file = new File(destination.getPath());
            RequestParams params = new RequestParams();
            try {
                params.put("profile_picture", file);
                Log.d("error", "image is stored");

            } catch (FileNotFoundException e) {
                Log.d("error", e.toString());
            }

            Log.d("error", "in captured image company_no : " + company_no);
            client.post(getString(R.string.URL) + "/company/upload/image/" + company_no, params, new FileAsyncHttpResponseHandler(this) {

                @Override
                public void onFailure(int i, Header[] headers, Throwable throwable, File file) {
                    Log.d("error", "fail!!");
                }

                @Override
                public void onSuccess(int i, Header[] headers, File file) {
                    Log.d("error", file.getName() + ", " + file.getPath());
                    Log.d("error", "success");
//                    Glide.clear((ImageView) findViewById(R.id.imageButton));
//                    Toast.makeText(
//                            EditProfileActivity.this,
//                            "사진 변경이 완료되었습니다.", Toast.LENGTH_SHORT)
//                            .show();

                    destination.deleteOnExit();
                    destination.delete();

                    Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
                    myIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
//                    drawer.closeDrawer(GravityCompat.START);
                    startActivity(myIntent);
                    overridePendingTransition(0, 0);
                    finish();

                }


            });

            ivImage.setImageBitmap(thumbnail);
        }
    }

    //갤러리에서 선택한 사진 표시
    private void onSelectFromGalleryResult(Intent data) {
        Log.d("error", "onSelectFromGalleryResult : " + company_no);
        Uri selectedImageUri = data.getData();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        String[] projection = {MediaStore.MediaColumns.DATA};
        Cursor cursor;
        String selectedImagePath = "";
        try {
            cursor = managedQuery(selectedImageUri, projection, null, null,
                    null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            cursor.moveToFirst();

            selectedImagePath = cursor.getString(column_index);

        } catch (NullPointerException e) {
            selectedImagePath = getOriginalImagePath();
        }

        final File destination = new File(Environment.getExternalStorageDirectory(),
                System.currentTimeMillis() + ".jpg");

        BitmapFactory.Options options = new BitmapFactory.Options();
        BitmapFactory.decodeFile(selectedImagePath, options);
        Log.d("error", "outHeight : " + options.outHeight);
        Log.d("error", "outWidth : " + options.outWidth);

        Bitmap resized = BitmapFactory.decodeFile(selectedImagePath, options);

        if (options.outHeight + options.outWidth < 1000)
            resized = Bitmap.createScaledBitmap(resized, 100, 100, true);
        else if (options.outHeight + options.outWidth < 2000)
            resized = Bitmap.createScaledBitmap(resized, 500, 500, true);
        else
            resized = Bitmap.createScaledBitmap(resized, 800, 800, true);

        ExifInterface exif = null;
        try {
            exif = new ExifInterface(selectedImagePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        int exifOrientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        float exifDegree = exifOrientationToDegrees(exifOrientation);
        Log.d("error", "exifDegree : " + exifOrientation);
        Log.d("error", "exifDegree : " + exifDegree);
        resized = imgRotate(resized, exifDegree);

        if (resized.getWidth() >= resized.getHeight()) {

            resized = Bitmap.createBitmap(
                    resized,
                    resized.getWidth() / 2 - resized.getHeight() / 2,
                    0,
                    resized.getHeight(),
                    resized.getHeight()
            );

        } else {

            resized = Bitmap.createBitmap(
                    resized,
                    0,
                    resized.getHeight() / 2 - resized.getWidth() / 2,
                    resized.getWidth(),
                    resized.getWidth()
            );
        }


        resized.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

        FileOutputStream fo;
        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        SaveBitmapToFileCache(resized, destination.getPath());

        resized.recycle();
        resized = null;

        File file = null;
        file = new File(destination.getPath());
        Log.d("error", destination.getPath());
        RequestParams params = new RequestParams();
        try {
            params.put("profile_picture", file);
            Log.d("error", "image is stored");

        } catch (FileNotFoundException e) {
            Log.d("error", e.toString());
        }


        Log.d("error", "in selected image company_no : " + company_no);
        client.post(getString(R.string.URL) + "/company/upload/image/" + company_no, params, new FileAsyncHttpResponseHandler(this) {

            @Override
            public void onFailure(int i, Header[] headers, Throwable throwable, File file) {
                Log.d("error", "fail!!");
            }

            @Override
            public void onSuccess(int i, Header[] headers, File file) {
                Log.d("error", file.getName() + ", " + file.getPath());
                Log.d("error", "success");
//                Toast.makeText(
//                        EditProfileActivity.this,
//                        "사진 변경이 완료되었습니다.", Toast.LENGTH_SHORT)
//                        .show();
                destination.deleteOnExit();
                destination.delete();
                Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
                myIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
//                drawer.closeDrawer(GravityCompat.START);
                startActivity(myIntent);
                overridePendingTransition(0, 0);
                finish();


            }

        });
        Log.d("error", "end process");
    }

    private Bitmap imgRotate(Bitmap bmp, float degree) {
        int width = bmp.getWidth();
        int height = bmp.getHeight();

        Matrix matrix = new Matrix();
        matrix.postRotate(degree);

        Bitmap resizedBitmap = Bitmap.createBitmap(bmp, 0, 0, width, height, matrix, true);
        bmp.recycle();

        return resizedBitmap;
    }

    private void SaveBitmapToFileCache(Bitmap bitmap, String strFilePath) {

        File fileCacheItem = new File(strFilePath);
        OutputStream out = null;

        try {
            fileCacheItem.createNewFile();
            out = new FileOutputStream(fileCacheItem);

            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public int exifOrientationToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
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
                bmImage.setImageBitmap(MainActivity.getCircularBitmap(result));
            } else {
                //bmImage.setImageResource(R.drawable.profile_default);
                Glide.with(mContext).load(R.drawable.profile_default)
                        .bitmapTransform(new CropCircleTransformation(mContext))
                        .into(bmImage);
            }
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
        super.onBackPressed();
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

    public String getOriginalImagePath() {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection, null, null, null);
        int column_index_data = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToLast();

        return cursor.getString(column_index_data);
    }
}







