package fhu.unmpathway;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static android.content.ContentValues.TAG;
import static android.content.Context.PRINT_SERVICE;
import static fhu.unmpathway.MainActivity.DRAW_HEIGHT;
import static fhu.unmpathway.MainActivity.DRAW_WIDTH;
import static fhu.unmpathway.MainActivity.botRightLat;
import static fhu.unmpathway.MainActivity.botRightLon;
import static fhu.unmpathway.MainActivity.botRightX;
import static fhu.unmpathway.MainActivity.botRightY;
import static fhu.unmpathway.MainActivity.buildingPixelsX;
import static fhu.unmpathway.MainActivity.buildingPixelsY;
import static fhu.unmpathway.MainActivity.buildings;
import static fhu.unmpathway.MainActivity.buildingsPath;
import static fhu.unmpathway.MainActivity.eventGetX;
import static fhu.unmpathway.MainActivity.eventGetY;
import static fhu.unmpathway.MainActivity.focusRequired;
import static fhu.unmpathway.MainActivity.lats;
import static fhu.unmpathway.MainActivity.lons;
import static fhu.unmpathway.MainActivity.nodeArray;
import static fhu.unmpathway.MainActivity.topLeftLat;
import static fhu.unmpathway.MainActivity.topLeftLon;
import static fhu.unmpathway.MainActivity.topLeftX;
import static fhu.unmpathway.MainActivity.topLeftY;
import static fhu.unmpathway.MainActivity.visitedPixels;


public class MapFrag extends Fragment
{
    TouchImageView img;
    boolean currentlyFocused = false;
    float screenWidth;
    float screenHeight;
    Bitmap bitmap;
    ListView searchListView;
    ArrayList<String> searchArray;
    ArrayAdapter<String> adapter;
    com.sothree.slidinguppanel.SlidingUpPanelLayout sliding;
    android.support.v7.widget.SearchView searchView;
    static final int REGULAR_SEARCH = 0;
    static final int STARTING_POINT = 1;
    static final int DESTINATION = 2;
    int searchMode = REGULAR_SEARCH;
    TextView fromText;
    TextView toText;

    LinearLayout buildingsCollapse;
    LinearLayout directionsCollapse;
    LinearLayout info_layout;
    LinearLayout directions_layout;
    TextView buildingTitle;
    String startingPoint = "";
    String destination = "";
    int sX = -1;
    int sY = -1;
    int eX = -1;
    int eY = -1;
    Button currentLocationAsStarting;
    boolean displayingPath = false;


    ArrayList<Node> open = new ArrayList<>();
    ArrayList<Node> closed = new ArrayList<>();
    int xDestination;
    int yDestination;
    int xStart;
    int yStart;

    ArrayList<Integer> pixelsX = new ArrayList<>();
    ArrayList<Integer> pixelsY = new ArrayList<>();
    ArrayList<Node> finalPath = new ArrayList<>();

    WebView web;

    LocationListener locationListener;

    static final int REQUEST_LOCATION = 1;
    LocationManager locationManager;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState)
    {
//        return super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.map_frag, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);

        //SET UP MAP
        RelativeLayout mainLayout = getView().findViewById(R.id.standard_layout);
        screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
        resetBitmap();
        img = new TouchImageView(getView().getContext());
        img.setImageBitmap(bitmap);
        mainLayout.addView(img);

        startListener();

        sliding = getView().findViewById(R.id.sliding_layout);
        sliding.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
        sliding.setDragView(R.id.building_collapse);

        searchListView = new ListView(getView().getContext());
        searchListView.setBackgroundColor(Color.WHITE);
        searchArray = new ArrayList<>();
        adapter = new ArrayAdapter<>(getView().getContext(), android.R.layout.simple_list_item_1, searchArray);
        searchListView.setAdapter(adapter);
        mainLayout.addView(searchListView);
        searchListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                String selectedItem = (String) parent.getItemAtPosition(position);

                if (searchMode == REGULAR_SEARCH)
                {
                    focusOnBuildingFromSearch(selectedItem);
                }
                if (searchMode == STARTING_POINT)
                {
                    fromText.setText(selectedItem);
                    searchView.setQuery("", false);
                    searchView.setIconified(true);
                    sliding.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                    currentLocationAsStarting.setVisibility(View.GONE);
                    searchMode = REGULAR_SEARCH;
                    startingPoint = selectedItem;
                    for (int i = 0; i < buildings.size(); i++)
                    {
                        if (selectedItem.equals(buildings.get(i)))
                        {
                            sX = buildingPixelsX.get(i);
                            sY = buildingPixelsY.get(i);
                            break;
                        }
                    }
                }
                if (searchMode == DESTINATION)
                {
                    toText.setText(selectedItem);
                    searchView.setQuery("", false);
                    searchView.setIconified(true);
                    sliding.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);

                    searchMode = REGULAR_SEARCH;
                    for (int i = 0; i < buildings.size(); i++)
                    {
                        if (selectedItem.equals(buildings.get(i)))
                        {
                            eX = buildingPixelsX.get(i);
                            eY = buildingPixelsY.get(i);
                            break;
                        }
                    }
                }
            }
        });

        sliding.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener()
        {
            @Override
            public void onPanelSlide(View panel, float slideOffset)
            {

            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState)
            {
                if (newState == SlidingUpPanelLayout.PanelState.EXPANDED)
                {

                    if (searchMode != REGULAR_SEARCH)
                    {
                        searchMode = REGULAR_SEARCH;
                        currentLocationAsStarting.setVisibility(View.GONE);
                        searchView.setQuery("", false);
                        searchView.setIconified(true);
                    }
                }
            }

        });

        fromText = getView().findViewById(R.id.from_edit);
        fromText.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                searchMode = STARTING_POINT;
                sliding.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                searchView.setQuery("", false);
                searchView.setIconified(false);
                currentLocationAsStarting.setVisibility(View.VISIBLE);
            }
        });
        toText = getView().findViewById(R.id.to_edit);
        toText.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                searchMode = DESTINATION;
                sliding.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);

                searchView.setQuery("", false);
                searchView.setIconified(false);
            }
        });

        Button findPath = getView().findViewById(R.id.find_path);
        findPath.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (sX == -1 || sY == -1 || eX == -1 || eY == -1)
                {
                    Snackbar.make(view, "Please select a starting point and destination", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    return;
                }
                if (sX == eX && sY == eY)
                {
                    Snackbar.make(view, "Starting point and destination are the same", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    return;
                }
                path();
                sliding.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            }
        });
        info_layout = getView().findViewById(R.id.information_layout);
        directions_layout = getView().findViewById(R.id.directions_layout);
        buildingsCollapse = getView().findViewById(R.id.building_collapse);
        directionsCollapse = getView().findViewById(R.id.directions_collapse);
        directions_layout.setVisibility(View.GONE);
        directionsCollapse.setVisibility(View.GONE);

        Button getDirectionsButton = getView().findViewById(R.id.get_directions_button);
        getDirectionsButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                directions_layout.setVisibility(View.VISIBLE);
                info_layout.setVisibility(View.GONE);
                sliding.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                buildingsCollapse.setVisibility(View.GONE);
                directionsCollapse.setVisibility(View.VISIBLE);

                toText.setText(destination);
                fromText.setText("Choose starting point");
                startingPoint = "";
                sX = -1;
                sY = -1;

                resetBitmap();
                getActivity().runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        img.setImageBitmap(bitmap);
                        sliding.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                    }
                });
                currentlyFocused = false;
            }
        });
        Button close = getView().findViewById(R.id.cancel_directions);
        close.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                sliding.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
                searchMode = REGULAR_SEARCH;
                currentLocationAsStarting.setVisibility(View.GONE);
                searchView.setQuery("", false);
                searchView.setIconified(true);
                if (displayingPath)
                {
                    resetBitmap();
                    displayingPath = false;
                }
                getActivity().runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        img.setImageBitmap(bitmap);

                    }
                });
            }
        });
        buildingTitle = getView().findViewById(R.id.building_title);
        currentLocationAsStarting = getView().findViewById(R.id.current_location_as_start);
        currentLocationAsStarting.setVisibility(View.GONE);
        currentLocationAsStarting.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                getLocation();
            }
        });
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        locationListener = new MyLocationListener();
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
        {

            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);

        FloatingActionButton fab = getView().findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                directions_layout.setVisibility(View.VISIBLE);
                info_layout.setVisibility(View.GONE);
                sliding.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                buildingsCollapse.setVisibility(View.GONE);
                directionsCollapse.setVisibility(View.VISIBLE);

                if (currentlyFocused)
                {
                    resetBitmap();
                    getActivity().runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            img.setImageBitmap(bitmap);
                        }
                    });
                    currentlyFocused = false;
                }
            }
        });

        web = getView().findViewById(R.id.webview_content);
        web.getSettings().setJavaScriptEnabled(true);
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
        bitmap = convertToMutable(getView().getContext(), bitmap);
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

    private void startListener()
    {
        Thread t2 = new Thread()
        {
            public void run()
            {
                while (true)
                {
                    while (!focusRequired)
                    {

                    }
                    focusOnBuildingFromClick();
                    focusRequired = false;
                }
            }
        };
        t2.start();
    }

    private void focusOnBuildingFromClick()
    {
        img.fixTrans();

        if (searchMode == REGULAR_SEARCH)
        {
            if (currentlyFocused)
            {
                resetBitmap();
                getActivity().runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        img.setImageBitmap(bitmap);
                        sliding.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
                    }
                });
                currentlyFocused = !currentlyFocused;
                return;
            }
            if (displayingPath)
            {
                return;
            }
            float[] info = img.getImageInfo();
            float x = (eventGetX + info[0]) / info[2];
            float y = (eventGetY + info[1]) / info[2];
            int index = 0;
            double minDistance = 99999;
            for (int i = 0; i < buildingPixelsX.size(); i++)
            {
                double distance = Math.sqrt(Math.pow(x - buildingPixelsX.get(i), 2) + Math.pow(y - buildingPixelsY.get(i), 2));
                if (distance < minDistance)
                {
                    minDistance = distance;
                    index = i;
                }
            }
            if (minDistance > 200)
            {
                return;
            }
            x = buildingPixelsX.get(index);
            y = buildingPixelsY.get(index);
            final String building = buildings.get(index);
            highlightBuilding(x, y);
            updateWebView(building);
            getActivity().runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    sliding.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                    buildingTitle.setText(building);
                    buildingsCollapse.setVisibility(View.VISIBLE);
                    directionsCollapse.setVisibility(View.GONE);
                    info_layout.setVisibility(View.VISIBLE);
                    directions_layout.setVisibility(View.GONE);
                }
            });
            currentlyFocused = true;
            destination = building;
            eX = (int) x;
            eY = (int) y;
        }
        if (searchMode == STARTING_POINT || searchMode == DESTINATION)
        {
            float[] info = img.getImageInfo();
            float x = (eventGetX + info[0]) / info[2];
            float y = (eventGetY + info[1]) / info[2];
            double lonCoordDiff = Math.abs((botRightLon - topLeftLon));
            double latCoordDiff = Math.abs((botRightLat - topLeftLat));
            float xPer = (x - topLeftX) / (botRightX - topLeftX);
            float yPer = (y - topLeftY) / (botRightY - topLeftY);
            double latCoord = topLeftLat - (yPer * latCoordDiff);
            double lonCoord = topLeftLon + (xPer * lonCoordDiff);

            int index = 0;
            double minDistance = 99999;
            for (int i = 0; i < buildingPixelsX.size(); i++)
            {
                double distance = Math.sqrt(Math.pow(x - buildingPixelsX.get(i), 2) + Math.pow(y - buildingPixelsY.get(i), 2));
                if (distance < minDistance)
                {
                    minDistance = distance;
                    index = i;
                }
            }
            String building;
            if (minDistance < 200)
            {
                x = buildingPixelsX.get(index);
                y = buildingPixelsY.get(index);
                building = buildings.get(index);
            }
            else
            {
                building = "@" + latCoord + "," + lonCoord;
            }
            final String title = building;
            if (searchMode == STARTING_POINT)
            {
                startingPoint = title;
                sX = (int) x;
                sY = (int) y;
            }
            if (searchMode == DESTINATION)
            {
                destination = title;
                eX = (int) x;
                eY = (int) y;
            }
            getActivity().runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    if (searchMode == STARTING_POINT)
                    {
                        fromText.setText(title);

                    }
                    if (searchMode == DESTINATION)
                    {
                        toText.setText(title);
                    }
                    sliding.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                    searchView.setQuery("", false);
                    searchView.setIconified(true);
                    searchMode = REGULAR_SEARCH;
                    currentLocationAsStarting.setVisibility(View.GONE);
                }
            });
        }
    }

    private void highlightBuilding(float x, float y)
    {
        final float x0 = x;
        final float y0 = y;


        getActivity().runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                Canvas canvas = new Canvas(bitmap);
                Paint paint = new Paint();
                paint.setColor(Color.RED);
                canvas.drawOval(x0 - 10, y0 - 10, x0 + 10, y0 + 10, paint);
                paint.setAlpha(128);
                canvas.drawOval(x0 - 15, y0 - 15, x0 + 15, y0 + 15, paint);

                img.setImageBitmap(bitmap);
            }

        });
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

        if (currentlyFocused || displayingPath)
        {
            displayingPath = false;
            resetBitmap();
        }
        currentlyFocused = true;

        int x = buildingPixelsX.get(i);
        int y = buildingPixelsY.get(i);
        int currentX = (int) img.getImageInfo()[0];
        int currentY = (int) img.getImageInfo()[1];
        float scale = img.getImageInfo()[2];
        final int deltaX = currentX - (int) (x * scale);
        final int deltaY = currentY - (int) (y * scale);

        searchView.setQuery("", false);
        searchView.setIconified(true);

        highlightBuilding(x, y);

        updateWebView(string);

        img.matrix.postTranslate(deltaX + (screenWidth / 4) * scale, deltaY + (screenHeight / 4) * scale);
        img.matrix.postScale(2 / scale, 2 / scale);
        img.saveScale = 2;
        img.fixTrans();
        img.setImageMatrix(img.matrix);
//        img.invalidate();

        getActivity().runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                sliding.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            }
        });
        buildingsCollapse.setVisibility(View.VISIBLE);
        directionsCollapse.setVisibility(View.GONE);
        info_layout.setVisibility(View.VISIBLE);
        directions_layout.setVisibility(View.GONE);
        buildingTitle.setText(string);
        destination = string;
        eX = x;
        eY = y;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        getActivity().getMenuInflater().inflate(R.menu.main, menu);

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
                if (sliding.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED)
                {
                    sliding.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                }
                img.fixTrans();

                searchArray.clear();
                if (s.equals(""))
                {
//                    searchListView.setVisibility(View.INVISIBLE);
                    adapter.notifyDataSetChanged();
                    return false;
                }
//                searchListView.setVisibility(View.VISIBLE);
                s = s.toUpperCase();
                for (String b : buildings)
                {
                    if (b.contains(s))
                        searchArray.add(b);
                }
                adapter.notifyDataSetChanged();
                return false;
            }
        });
    }

    private void updateWebView(String building)
    {
        String[] terms = building.split(" ");
        //http://search.unm.edu/search/index.html#gsc.tab=0&gsc.q=test1%20test2%20test3&gsc.sort=
        String urlString = "http://search.unm.edu/search/index.html#gsc.tab=0&gsc.q=";
        urlString += terms[0];
        for (int i = 1; i < terms.length; i++)
        {
            urlString = urlString + "%20" + terms[i];
        }
        urlString = urlString + "&gsc.sort=";
        final String finalUrl = urlString;
        getActivity().runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                web.loadUrl(finalUrl);
            }
        });
    }


    void getLocation()
    {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
        {

            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        }
        else
        {
//            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null)
            {
                double latti = location.getLatitude();
                double longi = location.getLongitude();
                int[] gps = convertCoordsToPixels(latti, longi);
                System.out.println(gps[1]);
                System.out.println(gps[0]);
                if (gps[0] > -1 && gps[0] < bitmap.getWidth() && gps[1] > -1 && gps[1] < bitmap.getHeight())
                {
                    fromText.setText("Current Location (@"+latti+", "+longi);
                    sX = gps[1];
                    sY = gps[0];

                    searchMode = REGULAR_SEARCH;
                    sliding.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                    searchView.setQuery("", false);
                    searchView.setIconified(true);
                    currentLocationAsStarting.setVisibility(View.GONE);
                }
                else
                {
                    Snackbar.make(getView(), "Current location is not on UNM central campus", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
            else
            {
                System.out.println("unable to get location");
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode)
        {
            case REQUEST_LOCATION:
                getLocation();
                break;
        }
    }

    private void path()
    {
//        image = new Image("/map25_cropped_main.png");
//        image = BitmapFactory.decodeResource(getView().getContext().getResources(), R.drawable.map25_cropped_main);


        if (displayingPath) resetBitmap();
        findPath(sX / 2, sY / 2, eX / 2, eY / 2);
        drawPath();
        drawStart();
        displayingPath = true;
    }

    private void findPath(int x0, int y0, int x1, int y1)
    {
        resetNodeArray();
        resetVisitedPixels();
        open.clear();
        closed.clear();


        xDestination = x1;
        yDestination = y1;
        xStart = x0;
        yStart = y0;
        System.out.println(x0);
        System.out.println(y0);

        if (buildingsPath[x0][y0])
        {
            int[] values = shift(x0, y0, x1, y1);
            xStart = values[0];
            yStart = values[1];
        }
        if (buildingsPath[x1][y1])
        {
            int[] values = shift(x1, y1, x0, y0);
            xDestination = values[0];
            yDestination = values[1];
        }
        Node start = new Node(xStart, yStart, 0, xDestination, yDestination, -1, -1);
        open.add(start);

        for (int i = 0; i < 500000; i++)
        {
            if (open.get(0).xPosition == xDestination && open.get(0).yPosition == yDestination)
            {
                break;
            }
            analyzeNode(open.get(0));
        }

        finalPath.clear();
        Node current = open.get(0);
        for (int i = 0; i < 100000; i++)
        {
            finalPath.add(current);
            int x = current.xOrigin;
            int y = current.yOrigin;
            for (Node c : closed)
            {
                if (c.xPosition == x && c.yPosition == y)
                {
                    current = c;
                    break;
                }
            }
            if (x == -1)
            {
                break;
            }
        }

    }

    private int[] shift(int x0, int y0, int x1, int y1)
    {
        resetVisitedPixels();
        pixelsX.clear();
        pixelsY.clear();
        int k;
        pixelsX.add(x0);
        pixelsY.add(y0);

        scanBuildings(x0, y0);
        for (k = 1; k < pixelsX.size(); k++)
        {
            scanBuildings(pixelsX.get(k), pixelsY.get(k));
        }
        int index = 0;
        double minDistance = 99999999;
        for (int l = 0; l < pixelsX.size(); l++)
        {
            double distance = Math.sqrt(Math.pow(pixelsX.get(l) - x1, 2) + Math.pow(pixelsY.get(l) - y1, 2));
            if (distance < minDistance)
            {
                minDistance = distance;
                index = l;
            }
        }
        int x = pixelsX.get(index);
        int y = pixelsY.get(index);
        if (!buildingsPath[x + 1][y]) x += 1;
        if (!buildingsPath[x - 1][y]) x -= 1;
        if (!buildingsPath[x][y + 1]) y += 1;
        if (!buildingsPath[x][y - 1]) y -= 1;

        int[] array = {x, y};
        return array;


    }

    private void analyzeNode(Node node)
    {
        int x = node.xPosition;
        int y = node.yPosition;
//        pixelWriter.setColor(x, y, Color.WHITE);
        for (int i = 0; i < 16; i++)
        {
            int x1 = 0;
            int y1 = 0;
            double pD = 1;
            //90degrees
            if (i == 0)
            {
                x1 = -1;
            }
            else if (i == 1)
            {
                x1 = 1;
            }
            else if (i == 2)
            {
                y1 = -1;
            }
            else if (i == 3)
            {
                y1 = 1;
            }
            //45degrees
            else if (i == 4)
            {
                x1 = -1;
                y1 = -1;
                pD = Math.sqrt(2);
            }
            else if (i == 5)
            {
                x1 = 1;
                y1 = -1;
                pD = Math.sqrt(2);
            }
            else if (i == 6)
            {
                x1 = -1;
                y1 = 1;
                pD = Math.sqrt(2);
            }
            else if (i == 7)
            {
                x1 = 1;
                y1 = 1;
                pD = Math.sqrt(2);
            }
            //22.5degrees
            else if (i == 8)
            {
                x1 = 1;
                y1 = -2;
                pD = Math.sqrt(5);
            }
            else if (i == 9)
            {
                x1 = 2;
                y1 = -1;
                pD = Math.sqrt(5);
            }
            else if (i == 10)
            {
                x1 = 2;
                y1 = 1;
                pD = Math.sqrt(5);
            }
            else if (i == 11)
            {
                x1 = 1;
                y1 = 2;
                pD = Math.sqrt(5);
            }
            else if (i == 12)
            {
                x1 = -1;
                y1 = 2;
                pD = Math.sqrt(5);
            }
            else if (i == 13)
            {
                x1 = -2;
                y1 = 1;
                pD = Math.sqrt(5);
            }
            else if (i == 14)
            {
                x1 = -2;
                y1 = -1;
                pD = Math.sqrt(5);
            }
            else
            {
                x1 = -1;
                y1 = -2;
                pD = Math.sqrt(5);
            }

            if (x + x1 >= DRAW_WIDTH || x + x1 < 0)
            {
                continue;
            }
            if (y + y1 >= DRAW_HEIGHT || y + y1 < 0)
            {
                continue;
            }

            if (!buildingsPath[x + x1][y + y1] && !visitedPixels[x + x1][y + y1])
            {
                if (nodeArray[x + x1][y + y1] == null)
                {
                    nodeArray[x + x1][y + y1] = new Node(x + x1, y + y1, node.pathDistance + pD, xDestination, yDestination, x, y);
                    open.add(nodeArray[x + x1][y + y1]);

                }
                else
                {
                    if (nodeArray[x + x1][y + y1].pathDistance > node.pathDistance + pD)
                    {
                        nodeArray[x + x1][y + y1].pathDistance = node.pathDistance + pD;
                        nodeArray[x + x1][y + y1].xOrigin = x;
                        nodeArray[x + x1][y + y1].yOrigin = y;
                        replaceNode(x + x1, y + y1, nodeArray[x + x1][y + y1]);
                    }
                }
            }
        }
        closed.add(node);
        open.remove(0);
        visitedPixels[node.xPosition][node.yPosition] = true;
        sortOpenNodes();
    }

    private void replaceNode(int x, int y, Node node)
    {
        for (int i = 0; i < open.size(); i++)
        {
            if (open.get(i).xPosition == x && open.get(i).yPosition == y)
            {
                open.remove(i);
                break;
            }
        }
        open.add(node);
    }

    private void sortOpenNodes()
    {
        Collections.sort(open, new Comparator<Node>()
        {
            @Override
            public int compare(Node lhs, Node rhs)
            {
                // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
                return lhs.weight > rhs.weight ? -1 : (lhs.weight < rhs.weight) ? 1 : 0;
            }
        });
        Collections.reverse(open);
    }

    private void drawStart()
    {
//        gtx.setFill(Color.GREEN);
//        gtx.fillOval(xStart - 5, yStart - 5, 10, 10);
//        gtx.setFill(Color.BLUE);
//        gtx.fillOval(xDestination - 5, yDestination - 5, 10, 10);
        getActivity().runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                Canvas canvas = new Canvas(bitmap);
                Paint paint = new Paint();
                paint.setColor(Color.GREEN);
                canvas.drawOval(xStart * 2 - 10, yStart * 2 - 10, xStart * 2 + 10, yStart * 2 + 10, paint);
                paint.setAlpha(128);
                canvas.drawOval(xStart * 2 - 15, yStart * 2 - 15, xStart * 2 + 15, yStart * 2 + 15, paint);
                paint.setColor(Color.BLUE);
                paint.setAlpha(255);
                canvas.drawOval(xDestination * 2 - 10, yDestination * 2 - 10, xDestination * 2 + 10, yDestination * 2 + 10, paint);
                paint.setAlpha(128);
                canvas.drawOval(xDestination * 2 - 15, yDestination * 2 - 15, xDestination * 2 + 15, yDestination * 2 + 15, paint);

                img.setImageBitmap(bitmap);
            }

        });
    }

    private void resetVisitedPixels()
    {
        for (int i = 0; i < DRAW_WIDTH; i++)
        {
            for (int j = 0; j < DRAW_HEIGHT; j++)
            {
                visitedPixels[i][j] = false;
            }
        }
    }

    private void resetNodeArray()
    {
        for (int i = 0; i < DRAW_WIDTH; i++)
        {
            for (int j = 0; j < DRAW_HEIGHT; j++)
            {
                nodeArray[i][j] = null;
            }
        }
    }

    private void scanBuildings(int x, int y)
    {
        visitedPixels[x][y] = true;
        if (x - 1 > -1)
        {
            if (buildingsPath[x - 1][y] && !visitedPixels[x - 1][y])
            {
                visitedPixels[x - 1][y] = true;
                pixelsX.add(x - 1);
                pixelsY.add(y);
            }
        }
        if (x + 1 < DRAW_WIDTH)
        {
            if (buildingsPath[x + 1][y] && !visitedPixels[x + 1][y])
            {
                visitedPixels[x + 1][y] = true;

                pixelsX.add(x + 1);
                pixelsY.add(y);
            }
        }
        if (y - 1 > -1)
        {
            if (buildingsPath[x][y - 1] && !visitedPixels[x][y - 1])
            {
                visitedPixels[x][y - 1] = true;

                pixelsX.add(x);
                pixelsY.add(y - 1);
            }
        }
        if (y + 1 < DRAW_HEIGHT)
        {
            if (buildingsPath[x][y + 1] && !visitedPixels[x][y + 1])
            {
                visitedPixels[x][y + 1] = true;

                pixelsX.add(x);
                pixelsY.add(y + 1);
            }
        }
    }

    private void drawPath()
    {
        getActivity().runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                for (Node n : finalPath)
                {
                    int x = n.xOrigin;
                    int y = n.yOrigin;
                    Canvas canvas = new Canvas(bitmap);
                    Paint paint = new Paint();
                    paint.setColor(Color.RED);
                    canvas.drawOval(x * 2 - 3, y * 2 - 3, x * 2 + 3, y * 2 + 3, paint);

                }

                img.setImageBitmap(bitmap);
            }

        });
    }

    private int[] convertCoordsToPixels(double lat, double lon)
    {
        double pixelDistanceX = botRightX - topLeftX;
        double pixelDistanceY = botRightY - topLeftY;
        double lonDistance = Math.abs(botRightLon - topLeftLon);
        double latDistance = Math.abs(botRightLat - topLeftLat);
        double lonPer = (lon - topLeftLon) / lonDistance;
        double latPer = (topLeftLat - lat) / latDistance;
        int[] values = {(int) ((latPer * pixelDistanceY) + topLeftY), (int) ((lonPer * pixelDistanceX) + topLeftX)};
//        buildingPixelsX.add((int) ((lonPer * pixelDistanceX) + topLeftX));
//        buildingPixelsY.add((int) ((latPer * pixelDistanceY) + topLeftY));
        return values;
    }

    private class MyLocationListener implements LocationListener
    {

        @Override
        public void onLocationChanged(Location loc)
        {
            Toast.makeText(getActivity(), "Location changed: Lat: " + loc.getLatitude() + " Lng: " + loc.getLongitude(), Toast.LENGTH_SHORT).show();
            String longitude = "Longitude: " + loc.getLongitude();
            Log.v(TAG, longitude);
            String latitude = "Latitude: " + loc.getLatitude();
            Log.v(TAG, latitude);
        }

        @Override
        public void onProviderDisabled(String provider)
        {
        }

        @Override
        public void onProviderEnabled(String provider)
        {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
        }
    }


}
