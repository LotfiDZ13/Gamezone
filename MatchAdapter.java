package com.dz.gamezone;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class MatchAdapter extends RecyclerView.Adapter<MatchAdapter.ViewHolder> {
    private List<Match> matchList;
    private Context context;

    public MatchAdapter(List<Match> matchList, Context context) {
        this.matchList = matchList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_match, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Match match = matchList.get(position);

        holder.txtTeams.setText(match.homeTeam + " vs " + match.awayTeam);
        holder.txtLeague.setText(match.league);

        // Load Logos
        Glide.with(context).load(match.homeLogo).into(holder.imgHome);
        Glide.with(context).load(match.awayLogo).into(holder.imgAway);

        // 🟢 SMART STATUS LOGIC
        if ("ENDED".equalsIgnoreCase(match.status)) {
            // Match Ended
            holder.txtTime.setText("ENDED");
            holder.txtTime.setTextColor(Color.GRAY);
        }
        else if ("LIVE".equalsIgnoreCase(match.status)) {
            // Match is Live
            holder.txtTime.setText("● LIVE");
            holder.txtTime.setTextColor(Color.RED);
        }
        else {
            // Match Upcoming - Calculate Countdown
            try {
                // 1. Parse Time (Assuming Algiers Time from Server)
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                sdf.setTimeZone(TimeZone.getTimeZone("Africa/Algiers"));
                Date dateObj = sdf.parse(match.time);

                // 2. Set Calendar to Today + Match Time
                Calendar matchCal = Calendar.getInstance(TimeZone.getTimeZone("Africa/Algiers"));
                matchCal.setTime(dateObj);

                Calendar now = Calendar.getInstance(TimeZone.getTimeZone("Africa/Algiers"));
                matchCal.set(Calendar.YEAR, now.get(Calendar.YEAR));
                matchCal.set(Calendar.MONTH, now.get(Calendar.MONTH));
                matchCal.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH));

                // 3. Calculate Difference
                long diff = matchCal.getTimeInMillis() - System.currentTimeMillis();

                if (diff > 0) {
                    long hours = diff / (1000 * 60 * 60);
                    long mins = (diff / (1000 * 60)) % 60;

                    if (hours > 0) {
                        holder.txtTime.setText("Live in " + hours + "h " + mins + "m");
                    } else {
                        holder.txtTime.setText("Live in " + mins + "m");
                    }
                    holder.txtTime.setTextColor(Color.parseColor("#4CAF50")); // Green
                } else {
                    // Time passed but API not LIVE yet (Starting Soon)
                    holder.txtTime.setText("Starting...");
                    holder.txtTime.setTextColor(Color.parseColor("#FFC107")); // Amber
                }

            } catch (Exception e) {
                // Fallback if parsing fails
                holder.txtTime.setText(match.time);
                holder.txtTime.setTextColor(Color.parseColor("#FFC107"));
            }
        }

        // On Click -> Open Player
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PlayerActivity.class);
            com.google.gson.Gson gson = new com.google.gson.Gson();
            String serversJson = gson.toJson(match.servers);
            intent.putExtra("servers_json", serversJson);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() { return matchList.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtTeams, txtTime, txtLeague;
        ImageView imgHome, imgAway;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTeams = itemView.findViewById(R.id.txt_teams);
            txtTime = itemView.findViewById(R.id.txt_time);
            txtLeague = itemView.findViewById(R.id.txt_league);
            imgHome = itemView.findViewById(R.id.img_home);
            imgAway = itemView.findViewById(R.id.img_away);
        }
    }
}