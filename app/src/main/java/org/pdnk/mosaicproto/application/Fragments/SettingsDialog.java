package org.pdnk.mosaicproto.application.Fragments;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import org.pdnk.mosaicproto.R;
import org.pdnk.mosaicproto.Utility.Utility;
import org.pdnk.mosaicproto.application.Fragments.Data.SettingsData;

/**
 * Created by pnovodon on 12/09/2016.
 */
public class SettingsDialog extends BaseGenericFragment<SettingsData>
{
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        ViewGroup dlgRoot = (ViewGroup) getActivity().getLayoutInflater().inflate(R.layout.dlg_settings, null);

        final EditText tileSizeEdit = (EditText) dlgRoot.findViewById(R.id.tileSizeEdit);
        final EditText apiEndpoint = (EditText) dlgRoot.findViewById(R.id.apiEndpointEdit);
        TextView tileSizeLabel = (TextView) dlgRoot.findViewById(R.id.tileSizeLabel);

        SettingsData settings = SettingsData.readCurrentSettings(getActivity());

        tileSizeLabel.append(String.format(" (%d-%d)", settings.minTileSize, settings.maxTileSize));

        tileSizeEdit.setText(Integer.toString(fragmentData.tileSize));
        apiEndpoint.setText(fragmentData.apiEndpoint);

        View okBtn = dlgRoot.findViewById(R.id.dlgOkBtn);
        if(okBtn != null)
            okBtn.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    dismissAllowingStateLoss();

                    SettingsData newData = new SettingsData();
                    newData.tileSize = Math.max(fragmentData.minTileSize, Math.min(fragmentData.maxTileSize, Integer.parseInt(tileSizeEdit.getText().toString())));
                    newData.apiEndpoint = apiEndpoint.getText().toString();
                    updateSettings(newData);
                }
            });


        builder.setView(dlgRoot);
        AlertDialog dlg = builder.create();
        dlg.setCanceledOnTouchOutside(true);
        return dlg;

    }

    private void updateSettings(SettingsData newData)
    {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(getString(R.string.shared_pref_loc), 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(getString(R.string.set_api_ip), newData.apiEndpoint);
        editor.putInt(getString(R.string.set_tile_size), newData.tileSize);
        editor.apply();
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
