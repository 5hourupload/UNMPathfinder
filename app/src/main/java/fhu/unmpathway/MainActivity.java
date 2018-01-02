package fhu.unmpathway;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.util.Base64;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener
{

    static boolean focusRequired = false;
    static float eventGetX = 0;
    static float eventGetY = 0;
    static float trueX = 0;

    static float botRightX = 2927;
    static float botRightY = 2495;
    //35.081051, -106.613288
    static double botRightLat = 35.081051;
    static double botRightLon = -106.613288;
    static float topLeftX = 479;
    static float topLeftY = 165;
    //35.090176, -106.625190
    static double topLeftLat = 35.090176;
    static double topLeftLon = -106.625190;

    static ArrayList<String> buildings = new ArrayList<>();
    static ArrayList<Double> lats = new ArrayList<>();
    static ArrayList<Double> lons = new ArrayList<>();
    static ArrayList<Integer> buildingPixelsX = new ArrayList<>();
    static ArrayList<Integer> buildingPixelsY = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener()
//        {
//            @Override
//            public void onClick(View view)
//            {
////                Snackbar.make(view, "Replace with your own action(s)", Snackbar.LENGTH_LONG)
////                        .setAction("Action", null).show();
//                //sliding.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
//                //img.matrix.postScale(3f, 3f);
////                img.setImageMatrix(img.matrix);
//            }
//        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        readFile();
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
            case R.id.elevation:
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


    private void readFile()
    {
        try
        {
            BufferedReader br = new BufferedReader(new
                    InputStreamReader(getAssets().open("buildings_index.txt")));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null)
            {
                buildings.add(line.substring(0, line.indexOf("@")));

                int atSign = line.indexOf("@");
                int comma1 = line.indexOf(",", atSign);
                lats.add(Double.parseDouble(line.substring(atSign + 1, comma1)));
                lons.add(Double.parseDouble(line.substring(comma1 + 1)));
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            String everything = sb.toString();
            convertCoordsToPixels();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void convertCoordsToPixels()
    {
        double pixelDistanceX = botRightX - topLeftX;
        double pixelDistanceY = botRightY - topLeftY;
        double lonDistance = Math.abs(botRightLon - topLeftLon);
        double latDistance = Math.abs(botRightLat - topLeftLat);
        for (int i = 0; i < buildings.size(); i++)
        {
            double lonPer = (lons.get(i) - topLeftLon) / lonDistance;
            double latPer = (topLeftLat - lats.get(i)) / latDistance;
            buildingPixelsX.add((int) ((lonPer * pixelDistanceX) + topLeftX));
            buildingPixelsY.add((int) ((latPer * pixelDistanceY) + topLeftY));
        }

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
