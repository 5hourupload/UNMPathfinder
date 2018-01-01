package fhu.unmpathway;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by alans on 12/31/2017.
 */

public class SchedulesFrag extends Fragment
{
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState)
    {
//        return super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.schedules_frag, container,false);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);




    }
}
