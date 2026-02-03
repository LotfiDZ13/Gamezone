package com.dz.gamezone;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ChannelListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. USE THE NEW LAYOUT
        setContentView(R.layout.activity_channel_list);

        // 2. Setup Back Button & Title
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        TextView titleView = findViewById(R.id.txt_category_title);

        RecyclerView rv = findViewById(R.id.recycler_view);
        rv.setLayoutManager(new GridLayoutManager(this, 2));

        // Get the category name passed from the previous screen
        String categoryName = getIntent().getStringExtra("category_name");

        // Set the title on the screen
        if (categoryName != null) {
            titleView.setText(categoryName);
        }
        // Fetch Channels for this Category
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://streamzone.alwaysdata.net/1/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService api = retrofit.create(ApiService.class);
        api.getChannelsByCat(categoryName).enqueue(new Callback<List<Channel>>() {
            @Override
            public void onResponse(Call<List<Channel>> call, Response<List<Channel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Reuse your existing ChannelAdapter!
                    rv.setAdapter(new ChannelAdapter(response.body(), ChannelListActivity.this));
                }
            }

            @Override
            public void onFailure(Call<List<Channel>> call, Throwable t) {
                Toast.makeText(ChannelListActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}