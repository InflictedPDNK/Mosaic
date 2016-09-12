package org.pdnk.mosaicproto.application.Fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.pdnk.canvaprocessor.Common.ParametricRunnable;
import org.pdnk.canvaprocessor.Data.ImageDataDescriptor;
import org.pdnk.canvaprocessor.Feedback.CompletedFeedback;
import org.pdnk.canvaprocessor.Feedback.ProgressFeedback;
import org.pdnk.canvaprocessor.Graph.Graph;
import org.pdnk.canvaprocessor.SinkNode.SurfaceRenderer;
import org.pdnk.canvaprocessor.SourceNode.BitmapSource;
import org.pdnk.canvaprocessor.TransformPipe.MosaicTransform.MosaicNetworkTransform;
import org.pdnk.canvaprocessor.TransformPipe.TransformPipe;
import org.pdnk.mosaicproto.R;
import org.pdnk.mosaicproto.Utility.Utility;
import org.pdnk.mosaicproto.application.Fragments.Data.MessageDlgData;
import org.pdnk.mosaicproto.application.Fragments.Data.ProcessPageData;
import org.pdnk.mosaicproto.application.Fragments.Data.SettingsData;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by pnovodon on 10/09/2016.
 */
public class ProcessPage extends BaseGenericFragment<ProcessPageData>
{
    private final int SELECT_IMAGE_RESULT = 1;

    FragmentFactory fragmentFactory;
    Bitmap bitmapOriginal;
    boolean processing;

    @BindView(R.id.imageSurface)
    SurfaceView imageSurface;

    @BindView(R.id.imageOriginal)
    ImageView imageOriginal;

    @BindView(R.id.selectImageBtn)
    View selectImageBtn;

    @BindView(R.id.settingsBtn)
    View settingsBtn;

    @BindView(R.id.runBtn)
    ImageView runBtn;

    @BindView(R.id.shareBtn)
    View shareBtn;

    @BindView(R.id.progress)
    ProgressBar progressBar;

    @BindView(R.id.statusMessage)
    TextView statusMessage;

    @BindView(R.id.spinner)
    View spinner;

    private Graph graph;
    private long elapsedTime;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        fragmentFactory = new FragmentFactory(getActivity().getSupportFragmentManager());
        ViewGroup myView = (ViewGroup) inflater.inflate(R.layout.frag_process_page,
                                                        container,
                                                        false);
        ButterKnife.bind(this, myView);

        layoutElements();

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(getString(R.string.shared_pref_loc), 0);
        if(sharedPreferences.getBoolean(getString(R.string.set_first_launch), true))
        {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(getString(R.string.set_first_launch), false);
            editor.apply();

            fragmentFactory.constructSettingsDialog(SettingsData.readCurrentSettings(getActivity()));

            fragmentFactory.constructMessageDialog(new MessageDlgData("Please set the API endpoint IP"));
        }

        return myView;
    }

    private void layoutElements()
    {
        imageSurface.getHolder().setFormat(PixelFormat.TRANSPARENT);
        imageSurface.setZOrderOnTop(true);

        resetControls();
    }

    @OnClick(R.id.selectImageBtn)
    protected void onSelectImageClick()
    {
        Utility.showImageChooser(this, SELECT_IMAGE_RESULT);
    }

    @OnClick(R.id.settingsBtn)
    protected void onSettingsClick()
    {
        fragmentFactory.constructSettingsDialog(SettingsData.readCurrentSettings(getActivity()));
    }

    @OnClick(R.id.shareBtn)
    protected void onShareClick()
    {
        if(graph != null && graph.readGraphOutput() != null)
        {
            ImageDataDescriptor dataDescriptor = (ImageDataDescriptor) graph.readGraphOutput();
            Bitmap b = Bitmap.createBitmap(dataDescriptor.getWidth(),
                                           dataDescriptor.getHeight(),
                                           Bitmap.Config.ARGB_8888);

            dataDescriptor.getData().rewind();
            b.copyPixelsFromBuffer(dataDescriptor.getData());

            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("image/jpeg");
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            b.compress(Bitmap.CompressFormat.JPEG, 85, bytes);

            String filename = "Mosaic.jpg";
            File f = new File(Environment.getExternalStorageDirectory() + File.separator + filename);
            try
            {
                f.createNewFile();
                FileOutputStream fo = new FileOutputStream(f);
                fo.write(bytes.toByteArray());
                fo.close();

            } catch (IOException e)
            {
                e.printStackTrace();
            }
            share.putExtra(Intent.EXTRA_TEXT, "Made in Mosaic Demo");
            share.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:///" + Environment.getExternalStorageDirectory() + File.separator + filename));
            startActivity(Intent.createChooser(share, "Share Image"));
        }


    }

    @OnClick(R.id.runBtn)
    protected void onRunClick()
    {
        if(processing)
        {
            spinner.setVisibility(View.GONE);
            imageSurface.setVisibility(View.INVISIBLE);
            imageOriginal.setVisibility(View.VISIBLE);
            resetControls();
            if(graph != null)
            {
                graph.cancel();
                statusMessage.setText(R.string.status_cancelled);
            }
        }else
        {
            spinner.setVisibility(View.VISIBLE);
            imageOriginal.setVisibility(View.INVISIBLE);
            imageSurface.setVisibility(View.VISIBLE);

            runBtn.setImageResource(R.drawable.ic_close_black_24dp);
            enableButton(selectImageBtn, false);
            enableButton(shareBtn, false);
            enableButton(settingsBtn, false);

            progressBar.setVisibility(View.VISIBLE);

            statusMessage.setText(R.string.status_processing);

            if(graph != null)
                doMosaicRepeat();
            else
                doMosaicFirstTime();
        }

        processing = !processing;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        processOriginalImage();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SELECT_IMAGE_RESULT)
        {
            if (resultCode == Activity.RESULT_OK)
            {
                if (data != null && data.getData() != null)
                {
                    fragmentData.imageLocation = data.getData();
                    processOriginalImage();
                }
            }
            else if (resultCode != Activity.RESULT_CANCELED)
                fragmentFactory.constructMessageDialog(new MessageDlgData("Failed to load image"));
        }

    }


    private void processOriginalImage()
    {
        bitmapOriginal = null;
        imageSurface.setVisibility(View.INVISIBLE);
        imageOriginal.setVisibility(View.VISIBLE);
        imageOriginal.setImageDrawable(null);
        resetControls();

        graph = null;

        try
        {
            InputStream is = getActivity().getContentResolver().openInputStream(fragmentData.imageLocation);
            if(is != null)
            {
                bitmapOriginal = BitmapFactory.decodeStream(is);
                is.close();
            }

        } catch (IOException e)
        {
            e.printStackTrace();

        }

        if(bitmapOriginal == null)
        {
            fragmentFactory.constructMessageDialog(new MessageDlgData("Failed to decode image"));
            enableButton(runBtn, false);
            statusMessage.setText(R.string.status_image_needed);
        }else
        {
            imageOriginal.setImageDrawable(new BitmapDrawable(getResources(), bitmapOriginal));
            statusMessage.setText(R.string.status_ready);
        }


    }

    private void doMosaicFirstTime()
    {

        SettingsData settings = SettingsData.readCurrentSettings(getActivity());

        graph = new Graph.Builder()
        .setSourceNode(new BitmapSource(bitmapOriginal))
        .setSinkNode(new SurfaceRenderer(imageSurface, settings.tileSize, settings.tileSize))
        .addTransformPipe(new MosaicNetworkTransform(getContext(), settings.tileSize, settings.tileSize, settings.apiEndpoint))
        .setEnableCacheOutput(true)
        .setOnCompletionFeedback(new ParametricRunnable<CompletedFeedback>()
        {
            @Override
            public void run(final CompletedFeedback param)
            {
                if(getView() != null)
                {
                    getView().post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            onMosaicCompleted(param);
                        }
                    });
                }

            }
        })
        .setOnProgressFeedback(new ParametricRunnable<ProgressFeedback>()
        {
            @Override
            public void run(final ProgressFeedback param)
            {
                if(getView() != null)
                {
                    getView().post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            onMosaicUpdate(param);
                        }
                    });
                }
            }
        }).buildGraph();


        graph.run();
    }

    void doMosaicRepeat()
    {
        TransformPipe transform = graph.peekTransform();
        if(transform instanceof MosaicNetworkTransform)
        {
            SettingsData settings = SettingsData.readCurrentSettings(getActivity());
            ((MosaicNetworkTransform) transform).setTileSize(settings.tileSize, settings.tileSize);
            ((MosaicNetworkTransform) transform).setAPIendpoint(settings.apiEndpoint);
        }
        graph.runLast();
    }

    void onMosaicCompleted(CompletedFeedback completedFeedback)
    {
        spinner.setVisibility(View.GONE);

        if (completedFeedback.isSuccessful())
        {
            if(completedFeedback.isPartial())
                statusMessage.setText(String.format("Partially completed in %dms", elapsedTime));
            else
                statusMessage.setText(String.format("Completed in %dms", elapsedTime));


        }else
        {
            imageOriginal.setVisibility(View.VISIBLE);
            imageSurface.setVisibility(View.INVISIBLE);
            statusMessage.setText(String.format("Failed (%s)",
                                                completedFeedback.getErrorDescription()));
        }

        resetControls();
        processing = false;
    }

    void onMosaicUpdate(ProgressFeedback progressFeedback)
    {
        if(progressFeedback.getCompletion() == 1.f)
        {
            progressBar.setVisibility(View.INVISIBLE);
        }else
        {
            progressBar.setProgress((int) (progressBar.getMax() * progressFeedback.getCompletion()));
            elapsedTime = progressFeedback.getElapsed();
        }
    }

    private void resetControls()
    {
        enableButton(selectImageBtn, true);
        enableButton(shareBtn, graph != null);
        enableButton(runBtn, true);
        enableButton(settingsBtn, true);

        progressBar.setVisibility(View.INVISIBLE);
        progressBar.setProgress(0);

        runBtn.setImageResource(R.drawable.ic_play_arrow_black_24dp);
    }

    private void enableButton(View button, boolean enable)
    {
        button.setEnabled(enable);
        button.setAlpha(enable ? 1.f : 0.3f);
    }
}
