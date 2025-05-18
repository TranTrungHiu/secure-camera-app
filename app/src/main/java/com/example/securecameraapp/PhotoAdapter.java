package com.example.securecameraapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {

    private Context context;
    private List<File> photoFiles;

    public PhotoAdapter(Context context, List<File> photoFiles) {
        this.context = context;
        this.photoFiles = photoFiles;
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_photo, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        File photoFile = photoFiles.get(position);

        // Load image using Glide
        Glide.with(context)
                .load(photoFile)
                .centerCrop()
                .into(holder.photoImageView);

        // Check if file is encrypted (for now just checking if the name contains
        // "encrypted")
        boolean isEncrypted = photoFile.getName().contains("encrypted");
        holder.encryptedBadge.setVisibility(isEncrypted ? View.VISIBLE : View.GONE);

        // Format and display the date
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        String formattedDate = sdf.format(new Date(photoFile.lastModified()));
        holder.photoDateText.setText(formattedDate);

        // Display the file size
        String fileSize = formatFileSize(photoFile.length());
        holder.photoSizeText.setText(fileSize + (isEncrypted ? " • Mã hóa" : ""));

        holder.itemView.setOnClickListener(v -> {
            // Open photo detail activity or show image in a dialog
            // For now just show a toast
            // Toast.makeText(context, "Đã chọn: " + photoFile.getName(),
            // Toast.LENGTH_SHORT).show();
        });
    }

    private String formatFileSize(long size) {
        if (size <= 0)
            return "0 B";
        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return String.format("%.1f %s", size / Math.pow(1024, digitGroups), units[digitGroups]);
    }

    @Override
    public int getItemCount() {
        return photoFiles.size();
    }

    static class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView photoImageView;
        ImageView encryptedBadge;
        TextView photoDateText;
        TextView photoSizeText;

        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            photoImageView = itemView.findViewById(R.id.photoImageView);
            encryptedBadge = itemView.findViewById(R.id.encryptedBadge);
            photoDateText = itemView.findViewById(R.id.photoDateText);
            photoSizeText = itemView.findViewById(R.id.photoSizeText);
        }
    }
}
