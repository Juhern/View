package com.example.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;

public class Camera extends Activity {
    private ImageView cameraPicture;
    public static final int TAKE_PHOTO = 1;
    private Button pictureSave = null;
    private Uri imageUri;
    private String uriden;
    private String token;
    private String pathiden;
    private String resultden;
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
        setContentView(R.layout.camera);

        pictureSave = findViewById(R.id.pictureSave);
        cameraPicture = findViewById(R.id.picture);

        // 创建一个File对象，用于保存摄像头拍下的图片，这里把图片命名为output_image.jpg
        // 并将它存放在手机SD卡的应用关联缓存目录下
        File outputImage = new File(getExternalCacheDir(), "output_image.jpg");
        // 对照片的更换设置
        try {
            // 如果上一次的照片存在，就删除
            if (outputImage.exists()) {
                outputImage.delete();
            }
            // 创建一个新的文件
            outputImage.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 如果Android版本大于等于7.0
        if (Build.VERSION.SDK_INT >= 24) {
            // 将File对象转换成一个封装过的Uri对象
            imageUri = FileProvider.getUriForFile(this, "com.example.myapplication.fileProvider", outputImage);
            Log.d("MainActivity", outputImage.toString() + "手机系统版本高于Android7.0");
        } else {
            // 将File对象转换为Uri对象，这个Uri标识着output_image.jpg这张图片的本地真实路径
            Log.d("MainActivity", outputImage.toString() + "手机系统版本低于Android7.0");
            imageUri = Uri.fromFile(outputImage);
        }
        // 动态申请权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, TAKE_PHOTO);
        } else {
            // 启动相机程序
            startCamera();
        }


        pictureSave.setOnClickListener(new pictureSaveFunction());

        Button button1 = findViewById(R.id.pictureback);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        /*文字识别*/
        Button button2 = findViewById(R.id.pictureIdentity);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println(uriden);
                new Thread() {
                    @Override
                    public void run() {
                        resultden = accurateBasic(uriden);
                        if (Build.VERSION.SDK_INT >= 23) {
                            int REQUEST_CODE_CONTACT = 101;
                            String[] permissions = {
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE};
                            //验证是否许可权限
                            for (String str : permissions) {
                                if (Camera.this.checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                                    //申请权限
                                    Camera.this.requestPermissions(permissions, REQUEST_CODE_CONTACT);
                                    return;
                                } else {
                                    FileLog fileLog = new FileLog();
                                    fileLog.saveLog("报告", resultden, "安排");
                                    handler.sendEmptyMessage(0x123);
                                }
                            }

                        }
                    }
                }.start();
                System.out.println(resultden);
            }
        });


    }

    private void startCamera() {
        Intent intent4 = new Intent("android.media.action.IMAGE_CAPTURE");
        // 指定图片的输出地址为imageUri
        intent4.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent4, TAKE_PHOTO);

    }


    private class pictureSaveFunction implements View.OnClickListener {
        public void onClick(View view) {
            BitmapDrawable bmpDrawable = (BitmapDrawable) cameraPicture.getDrawable();
            Bitmap bitmap = bmpDrawable.getBitmap();
            saveToSystemGallery(bitmap);//将图片保存到本地
            Toast.makeText(getApplicationContext(), "图片保存成功！", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case TAKE_PHOTO:
                if (requestCode == TAKE_PHOTO && resultCode == RESULT_OK) {
                    try {
                        // 将图片解析成Bitmap对象
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        cameraPicture.setImageBitmap(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;
            default:
                break;
        }
    }

    public void saveToSystemGallery(Bitmap bmp) {
        // 首先保存图片
        File appDir = new File(Environment.getExternalStorageDirectory(), "cinema");
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 其次把文件插入到系统图库
        try {
            MediaStore.Images.Media.insertImage(getContentResolver(),
                    file.getAbsolutePath(), fileName, null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        //sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse(file.getAbsolutePath())));
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(file);
        intent.setData(uri);
        sendBroadcast(intent);// 发送广播，通知图库更新
        uriden = uri.getPath();
    }


    /*识别方法*/
    public String accurateBasic(String uripath) {
        // 请求url
        AccessToken accessToken1 = new AccessToken();
        token = accessToken1.getAuth();


        String url = "https://aip.baidubce.com/rest/2.0/ocr/v1/accurate_basic";
        try {
            // 本地文件路径
            String filePath = uripath;
            byte[] imgData = FileUtil.readFileByBytes(filePath);
            String imgStr = Base64Util.encode(imgData);
            String imgParam = URLEncoder.encode(imgStr, "UTF-8");

            String param = "image=" + imgParam;

            // 注意这里仅为了简化编码每一次请求都去获取access_token，线上环境access_token有过期时间， 客户端可自行缓存，过期后重新获取。
            String accessToken = token;

            String result = HttpUtil.post(url, accessToken, param);
            System.out.println(result);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}
