package com.example.securecameraapp;

import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {

    private RecyclerView photosRecyclerView;
    private TextView emptyView;
    private PhotoAdapter photoAdapter;
    private List<File> photoFiles = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        photosRecyclerView = findViewById(R.id.photosRecyclerView);
        emptyView = findViewById(R.id.emptyView);

        // Set up toolbar
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Set up recyclerview
        photoAdapter = new PhotoAdapter(this, photoFiles);
        photosRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        photosRecyclerView.setAdapter(photoAdapter);

        loadPhotos();
    }

    private void loadPhotos() {
        File picturesDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (picturesDir != null && picturesDir.exists()) {
            File[] files = picturesDir.listFiles(
                    (dir, name) -> name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".jpeg"));

            if (files != null && files.length > 0) {
                // Sort files by last modified date (newest first)
                Arrays.sort(files, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));

                photoFiles.clear();
                photoFiles.addAll(Arrays.asList(files));
                photoAdapter.notifyDataSetChanged();

                emptyView.setVisibility(View.GONE);
                photosRecyclerView.setVisibility(View.VISIBLE);
            } else {
                emptyView.setVisibility(View.VISIBLE);
                photosRecyclerView.setVisibility(View.GONE);
            }
        } else {
            Toast.makeText(this, "Không thể truy cập thư mục ảnh", Toast.LENGTH_SHORT).show();
            emptyView.setVisibility(View.VISIBLE);
            photosRecyclerView.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh photos when returning to this screen
        loadPhotos();
    }
}
