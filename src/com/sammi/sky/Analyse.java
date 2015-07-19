package com.sammi.sky;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;

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

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera);

        surfaceView = (SurfaceView) findViewById(R.id.surfacePhoto);

        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);

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
                ImageView view = (ImageView) findViewById(R.id.takenImage);
                view.setScaleType(ImageView.ScaleType.FIT_XY);
                view.setImageBitmap(bitmap);
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

        System.out.println("Size ");
    }
}
