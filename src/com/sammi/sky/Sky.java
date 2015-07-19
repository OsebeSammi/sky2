package com.sammi.sky;

import android.app.Activity;
import android.content.Intent;
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
}
