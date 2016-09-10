package org.pdnk.mosaicproto.application.Fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.pdnk.mosaicproto.R;
import org.pdnk.mosaicproto.Utility.Utility;
import org.pdnk.mosaicproto.application.Fragments.Data.ConfirmDlgData;
import org.pdnk.mosaicproto.application.Fragments.Data.FragmentData;
import org.pdnk.mosaicproto.application.Fragments.Data.MessageDlgData;
import org.pdnk.mosaicproto.application.Fragments.Data.ProcessPageData;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by pnovodon on 10/09/2016.
 */
public class LandingPage extends BaseGenericFragment<FragmentData>
{
    private FragmentFactory fragmentFactory;
    private final int SELECT_IMAGE_RESULT = 1;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {

        fragmentFactory = new FragmentFactory(getActivity().getSupportFragmentManager());
        ViewGroup myView = (ViewGroup) inflater.inflate(R.layout.frag_landing_page,
                                                        container,
                                                        false);

        ButterKnife.bind(this, myView);

        return myView;
    }


    @OnClick(R.id.selectImagelink)
    protected void onSelectImageClick()
    {
        Utility.showImageChooser(this, SELECT_IMAGE_RESULT);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SELECT_IMAGE_RESULT)
        {
            if(resultCode == Activity.RESULT_OK)
            {
                if (data != null && data.getData() != null)
                {
                    ProcessPageData processPageData = new ProcessPageData();
                    processPageData.imageLocation = data.getData();

                    fragmentFactory.constructProcessPage(processPageData);
                    closeSelf();
                }
            }else if(resultCode != Activity.RESULT_CANCELED)
                fragmentFactory.constructMessageDialog(new MessageDlgData("Failed to load image"));
        }


    }

    @Override
    public boolean handleBackButton()
    {
        confirmExit();
        return true;
    }

    private void confirmExit()
    {
        ConfirmDlgData confirmDlgData = new ConfirmDlgData();
        confirmDlgData.promptMessage = getString(R.string.confirm_exit_text);
        confirmDlgData.positiveAction = new Runnable()
        {
            @Override
            public void run()
            {
                getActivity().finish();
            }
        };

        fragmentFactory.constructConfirmationDialog(confirmDlgData);
    }
}
