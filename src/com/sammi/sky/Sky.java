package com.sammi.sky;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import java.io.*;

public class Sky extends Activity
{
    DBAdapter dbAdapter;
    Cursor blue,white,grey;
    int status;//will be used to determine whether there is trained data or not


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        //Content
        setContentView(R.layout.main);

        //Setting DB on first launch
        //Setting the DB
        String destDir = "/data/data/" + getPackageName() + "/databases/";
        String destPath = destDir + "sky";
        File f = new File(destPath);

        if (!f.exists())
        {
            //---make sure directory exists---
            File directory = new File(destDir);
            directory.mkdirs();
            //---copy the db from the assets folder into
            // the databases folder---
            try
            {
                CopyDB(getBaseContext().getAssets().open("sky"),new FileOutputStream(destPath));
            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);
        Toast.makeText(this.getApplicationContext(),"TOUCH THE CLOUDS TO CONTINUE",Toast.LENGTH_LONG).show();
    }

    public void CopyDB(InputStream inputStream, OutputStream outputStream) throws IOException
    {
        //---copy 1K bytes at a time---
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) > 0)
        {
            outputStream.write(buffer, 0, length);
        }
        inputStream.close();
        outputStream.close();
    }

    //after user touches the screen on land
    public void land(View view)
    {
        Intent intentAnalyse = new Intent(getBaseContext(),Analyse.class);
        startActivity(intentAnalyse);
    }

    public void SetDB()
    {
        //Setting DB adapter
        dbAdapter = new DBAdapter(getBaseContext());

        //Loading trained data from db
        blue=dbAdapter.getBlue();
        white = dbAdapter.getWhite();
        grey = dbAdapter.getGrey();

        if(blue.getCount()==0 || white.getCount()==0 || grey.getCount()==0)
            status=0;
        else
            status=1;
    }
}
