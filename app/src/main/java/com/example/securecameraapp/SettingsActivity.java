package com.example.securecameraapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import androidx.appcompat.widget.SwitchCompat;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;

import com.google.android.material.appbar.MaterialToolbar;

public class SettingsActivity extends AppCompatActivity {

    // Constants for preference keys
    public static final String PREF_DEFAULT_ENCRYPTION = "default_encryption";
    public static final String PREF_AUTO_DELETE = "auto_delete";
    public static final String PREF_FLASH_MODE = "flash_mode";
    public static final String PREF_CAMERA_TYPE = "camera_type";
    public static final String PREF_PHOTO_QUALITY = "photo_quality";

    private SharedPreferences preferences;
    private SwitchCompat defaultEncryptionSwitch;
    private SwitchCompat autoDeleteSwitch;
    private Spinner flashModeSpinner;
    private Spinner cameraSpinner;
    private Spinner photoQualitySpinner;
    private Button resetButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Initialize views
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        defaultEncryptionSwitch = findViewById(R.id.defaultEncryptionSwitch);
        autoDeleteSwitch = findViewById(R.id.autoDeleteSwitch);
        flashModeSpinner = findViewById(R.id.flashModeSpinner);
        cameraSpinner = findViewById(R.id.cameraSpinner);
        photoQualitySpinner = findViewById(R.id.photoQualitySpinner);
        resetButton = findViewById(R.id.resetSettingsButton);

        // Set up toolbar
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Get preferences
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Load saved preferences
        loadPreferences();

        // Set up listeners
        defaultEncryptionSwitch.setOnCheckedChangeListener(
                (buttonView, isChecked) -> savePreference(PREF_DEFAULT_ENCRYPTION, isChecked));

        autoDeleteSwitch
                .setOnCheckedChangeListener((buttonView, isChecked) -> savePreference(PREF_AUTO_DELETE, isChecked));

        flashModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                savePreference(PREF_FLASH_MODE, position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
        cameraSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                savePreference(PREF_CAMERA_TYPE, position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        photoQualitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                savePreference(PREF_PHOTO_QUALITY, position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        resetButton.setOnClickListener(v -> resetSettings());
    }

    private void loadPreferences() {
        // Load encryption setting
        boolean defaultEncryption = preferences.getBoolean(PREF_DEFAULT_ENCRYPTION, false);
        defaultEncryptionSwitch.setChecked(defaultEncryption);

        // Load auto-delete setting
        boolean autoDelete = preferences.getBoolean(PREF_AUTO_DELETE, false);
        autoDeleteSwitch.setChecked(autoDelete);

        // Load flash mode setting
        int flashMode = preferences.getInt(PREF_FLASH_MODE, 0); // Default is OFF (0)
        flashModeSpinner.setSelection(flashMode); // Load camera type setting
        int cameraType = preferences.getInt(PREF_CAMERA_TYPE, 0); // Default is BACK (0)
        cameraSpinner.setSelection(cameraType);

        // Load photo quality setting
        int photoQuality = preferences.getInt(PREF_PHOTO_QUALITY, 1); // Default is Medium (1)
        photoQualitySpinner.setSelection(photoQuality);
    }

    private void savePreference(String key, boolean value) {
        preferences.edit().putBoolean(key, value).apply();
        showToast(getString(R.string.settings_saved));
    }

    private void savePreference(String key, int value) {
        preferences.edit().putInt(key, value).apply();
        showToast(getString(R.string.settings_saved));
    }

    private void resetSettings() {
        // Clear all preferences
        preferences.edit().clear().apply(); // Reset UI
        defaultEncryptionSwitch.setChecked(false);
        autoDeleteSwitch.setChecked(false);
        flashModeSpinner.setSelection(0);
        cameraSpinner.setSelection(0);
        photoQualitySpinner.setSelection(1); // Default to Medium quality

        showToast(getString(R.string.settings_saved));
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Helper method to convert spinner position to camera flash mode
     */
    public static int getFlashModeFromPref(int spinnerPosition) {
        switch (spinnerPosition) {
            case 0:
                return ImageCapture.FLASH_MODE_OFF;
            case 1:
                return ImageCapture.FLASH_MODE_ON;
            case 2:
                return ImageCapture.FLASH_MODE_AUTO;
            default:
                return ImageCapture.FLASH_MODE_OFF;
        }
    }

    /**
     * Helper method to convert spinner position to camera lens facing
     */
    public static int getCameraLensFacingFromPref(int spinnerPosition) {
        switch (spinnerPosition) {
            case 0:
                return CameraSelector.LENS_FACING_BACK;
            case 1:
                return CameraSelector.LENS_FACING_FRONT;
            default:
                return CameraSelector.LENS_FACING_BACK;
        }
    }

    /**
     * Helper method to convert spinner position to photo quality (resolution)
     */
    public static int getPhotoQualityFromPref(int spinnerPosition) {
        switch (spinnerPosition) {
            case 0: // Low
                return 1080; // 1080p
            case 1: // Medium
                return 1440; // 2K
            case 2: // High
                return 2160; // 4K
            case 3: // Max
                return 0; // 0 indicates maximum available resolution
            default:
                return 1440; // Default to medium (2K)
        }
    }
}
