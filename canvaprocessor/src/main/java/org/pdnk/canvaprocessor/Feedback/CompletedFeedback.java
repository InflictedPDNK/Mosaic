package org.pdnk.canvaprocessor.Feedback;

/**
 * Created by pnovodon on 8/09/2016.
 */
public class CompletedFeedback
{
    private boolean successful;
    private boolean partial;
    private String errorDescription;

    public CompletedFeedback(boolean successful, boolean partial, String errorDescription)
    {

        this.successful = successful;
        this.partial = partial;
        this.errorDescription = errorDescription;
    }

    public boolean isSuccessful()
    {
        return successful;
    }

    public boolean isPartial()
    {
        return partial;
    }

    public String getErrorDescription()
    {
        return errorDescription;
    }
}
