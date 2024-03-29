package com.example.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;

import java.util.LinkedList;
import java.util.List;

public class Albums extends Activity {
    private ImageView albumsPicture;
    public static final int CHOOSE_PHOTO = 2;
    private Button button1, button2;
    private String token;
    private String pathiden;
    private String resultden;
    //弹窗显示
    private AlertDialog mAlertDialog = null;

    private List<String> result = new LinkedList<>();
    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == 0x123) {
                Toast.makeText(getApplicationContext(), "文档生成成功！", Toast.LENGTH_SHORT).show();
            }
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.albums);
        albumsPicture = super.findViewById(R.id.picture);


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, CHOOSE_PHOTO);
        } else {
            openAlbum();
        }



        /*文字识别部分*/
        button1 = findViewById(R.id.pictureIdentity2);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread() {
                    @Override
                    public void run() {
                        ocrPost ocrpost = new ocrPost();
                        resultden = ocrpost.accurateBasic(pathiden);
                        /*处理返回的字符串*/
                        AlBean resultBean = new Gson().fromJson(resultden,AlBean.class);
                        System.out.println(resultBean.getWords_result());
                        for (AlBean.Pdf pdf: resultBean.getWords_result()) {
                            System.out.println(pdf.getWords());
                            result.add(pdf.getWords());
                        }
                        resultden = result.toString();
                        /*
                        在不同的线程中处理返回的字符串，例如在一个异步任务中，需要确保在主线程中更新UI。
                        可以使用runOnUiThread()方法来更新UI
                        */
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
//                                 在主线程中更新UI
                                final CustomDialog mDialog = new CustomDialog(Albums.this, "识别结果", resultden, "确定", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Toast.makeText(getApplicationContext(), "这是你的生成结果", Toast.LENGTH_SHORT).show();
                                    }
                                },"取消");
                                mDialog.setCanceledOnTouchOutside(false);
                                mDialog.show();
                            }
                        });

                        if (Build.VERSION.SDK_INT >= 23) {
                            int REQUEST_CODE_CONTACT = 101;
                            String[] permissions = {
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE};
                            //验证是否许可权限
                            for (String str : permissions) {
                                if (Albums.this.checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                                    //申请权限
                                    Albums.this.requestPermissions(permissions, REQUEST_CODE_CONTACT);
                                    return;
                                } else {
                                    FileLog fileLog = new FileLog();
                                    fileLog.saveLog("报告", resultden, "识别结果");
                                    Looper.prepare();
                                    Toast.makeText(getApplicationContext(), "文档生成成功！", Toast.LENGTH_SHORT).show();
                                    Looper.loop();
                                }
                            }

                        }


                    }
                }.start();
                System.out.println(resultden);
            }
        });

        button2 = findViewById(R.id.pictureback2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(v.getContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

    private void openAlbum() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, CHOOSE_PHOTO);//打开相册
    }


    // 使用startActivityForResult()方法开启Intent的回调
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case CHOOSE_PHOTO:
                //相册照片
                if (requestCode == CHOOSE_PHOTO && resultCode == RESULT_OK && null != data) {
                    if (Build.VERSION.SDK_INT >= 19) {
                        handleImageOnKitkat(data);
                    } else {
                        handleImageBeforeKitKat(data);
                    }
                }
            default:
                break;
        }
    }

    @TargetApi(19)
    private void handleImageOnKitkat(Intent data) {
        String imagePath = null;
        Uri uri = data.getData();
        if (DocumentsContract.isDocumentUri(this, uri)) {
            //如果是document类型的uri，则通过document id处理
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1];
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content:" +
                        "//downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            //如果是content类型的uri，则使用普通方式处理
            imagePath = getImagePath(uri, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            //如果是File类型的uri，直接获取图片路径即可
            imagePath = uri.getPath();
        }
        //根据图片路径显示图片
        displayImage(imagePath);
        pathiden = imagePath;
    }

    private void handleImageBeforeKitKat(Intent data) {
        Uri uri = data.getData();
        String imagePath = getImagePath(uri, null);
        displayImage(imagePath);
    }

    private String getImagePath(Uri uri, String selection) {
        String path = null;
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    private void displayImage(String imagePath) {
        if (imagePath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            albumsPicture.setImageBitmap(bitmap);//将图片放置在控件上
        } else {
            Toast.makeText(this, "得到图片失败", Toast.LENGTH_SHORT).show();
        }
    }



}
