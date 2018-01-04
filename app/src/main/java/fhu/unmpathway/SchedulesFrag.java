package fhu.unmpathway;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by alans on 12/31/2017.
 */


public class SchedulesFrag extends Fragment
{
    ListView schedules;

    String[] ListElements = new String[]{
            "Android",
            "PHP"};
    List<String> ListElementsArrayList;
    ArrayAdapter<String> adapter;
    int scheduleCount =1;

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


        schedules = (ListView) getView().findViewById(R.id.schedulesListView);
        //I totally forgot that you have to use some adapters to add things to listviews
        ListElementsArrayList = new ArrayList<String>(Arrays.asList(ListElements));

        adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, ListElementsArrayList);

        schedules.setAdapter(adapter);
        FloatingActionButton fab = (FloatingActionButton) getView().findViewById(R.id.add_schedule);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                ListElementsArrayList.add("Schedule " + scheduleCount);
                adapter.notifyDataSetChanged();
                scheduleCount++;
            }
        });

        schedules.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l)
            {
                Intent intent = new Intent(getActivity(),SchedulesActivity.class);
                //based on item add info to intent
                intent.putExtra("entry",position);
                startActivity(intent);
            }
        });
    }
}
