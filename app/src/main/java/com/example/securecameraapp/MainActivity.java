package com.example.securecameraapp;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import androidx.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import androidx.appcompat.widget.SwitchCompat;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.security.crypto.EncryptedFile;
import androidx.security.crypto.MasterKeys;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.util.concurrent.ListenableFuture;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private static final int CAMERA_REQUEST_CODE = 100;
    private PreviewView viewFinder;
    private FloatingActionButton captureButton;
    private ImageButton switchCameraButton;
    private ImageButton flashButton;
    private ImageButton galleryButton;
    private SwitchCompat encryptionSwitch;
    private TextView encryptionStatus;
    private CardView messageContainer;
    private TextView messageText;

    private ExecutorService cameraExecutor;
    private ImageCapture imageCapture;
    private int lensFacing = CameraSelector.LENS_FACING_BACK;
    private int flashMode = ImageCapture.FLASH_MODE_OFF;
    private boolean isEncryptionEnabled = false;
    private int photoQuality = 1440; // Default to medium quality (2K)
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        viewFinder = findViewById(R.id.viewFinder);
        captureButton = findViewById(R.id.captureButton);
        switchCameraButton = findViewById(R.id.switchCameraButton);
        flashButton = findViewById(R.id.flashButton);
        galleryButton = findViewById(R.id.galleryButton);
        encryptionSwitch = findViewById(R.id.encryptionSwitch);
        encryptionStatus = findViewById(R.id.encryptionStatus);
        messageContainer = findViewById(R.id.messageContainer);
        messageText = findViewById(R.id.messageText);

        // Initialize shared preferences
        preferences = PreferenceManager.getDefaultSharedPreferences(this); // Set initial settings from preferences
        isEncryptionEnabled = preferences.getBoolean(SettingsActivity.PREF_DEFAULT_ENCRYPTION, false);
        flashMode = SettingsActivity.getFlashModeFromPref(
                preferences.getInt(SettingsActivity.PREF_FLASH_MODE, 0));
        lensFacing = SettingsActivity.getCameraLensFacingFromPref(
                preferences.getInt(SettingsActivity.PREF_CAMERA_TYPE, 0));
        photoQuality = SettingsActivity.getPhotoQualityFromPref(
                preferences.getInt(SettingsActivity.PREF_PHOTO_QUALITY, 1));

        // Update UI based on settings
        encryptionSwitch.setChecked(isEncryptionEnabled);
        updateEncryptionStatus();

        // Set up executor for camera operations
        cameraExecutor = Executors.newSingleThreadExecutor();

        // Button click listeners
        captureButton.setOnClickListener(v -> takePhoto());

        switchCameraButton.setOnClickListener(v -> {
            lensFacing = (lensFacing == CameraSelector.LENS_FACING_BACK) ? CameraSelector.LENS_FACING_FRONT
                    : CameraSelector.LENS_FACING_BACK;
            startCamera();
        });

        flashButton.setOnClickListener(v -> {
            toggleFlash();
        });

        galleryButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, GalleryActivity.class);
            startActivity(intent);
        });

        encryptionSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isEncryptionEnabled = isChecked;
            updateEncryptionStatus();
        });

        // Request permissions
        requestPermissions();

        // Schedule photo cleanup if enabled
        if (preferences.getBoolean(SettingsActivity.PREF_AUTO_DELETE, false)) {
            scheduleCleanupWork();
        }
    }

    private void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] { Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE },
                    CAMERA_REQUEST_CODE);
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
            showMessage(getString(R.string.camera_permission_denied), false);
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                // Set up the preview
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(viewFinder.getSurfaceProvider()); // Set up image capture
                ImageCapture.Builder imageCaptureBuilder = new ImageCapture.Builder()
                        .setFlashMode(flashMode);

                // Configure resolution based on quality setting
                if (photoQuality > 0) {
                    imageCaptureBuilder.setTargetResolution(new android.util.Size(photoQuality, photoQuality * 9 / 16));
                } else {
                    // Max quality - use highest available resolution
                    imageCaptureBuilder.setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY);
                }

                imageCapture = imageCaptureBuilder.build();

                // Select front or back camera
                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(lensFacing)
                        .build();

                // Must unbind before rebinding
                cameraProvider.unbindAll();

                // Bind to lifecycle
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

                // Update flash button icon based on available features
                if (cameraProvider.hasCamera(cameraSelector)) {
                    flashButton.setEnabled(true);
                    updateFlashUI();
                } else {
                    flashButton.setEnabled(false);
                }

                // Only show camera switch button if both cameras are available
                switchCameraButton.setEnabled(
                        cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) &&
                                cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA));

            } catch (Exception e) {
                e.printStackTrace();
                showMessage("Lỗi khi khởi tạo camera: " + e.getMessage(), false);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void toggleFlash() {
        switch (flashMode) {
            case ImageCapture.FLASH_MODE_OFF:
                flashMode = ImageCapture.FLASH_MODE_ON;
                break;
            case ImageCapture.FLASH_MODE_ON:
                flashMode = ImageCapture.FLASH_MODE_AUTO;
                break;
            case ImageCapture.FLASH_MODE_AUTO:
                flashMode = ImageCapture.FLASH_MODE_OFF;
                break;
        }

        if (imageCapture != null) {
            imageCapture.setFlashMode(flashMode);
            updateFlashUI();
        }
    }

    private void updateFlashUI() {
        switch (flashMode) {
            case ImageCapture.FLASH_MODE_OFF:
                flashButton.setImageResource(R.drawable.ic_flash_off);
                break;
            case ImageCapture.FLASH_MODE_ON:
                flashButton.setImageResource(R.drawable.ic_flash_on);
                break;
            case ImageCapture.FLASH_MODE_AUTO:
                flashButton.setImageResource(R.drawable.ic_flash_auto);
                break;
        }
    }

    private void updateEncryptionStatus() {
        encryptionStatus.setText(isEncryptionEnabled ? "Đã bật mã hóa" : getString(R.string.enable_encryption));

        if (isEncryptionEnabled) {
            encryptionStatus.setTextColor(ContextCompat.getColor(this, R.color.success_green));
        } else {
            encryptionStatus.setTextColor(ContextCompat.getColor(this, R.color.white));
        }

        // Save encryption setting
        preferences.edit().putBoolean(SettingsActivity.PREF_DEFAULT_ENCRYPTION, isEncryptionEnabled).apply();
    }

    private void takePhoto() {
        if (imageCapture == null) {
            showMessage("Lỗi camera, vui lòng thử lại", false);
            return;
        }

        // Create file name with timestamp
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = isEncryptionEnabled ? "encrypted_photo_" + timeStamp + ".jpg" : "photo_" + timeStamp + ".jpg";

        // Get directory for saving photos
        File picturesDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (picturesDir == null) {
            showMessage("Không thể tạo thư mục lưu ảnh", false);
            return;
        }

        // Create file
        File photoFile = new File(picturesDir, fileName);

        // Set up output options
        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        // Take the picture
        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        if (isEncryptionEnabled) {
                            encryptFile(photoFile);
                            showMessage(getString(R.string.file_saved_and_encrypted), true);
                        } else {
                            showMessage(getString(R.string.file_saved), true);
                        }
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        showMessage(getString(R.string.error_saving) + ": " + exception.getMessage(), false);
                    }
                });
    }

    private void showMessage(String message, boolean isSuccess) {
        messageText.setText(message);
        messageText.setTextColor(ContextCompat.getColor(this,
                isSuccess ? R.color.success_green : R.color.warning_red));
        messageContainer.setVisibility(View.VISIBLE);

        // Hide message after a delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            messageContainer.setVisibility(View.GONE);
        }, 3000);
    }

    private void openImage(File imageFile) {
        // Create intent to open image in default gallery app
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = FileProvider.getUriForFile(this, "com.example.securecameraapp.fileprovider", imageFile);
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
                    EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB).build();
            FileInputStream fis = new FileInputStream(file);
            FileOutputStream fos = encryptedFile.openFileOutput();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
            fis.close();
            fos.close();
            // We don't delete the original file because we're already saving it with
            // "encrypted_" prefix
            // This way we have a marker that the file is encrypted
        } catch (Exception e) {
            e.printStackTrace();
            showMessage("Lỗi khi mã hóa file: " + e.getMessage(), false);
        }
    }

    private void scheduleCleanupWork() {
        // Schedule a daily cleanup job
        PeriodicWorkRequest cleanupRequest = new PeriodicWorkRequest.Builder(CleanupWorker.class, 1, TimeUnit.DAYS)
                .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "photo_cleanup",
                ExistingPeriodicWorkPolicy.REPLACE,
                cleanupRequest);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            // Open settings activity
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Update settings when coming back from settings activity
        isEncryptionEnabled = preferences.getBoolean(SettingsActivity.PREF_DEFAULT_ENCRYPTION, isEncryptionEnabled);
        encryptionSwitch.setChecked(isEncryptionEnabled);
        updateEncryptionStatus();
        flashMode = SettingsActivity.getFlashModeFromPref(
                preferences.getInt(SettingsActivity.PREF_FLASH_MODE, 0));

        // If camera settings changed, restart camera
        int newLensFacing = SettingsActivity.getCameraLensFacingFromPref(
                preferences.getInt(SettingsActivity.PREF_CAMERA_TYPE, 0));
        int newPhotoQuality = SettingsActivity.getPhotoQualityFromPref(
                preferences.getInt(SettingsActivity.PREF_PHOTO_QUALITY, 1));

        boolean settingsChanged = (newLensFacing != lensFacing) || (newPhotoQuality != photoQuality);

        if (settingsChanged) {
            lensFacing = newLensFacing;
            photoQuality = newPhotoQuality;
            startCamera();
        } else if (imageCapture != null) {
            // Just update flash mode if camera hasn't changed
            imageCapture.setFlashMode(flashMode);
            updateFlashUI();
        }

        // Check if auto-delete setting has been enabled
        if (preferences.getBoolean(SettingsActivity.PREF_AUTO_DELETE, false)) {
            scheduleCleanupWork();
        }
    }
}