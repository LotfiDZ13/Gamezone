package com.dz.gamezone;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MatchesFragment extends Fragment {

    private RecyclerView rvMatches;
    private SwipeRefreshLayout swipeRefresh;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        rvMatches = view.findViewById(R.id.recycler_view);
        rvMatches.setLayoutManager(new LinearLayoutManager(getContext()));

        swipeRefresh.setOnRefreshListener(this::loadMatches);
        loadMatches();

        return view;
    }

    private void loadMatches() {
        swipeRefresh.setRefreshing(true);
        // 🟢 MAKE SURE THIS URL IS YOUR EXACT PHP SERVER URL
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://streamzone.alwaysdata.net/1/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService api = retrofit.create(ApiService.class);
        api.getMatches().enqueue(new Callback<List<Match>>() {
            @Override
            public void onResponse(Call<List<Match>> call, Response<List<Match>> response) {
                swipeRefresh.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<Match> matches = response.body();

                    // 🟢 1. APPLY SORTING HERE
                    sortMatches(matches);

                    rvMatches.setAdapter(new MatchAdapter(matches, getContext()));
                }
            }

            @Override
            public void onFailure(Call<List<Match>> call, Throwable t) {
                swipeRefresh.setRefreshing(false);
                if (getContext() != null)
                    Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 🟢 SMART SORT FUNCTION (Mirrors your Website/Admin Logic)
    private void sortMatches(List<Match> matches) {
        Collections.sort(matches, new Comparator<Match>() {
            @Override
            public int compare(Match m1, Match m2) {
                int p1 = getPriority(m1.league);
                int p2 = getPriority(m2.league);

                // 1. Sort by Priority (Higher Score First)
                if (p1 != p2) return p2 - p1;

                // 2. Sort by Time (Ascending)
                String t1 = m1.time != null ? m1.time : "";
                String t2 = m2.time != null ? m2.time : "";
                return t1.compareTo(t2);
            }
        });
    }

    // 🟢 PRIORITY SYSTEM
    private int getPriority(String leagueName) {
        if (leagueName == null) return 0;
        String name = leagueName.toLowerCase();

        // A. DEMOTION (Lower Tier Leagues go to bottom)
        if (name.contains("2nd") || name.contains("women") || name.contains("u21") || name.contains("reserves") ||
                name.contains("الدرجة الثانية") || name.contains("سيدات") || name.contains("رديف") || name.contains("تحت")) {
            return -100;
        }

        // B. TOP TIER LEAGUES (Higher Number = Higher Position)
        // Champions Leagues
        if (name.contains("champions league") || name.contains("أبطال أوروبا")) return 100;
        if (name.contains("caf") || name.contains("أفريقيا")) return 99;
        if (name.contains("afc") || name.contains("آسيا")) return 98;

        // Top 5 Europe
        if (name.contains("premier league") || name.contains("الإنجليزي")) return 95;
        if (name.contains("laliga") || name.contains("الإسباني")) return 90;
        if (name.contains("serie a") || name.contains("الإيطالي")) return 85;
        if (name.contains("bundesliga") || name.contains("الألماني")) return 80;
        if (name.contains("ligue 1") || name.contains("الفرنسي")) return 75;

        // Arab Leagues (Your Requests)
        if (name.contains("saudi") || name.contains("روشن")) return 70;
        if (name.contains("algeria") || name.contains("الجزائرية")) return 65;
        if (name.contains("egypt") || name.contains("المصري")) return 60;

        // Other Cups
        if (name.contains("europa") || name.contains("الأوروبي")) return 50;
        if (name.contains("conference") || name.contains("المؤتمر")) return 45;
        if (name.contains("cup") || name.contains("كأس")) return 40;

        return 0; // Everything else
    }
}