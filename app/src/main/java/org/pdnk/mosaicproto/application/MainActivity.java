package org.pdnk.mosaicproto.application;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.pdnk.canvaprocessor.Common.ParametricRunnable;
import org.pdnk.canvaprocessor.Data.DataDescriptor;
import org.pdnk.canvaprocessor.Feedback.CompletedFeedback;
import org.pdnk.canvaprocessor.Feedback.ProgressFeedback;
import org.pdnk.canvaprocessor.Graph.Graph;
import org.pdnk.canvaprocessor.SinkNode.ByteArraySink;
import org.pdnk.canvaprocessor.SourceNode.ByteArraySource;
import org.pdnk.canvaprocessor.TransformPipe.NullTransform;
import org.pdnk.canvaprocessor.TransformPipe.ReverseTransform;
import org.pdnk.mosaicproto.R;

public class MainActivity extends AppCompatActivity
{
    Graph g;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        g = new Graph.Builder()
        .setSourceNode(new ByteArraySource("Test string".getBytes()))
        .setSinkNode(new ByteArraySink())
        .addTransformPipe(new ReverseTransform())
        .addTransformPipe(new NullTransform())
        .setEnableCacheOutput(true)
        .setOnCompletionFeedback(new ParametricRunnable<CompletedFeedback>()
        {
            boolean once;
            @Override
            public void run(CompletedFeedback param)
            {
                Log.d("TEST", "Completed: " + param.isSuccessful() + " Error: " + param.getErrorDescription());

                if (param.isSuccessful())
                {

                    DataDescriptor d = g.readGraphOutput();
                    Log.d("TEST", "OUTPUT: " + new String(d.getData()));

                    if(!once)
                    {
                        once = true;
                        g.pushTransform(new ReverseTransform());
                        g.runLast();
                    }

                }
            }
        })
        .setOnProgressFeedback(new ParametricRunnable<ProgressFeedback>()
        {
            @Override
            public void run(ProgressFeedback param)
            {
                Log.d("TEST",
                      "Completion: " + param.getCompletion() + " Elapsed: " + param.getElapsed());
            }
        }).buildGraph();


        g.run();
    }
}
