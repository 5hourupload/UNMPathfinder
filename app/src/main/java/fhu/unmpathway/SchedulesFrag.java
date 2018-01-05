package fhu.unmpathway;

import android.app.Activity;
import android.app.Fragment;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static fhu.unmpathway.MainActivity.buildingPixelsX;
import static fhu.unmpathway.MainActivity.buildingPixelsY;
import static fhu.unmpathway.MainActivity.buildings;

/**
 * Created by alans on 12/31/2017.
 */


public class SchedulesFrag extends Fragment
{
    ListView schedules;
    ListView searchListView;
    ArrayList<String> searchArray;

    static ArrayList<String> arrayListString = new ArrayList<>();
    static ArrayList<Integer> buildingNumbers = new ArrayList<>();
    //    ArrayAdapter<String> adapter;
    int scheduleCount = 1;
    static CustomAdapter1 adapter;
    static ArrayAdapter<String> searchAdapter;
    android.support.v7.widget.SearchView searchView;
    static SharedPreferences sharedpreferences;
Activity mActivity;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        System.out.println("attached");
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState)
    {
//        return super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.schedules_frag, container, false);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);

        buildingNumbers.clear();
        arrayListString.clear();
        sharedpreferences = mActivity.getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        loadArray();

        schedules = getView().findViewById(R.id.schedulesListView);
        for (int i = 0; i < buildingNumbers.size(); i++)
        {
            arrayListString.add(buildings.get(buildingNumbers.get(i)));
        }
        adapter = new CustomAdapter1(mActivity, arrayListString);
        schedules.setAdapter(adapter);

        FloatingActionButton fab = getView().findViewById(R.id.add_schedule);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                searchView.setIconified(false);
            }
        });


        searchListView = new ListView(getView().getContext());
        searchListView.setBackgroundColor(Color.WHITE);
        searchArray = new ArrayList<>();
        searchAdapter = new ArrayAdapter<>(mActivity, android.R.layout.simple_list_item_1, searchArray);
        searchListView.setAdapter(searchAdapter);
        RelativeLayout mainLayout = getView().findViewById(R.id.main_schedules_layout);
        mainLayout.addView(searchListView);
        searchListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                String selectedItem = (String) parent.getItemAtPosition(position);
                int i;
                for (i = 0; i < buildings.size(); i++)
                {
                    if (buildings.get(i).contains(selectedItem))
                    {
                        break;
                    }
                }

                arrayListString.add(selectedItem);
                schedules.setAdapter(adapter);
                searchView.setQuery("", false);
                searchView.setIconified(true);

                buildingNumbers.add(i);
                saveArray();
            }
        });

    }

    public static boolean saveArray()
    {
        SharedPreferences.Editor editor = sharedpreferences.edit();

        editor.putInt("Status_size", buildingNumbers.size());

        for (int i = 0; i < buildingNumbers.size(); i++)
        {
            editor.remove("Status_" + i);
            editor.putInt("Status_" + i, buildingNumbers.get(i));
        }

        return editor.commit();
    }

    public void loadArray()
    {
        buildingNumbers.clear();
        int size = sharedpreferences.getInt("Status_size", 0);

        for (int i = 0; i < size; i++)
        {
            buildingNumbers.add(sharedpreferences.getInt("Status_" + i, -1));
        }

    }

    public static void deleteRow(int position)
    {
        arrayListString.remove(position);
        adapter.notifyDataSetChanged();
        buildingNumbers.remove(position);
        saveArray();
//        schedules.setAdapter(adapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        System.out.println(inflater);
        mActivity.getMenuInflater().inflate(R.menu.main, menu);

        MenuItem item = menu.findItem(R.id.menu_search);
        searchView = (android.support.v7.widget.SearchView) item.getActionView();
        searchView.setOnQueryTextListener(new android.support.v7.widget.SearchView.OnQueryTextListener()
        {
            @Override
            public boolean onQueryTextSubmit(String s)
            {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String s)
            {
                searchArray.clear();
                if (s.equals(""))
                {
                    searchAdapter.notifyDataSetChanged();
                    return false;
                }
                s = s.toUpperCase();
                for (String b : buildings)
                {
                    if (b.contains(s))
                        searchArray.add(b);
                }
                searchAdapter.notifyDataSetChanged();
                return false;
            }
        });
    }
}
