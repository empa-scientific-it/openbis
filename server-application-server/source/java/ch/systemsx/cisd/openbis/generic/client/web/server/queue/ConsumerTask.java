package ch.systemsx.cisd.openbis.generic.client.web.server.queue;

import java.io.IOException;
import java.io.Writer;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.client.web.server.UploadedFilesBean;

public abstract class ConsumerTask
{
    private UploadedFilesBean filesForTask;

    public abstract String getName();

    public abstract String getUserEmail();

    public abstract void doActionOrThrowException(Writer writer);

    public ConsumerTask(UploadedFilesBean filesForTask) throws ch.systemsx.cisd.common.exceptions.UserFailureException
    {
        this.filesForTask = filesForTask;
    }

    protected UploadedFilesBean getFilesForTask()
    {
        return filesForTask;
    }

    public boolean doAction(Writer messageWriter)
    {
        try
        {
            doActionOrThrowException(messageWriter);
        } catch (RuntimeException ex)
        {
            try
            {
                messageWriter.write(getName() + " has failed with a following exception: ");
                messageWriter.write(ex.getMessage());
                messageWriter.write("\n\nPlease correct the error or contact your administrator.");
            } catch (IOException writingEx)
            {
                throw new UserFailureException(writingEx.getMessage()
                        + " when trying to throw exception: " + ex.getMessage(), ex);
            }
            throw ex;
        } finally
        {
            filesForTask.deleteTransferredFiles();
        }
        return true;
    }

}
