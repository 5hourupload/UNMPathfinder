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
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;

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
    static boolean initializing = true;
    Bitmap bitmap;

    ListView searchListView;
    ArrayList<String> searchArray;
    ArrayAdapter<String> adapter;
    float botRightX = 2927;
    float botRightY = 2495;
    //35.081051, -106.613288
    double botRightLat = 35.081051;
    double botRightLon = -106.613288;
    float topLeftX = 479;
    float topLeftY = 165;
    //35.090176, -106.625190
    double topLeftLat = 35.090176;
    double topLeftLon = -106.625190;


    com.sothree.slidinguppanel.SlidingUpPanelLayout sliding;
    ArrayList<String> buildings = new ArrayList<>();
    ArrayList<Double> lats = new ArrayList<>();
    ArrayList<Double> lons = new ArrayList<>();
    ArrayList<Integer> buildingPixelsX = new ArrayList<>();
    ArrayList<Integer> buildingPixelsY = new ArrayList<>();
    android.support.v7.widget.SearchView searchView;

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
                //img.matrix.postScale(3f, 3f);
//                img.setImageMatrix(img.matrix);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        final RelativeLayout mainLayout = findViewById(R.id.standard_layout);
        sliding = findViewById(R.id.sliding_layout);

        screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
        resetBitmap();
        img = new TouchImageView(getApplicationContext(), minScale, 1);
        img.setImageBitmap(bitmap);
        //img.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mainLayout.addView(img);
        startListener();

        sliding.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);

        searchListView = new ListView(this);
        searchListView.setBackgroundColor(Color.WHITE);
        searchArray = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, searchArray);
        searchListView.setAdapter(adapter);
        mainLayout.addView(searchListView);
        readFile();
        searchListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                // Get the selected item text from ListView
                String selectedItem = (String) parent.getItemAtPosition(position);

                // Display the selected item text on TextView
                focusOnBuildingFromSearch(selectedItem);
            }
        });

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


    private void focusOnBuildingFromSearch(String string)
    {
        int i;
        for (i = 0; i < buildings.size(); i++)
        {
            if (buildings.get(i).contains(string))
            {
                break;
            }
        }

        currentlyFocused = true;

//        img.matrix.postScale(1,1);
//        img.saveScale = 1;
//        img.fixTrans();

        int x = buildingPixelsX.get(i);
        int y = buildingPixelsY.get(i);
        int currentX = (int) img.getImageInfo()[0];
        int currentY = (int) img.getImageInfo()[1];
        float scale = img.getImageInfo()[2];
        final int deltaX = currentX - (int)(x * scale);
        final int deltaY = currentY - (int)(y*scale);
        System.out.println(deltaX);
        System.out.println(deltaY);

        searchView.setQuery("", false);
        searchView.setIconified(true);

        img.matrix.postTranslate(deltaX + (screenWidth/4)*scale, deltaY + (screenHeight/4)*scale);

        img.matrix.postScale(2/scale,2/scale);
        img.saveScale = 2;
        img.fixTrans();
        img.setImageMatrix(img.matrix);
        img.invalidate();
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
        InputStream inputStream = getResources().openRawResource(+R.drawable.map50_cropped_main);
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

        bitmap = bitmapRegionDecoder.decodeRegion(new Rect(0, 0, width, height), options);
        bitmap = convertToMutable(this, bitmap);
    }

    private void setBitmap()
    {

//        BitmapFactory.Options tmpOptions = new BitmapFactory.Options();
//        tmpOptions.inJustDecodeBounds = false;
//        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.map50_cropped_main, tmpOptions);

        InputStream inputStream = getResources().openRawResource(+R.drawable.map50_cropped_main);
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

        bitmap = bitmapRegionDecoder.decodeRegion(new Rect(0, 0, width, height), options);
        bitmap = convertToMutable(this, bitmap);
    }

    private void getActualScreenHeight()
    {
        final RelativeLayout mainLayout = findViewById(R.id.standard_layout);

        img = new TouchImageView(getApplicationContext(), minScale, -1);
        Bitmap bmp = Bitmap.createBitmap(1, 10000, Bitmap.Config.ARGB_8888);
        img.setImageBitmap(bmp);
        mainLayout.addView(img);

        ViewTreeObserver vto = img.getViewTreeObserver();
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener()
        {
            public boolean onPreDraw()
            {
                img.getViewTreeObserver().removeOnPreDrawListener(this);
                screenHeight = img.getMeasuredHeight();
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
        double lonCoordDiff = Math.abs((botRightLon - topLeftLon));
        double latCoordDiff = Math.abs((botRightLat - topLeftLat));
        float xPer = (x - topLeftX) / (botRightX - topLeftX);
        float yPer = (y - topLeftY) / (botRightY - topLeftY);

        double latCoord = topLeftLat - (yPer * latCoordDiff);
        double lonCoord = topLeftLon + (xPer * lonCoordDiff);


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

                float x0 = info[0];
                float y0 = info[1];


                xPer = xPer + (x0 - .25f) / fullScale;
                yPer = yPer + (y0 - .25f) / fullScale;
                xPer = Math.max(xPer, 0);
                yPer = Math.max(yPer, 0);
                xPer = Math.min(xPer, 1);
                yPer = Math.min(yPer, 1);

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
                    searchListView.setVisibility(View.INVISIBLE);
                    //adapter.notifyDataSetChanged();
                    return false;
                }
                searchListView.setVisibility(View.VISIBLE);
                s = s.toUpperCase();
                for (String b : buildings)
                {
                    if (b.contains(s))
                        searchArray.add(b);
                }
                img.fixTrans();
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
