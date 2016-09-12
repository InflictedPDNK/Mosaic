package org.pdnk.mosaicproto.application.Fragments.Data;

import android.app.Activity;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import org.pdnk.mosaicproto.R;

/**
 * Created by pnovodon on 12/09/2016.
 */
public class SettingsData extends FragmentData
{
    public int minTileSize;
    public int maxTileSize;
    public int tileSize;
    public String apiEndpoint;

    public static SettingsData readCurrentSettings(@NonNull Activity context)
    {
        SettingsData settings = new SettingsData();
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.shared_pref_loc), 0);
        settings.tileSize = sharedPreferences.getInt(context.getString(R.string.set_tile_size), context.getResources().getInteger(R.integer.default_tile_size));
        settings.apiEndpoint = sharedPreferences.getString(context.getString(R.string.set_api_ip), context.getString(
                        R.string.default_api_ip));

        settings.minTileSize = context.getResources().getInteger(R.integer.min_tile_size);
        settings.maxTileSize = context.getResources().getInteger(R.integer.max_tile_size);
        return settings;
    }
}
