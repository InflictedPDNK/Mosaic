package org.pdnk.mosaicproto.application.Fragments;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import org.pdnk.mosaicproto.R;
import org.pdnk.mosaicproto.application.Fragments.Data.ConfirmDlgData;
import org.pdnk.mosaicproto.application.Fragments.Data.FragmentData;
import org.pdnk.mosaicproto.application.Fragments.Data.MessageDlgData;
import org.pdnk.mosaicproto.application.Fragments.Data.ProcessPageData;

import java.util.LinkedHashMap;


/**
 * Created by pnovodon on 11/04/2016.
 */
@SuppressWarnings("UnusedReturnValue")
public final class FragmentFactory implements BaseGenericFragment.OnFragmentDetachListener,
        BaseGenericFragment.OnFragmentDataLoadListener
{

    class StackRecord
    {
        public FragmentData fragmentData;
        public int transactionId;
    }

    private final LinkedHashMap<Fragment, StackRecord> fragmentStack = new LinkedHashMap<>();
    private final FragmentManager fragmentManager;

    private BaseGenericFragment.OnFragmentInteractionListener interactionListener;
    private BaseGenericFragment.OnFragmentDetachListener attachedListener;

    public FragmentFactory(FragmentManager fm)
    {
        fragmentManager = fm;
    }



    public void setInteractionListener(BaseGenericFragment.OnFragmentInteractionListener interactionListener)
    {
        this.interactionListener = interactionListener;
    }

    public void setAttachedListener(BaseGenericFragment.OnFragmentDetachListener attachListener)
    {
        this.attachedListener = attachListener;
    }


    public void popAllFragments()
    {
        if(fragmentManager.getBackStackEntryCount() > 0)
        {
            //noinspection StatementWithEmptyBody
            while (fragmentManager.popBackStackImmediate());
        }
    }

    public BaseGenericFragment getTopFragment()
    {
        Fragment topFragment = null;

        if(!fragmentStack.isEmpty())
        {
            for (Fragment fragment : fragmentStack.keySet())
            {
                topFragment = fragment;
            }
        }

        return (BaseGenericFragment) topFragment;
    }

    public int getActiveFragmentCount()
    {
        return fragmentManager.getBackStackEntryCount();
    }

    public BaseGenericFragment constructLandingPage()
    {
        BaseGenericFragment f = new LandingPage();
        showFragment(f, null);
        return f;
    }

    public BaseGenericFragment constructProcessPage(ProcessPageData data)
    {
        BaseGenericFragment f = new ProcessPage();
        showFragment(f, data);
        return f;
    }


    public BaseGenericFragment constructConfirmationDialog(ConfirmDlgData data)
    {
        BaseGenericFragment f =  new ConfirmationDialog();
        showDialog(f, data);
        return f;
    }

    public BaseGenericFragment constructMessageDialog(MessageDlgData data)
    {
        BaseGenericFragment f =  new MessageDialog();
        showDialog(f, data);
        return f;
    }


    public synchronized void closeFragment(BaseGenericFragment fragment)
    {
        if(fragment != null)
        {
            //check the record first in case it was already removed
            StackRecord record = fragmentStack.get(fragment);
            if(record != null)
            {
                if(!fragment.isSaved())
                    fragmentManager.popBackStack(record.transactionId, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                else
                    fragmentStack.remove(record);
            }
        }
    }

    private void showFragment(BaseGenericFragment fragment, FragmentData data)
    {
        fragment.attachFragmentFactory(this);
        attachListeners(fragment);

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.fade_in,
                                        R.anim.fade_out,
                                        R.anim.fade_in,
                                        R.anim.fade_out);
        transaction.add(R.id.fragmentContainer, fragment);
        transaction.addToBackStack(null);


        StackRecord record = new StackRecord();
        record.fragmentData = data;
        fragmentStack.put(fragment, record);
        record.transactionId = transaction.commit();


    }

    private void showDialog(BaseGenericFragment fragment, FragmentData data)
    {
        fragment.attachFragmentFactory(this);
        attachListeners(fragment);

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.addToBackStack(null);
        StackRecord record = new StackRecord();
        record.fragmentData = data;
        fragmentStack.put(fragment, record);
        record.transactionId = fragment.show(transaction, "");


    }

    private void attachListeners(BaseGenericFragment fragment)
    {
        fragment.addDetachedListener(this);
        fragment.setDataLoadListener(this);

        if(attachedListener != null)
            fragment.addDetachedListener(attachedListener);

        if(interactionListener != null)
            fragment.addInteractionListener(interactionListener);
    }

    @Override
    public void onFragmentDetached(Fragment fragment)
    {
        fragmentStack.remove(fragment);
    }

    @Override
    public FragmentData loadFragmentData(Fragment fragment)
    {
        return fragmentStack.get(fragment).fragmentData;
    }
}
