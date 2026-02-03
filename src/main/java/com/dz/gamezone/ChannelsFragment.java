package com.dz.gamezone;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ChannelsFragment extends Fragment {

    private RecyclerView rvChannels;
    private SwipeRefreshLayout swipeRefresh;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        rvChannels = view.findViewById(R.id.recycler_view);

        // Grid Layout (2 columns) is perfect for Categories/Folders
        rvChannels.setLayoutManager(new GridLayoutManager(getContext(), 2));

        swipeRefresh.setOnRefreshListener(this::loadCategories);
        loadCategories();

        return view;
    }

    private void loadCategories() {
        swipeRefresh.setRefreshing(true);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://streamzone.alwaysdata.net/1/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService api = retrofit.create(ApiService.class);

        // --- CHANGED: Now we fetch Categories, not Channels ---
        api.getCategories().enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                swipeRefresh.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    // --- CHANGED: Use CategoryAdapter ---
                    rvChannels.setAdapter(new CategoryAdapter(response.body(), getContext()));
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                swipeRefresh.setRefreshing(false);
                if (getContext() != null)
                    Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}