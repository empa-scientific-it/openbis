package ch.ethz.sis.openbis.generic.asapi.v3.exceptions;

import ch.systemsx.cisd.common.exceptions.UserFailureException;

public class NotFetchedException extends UserFailureException
{
    private static final long serialVersionUID = 1L;

    public NotFetchedException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public NotFetchedException(String message)
    {
        super(message);
    }

}
