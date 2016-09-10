package org.pdnk.mosaicproto.application.Fragments;

import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;

import org.pdnk.mosaicproto.R;
import org.pdnk.mosaicproto.application.Fragments.Data.FragmentData;

import java.util.HashSet;


/**
 * Created by pnovodon on 9/03/2016.
 */
public class BaseGenericFragment<DataType extends FragmentData> extends DialogFragment
{

    private final HashSet<OnFragmentInteractionListener> interactionListeners = new HashSet<>();
    private final HashSet<OnFragmentDetachListener> detachedListeners = new HashSet<>();
    private OnFragmentDataLoadListener dataLoadListener;



    protected DataType fragmentData;
    private boolean firstEntry = true;
    private boolean wasSaved;
    private FragmentFactory factory;

    public BaseGenericFragment()
    {
        // Required empty public constructor
    }

    public void attachFragmentFactory(FragmentFactory factory)
    {
        this.factory = factory;
    }

    public synchronized void addInteractionListener(OnFragmentInteractionListener listener)
    {
        interactionListeners.add(listener);
    }

    public synchronized void addDetachedListener(OnFragmentDetachListener listener)
    {
        detachedListeners.add(listener);
    }

    public synchronized void setDataLoadListener(OnFragmentDataLoadListener listener)
    {
        dataLoadListener = listener;
    }

    public boolean isSaved()
    {
        return wasSaved;
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        wasSaved = true;
        super.onSaveInstanceState(outState);
    }

    @CallSuper
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        wasSaved = false;
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);

        if(dataLoadListener != null)
        {
            try
            {
                //noinspection unchecked
                fragmentData = (DataType)dataLoadListener.loadFragmentData(this);
            }catch (Exception e)
            {
                Log.e("TechRadar", "BaseGenericFragment onCreate: you must pass the appropriate subclass of FragmentData to the fragment");
                e.printStackTrace();
            }

        }

    }

    @Override
    public void onStart()
    {
        super.onStart();

        if(getDialog() != null)
            adjustWindow();
    }

    @Override
    public void onDetach()
    {
        super.onDetach();

        if(!getActivity().isDestroyed())
        {
            for (OnFragmentDetachListener listener : detachedListeners)
                listener.onFragmentDetached(this);
        }

        detachedListeners.clear();
        interactionListeners.clear();
        dataLoadListener = null;
        factory = null;
    }

    @CallSuper
    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim)
    {
        Animation anim = (enter && !firstEntry)  ? new Animation(){} : super.onCreateAnimation(transit, enter, nextAnim) ;

        if(enter)
            firstEntry = false;

        return anim;
    }

    protected void callOnControlClick(View v, Object data)
    {
        for(OnFragmentInteractionListener listener : interactionListeners)
            listener.onFragmentControlClick(v, data);
    }


    protected void closeSelf()
    {
        factory.closeFragment(this);
    }

    protected void adjustWindow()
    {
        getDialog().getWindow().setBackgroundDrawableResource(R.drawable.panel_rounded_bkg);
    }

    public interface OnFragmentInteractionListener
    {
        @SuppressWarnings("UnusedParameters")
        void onFragmentControlClick(View v, Object data);

    }

    public interface OnFragmentDetachListener
    {
        void onFragmentDetached(Fragment fragment);
    }

    public interface OnFragmentDataLoadListener
    {
        FragmentData loadFragmentData(Fragment fragment);
    }

    public DataType getFragmentData()
    {
        return fragmentData;
    }

    public boolean handleBackButton()
    {
        return false;
    }
}
