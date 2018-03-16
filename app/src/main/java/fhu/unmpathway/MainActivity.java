package fhu.unmpathway;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        displaySelectedScreen(R.id.standard);


    }

    private void displaySelectedScreen(int id)
    {
        Fragment fragment = null;
        switch (id)
        {
            case R.id.standard:
                fragment = new MapFrag();
                break;
            case R.id.schedules:
                fragment = new SchedulesFrag();
                break;
        }
        if (fragment != null)
        {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.content_main, fragment);
            ft.commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }



    @Override
    public void onBackPressed()
    {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START))
        {
            drawer.closeDrawer(GravityCompat.START);
        }
        else
        {
            super.onBackPressed();
        }
    }





//    @Override
//    public boolean onCreateOptionsMenu(Menu menu)
//    {
//        getMenuInflater().inflate(R.menu.main, menu);
//
//
//        MenuItem item = menu.findItem(R.id.menu_search);
//        searchView = (android.support.v7.widget.SearchView) item.getActionView();
//        searchView.setOnQueryTextListener(new android.support.v7.widget.SearchView.OnQueryTextListener()
//        {
//            @Override
//            public boolean onQueryTextSubmit(String s)
//            {
//
//                return false;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String s)
//            {
//                img.fixTrans();
//
//                searchArray.clear();
//                if (s.equals(""))
//                {
//                    adapter.notifyDataSetChanged();
//                    return false;
//                }
//                s = s.toUpperCase();
//                for (String b : buildings)
//                {
//                    if (b.contains(s))
//                        searchArray.add(b);
//                }
//                adapter.notifyDataSetChanged();
//                return false;
//            }
//        });
//        return true;
//    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        displaySelectedScreen(id);
        return true;
    }

}
