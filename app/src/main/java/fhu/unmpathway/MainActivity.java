package fhu.unmpathway;

import android.annotation.TargetApi;
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
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener
{

    TouchImageView img;
    static boolean newImageRequired = false;
    static boolean focusRequired = false;
    boolean currentlyFocused = false;
    static float eventGetX = 0;
    static float eventGetY = 0;
    float minScale;
    static float trueX = 0;
    static float trueY = 0;
    static float fullOrigWidth = 0;
    static float fullOrigHeight = 0;
    static float fullScale = 0;
    static float xPer = 0;
    static float yPer = 0;
    static float screenWidth;
    static float screenHeight;
    static boolean initialiazing = true;
    Bitmap bitmap;

    ListView searchListView;
    ArrayList<String> searchArray;
    ArrayAdapter<String> adapter;


    com.sothree.slidinguppanel.SlidingUpPanelLayout sliding;


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
//                Snackbar.make(view, "Replace with your own action(s)", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                //sliding.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                img.matrix.postScale(3f, 3f);
                img.setImageMatrix(img.matrix);
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                    }
                });
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
        sliding = findViewById(R.id.sliding_layout);

        //sliding.setTouchEnabled(false);

        //sliding.setPanelHeight(500);
//        InputStream inputStream = getResources().openRawResource(+R.drawable.map50);
//        BitmapFactory.Options tmpOptions = new BitmapFactory.Options();
//        tmpOptions.inJustDecodeBounds = true;
//        tmpOptions.inMutable = true;
//        BitmapFactory.decodeStream(inputStream, null, tmpOptions);
//
//        int width = tmpOptions.outWidth;
//        int height = tmpOptions.outHeight;
//        fullOrigWidth = tmpOptions.outWidth;
//        fullOrigHeight = tmpOptions.outWidth;

// Crop image:
// Crop a rect with 200 pixel width and height from center of image
//        BitmapRegionDecoder bitmapRegionDecoder = null;
//        try
//        {
//            bitmapRegionDecoder = BitmapRegionDecoder.newInstance(inputStream, false);
//        } catch (IOException e)
//        {
//            e.printStackTrace();
//        }
//
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inPreferredConfig = Bitmap.Config.RGB_565;
//        options.inMutable = true;
        screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
        //getActualScreenHeight();
resetBitmap();
        //xPer = ((float) width - ((float) screenWidth * 1.5f)) / fullOrigWidth;
        //yPer = (height - screenHeight) / (height *2) ;

//        bitmap = bitmapRegionDecoder.decodeRegion(new Rect((int) ((double) width * .3), 0, width, height), options);
        //bitmap = convertToMutable(this, bitmap);
        //Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, screenWidth * 2, (int)screenHeight * 2, false);
        img = new TouchImageView(getApplicationContext(), minScale, 1);
        img.setImageBitmap(bitmap);
        //img.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mainLayout.addView(img);
        //sliding.addView(img);
        startListener();
        //fullScale = .5f;

        sliding.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);

        searchListView = new ListView(this);
        searchListView.setBackgroundColor(Color.WHITE);
//        ListView listView = (ListView) findViewById(R.id.searchListView);

        searchArray = new ArrayList<>();
        searchArray.add("adfadsfafd");
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, searchArray);
        searchListView.setAdapter(adapter);
        mainLayout.addView(searchListView);

    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public static Bitmap convertToMutable(final Context context, final Bitmap imgIn)
    {
        final int width = imgIn.getWidth(), height = imgIn.getHeight();
        final Bitmap.Config type = imgIn.getConfig();
        File outputFile = null;
        final File outputDir = context.getCacheDir();
        try
        {
            outputFile = File.createTempFile(Long.toString(System.currentTimeMillis()), null, outputDir);
            outputFile.deleteOnExit();
            final RandomAccessFile randomAccessFile = new RandomAccessFile(outputFile, "rw");
            final FileChannel channel = randomAccessFile.getChannel();
            final MappedByteBuffer map = channel.map(FileChannel.MapMode.READ_WRITE, 0, imgIn.getRowBytes() * height);
            imgIn.copyPixelsToBuffer(map);
            imgIn.recycle();
            final Bitmap result = Bitmap.createBitmap(width, height, type);
            map.position(0);
            result.copyPixelsFromBuffer(map);
            channel.close();
            randomAccessFile.close();
            outputFile.delete();
            return result;
        } catch (final Exception e)
        {
        } finally
        {
            if (outputFile != null)
                outputFile.delete();
        }
        return null;
    }

    private void resetBitmap()
    {
        InputStream inputStream = getResources().openRawResource(+R.drawable.map50);
        BitmapFactory.Options tmpOptions = new BitmapFactory.Options();
        tmpOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(inputStream, null, tmpOptions);

        int width = tmpOptions.outWidth;
        int height = tmpOptions.outHeight;
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
        //screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        //screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;

        bitmap = bitmapRegionDecoder.decodeRegion(new Rect((int) ((double) width * .3), 0, width, height), options);
        bitmap = convertToMutable(this, bitmap);
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
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener()
        {
            public boolean onPreDraw()
            {
                img.getViewTreeObserver().removeOnPreDrawListener(this);
                screenHeight = img.getMeasuredHeight();
                System.out.println("in tree" + screenHeight);
                initialiazing = false;
                return true;
            }
        });

    }

    public int getStatusBarHeight()
    {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0)
        {
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
        Thread t2 = new Thread()
        {
            public void run()
            {
                while (true)
                {
                    while (!focusRequired)
                    {

                    }
                    System.out.println("focus");
                    focusOnBuilding();
                    focusRequired = false;
                }
            }
        };
        t2.start();
    }

    private void focusOnBuilding()
    {

        float[] info = img.getImageInfo();
        float x = eventGetX / info[2] + info[0];
        float y = eventGetY / info[2] + info[1];
        System.out.println(x);
        System.out.println(y);
        if (!currentlyFocused)
        {
            highlightBuilding(x, y);

        }
        else
        {
            resetBitmap();

        }
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                if (!currentlyFocused)
                {
                    sliding.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);

                }
                else
                {
                    img.setImageBitmap(bitmap);

                    sliding.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
                }
                currentlyFocused = !currentlyFocused;


            }
        });
    }

    private void highlightBuilding(float x, float y)
    {
        final float x0 = x;
        final float y0 = y;


        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                //Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

                Canvas canvas = new Canvas(bitmap);
                Paint paint = new Paint();
                paint.setColor(Color.BLUE);
                canvas.drawRect(x0, y0, x0 + 100, y0 + 100, paint);
                //bitmap = mutableBitmap;
                img.setImageBitmap(bitmap);
            }

        });
    }

    public void newImage() throws IOException
    {

        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                float[] info = img.getImageInfo();
                float tempScale = img.getScale();

                System.out.println("new scale" + fullScale);
                float x0 = info[0];
                float y0 = info[1];

                System.out.println("xPer: " + xPer);
                System.out.println("x0: " + x0);
                System.out.println("yPer: " + yPer);
                System.out.println("y0: " + y0);
                xPer = xPer + (x0 - .25f) / fullScale;
                yPer = yPer + (y0 - .25f) / fullScale;
                xPer = Math.max(xPer, 0);
                yPer = Math.max(yPer, 0);
                xPer = Math.min(xPer, 1);
                yPer = Math.min(yPer, 1);

                System.out.println(xPer);
                System.out.println(yPer);

                fullScale *= tempScale;
                float mapScreenSizeWidth = (screenWidth / fullScale);
                float mapScreenSizeHeight = (screenHeight / fullScale);

                InputStream inputStream;

                if (fullScale <= 0.5)
                {
                    inputStream = getResources().openRawResource(+R.drawable.map50);
                    mapScreenSizeWidth /= 2;
                    mapScreenSizeHeight /= 2;
                }
                else
                {
                    inputStream = getResources().openRawResource(+R.drawable.clean_campus_map);
                }


// Get image width and height:
                //InputStream inputStream = context.getAssets().open("R.drawable.clean_campus_map.png");

                BitmapFactory.Options tmpOptions = new BitmapFactory.Options();
                tmpOptions.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(inputStream, null, tmpOptions);
                int width = tmpOptions.outWidth;
                int height = tmpOptions.outHeight;

                BitmapRegionDecoder bitmapRegionDecoder = null;
                try
                {
                    bitmapRegionDecoder = BitmapRegionDecoder.newInstance(inputStream, false);
                } catch (IOException e)
                {
                    e.printStackTrace();
                }

                int left = (int) (xPer * (float) width);
                int top = (int) (yPer * (float) height);
                System.out.println("left: " + left);
                System.out.println("top: " + top);


                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                Bitmap bitmap = bitmapRegionDecoder.decodeRegion(new Rect(left - (int) (mapScreenSizeWidth / 2), top - (int) (mapScreenSizeHeight / 2), left + (int) (3 * mapScreenSizeWidth / 2), top + (int) (3 * mapScreenSizeHeight / 2)), options);
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, (int) screenWidth * 2, (int) screenHeight * 2, false);
                //mImageView.setImageBitmap(bitmap);
                img.setImageBitmap(null);
                img = new TouchImageView(getApplicationContext(), minScale, 1);
                img.setImageBitmap(scaledBitmap);
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

//        // Get the SearchView and set the searchable configuration
//        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
//        android.support.v7.widget.SearchView searchView = (android.support.v7.widget.SearchView) menu.findItem(R.id.menu_search).getActionView();
//        // Assumes current activity is the searchable activity
//        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
//        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default


        MenuItem item = menu.findItem(R.id.menu_search);
        android.support.v7.widget.SearchView searchView = (android.support.v7.widget.SearchView) item.getActionView();
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
                System.out.println("text change");
                searchArray.add("New schedule");
                //searchArray.remove(0);
                adapter.notifyDataSetChanged();
                return false;
            }
        });
        return true;
    }


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
