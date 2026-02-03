package com.dz.gamezone;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {
    private List<Category> list;
    private Context context;

    public CategoryAdapter(List<Category> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_channel, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category c = list.get(position);
        holder.name.setText(c.name);
        Glide.with(context).load(c.logo).circleCrop().into(holder.logo);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChannelListActivity.class);
            intent.putExtra("category_name", c.name);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() { return list.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView logo;
        TextView name;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // 🟢 FIX: Updated IDs to match item_channel.xml
            logo = itemView.findViewById(R.id.img_channel);
            name = itemView.findViewById(R.id.tv_channel_name);
        }
    }
}