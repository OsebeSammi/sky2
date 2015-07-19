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
import android.widget.*;

import java.io.IOException;
import java.util.List;

/**
 * Created by sammi on 7/19/15.
 */
public class Analyse extends Activity implements SurfaceHolder.Callback
{
    private Camera camera;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private ProgressDialog progressDialog;
    private Camera.PictureCallback photoCallback;
    private int blueOutlier=0,anomaly=0,whitish=0,greyish=0;
    private int OUTLIER_THRESHOLD=20;

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


                progressDialog.setMessage("..processing..");
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setIndeterminate(true);
                progressDialog.show();
                process(bitmap);


                ScrollView layout = (ScrollView) findViewById(R.id.process_layout);
                TextView view = (TextView) findViewById(R.id.resultText);
                if(blueOutlier>anomaly && blueOutlier>whitish && blueOutlier>greyish)
                {
                    Toast.makeText(getBaseContext(),"THE SKY IS BLUE..IT MIGHT NOT RAIN",Toast.LENGTH_LONG).show();
                    view.setText("THE SKY IS BLUE..IT MIGHT NOT RAIN");
                }
                else if(whitish>greyish && whitish>blueOutlier && whitish>anomaly)
                {
                    Toast.makeText(getBaseContext(),"THE SKY IS WHITE WITH CLOUDS..IT MIGHT RAIN",Toast.LENGTH_LONG).show();
                    view.setText("THE SKY IS WHITE WITH CLOUDS..IT MIGHT RAIN");
                    layout.setBackgroundColor(Color.WHITE);
                }
                else if(greyish>whitish && greyish>blueOutlier && greyish>anomaly)
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
                outlier(Color.red(temp),Color.green(temp),Color.blue(temp));

            }

        }
    }

    private void outlier(int red, int green, int blue)
    {
        //this is a simple outlier detection algorithm
        int percentDiffRed=100,percentDiffGreen,percentDiffblue;

        percentDiffGreen = (int) (green/red)*100;//casting to int
        percentDiffblue = (int) (blue/red)*100;//casting to int



        //Getting outliers
        if(scalarDifference(percentDiffRed,percentDiffGreen)>OUTLIER_THRESHOLD && scalarDifference(percentDiffRed,percentDiffblue)>OUTLIER_THRESHOLD)
            anomaly++;

        else if(scalarDifference(percentDiffRed,percentDiffGreen)>OUTLIER_THRESHOLD)
            anomaly++;

        else if(scalarDifference(percentDiffRed,percentDiffblue)>OUTLIER_THRESHOLD)
            blueOutlier++;

        else
        {
            //Determining if greyish or whitish
            int avg = red+green+blue;
            avg /=3;
            if(avg>190)
                whitish++;
            else
                greyish++;
        }
    }

    private int scalarDifference(int a,int b)
    {
        //this removes -ves from numbers
        int diff=a-b;
        if(diff<0)
            return diff*-1;
        else
            return diff;
    }
}
