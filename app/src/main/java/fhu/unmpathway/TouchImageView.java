package fhu.unmpathway;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;

import static fhu.unmpathway.MainActivity.trueX;

public class TouchImageView extends ImageView
{
    Matrix matrix;
    // We can be in one of these 3 states
    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;

    int mode = NONE;

    // Remember some things for zooming
    PointF last = new PointF();
    PointF start = new PointF();
    //    float minScale = 1f;
//    float maxScale = 3f;
    float minScale =-50f;
    float maxScale = 50f;
    float[] m;
    int viewWidth, viewHeight;

    static final int CLICK = 3;

    float saveScale = 1f;

    protected float origWidth, origHeight;

    int oldMeasuredWidth, oldMeasuredHeight;

    ScaleGestureDetector mScaleDetector;

    Context context;

    long lastUse = 0;
    boolean threadActive = false;


    public TouchImageView(Context context, float mS, float cS)
    {
        super(context);
        sharedConstructing(context);
        minScale = mS;
        saveScale = cS;
    }

    public TouchImageView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        sharedConstructing(context);
    }

    private void sharedConstructing(Context context)
    {

        super.setClickable(true);

        this.context = context;

        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());

        matrix = new Matrix();

        m = new float[9];

        setImageMatrix(matrix);

        setScaleType(ScaleType.MATRIX);

        setOnTouchListener(new OnTouchListener()
        {

            @Override
            public boolean onTouch(View v, MotionEvent event)
            {

                System.out.println(saveScale);

                mScaleDetector.onTouchEvent(event);

                PointF curr = new PointF(event.getX(), event.getY());

                switch (event.getAction())
                {

                    case MotionEvent.ACTION_DOWN:

                        last.set(curr);

                        start.set(last);

                        mode = DRAG;
                        break;

                    case MotionEvent.ACTION_MOVE:

                        if (mode == DRAG)
                        {

                            float deltaX = curr.x - last.x;

                            float deltaY = curr.y - last.y;

                            float fixTransX = getFixDragTrans(deltaX, viewWidth, origWidth * saveScale);

                            float fixTransY = getFixDragTrans(deltaY, viewHeight, origHeight * saveScale);

                            matrix.postTranslate(fixTransX, fixTransY);

                            fixTrans();

                            last.set(curr.x, curr.y);
                        }
                        break;

                    case MotionEvent.ACTION_UP:

                        mode = NONE;

                        int xDiff = (int) Math.abs(curr.x - start.x);

                        int yDiff = (int) Math.abs(curr.y - start.y);

                        if (xDiff < CLICK && yDiff < CLICK)

                            performClick();

                        break;

                    case MotionEvent.ACTION_POINTER_UP:

                        mode = NONE;

                        break;

                }

                setImageMatrix(matrix);

                //newImage();

                invalidate();

                lastUse = System.currentTimeMillis();

                return true; // indicate event was handled

            }

        });


    }

    private void newImage()
    {
        if (threadActive)
        {
            return;
        }
        Thread t = new Thread()
        {
            public void run()
            {
                threadActive = true;
                while (System.currentTimeMillis() - lastUse < 1000)
                {

                }
                System.out.println("done");
                threadActive = false;
                MainActivity.newImageRequired = true;
//                try
//                {
//                    setNewImage();
//                } catch (IOException e)
//                {
//                    e.printStackTrace();
//                }
            }
        };
        t.start();


    }
    public float[] getImageInfo()
    {
        matrix.getValues(m);
        float transX = m[Matrix.MTRANS_X];
        float transY = m[Matrix.MTRANS_Y];
        float[] info = new float[4];


        info[0] = (-transX / saveScale) / origWidth;
        info[1] = (-transY / saveScale) / origHeight;
        info[2] = ((-transX + this.getWidth())  / saveScale) / origWidth;
        info[3] = ((-transY + this.getHeight()) / saveScale) / origHeight;
        //float wholeScale = origWidth/MainActivity.trueOrigWidth;
        MainActivity.trueX += (-transX / saveScale)+MainActivity.trueX;
        MainActivity.trueY += (-transY / saveScale) + MainActivity.trueY;

        return info;
    }
    public float getOrigHeight()
    {
        float scale;

        Drawable drawable = getDrawable();

        if (drawable == null || drawable.getIntrinsicWidth() == 0 || drawable.getIntrinsicHeight() == 0)

            return 69f;
        int bmWidth = drawable.getIntrinsicWidth();

        int bmHeight = drawable.getIntrinsicHeight();

        Log.d("bmSize", "bmWidth: " + bmWidth + " bmHeight : " + bmHeight);

        float scaleX = (float) viewWidth / (float) bmWidth;

        float scaleY = (float) viewHeight / (float) bmHeight;

        scale = Math.min(scaleX, scaleY);
        float redundantYSpace = (float) viewHeight - (scale * (float) bmHeight);

        redundantYSpace /= (float) 2;

        origHeight = viewHeight - 2 * redundantYSpace;
        return origHeight;
    }
    private void setNewImage() throws IOException
    {

        //System.out.println("here");
//        Bitmap fullImage = BitmapFactory.decodeResource(getResources(),R.drawable.quad2);
//        BitmapDrawable fullImage = (BitmapDrawable) getResources().getDrawable(R.drawable.clean_campus_map);
        //Bitmap fullImageBitmap = fullImage.getBitmap();
//        final BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inJustDecodeBounds = true;
//        options.inSampleSize = 6;

//        Bitmap fullImage = BitmapFactory.decodeResource(getResources(), R.drawable.clean_campus_map,options);
//
//        Bitmap resizedImage = Bitmap.createBitmap(fullImage, 0,0,500, 500);
//        System.out.println(resizedImage.getHeight());

//        Bitmap map = (
//                decodeSampledBitmapFromResource(getResources(), R.drawable.quad2, 100, 100));
//
//        System.out.println(map.getWidth());
        //img.setImageBitmap(map);

        // Get image width and height:
        //InputStream inputStream = context.getAssets().open("R.drawable.clean_campus_map.png");
        InputStream inputStream = getResources().openRawResource(+ R.drawable.clean_campus_map);
        BitmapFactory.Options tmpOptions = new BitmapFactory.Options();
        tmpOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(inputStream, null, tmpOptions);
        int width = tmpOptions.outWidth;
        int height = tmpOptions.outHeight;

// Crop image:
// Crop a rect with 200 pixel width and height from center of image
        BitmapRegionDecoder bitmapRegionDecoder = BitmapRegionDecoder.newInstance(inputStream, false);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap bitmap = bitmapRegionDecoder.decodeRegion(new Rect(width / 2 - 100, height / 2 - 100, width / 2 + 100, height / 2 + 100), options);
        //mImageView.setImageBitmap(bitmap);



        //this.setImageBitmap(bitmap);

    }

    public void setMaxZoom(float x)
    {

        maxScale = x;

    }

    public void setMinZoom(float x)
    {

        minScale = x;

    }

    public float getScale()
    {
        return saveScale;
    }
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener
    {

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector)
        {

            mode = ZOOM;

            return true;

        }

        @Override
        public boolean onScale(ScaleGestureDetector detector)
        {

            float mScaleFactor = detector.getScaleFactor();

            float origScale = saveScale;

            saveScale *= mScaleFactor;

            if (saveScale > maxScale)
            {

                saveScale = maxScale;

                mScaleFactor = maxScale / origScale;

            }
            else if (saveScale < minScale)
            {

                saveScale = minScale;

                mScaleFactor = minScale / origScale;

            }

            if (origWidth * saveScale <= viewWidth || origHeight * saveScale <= viewHeight)

                matrix.postScale(mScaleFactor, mScaleFactor, viewWidth / 2, viewHeight / 2);

            else

                matrix.postScale(mScaleFactor, mScaleFactor, detector.getFocusX(), detector.getFocusY());

            fixTrans();

            return true;

        }

    }

    void fixTrans()
    {

        matrix.getValues(m);

        float transX = m[Matrix.MTRANS_X];

        float transY = m[Matrix.MTRANS_Y];

        float fixTransX = getFixTrans(transX, viewWidth, origWidth * saveScale);

        float fixTransY = getFixTrans(transY, viewHeight, origHeight * saveScale);

        if (fixTransX != 0 || fixTransY != 0)

            matrix.postTranslate(fixTransX, fixTransY);

    }


    float getFixTrans(float trans, float viewSize, float contentSize)
    {

        float minTrans, maxTrans;

        if (contentSize <= viewSize)
        {

            minTrans = 0;

            maxTrans = viewSize - contentSize;

        }
        else
        {

            minTrans = viewSize - contentSize;

            maxTrans = 0;

        }

        if (trans < minTrans)

            return -trans + minTrans;

        if (trans > maxTrans)

            return -trans + maxTrans;

        return 0;

    }

    float getFixDragTrans(float delta, float viewSize, float contentSize)
    {

        if (contentSize <= viewSize)
        {

            return 0;

        }

        return delta;

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        viewWidth = MeasureSpec.getSize(widthMeasureSpec);

        viewHeight = MeasureSpec.getSize(heightMeasureSpec);

        //
        // Rescales image on rotation
        //
        if (oldMeasuredHeight == viewWidth && oldMeasuredHeight == viewHeight

                || viewWidth == 0 || viewHeight == 0)

            return;

        oldMeasuredHeight = viewHeight;

        oldMeasuredWidth = viewWidth;

        if (saveScale == 1)
        {
            //Fit to screen.

            float scale;

            Drawable drawable = getDrawable();

            if (drawable == null || drawable.getIntrinsicWidth() == 0 || drawable.getIntrinsicHeight() == 0)

                return;

            int bmWidth = drawable.getIntrinsicWidth();

            int bmHeight = drawable.getIntrinsicHeight();

            Log.d("bmSize", "bmWidth: " + bmWidth + " bmHeight : " + bmHeight);

            float scaleX = (float) viewWidth / (float) bmWidth;

            float scaleY = (float) viewHeight / (float) bmHeight;

            scale = Math.min(scaleX, scaleY);

            //added line
            scale = 1f;

            matrix.setScale(scale, scale);


            // Center the image

            float redundantYSpace = (float) viewHeight - (scale * (float) bmHeight);

            float redundantXSpace = (float) viewWidth - (scale * (float) bmWidth);

            redundantYSpace /= (float) 2;

            redundantXSpace /= (float) 2;

            matrix.postTranslate(redundantXSpace, redundantYSpace);

            origWidth = viewWidth - 2 * redundantXSpace;

            origHeight = viewHeight - 2 * redundantYSpace;

            setImageMatrix(matrix);

        }
        if (saveScale == -1)
        {
            //Fit to screen.

            float scale;

            Drawable drawable = getDrawable();

            if (drawable == null || drawable.getIntrinsicWidth() == 0 || drawable.getIntrinsicHeight() == 0)

                return;

            int bmWidth = drawable.getIntrinsicWidth();

            int bmHeight = drawable.getIntrinsicHeight();

            Log.d("bmSize", "bmWidth: " + bmWidth + " bmHeight : " + bmHeight);

            float scaleX = (float) viewWidth / (float) bmWidth;

            float scaleY = (float) viewHeight / (float) bmHeight;

            scale = Math.min(scaleX, scaleY);

            //added line
            //scale = 1f;

            matrix.setScale(scale, scale);


            // Center the image

            float redundantYSpace = (float) viewHeight - (scale * (float) bmHeight);

            float redundantXSpace = (float) viewWidth - (scale * (float) bmWidth);

            redundantYSpace /= (float) 2;

            redundantXSpace /= (float) 2;

            matrix.postTranslate(redundantXSpace, redundantYSpace);

            origWidth = viewWidth - 2 * redundantXSpace;

            origHeight = viewHeight - 2 * redundantYSpace;

            setImageMatrix(matrix);

        }
        fixTrans();

    }


}