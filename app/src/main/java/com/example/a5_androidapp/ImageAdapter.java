package com.example.a5_androidapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
    private final List<File> imageFiles;
    private final OnImageClickListener listener;

    public interface OnImageClickListener {
        void onImageClick(File imageFile);
    }

    public ImageAdapter(List<File> imageFiles, OnImageClickListener listener) {
        this.imageFiles = imageFiles;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        File imageFile = imageFiles.get(position);
        Glide.with(holder.itemView.getContext())
                .load(imageFile)
                .centerCrop()
                .into(holder.imageView);

        holder.itemView.setOnClickListener(v -> listener.onImageClick(imageFile));
    }

    @Override
    public int getItemCount() {
        return imageFiles.size();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        ImageViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageThumbnail);
        }
    }
} 