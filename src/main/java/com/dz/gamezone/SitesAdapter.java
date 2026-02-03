package com.dz.gamezone;

import android.app.AlertDialog;
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

public class SitesAdapter extends RecyclerView.Adapter<SitesAdapter.ViewHolder> {

    private List<Channel> list;
    private Context context;

    public SitesAdapter(List<Channel> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_channel, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Channel site = list.get(position);
        holder.name.setText(site.name);
        Glide.with(context).load(site.logo).into(holder.logo);

        holder.itemView.setOnClickListener(v -> {

            // Handle History
            if (site.name.contains("History")) {
                Intent intent = new Intent(context, HistoryActivity.class);
                context.startActivity(intent);
            }
            // Handle TopCinema Dialog
            else if (site.name.equalsIgnoreCase("TopCinema")) {
                showTopCinemaDialog(); // Ensure this method exists in your Adapter
            }
            // Handle Others
            else {
                Intent intent = new Intent(context, BrowserActivity.class);
                if (site.url != null && !site.url.isEmpty()) {
                    intent.putExtra("site_url", site.url);
                }
                context.startActivity(intent);
            }
        });
    }

    // 🟢 القائمة المنبثقة باختيار الأقسام
    private void showTopCinemaDialog() {
        String[] options = {"المضاف حديثا (Recent)", "افلام (Movies)", "مسلسلات (Series)"};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose Category");
        builder.setItems(options, (dialog, which) -> {
            String url = "";
            String mode = "";

            switch (which) {
                case 0: // Recent
                    url = "https://topcinema.rip/recent/";
                    mode = "recent";
                    break;
                case 1: // Movies
                    url = "https://topcinema.rip/movies/";
                    mode = "movies";
                    break;
                case 2: // Series
                    // 🟢 FIX: Use Arabic text directly. OkHttp will encode it safely once.
                    url = "https://topcinema.rip/category/مسلسلات-اجنبي/";
                    mode = "series";
                    break;
            }

            // Note: We use 'list' action for Movies/Series, but 'recent' for Recent if using my previous split scraper.
            // If using the Universal Scraper I sent last, 'list' works for everything.
            // Let's assume you are using the 'Universal Scraper' I sent in the previous message.
            String action = (mode.equals("recent")) ? "recent" : "list";

            String apiLink = "scraper_topcinema.php?action=" + action + "&url=" + url;

            Intent intent = new Intent(context, BrowserActivity.class);
            intent.putExtra("site_url", apiLink);
            intent.putExtra("mode", mode);
            context.startActivity(intent);
        });
        builder.show();
    }

    @Override
    public int getItemCount() { return list.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView logo;
        TextView name;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            logo = itemView.findViewById(R.id.img_channel);
            name = itemView.findViewById(R.id.tv_channel_name);
        }
    }
}