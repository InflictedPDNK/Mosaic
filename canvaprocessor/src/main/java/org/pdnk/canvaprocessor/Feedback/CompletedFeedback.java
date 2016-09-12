package org.pdnk.canvaprocessor.Feedback;

/**
 * Created by pnovodon on 8/09/2016.
 */

/**
 * Completion feedback. Sent by nodes and graph upon completion, respectively, of node procedure or
 * total graph run
 */
public class CompletedFeedback
{
    private boolean successful;
    private boolean partial;
    private String errorDescription;


    /**
     *
     * @param successful true if operation was successful. If the sender tried to complete partially but was
     * disallowed for doing so, the operation must be marked as not successful.
     * @param partial true if node completed partially, granted it was allowed to do so
     * @param errorDescription description of error if operation failed or null
     */
    public CompletedFeedback(boolean successful, boolean partial, String errorDescription)
    {

        this.successful = successful;
        this.partial = partial;
        this.errorDescription = errorDescription;
    }

    /**
     *
     * @return true if operation was successful. If the sender tried to complete partially but was
     * disallowed for doing so, the operation will be marked as not successful.
     */
    public boolean isSuccessful()
    {
        return successful;
    }

    /**
     *
     * @return true if node completed partially, granted it was allowed to do so
     */
    public boolean isPartial()
    {
        return partial;
    }

    /**
     *
     * @return description of error if operation failed or null
     */
    public String getErrorDescription()
    {
        return errorDescription;
    }
}
