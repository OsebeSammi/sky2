package com.sammi.sky;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

public class Sky extends Activity
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        //Content
        setContentView(R.layout.main);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);

        //Analyse picture
        /*Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bluesky);
        process(bitmap);*/
    }

    //after user touches the screen on land
    public void Outlier(View view)
    {
        Intent intentOutlier = new Intent(getBaseContext(),Analyse.class);
        startActivity(intentOutlier);
    }

    public void Cluster(View view)
    {
        Intent intentCluster = new Intent(getBaseContext(),Clustering.class);
        startActivity(intentCluster);
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
                System.out.println("Red "+ Color.red(temp)+" Green "+Color.green(temp)+" Blue "+Color.blue(temp));
            }
        }
    }
}
