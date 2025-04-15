package com.example.a5_androidapp;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

import com.bumptech.glide.Glide;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImageDetailsActivity extends AppCompatActivity {
    private File imageFile;
    private DocumentFile documentFile;
    private boolean isUsingDocumentFile = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_details);

        // Get image from intent (either path or uri)
        String imagePath = getIntent().getStringExtra("imagePath");
        String imageUri = getIntent().getStringExtra("imageUri");

        if (imagePath != null) {
            // Standard file from internal storage
            imageFile = new File(imagePath);
            if (!imageFile.exists()) {
                Toast.makeText(this, "Image not found", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        } else if (imageUri != null) {
            // DocumentFile from content provider
            isUsingDocumentFile = true;
            documentFile = DocumentFile.fromSingleUri(this, Uri.parse(imageUri));
            if (documentFile == null || !documentFile.exists()) {
                Toast.makeText(this, "Image not found", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        } else {
            finish();
            return;
        }

        ImageView imageView = findViewById(R.id.imageView);
        TextView imageName = findViewById(R.id.imageName);
        TextView imagePathText = findViewById(R.id.imagePath);
        TextView imageSize = findViewById(R.id.imageSize);
        TextView imageDate = findViewById(R.id.imageDate);
        Button deleteButton = findViewById(R.id.deleteButton);

        if (isUsingDocumentFile) {
            Glide.with(this)
                    .load(documentFile.getUri())
                    .into(imageView);

            imageName.setText("Name: " + documentFile.getName());
            imagePathText.setText("Path: " + documentFile.getUri().toString());
            imageSize.setText("Size: " + formatFileSize(documentFile.length()));
            imageDate.setText("Date: " + formatDate(documentFile.lastModified()));
        } else {
            Glide.with(this)
                    .load(imageFile)
                    .into(imageView);

            imageName.setText("Name: " + imageFile.getName());
            imagePathText.setText("Path: " + imageFile.getAbsolutePath());
            imageSize.setText("Size: " + formatFileSize(imageFile.length()));
            imageDate.setText("Date: " + formatDate(imageFile.lastModified()));
        }

        deleteButton.setOnClickListener(v -> showDeleteConfirmationDialog());
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Image")
                .setMessage("Are you sure you want to delete this image?")
                .setPositiveButton("Delete", (dialog, which) -> deleteImage())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteImage() {
        boolean success = false;
        
        if (isUsingDocumentFile && documentFile != null) {
            success = documentFile.delete();
        } else if (imageFile != null) {
            success = imageFile.delete();
        }
        
        if (success) {
            Toast.makeText(this, "Image deleted", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to delete image", Toast.LENGTH_SHORT).show();
        }
    }

    private String formatFileSize(long size) {
        if (size <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return String.format(Locale.getDefault(), "%.1f %s", size / Math.pow(1024, digitGroups), units[digitGroups]);
    }

    private String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
} 