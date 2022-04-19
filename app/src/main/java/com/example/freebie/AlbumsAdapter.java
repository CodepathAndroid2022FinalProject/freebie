package com.example.freebie;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.freebie.models.Album;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class AlbumsAdapter extends RecyclerView.Adapter<AlbumsAdapter.ViewHolder> {

    public static final String TAG = "AlbumsAdapter";

    private Context context;
    public ArrayList<Album> albums;

    private RequestOptions requestOptions;
    private Drawable placeholderFigure;

    public AlbumsAdapter(Context context, ArrayList<Album> albums) {
        this.context = context;
        this.albums = albums;

        requestOptions = new RequestOptions();
        requestOptions.transform(new CenterCrop(), new RoundedCorners(32));

        Resources res = context.getResources();
        placeholderFigure = ResourcesCompat.getDrawable(res, android.R.drawable.ic_menu_gallery, null);
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_album, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Album album = albums.get(position);
        holder.bind(album);
    }

    @Override
    public int getItemCount() { return albums.size(); }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvAlbumTitle;
        private TextView tvAlbumArtist;
        private ImageView ivAlbum;
        private LinearLayout btnAlbum;

        public ViewHolder (@NonNull @NotNull View itemView) {
            super(itemView);
            tvAlbumTitle = itemView.findViewById(R.id.tvAlbumTitle);
            tvAlbumArtist = itemView.findViewById(R.id.tvAlbumArtist);
            ivAlbum = itemView.findViewById(R.id.ivAlbum);
            btnAlbum = itemView.findViewById(R.id.btnAlbum);
        }

        public void bind(Album album) {
            tvAlbumTitle.setText(album.getTitle());
            tvAlbumArtist.setText(album.getArtist());

            btnAlbum.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.i(TAG, "Album " + album.getTitle() + " was clicked!");
                }
            });

            Glide.with(context)
                    .load(album.getAlbumArt())
                    .apply(requestOptions)
                    .placeholder(placeholderFigure)
                    .error(placeholderFigure)
                    .into(ivAlbum);
        }
    }

    // Clean all elements of the recycler
    public void clear() {
        albums.clear();
        notifyDataSetChanged();
    }

    // Add a list of items
    public void addAll(List<Album> albums) {
        this.albums.addAll(albums);
        notifyDataSetChanged();
    }

    public void add(Album album) { this.albums.add(album); }
}


















































