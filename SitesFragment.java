package com.dz.gamezone;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class SitesFragment extends Fragment {
    private RecyclerView rv;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        rv = view.findViewById(R.id.recycler_view);
        rv.setLayoutManager(new GridLayoutManager(getContext(), 2));

        loadSites();

        return view;
    }

    private void loadSites() {
        List<Channel> sites = new ArrayList<>();

        // 1. HISTORY BUTTON
        Channel history = new Channel();
        history.name = "History / Continue Watching";
        history.logo = "https://ui-avatars.com/api/?name=History&background=333333&color=ffffff&size=200&font-size=0.4";
        history.category = "System";
        sites.add(history);

        // 2. TOP CINEMA
        Channel topC = new Channel();
        topC.name = "TopCinema";
        topC.logo = "https://ui-avatars.com/api/?name=TC&background=000000&color=e50914&size=200";
        topC.category = "Movies";
        topC.url = ""; // Handled in Adapter
        sites.add(topC);

        // 3. FASEL HD
        Channel fasel = new Channel();
        fasel.name = "FaselHD";
        fasel.logo = "https://ui-avatars.com/api/?name=Fasel+HD&background=ffcc00&color=000000&size=200";
        fasel.category = "Movies";
        fasel.url = "https://streamzone.alwaysdata.net/1/scraper_fasel.php?action=list";
        sites.add(fasel);

        // Set Adapter
        if (getContext() != null) {
            rv.setAdapter(new SitesAdapter(sites, getContext()));
        }
    }
}