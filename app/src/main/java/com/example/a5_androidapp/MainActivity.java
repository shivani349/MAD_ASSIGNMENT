package com.example.a5_androidapp;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int REQUEST_IMAGE_CAPTURE = 101;
    private static final int REQUEST_STORAGE_PERMISSION = 102;
    private static final int REQUEST_PICK_SAVE_FOLDER = 103;
    private static final int REQUEST_MEDIA_IMAGES = 104;
    
    private String currentPhotoPath;
    private RecyclerView imageGrid;
    private ImageAdapter imageAdapter;
    private List<File> imageFiles;
    private Uri currentSaveFolderUri;
    private boolean shouldSaveToCustomFolder = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Load saved folder URI if exists
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String savedFolderUri = prefs.getString("saveFolderUri", null);
        if (savedFolderUri != null) {
            currentSaveFolderUri = Uri.parse(savedFolderUri);
            shouldSaveToCustomFolder = true;
        }
        
        imageGrid = findViewById(R.id.imageGrid);
        imageGrid.setLayoutManager(new GridLayoutManager(this, 3));
        imageFiles = new ArrayList<>();
        imageAdapter = new ImageAdapter(imageFiles, this::onImageClick);
        imageGrid.setAdapter(imageAdapter);
        
        FloatingActionButton fab = findViewById(R.id.fabTakePhoto);
        fab.setOnClickListener(v -> checkPermissionsAndTakePhoto());
        
        Button galleryButton = findViewById(R.id.galleryButton);
        galleryButton.setOnClickListener(v -> startGalleryActivity());
        
        Button chooseFolderButton = findViewById(R.id.chooseFolderButton);
        chooseFolderButton.setOnClickListener(v -> chooseSaveFolder());
        
        loadImages();
    }
    
    private void startGalleryActivity() {
        Intent intent = new Intent(this, GalleryActivity.class);
        startActivity(intent);
    }
    
    private void chooseSaveFolder() {
        // For Android 10+ (API 29+), we don't need WRITE_EXTERNAL_STORAGE permission to use ACTION_OPEN_DOCUMENT_TREE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            openSaveFolderPicker();
        } else {
            // For Android 9 and below, we need to check for WRITE_EXTERNAL_STORAGE permission
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_STORAGE_PERMISSION);
            } else {
                openSaveFolderPicker();
            }
        }
    }
    
    private void openSaveFolderPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        try {
            startActivityForResult(intent, REQUEST_PICK_SAVE_FOLDER);
        } catch (Exception e) {
            Toast.makeText(this, "Unable to open folder picker: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private void checkPermissionsAndTakePhoto() {
        boolean hasPermissions = false;
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+: We need CAMERA permission only
            hasPermissions = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED;
            
            if (!hasPermissions) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        REQUEST_CAMERA_PERMISSION);
                return;
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10-12: We need CAMERA permission only
            hasPermissions = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED;
            
            if (!hasPermissions) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        REQUEST_CAMERA_PERMISSION);
                return;
            }
        } else {
            // Android 9 and below: We need both CAMERA and WRITE_EXTERNAL_STORAGE
            hasPermissions = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
            
            if (!hasPermissions) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_CAMERA_PERMISSION);
                return;
            }
        }
        
        dispatchTakePictureIntent();
    }
    
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(this, "Error creating image file", Toast.LENGTH_SHORT).show();
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.a5_androidapp.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }
    
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }
    
    private void loadImages() {
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (storageDir != null && storageDir.exists()) {
            File[] files = storageDir.listFiles();
            if (files != null) {
                imageFiles.clear();
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".jpg")) {
                        imageFiles.add(file);
                    }
                }
                imageAdapter.notifyDataSetChanged();
            }
        }
    }
    
    private void savePhotoToCustomFolder(File sourceFile) {
        if (currentSaveFolderUri != null) {
            DocumentFile folder = DocumentFile.fromTreeUri(this, currentSaveFolderUri);
            if (folder != null && folder.exists()) {
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                String imageFileName = "PHOTO_" + timeStamp + ".jpg";
                
                try {
                    DocumentFile newFile = folder.createFile("image/jpeg", imageFileName);
                    if (newFile != null) {
                        try (InputStream is = getContentResolver().openInputStream(Uri.fromFile(sourceFile));
                             OutputStream os = getContentResolver().openOutputStream(newFile.getUri())) {
                            
                            if (is != null && os != null) {
                                byte[] buffer = new byte[1024];
                                int length;
                                while ((length = is.read(buffer)) > 0) {
                                    os.write(buffer, 0, length);
                                }
                                Toast.makeText(this, "Image saved to custom folder", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                } catch (IOException e) {
                    Toast.makeText(this, "Failed to save image to custom folder: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        }
    }
    
    private void onImageClick(File imageFile) {
        Intent intent = new Intent(this, ImageDetailsActivity.class);
        intent.putExtra("imagePath", imageFile.getAbsolutePath());
        startActivity(intent);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // If we have a custom folder, save the photo there as well
            if (shouldSaveToCustomFolder && currentPhotoPath != null) {
                File sourceFile = new File(currentPhotoPath);
                savePhotoToCustomFolder(sourceFile);
            }
            loadImages();
        } else if (requestCode == REQUEST_PICK_SAVE_FOLDER && resultCode == Activity.RESULT_OK) {
            if (data != null && data.getData() != null) {
                try {
                    currentSaveFolderUri = data.getData();
                    shouldSaveToCustomFolder = true;
                    
                    // Take persistable URI permission for future access
                    final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
                    getContentResolver().takePersistableUriPermission(currentSaveFolderUri, takeFlags);
                    
                    // Save the URI for future use
                    SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                    prefs.edit().putString("saveFolderUri", currentSaveFolderUri.toString()).apply();
                    
                    Toast.makeText(this, "Selected folder for saving photos", Toast.LENGTH_SHORT).show();
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
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else {
                Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openSaveFolderPicker();
            } else {
                Toast.makeText(this, "Storage permission required", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        loadImages();
    }
}