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

import static fhu.unmpathway.MainActivity.eventGetX;
import static fhu.unmpathway.MainActivity.eventGetY;
import static fhu.unmpathway.MainActivity.focusRequired;
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
    float minScale = .5f;
    float maxScale = 50f;
    float[] m;
    int viewWidth, viewHeight;

    static final int CLICK = 3;

    float saveScale = 1f;

    protected float origWidth, origHeight;

    int oldMeasuredWidth, oldMeasuredHeight;
    boolean movement = false;

    ScaleGestureDetector mScaleDetector;

    Context context;

    long lastUse = 0;
    boolean threadActive = false;


    public TouchImageView(Context context)
    {
        super(context);
        sharedConstructing(context);
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

                //System.out.println(saveScale);

                mScaleDetector.onTouchEvent(event);

                PointF curr = new PointF(event.getX(), event.getY());

                switch (event.getAction())
                {

                    case MotionEvent.ACTION_DOWN:
                        movement = false;

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
                            movement = true;
                        }
                        break;

                    case MotionEvent.ACTION_UP:

                        mode = NONE;

                        if(!movement)
                        {
                            focusRequired = true;
                            eventGetX = event.getX();
                            eventGetY = event.getY();
                        }

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

    //DONT DELETE CAUSE I NEED TO PUT DIS IN WAC
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
                while (System.currentTimeMillis() - lastUse < 2000)
                {

                }
                System.out.println("done");
                threadActive = false;
                //MainActivity.newImageRequired = true;
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
        float[] info = new float[3];


        info[0] = -transX;
        info[1] = -transY;
        info[2] = saveScale;

        return info;
    }
//    public float[] getImageInfo()
//    {
//        matrix.getValues(m);
//        float transX = m[Matrix.MTRANS_X];
//        float transY = m[Matrix.MTRANS_Y];
//        float[] info = new float[4];
//
//
//        info[0] = (-transX / saveScale) / origWidth;
//        info[1] = (-transY / saveScale) / origHeight;
//        info[2] = ((-transX + this.getWidth()) / saveScale) / origWidth;
//        info[3] = ((-transY + this.getHeight()) / saveScale) / origHeight;
//        float wholeScale = origWidth/MainActivity.trueOrigWidth;
//        MainActivity.trueX += (-transX / saveScale) + MainActivity.trueX;
//        MainActivity.trueY += (-transY / saveScale) + MainActivity.trueY;
//
//        return info;
//    }



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