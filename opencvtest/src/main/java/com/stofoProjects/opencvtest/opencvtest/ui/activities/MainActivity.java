package com.stofoProjects.opencvtest.opencvtest.ui.activities;

import android.app.Activity;
import android.os.Bundle;

import com.stofoProjects.opencvtest.opencvtest.R;
import com.stofoProjects.opencvtest.opencvtest.ui.fragments.MainFragment;

/**
 * Created by Martin Stofanak on 5.5.2014.
 */
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_general);

        if(savedInstanceState == null){

            MainFragment fragment = MainFragment.newInstance();

            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, fragment, MainFragment.TAG)
                    .commit();
        }
    }
}
