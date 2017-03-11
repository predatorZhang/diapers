package com.worldlink.locker.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.worldlink.locker.R;

/**
 * Created by Stevens on 08/03/2017.
 */

public class SearchActivity extends Activity {

    private static final String LOG_TAG = "SearchActivity";

    private ListView lv_city;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_search);

//        ActionBar actionBar = getActionBar();
//        actionBar.setTitle(R.string.actionbar_citylist);
//        actionBar.show();
        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Toast.makeText(this, "search query is " + query, Toast.LENGTH_SHORT).show();
            Log.d(LOG_TAG, "search query is " + query);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.array_city));
        lv_city = (ListView) findViewById(R.id.lv_city);
        lv_city.setAdapter(adapter);
        lv_city.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                finish();
            }
        });
    }
}
