package fhu.unmpathway;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import static fhu.unmpathway.SchedulesFrag.deleteRow;

public class CustomAdapter1 extends BaseAdapter
{
    ArrayList<String> titles;
    Context context;
    private static LayoutInflater inflater = null;

    public CustomAdapter1(Context context, ArrayList<String> prgmNameList)
    {
//        super(context, R.layout.custom_row_1, prgmNameList);
        // TODO Auto-generated constructor stub
        titles = prgmNameList;
        this.context = context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public int getCount()
    {
        return titles.size();
    }

    @Override
    public Object getItem(int i)
    {
        return null;
    }


    @Override
    public long getItemId(int i)
    {
        return 0;
    }
//
//    public class Holder
//    {
//        TextView tv;
//        ImageView img;
//    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent)
    {
//        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View customView = inflater.inflate(R.layout.custom_row_1, parent, false);
        final Button delete = customView.findViewById(R.id.delete_schedule);
        delete.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                deleteRow(position);
            }
        });
        final Button edit = customView.findViewById(R.id.edit_schedule);
        edit.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                Intent intent = new Intent(context,SchedulesActivity.class);
                //based on item add info to intent
                intent.putExtra("entry",position);
                context.startActivity(intent);
            }
        });
        TextView title = customView.findViewById(R.id.schedule_title);
        title.setText(titles.get(position));

//        Holder holder = new Holder();
//        View rowView;
//        rowView = inflater.inflate(R.layout.custom_row_1, null);
//        holder.tv = (TextView) rowView.findViewById(R.id.schedule_title);
//        holder.tv.setText(titles.get(position));
        customView.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // TODO Auto-generated method stub
                Toast.makeText(context, "You Clicked " + titles.get(position), Toast.LENGTH_LONG).show();
            }
        });
        return customView;

    }

}