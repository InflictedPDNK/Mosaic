package org.pdnk.mosaicproto.application.Fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.pdnk.canvaprocessor.Common.ParametricRunnable;
import org.pdnk.canvaprocessor.Feedback.CompletedFeedback;
import org.pdnk.canvaprocessor.Feedback.ProgressFeedback;
import org.pdnk.canvaprocessor.Graph.Graph;
import org.pdnk.canvaprocessor.SinkNode.SimpleSurfaceRenderer;
import org.pdnk.canvaprocessor.SourceNode.BitmapSource;
import org.pdnk.canvaprocessor.TransformPipe.NullTransform;
import org.pdnk.mosaicproto.R;
import org.pdnk.mosaicproto.Utility.Utility;
import org.pdnk.mosaicproto.application.Fragments.Data.MessageDlgData;
import org.pdnk.mosaicproto.application.Fragments.Data.ProcessPageData;

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

        layoutElements(myView);
        return myView;
    }

    private void layoutElements(ViewGroup myView)
    {
        resetControls();
    }

    @OnClick(R.id.selectImageBtn)
    protected void onSelectImageClick()
    {
        Utility.showImageChooser(this, SELECT_IMAGE_RESULT);
    }

    @OnClick(R.id.runBtn)
    protected void onRunClick()
    {
        if(processing)
        {
            resetControls();
            if(graph != null)
            {
                graph.cancel();
                statusMessage.setText(R.string.status_cancelled);
            }
        }else
        {
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

        imageSurface.setVisibility(View.VISIBLE);

        graph = new Graph.Builder()
        .setSourceNode(new BitmapSource(bitmapOriginal))
        .setSinkNode(new SimpleSurfaceRenderer(imageSurface))
        .addTransformPipe(new NullTransform())
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
        graph.runLast();
    }

    void onMosaicCompleted(CompletedFeedback completedFeedback)
    {
        if (completedFeedback.isSuccessful())
        {
            if(completedFeedback.isPartial())
                statusMessage.setText(String.format("Partially completed in %dms", elapsedTime));
            else
                statusMessage.setText(String.format("Completed in %dms", elapsedTime));

            imageOriginal.setVisibility(View.INVISIBLE);
        }else
        {
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
        enableButton(shareBtn, graph != null && graph.readGraphOutput() != null);
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
