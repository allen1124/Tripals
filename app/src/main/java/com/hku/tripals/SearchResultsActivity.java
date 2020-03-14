package com.hku.tripals;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.algolia.search.saas.AlgoliaException;
import com.algolia.search.saas.Client;
import com.algolia.search.saas.CompletionHandler;
import com.algolia.search.saas.Index;
import com.algolia.search.saas.Query;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hku.tripals.adapter.EventAdapter;
import com.hku.tripals.model.Event;
import com.hku.tripals.model.Tag;
import com.yalantis.filter.adapter.FilterAdapter;
import com.yalantis.filter.listener.FilterListener;
import com.yalantis.filter.widget.Filter;
import com.yalantis.filter.widget.FilterItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.util.TimeUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class SearchResultsActivity extends AppCompatActivity implements FilterListener<Tag> {

    private static final String TAG = "SearchResultsActivity";
    private SearchView searchView;
    private String query;
    private RecyclerView searchResult;
    private LinearLayoutManager eventLayoutManager;
    private EventAdapter eventAdapter;
    private Filter<Tag> filter;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    Client searchClient;
    Index index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
        super.onCreate(savedInstanceState);
        searchClient = new Client(getResources().getString(R.string.search_app_id), getResources().getString(R.string.search_key));
        index = searchClient.getIndex("events");
        setContentView(R.layout.activity_search_results);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        searchResult = findViewById(R.id.event_search_result_recyclerView);
        filter = findViewById(R.id.event_search_filer);
        filter.setAdapter(new Adapter(getTags()));
        filter.setNoSelectedItemText(getString(R.string.all_selected));
        filter.setListener(this);
        filter.build();
        eventLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        searchResult.setLayoutManager(eventLayoutManager);
        eventAdapter = new EventAdapter(this);
        searchResult.setAdapter(eventAdapter);
//        FloatingActionButton fab = findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Create Event", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
        searchView = findViewById(R.id.action_search);
        handleIntent(getIntent());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setIconifiedByDefault(false);
        searchView.setQuery(query, false);
        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            query = intent.getStringExtra(SearchManager.QUERY);
            searchEvent(new Query(query));
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
    }

    private void searchEvent(Query query){
        Log.d(TAG, "searchEvent: Query"+query.getQuery());
        Log.d(TAG, "searchEvent: Filter"+query.getFilters());
        index.searchAsync(query, new CompletionHandler() {
            @Override
            public void requestCompleted(@Nullable JSONObject jsonObject, @Nullable AlgoliaException e) {
                try{
                    JSONArray hits = jsonObject.getJSONArray("hits");
                    List<Event> result = new ArrayList<>();
                    for(int i = 0; i < hits.length(); i++){
                        JSONObject eventJSON = hits.getJSONObject(i);
                        Event event = parseJSON(eventJSON);
                        if(event != null)
                            result.add(event);
                    }
                    eventAdapter.setEventList(result);
                    eventAdapter.notifyDataSetChanged();
                }catch (JSONException err){
                    err.printStackTrace();
                }
            }
        });
    }

    private Event parseJSON(JSONObject json){
        Event event = null;
        Log.d(TAG, "parseJSON: "+json);
        try{
            List<String> interests = new ArrayList<String>();
            List<String> participants = new ArrayList<String>();
            if(json.has("interests") && !json.isNull("interests")) {
                for (int i = 0; i < json.getJSONArray("interests").length(); i++) {
                    interests.add(json.getJSONArray("interests").getString(i));
                }
            }
            if(json.has("participants") && !json.isNull("participants")) {
                for (int i = 0; i < json.getJSONArray("participants").length(); i++) {
                    participants.add(json.getJSONArray("participants").getString(i));
                }
            }
            event = new Event(
                    json.getString("id"),
                    json.getString("host"),
                    json.getString("hostName"),
                    json.getString("hostAvatarUrl"),
                    participants,
                    json.getString("title"),
                    json.getString("description"),
                    json.getString("privacy"),
                    json.getString("openness"),
                    json.getInt("quota"),
                    new Date(json.getJSONObject("datetime").getLong("_seconds")*1000+ TimeUnit.NANOSECONDS.toMillis(json.getJSONObject("datetime").getInt("_nanoseconds"))),
                    json.getString("location"),
                    json.getString("locationName"),
                    json.getString("photoUrl"),
                    interests,
                    new Date(json.getJSONObject("timestamp").getLong("_seconds")*1000+ TimeUnit.NANOSECONDS.toMillis(json.getJSONObject("timestamp").getInt("_nanoseconds")))
            );
        }catch (JSONException err){
            err.printStackTrace();
        }
        return event;
    }

    private List<Tag> getTags() {
        List<Tag> tags = new ArrayList<>();
        String[] listItems = getResources().getStringArray(R.array.interest_options);
        for(int i = 0; i < listItems.length; i++){
            tags.add(new Tag(listItems[i]));
        }
        return tags;
    }

    @Override
    public void onFilterDeselected(Tag tag) {

    }

    @Override
    public void onFilterSelected(Tag tag) {
        Log.d(TAG, "onFiltersSelected: "+query+" filter:"+tag.getText());
        Query q = new Query(query).setFilters("interests:" + tag.getText());
        searchEvent(q);
    }

    @Override
    public void onFiltersSelected(@NotNull ArrayList<Tag> arrayList) {
        String filter = "";
        for(int i = 0; i < arrayList.size(); i++){
            if(i != arrayList.size()-1) {
                filter += "interests:" + arrayList.get(i).getText() + " AND ";
            }else{
                filter += "interests:" + arrayList.get(i).getText();
            }
        }
        Log.d(TAG, "onFiltersSelected: "+query+" filter:"+filter);
        Query q = new Query(query).setFilters(filter);
        searchEvent(q);
    }

    @Override
    public void onNothingSelected() {
        searchEvent(new Query(query));
    }

    class Adapter extends FilterAdapter<Tag> {

        Adapter(@NotNull List<? extends Tag> items) {
            super(items);
        }

        @NotNull
        @Override
        public FilterItem createView(int position, Tag item) {
            FilterItem filterItem = new FilterItem(SearchResultsActivity.this);

            filterItem.setStrokeColor(getResources().getColor(R.color.colorPrimaryDark));
            filterItem.setTextColor(getResources().getColor(R.color.places_text_black_alpha_87));
            filterItem.setCornerRadius(14);
            filterItem.setCheckedTextColor(ContextCompat.getColor(SearchResultsActivity.this, android.R.color.white));
            filterItem.setColor(ContextCompat.getColor(SearchResultsActivity.this, android.R.color.white));
            filterItem.setCheckedColor(getResources().getColor(R.color.colorPrimaryDark));
            filterItem.setText(item.getText());
            filterItem.deselect();

            return filterItem;
        }
    }
}