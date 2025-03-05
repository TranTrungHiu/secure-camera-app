package com.example.securecameraapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.security.crypto.EncryptedFile;
import androidx.security.crypto.MasterKeys;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity {
    private static final int CAMERA_REQUEST_CODE = 100;
    private PreviewView viewFinder;
    private Button captureButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewFinder = findViewById(R.id.viewFinder);
        captureButton = findViewById(R.id.captureButton);

        requestPermissions();
    }

    private void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
        } else {
            startCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST_CODE && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            Toast.makeText(this, "Quyền Camera bị từ chối", Toast.LENGTH_SHORT).show();
        }
    }
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(viewFinder.getSurfaceProvider());
                ImageCapture imageCapture = new ImageCapture.Builder().build();
                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
                captureButton.setOnClickListener(v -> takePhoto(imageCapture));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }
    private void takePhoto(ImageCapture imageCapture) {
        // Tạo đường dẫn lưu file vào thư mục Pictures của thiết bị
        File picturesDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (picturesDir != null) {
            File file = new File(picturesDir, "secure_photo.jpg");
            ImageCapture.OutputFileOptions outputFileOptions =
                    new ImageCapture.OutputFileOptions.Builder(file).build();

            imageCapture.takePicture(outputFileOptions,
                    ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
                        @Override
                        public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                            // Hiển thị thông báo với đường dẫn nơi lưu ảnh
                            String filePath = file.getAbsolutePath(); // Lấy đường dẫn tuyệt đối của file
                            Toast.makeText(MainActivity.this, "Ảnh đã lưu thành công tại: " + filePath, Toast.LENGTH_LONG).show();

                            // Mở ảnh sau khi chụp
                            // openImage(file);
                        }

                        @Override
                        public void onError(@NonNull ImageCaptureException exception) {
                            Toast.makeText(MainActivity.this, "Lỗi khi chụp ảnh", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void openImage(File imageFile) {
        // Tạo Intent để mở ảnh trong ứng dụng xem ảnh mặc định
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = FileProvider.getUriForFile(this, "iuh.fit.se.lab2_camerax.fileprovider", imageFile);
        intent.setDataAndType(uri, "image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
    }


    private void encryptFile(File file) {
        try {
            EncryptedFile encryptedFile = new EncryptedFile.Builder(
                    file,
                    this,
                    MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
                    EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
            ).build();
            FileInputStream fis = new FileInputStream(file);
            FileOutputStream fos = encryptedFile.openFileOutput();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
            fis.close();
            fos.close();
            file.delete(); // Xóa file gốc để tránh lộ thông tin
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}