package org.pdnk.mosaicproto.application;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import org.pdnk.mosaicproto.R;
import org.pdnk.mosaicproto.Utility.Utility;
import org.pdnk.mosaicproto.application.Fragments.BaseGenericFragment;
import org.pdnk.mosaicproto.application.Fragments.FragmentFactory;

public class MainActivity extends AppCompatActivity
{
    FragmentFactory fragmentFactory;
    //Graph g;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Utility.Initialise(this);

        fragmentFactory = new FragmentFactory(getSupportFragmentManager());
        fragmentFactory.constructLandingPage();

    }

    @Override
    public void onBackPressed()
    {
        if(fragmentFactory != null)
        {
            BaseGenericFragment topFragment = fragmentFactory.getTopFragment();

            if(topFragment.handleBackButton())
                return;
        }

        super.onBackPressed();
    }


}
