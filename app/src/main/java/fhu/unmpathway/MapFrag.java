package fhu.unmpathway;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.Context;
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
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import static fhu.unmpathway.MainActivity.buildingPixelsX;
import static fhu.unmpathway.MainActivity.buildingPixelsY;
import static fhu.unmpathway.MainActivity.buildings;
import static fhu.unmpathway.MainActivity.eventGetX;
import static fhu.unmpathway.MainActivity.eventGetY;
import static fhu.unmpathway.MainActivity.focusRequired;

/**
 * Created by alans on 12/31/2017.
 */

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
        final RelativeLayout mainLayout = getView().findViewById(R.id.standard_layout);
        screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
        resetBitmap();
        img = new TouchImageView(getView().getContext());
        img.setImageBitmap(bitmap);
        mainLayout.addView(img);

        startListener();

        sliding = getView().findViewById(R.id.sliding_layout);
        sliding.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);

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
                }
                if (searchMode == DESTINATION)
                {
                    toText.setText(selectedItem);
                    searchView.setQuery("", false);
                    searchView.setIconified(true);
                    sliding.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                }
            }
        });


//        sliding.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener()
//        {
//            @Override
//            public void onPanelSlide(View panel, float slideOffset)
//            {
//
//            }
//
//            @Override
//            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState)
//            {
//                LinearLayout collapsedView = findViewById(R.id.collapsed_view);
//
//                if (newState == SlidingUpPanelLayout.PanelState.EXPANDED)
//                {
//                    collapsedView.setVisibility(View.GONE);
//                }
//                if (newState == SlidingUpPanelLayout.PanelState.COLLAPSED)
//                {
//                    collapsedView.setVisibility(View.VISIBLE);
//                }
//            }
//
//        });
//
//
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


        Button findPath = (Button) getView().findViewById(R.id.find_path);
        findPath.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

            }
        });
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
        float[] info = img.getImageInfo();
        float x = (eventGetX + info[0]) / info[2];
        float y = (eventGetY + info[1]) / info[2];
//        double lonCoordDiff = Math.abs((botRightLon - topLeftLon));
//        double latCoordDiff = Math.abs((botRightLat - topLeftLat));
//        float xPer = (x - topLeftX) / (botRightX - topLeftX);
//        float yPer = (y - topLeftY) / (botRightY - topLeftY);
//        double latCoord = topLeftLat - (yPer * latCoordDiff);
//        double lonCoord = topLeftLon + (xPer * lonCoordDiff);

        System.out.println(info[0]);
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
        getActivity().runOnUiThread(new Runnable()
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

        if (currentlyFocused)
        {
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
        updateTextViews(string);


    }

    private void updateTextViews(String string)
    {
        TextView buildingTitle = (TextView) getView().findViewById(R.id.building_title);
        buildingTitle.setText(string);
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
                System.out.println(s);
                return false;
            }
        });
    }
}
