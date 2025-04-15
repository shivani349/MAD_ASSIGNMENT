package com.example.a5_androidapp;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {
    private static final int REQUEST_STORAGE_PERMISSION = 200;
    private static final int REQUEST_PICK_FOLDER = 201;
    private static final int REQUEST_MEDIA_IMAGES = 202;
    
    private RecyclerView imageGrid;
    private TextView folderPathText;
    private Button selectFolderButton;
    
    private GalleryImageAdapter imageAdapter;
    private List<DocumentFile> imageFiles;
    private Uri currentFolderUri;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        
        folderPathText = findViewById(R.id.folderPath);
        selectFolderButton = findViewById(R.id.selectFolderButton);
        imageGrid = findViewById(R.id.galleryGrid);
        
        imageGrid.setLayoutManager(new GridLayoutManager(this, 3));
        imageFiles = new ArrayList<>();
        imageAdapter = new GalleryImageAdapter(imageFiles, this::onImageClick);
        imageGrid.setAdapter(imageAdapter);
        
        selectFolderButton.setOnClickListener(v -> checkPermissionsAndPickFolder());
    }
    
    private void checkPermissionsAndPickFolder() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+, check for READ_MEDIA_IMAGES
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                        REQUEST_MEDIA_IMAGES);
            } else {
                openFolderPicker();
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10-12, we don't need explicit permissions for folder picker
            openFolderPicker();
        } else {
            // Android 9 and below
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_STORAGE_PERMISSION);
            } else {
                openFolderPicker();
            }
        }
    }
    
    private void openFolderPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        
        try {
            startActivityForResult(intent, REQUEST_PICK_FOLDER);
        } catch (Exception e) {
            Toast.makeText(this, "Unable to open folder picker: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private void loadImagesFromFolder(Uri folderUri) {
        currentFolderUri = folderUri;
        folderPathText.setText("Folder: " + folderUri.getPath());
        
        DocumentFile folder = DocumentFile.fromTreeUri(this, folderUri);
        if (folder != null && folder.exists()) {
            imageFiles.clear();
            DocumentFile[] files = folder.listFiles();
            for (DocumentFile file : files) {
                String mimeType = file.getType();
                if (mimeType != null && mimeType.startsWith("image/")) {
                    imageFiles.add(file);
                }
            }
            imageAdapter.notifyDataSetChanged();
            
            if (imageFiles.isEmpty()) {
                Toast.makeText(this, "No images found in this folder", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void onImageClick(DocumentFile imageFile) {
        Intent intent = new Intent(this, ImageDetailsActivity.class);
        intent.putExtra("imageUri", imageFile.getUri().toString());
        startActivity(intent);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_FOLDER && resultCode == Activity.RESULT_OK) {
            if (data != null && data.getData() != null) {
                try {
                    Uri folderUri = data.getData();
                    // Take persistable URI permission for future access
                    int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
                    getContentResolver().takePersistableUriPermission(folderUri, takeFlags);
                    loadImagesFromFolder(folderUri);
                } catch (Exception e) {
                    Toast.makeText(this, "Error accessing folder: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_STORAGE_PERMISSION || requestCode == REQUEST_MEDIA_IMAGES) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openFolderPicker();
            } else {
                Toast.makeText(this, "Storage access permission required", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        if (currentFolderUri != null) {
            loadImagesFromFolder(currentFolderUri);
        }
    }
} 