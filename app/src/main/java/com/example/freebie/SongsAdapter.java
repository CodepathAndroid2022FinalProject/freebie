package com.example.freebie;

import static com.example.freebie.MainActivity.currentlyPlayingSong;
import static com.example.freebie.MainActivity.mediaPlayer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.freebie.models.Song;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.ViewHolder> {

    private Context context;
    private List<Song> songs;

    public SongsAdapter(Context context, List<Song> songs) {
        this.context = context;
        this.songs = songs;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_song, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
        Song song = songs.get(position);
        holder.bind(song);
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvTitle;
        private ImageView ivAlbum;
        private TextView tvArtist;
        private TextView tvSongLength;
        private RelativeLayout btnSong;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvSongTitle);
            ivAlbum = itemView.findViewById(R.id.ivAlbum);
            tvArtist = itemView.findViewById(R.id.tvArtist);
            tvSongLength = itemView.findViewById(R.id.tvSongLength);
            btnSong = itemView.findViewById(R.id.btnSong);
        }

        public void bind(Song song) {
            tvTitle.setText(song.getTitle());
            tvArtist.setText(song.getArtist());
            tvSongLength.setText(song.getLength());

            btnSong.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        // Reset the song that the media player is referencing
                        mediaPlayer.reset();

                        // Set the correct song path and start the player
                        mediaPlayer.setDataSource(song.getPath());
                        mediaPlayer.prepare();
                        mediaPlayer.start();

                        // Track the currently playing song globally
                        currentlyPlayingSong = song;
                    } catch (Exception e) { e.printStackTrace(); }
                }
            });

            RequestOptions requestOptions = new RequestOptions();
            requestOptions = requestOptions.transform(new RoundedCorners(32));

            Glide.with(context)
                    .load(song.getAlbumArt())
                    .apply(requestOptions)
                    .into(ivAlbum);
        }
    }
    // Clean all elements of the recycler
    public void clear() {
        songs.clear();
        notifyDataSetChanged();
    }

    // Add a list of items -- change to type used
    public void addAll(List<Song> songs) {
        this.songs.addAll(songs);
        notifyDataSetChanged();
    }
}