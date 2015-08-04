package com.sammi.sky;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

/**
 * Created by sammi on 7/19/15.
 */
public class Clustering extends Activity implements SurfaceHolder.Callback
{
    private Camera camera;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private ProgressDialog progressDialog;
    private Camera.PictureCallback photoCallback;
    private int blueish =0,anomaly=0,whitish=0,greyish=0;
    private int[] whiteCentroid = {245,245,245};
    private int[] greyCentroid = {140,140,140};
    private int[] greyCentroid2 = {170,170,170};
    private int[] blueCentroid = {105,120,140};
    private int[] blueCentroid2 = {120,140,160};
    private int MAX_THRESHOLD =20;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera);

        surfaceView = (SurfaceView) findViewById(R.id.surfacePhoto);

        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);

        progressDialog = new ProgressDialog(this);

        photoCallback = new Camera.PictureCallback()
        {

            @Override
            public void onPictureTaken(byte[] data, Camera camera)
            {
                //refresh camera
                refreshCamera();

                //Get Bitmap
                setContentView(R.layout.process);
                Bitmap bitmap = BitmapFactory.decodeByteArray(data,0,data.length);
                //Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bluesky);
                /*ImageView view = (ImageView) findViewById(R.id.takenImage);
                view.setScaleType(ImageView.ScaleType.FIT_XY);
                view.setImageBitmap(bitmap);*/

                progressDialog.setMessage("..processing..");
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setIndeterminate(true);
                progressDialog.show();
                process(bitmap);

                ScrollView layout = (ScrollView) findViewById(R.id.process_layout);
                TextView view = (TextView) findViewById(R.id.resultText);
                if(blueish>anomaly && blueish>whitish && blueish>greyish)
                {
                    Toast.makeText(getBaseContext(),"THE SKY IS BLUE..IT MIGHT NOT RAIN",Toast.LENGTH_LONG).show();
                    view.setText("THE SKY IS BLUE..IT MIGHT NOT RAIN");
                }
                else if(whitish>greyish && whitish>blueish && whitish>anomaly)
                {
                    Toast.makeText(getBaseContext(),"THE SKY IS WHITE WITH CLOUDS..IT MIGHT RAIN",Toast.LENGTH_LONG).show();
                    view.setText("THE SKY IS WHITE WITH CLOUDS..IT MIGHT RAIN");
                    layout.setBackgroundColor(Color.WHITE);
                }
                else if(greyish>whitish && greyish>blueish && greyish>anomaly)
                {
                    Toast.makeText(getBaseContext(),"THE SKY HAS DARK CLOUDS..IT WILL RAIN",Toast.LENGTH_LONG).show();
                    view.setText("THE SKY HAS DARK CLOUDS..IT WILL RAIN");
                    layout.setBackgroundColor(Color.GRAY);
                }
                else
                {
                    //anomaly is greatest
                    Toast.makeText(getBaseContext(),"I DINT GET A GOOD VIEW OF THE SKY! TRY AGAIN",Toast.LENGTH_LONG).show();
                    view.setText("I DINT GET A GOOD VIEW OF THE SKY! TRY AGAIN");
                }

                progressDialog.cancel();
            }
        };
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

    public void captureImage(View v) throws IOException
    {
        //Toast.makeText(getApplicationContext(),"Camera",Toast.LENGTH_SHORT).show();
        camera.takePicture(null, null, photoCallback);
    }

    public void refreshCamera()
    {
        if (surfaceHolder.getSurface() == null)
        {
            return;
        }

        try
        {
            camera.stopPreview();
        }

        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }



    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        try
        {
            camera = Camera.open();
        }
        catch (RuntimeException e)
        {
            e.printStackTrace();
            return;
        }



        Camera.Parameters param;
        param = camera.getParameters();

        //Getting allowed sizes to avoid exceptions
        List<Camera.Size> sizeList = param.getSupportedPreviewSizes();

        //Default size
        int width = 640;
        int height = 480;

        //Get size closest to 1000
        for(int x=0;x<sizeList.size();x++)
        {
            if(sizeList.get(x).width>=width && sizeList.get(x).width<1000)
            {
                if(sizeList.get(x).height>=height && sizeList.get(x).height<1000)
                {
                    width = sizeList.get(x).width;
                    height = sizeList.get(x).height;
                }
            }
        }

        param.setPreviewSize(width,height);
        param.setPictureSize(width,height);

        camera.setParameters(param);

        try
        {
            //Setting the orientation
            camera.setDisplayOrientation(90);
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {
        //Refresh camera
        refreshCamera();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {
        //Releasing camera resource
        camera.stopPreview();
        camera.release();
        camera = null;
    }

    private void process(Bitmap bitmap)
    {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        //Looping through pixels
        int temp;
        for(int w=0;w<width;w++)
        {
            for(int h=0;h<height;h++)
            {
                temp = bitmap.getPixel(w,h);
                int[] feed = {Color.red(temp),Color.green(temp),Color.blue(temp)};
                cluster(feed);
            }
        }
    }

    private void cluster(int[] colors)
    {
        //int tempWhite=0, tempBlue=0, tempGrey=0;

        //Getting closest centroid
        int diffblue,diffblue2,diffwhite,diffGrey,diffGrey2;

        for(int x=0;x<3;x++)
        {
            diffblue = difference(blueCentroid[x] , colors[x]);
            diffblue2 = difference(blueCentroid2[x] , colors[x]);
            diffGrey = difference(greyCentroid[x] , colors[x]);
            diffGrey2 = difference(greyCentroid2[x] , colors[x]);
            diffwhite = difference(whiteCentroid[x] , colors[x]);

            //System.out.println("Blur"+diffblue+"Grey"+diffGrey+"white"+diffwhite);

            //Vote for temp
            if(diffblue<diffGrey && diffblue<diffwhite && diffblue<MAX_THRESHOLD)
                blueish++;
            else if(diffblue2<diffGrey && diffblue2<diffwhite && diffblue2<MAX_THRESHOLD)
                blueish++;
            else if(diffGrey<diffblue && diffGrey<diffwhite && diffGrey<MAX_THRESHOLD)
                greyish++;
            else if(diffGrey2<diffblue && diffGrey2<diffwhite && diffGrey2<MAX_THRESHOLD)
                greyish++;
            else if(diffwhite<diffblue && diffwhite<diffGrey  && diffwhite<MAX_THRESHOLD)
                whitish++;
            else
                anomaly++;
        }

        //Vote colors
        /*if(tempBlue>=2)
            blueish++;
        else if(tempGrey>=2)
            greyish++;
        else if(tempWhite>=2)
            whitish++;
        else
            anomaly++;*/
    }

    private int difference(int a, int b)
    {
        int diff = a-b;
        if(diff<0)
            return diff*-1;
        else
            return diff;
    }
}
