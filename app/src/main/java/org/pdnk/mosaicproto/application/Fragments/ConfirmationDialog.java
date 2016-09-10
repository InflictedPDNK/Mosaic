package org.pdnk.mosaicproto.application.Fragments;

import android.app.Dialog;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.pdnk.mosaicproto.R;
import org.pdnk.mosaicproto.Utility.Utility;
import org.pdnk.mosaicproto.application.Fragments.Data.ConfirmDlgData;


/**
 * Created by pnovodon on 8/04/2016.
 */
public class ConfirmationDialog extends BaseGenericFragment<ConfirmDlgData>
{

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        // Use the Builder class for convenient dialog construction
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());


        ViewGroup dlgRoot = (ViewGroup) getActivity().getLayoutInflater().inflate(R.layout.dlg_confirm, null);

        final TextView msg = (TextView) dlgRoot.findViewById(R.id.messageText);
        if(msg != null)
        {
            msg.setText(fragmentData.promptMessage);
        }

        View cancelBtn = dlgRoot.findViewById(R.id.dlgCancelBtn);
        if(cancelBtn != null)
            cancelBtn.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    dismissAllowingStateLoss();

                    if(fragmentData.negativeAction != null)
                        fragmentData.negativeAction.run();
                }
            });

        View okBtn = dlgRoot.findViewById(R.id.dlgOkBtn);
        if(okBtn != null)
            okBtn.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    dismissAllowingStateLoss();

                    if(fragmentData.positiveAction != null)
                        fragmentData.positiveAction.run();
                }
            });


        builder.setView(dlgRoot);
        AlertDialog dlg = builder.create();
        dlg.setCanceledOnTouchOutside(true);
        return dlg;

    }

    @Override
    public void adjustWindow()
    {
        super.adjustWindow();

        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
        {
            getDialog().getWindow().setLayout((int) (Utility.dpToPx(getResources().getConfiguration().screenWidthDp) * 0.5),
                                              ViewGroup.LayoutParams.WRAP_CONTENT);


        }else
        {
            getDialog().getWindow().setLayout((int) (Utility.dpToPx(getResources().getConfiguration().screenWidthDp) * 0.95),
                                              ViewGroup.LayoutParams.WRAP_CONTENT);
        }

    }
}
