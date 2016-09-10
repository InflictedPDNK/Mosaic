package org.pdnk.mosaicproto.Utility;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.util.TypedValue;

/**
 * Created by pnovodon on 3/03/2016.
 */
@SuppressWarnings("unused")
public class Utility
{
    private static Context context = null;

    public static void Initialise(Context ctx)
    {
        if (context == null)
            context = ctx;
    }


    public static float pxToDp(float px)
    {
        return px / context.getResources().getDisplayMetrics().density;
    }

    public static float dpToPx(float dp)
    {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                                         dp,
                                         context.getResources().getDisplayMetrics());
    }

    public static void showImageChooser(Fragment fragment, int resultCode)
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        fragment.startActivityForResult(Intent.createChooser(intent, "Select Picture"), resultCode);
    }


}
