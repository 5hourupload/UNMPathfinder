package fhu.unmpathway;

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
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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
import android.widget.RelativeLayout;

import java.io.IOException;
import java.io.InputStream;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener
{

    TouchImageView img;
    static boolean newImageRequired = false;
    float minScale;
    static float trueX = 0;
    static float trueY = 0;
    static float fullOrigWidth = 0;
    static float fullOrigHeight = 0;
    static float fullScale = 0;
    static float xPer = 0;
    static float yPer = 0;
    static float screenHeight;
    static boolean initialiazing = true;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Snackbar.make(view, "Replace with your own action(s)", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

//        Bitmap map = (
//                decodeSampledBitmapFromResource(getResources(), R.drawable.clean_campus_map, 100, 100));
//        int mapHeight = map.getHeight();
//        int screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
        //minScale = screenHeight / mapHeight;
//        img = new TouchImageView(this, minScale, 1);
//
//        img.setImageBitmap(map);
//        img.setMaxZoom(50f);
//        img.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//        mainLayout.addView(img);

        final RelativeLayout mainLayout = findViewById(R.id.standard_layout);


        InputStream inputStream = getResources().openRawResource(+R.drawable.clean_campus_map);
        BitmapFactory.Options tmpOptions = new BitmapFactory.Options();
        tmpOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(inputStream, null, tmpOptions);

        int width = tmpOptions.outWidth;
        int height = tmpOptions.outHeight;
        fullOrigWidth = tmpOptions.outWidth;
        fullOrigHeight = tmpOptions.outWidth;

// Crop image:
// Crop a rect with 200 pixel width and height from center of image
        BitmapRegionDecoder bitmapRegionDecoder = null;
        try
        {
            bitmapRegionDecoder = BitmapRegionDecoder.newInstance(inputStream, false);
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
        //getActualScreenHeight();

        xPer = ((float) width - ((float) screenWidth * 1.5f)) / fullOrigWidth;
        yPer = ((float) height - (screenHeight * 1.5f)) / fullOrigHeight;
        System.out.println("xPer" + xPer);


        Bitmap bitmap = bitmapRegionDecoder.decodeRegion(new Rect(width - (screenWidth* 2), height - ((int)screenHeight * 2), width, height),options);

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, screenWidth * 2, (int)screenHeight * 2, false);
        img = new TouchImageView(getApplicationContext(), minScale, 1);
        img.setImageBitmap(scaledBitmap);
        img.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mainLayout.addView(img);
        startListener();

        getActualScreenHeight();

    }
    public static Bitmap createImage(int width, int height, int color) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColor(color);
        canvas.drawRect(0F, 0F, (float) width, (float) height, paint);
        return bitmap;
    }
    private void getActualScreenHeight()
    {
        final RelativeLayout mainLayout = findViewById(R.id.standard_layout);

        img = new TouchImageView(getApplicationContext(), minScale, -1);
        Bitmap bmp = Bitmap.createBitmap(1, 10000, Bitmap.Config.ARGB_8888);
        img.setImageBitmap(bmp);
        mainLayout.addView(img);

        System.out.println("about");
        ViewTreeObserver vto = img.getViewTreeObserver();
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                img.getViewTreeObserver().removeOnPreDrawListener(this);
                screenHeight = img.getMeasuredHeight();
                System.out.println("in tree" + screenHeight);
                initialiazing = false;
                return true;
            }
        });

    }
    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
    private void startListener()
    {
        Thread t = new Thread()
        {
            public void run()
            {
                while (true)
                {
                    while (!newImageRequired)
                    {

                    }
                    System.out.println("passed");
                    try
                    {
                        newImage();
                    } catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                    newImageRequired = false;


                }
            }
        };
        t.start();
    }

    public void newImage() throws IOException
    {

        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                float[] info = img.getImageInfo();
                float scale = img.getScale();
// Get image width and height:
                //InputStream inputStream = context.getAssets().open("R.drawable.clean_campus_map.png");
                InputStream inputStream = getResources().openRawResource(+R.drawable.clean_campus_map);
                BitmapFactory.Options tmpOptions = new BitmapFactory.Options();
                tmpOptions.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(inputStream, null, tmpOptions);
                int width = tmpOptions.outWidth;
                int height = tmpOptions.outHeight;

// Crop image:
// Crop a rect with 200 pixel width and height from center of image
                BitmapRegionDecoder bitmapRegionDecoder = null;
                try
                {
                    bitmapRegionDecoder = BitmapRegionDecoder.newInstance(inputStream, false);
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
                int x0 =(int)((float) width * info[0]);
                int y0 =(int)((float) height * info[1]);
                int x1 =(int)((float) width * info[2]);
                int y1 =(int)((float) height * info[3]);


                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                Bitmap bitmap = bitmapRegionDecoder.decodeRegion(new Rect(x0, y0, x1, y1),options);
                //mImageView.setImageBitmap(bitmap);
                System.out.println(bitmap.getHeight());
                img.setImageBitmap(null);
                img = new TouchImageView(getApplicationContext(), minScale, scale);
                img.setImageBitmap(bitmap);
                img.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                RelativeLayout mainLayout = findViewById(R.id.standard_layout);

                mainLayout.addView(img);

            }
        });


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

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        android.support.v7.widget.SearchView searchView = (android.support.v7.widget.SearchView) menu.findItem(R.id.menu_search).getActionView();
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default

        return true;
    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item)
//    {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings)
//        {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item)
    {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.standard)
        {

            RelativeLayout standard = findViewById(R.id.standard_layout);
            standard.setVisibility(View.VISIBLE);
            RelativeLayout elevation = findViewById(R.id.elevation_layout);
            elevation.setVisibility(View.INVISIBLE);
        }
        else if (id == R.id.elevation)
        {
            RelativeLayout standard = findViewById(R.id.standard_layout);
            standard.setVisibility(View.INVISIBLE);
            RelativeLayout elevation = findViewById(R.id.elevation_layout);
            elevation.setVisibility(View.VISIBLE);
        }
        else if (id == R.id.schedules)
        {
            Intent intent = new Intent(this, SchedulesActivity.class);
            startActivity(intent);
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight)
    {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth)
        {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth)
            {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                         int reqWidth, int reqHeight)
    {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }
}
