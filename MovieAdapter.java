package com.dz.gamezone;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.ViewHolder> {

    private List<Movie> movieList;
    private Context context;
    private OnMovieClickListener listener;

    public interface OnMovieClickListener {
        void onMovieClick(Movie movie);
    }

    public MovieAdapter(Context context, List<Movie> movieList, OnMovieClickListener listener) {
        this.context = context;
        this.movieList = movieList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_channel, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Movie movie = movieList.get(position);

        holder.title.setText(movie.name);

        // 🟢 SHOW BADGE LOGIC
        if (movie.badge != null && !movie.badge.isEmpty()) {
            holder.badge.setText(movie.badge);
            holder.badge.setVisibility(View.VISIBLE);
        } else {
            holder.badge.setVisibility(View.GONE);
        }

        Glide.with(context)
                .load(movie.logo)
                .centerCrop()
                .placeholder(R.mipmap.ic_launcher)
                .into(holder.poster);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onMovieClick(movie);
        });
    }

    @Override
    public int getItemCount() { return movieList.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView poster;
        TextView title;
        TextView badge; // 🟢 New View

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            poster = itemView.findViewById(R.id.img_channel);
            title = itemView.findViewById(R.id.tv_channel_name);
            badge = itemView.findViewById(R.id.tv_badge); // 🟢 Match XML ID
        }
    }
}