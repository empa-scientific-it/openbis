package ch.systemsx.cisd.etlserver.entityregistration;

/**
 * An interface to describe success and errors encountered during registration.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
interface IRegistrationStatus
{
    boolean isError();

    Throwable getError();

    String getMessage();
}

/**
 * Abstract superclass for success and errors.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
abstract class AbstractRegistrationStatus implements IRegistrationStatus
{
    protected AbstractRegistrationStatus()
    {
    }
}

class RegistrationError extends AbstractRegistrationStatus
{
    private final Throwable error;

    RegistrationError(Throwable error)
    {
        this.error = error;
    }

    @Override
    public boolean isError()
    {
        return true;
    }

    @Override
    public Throwable getError()
    {
        return error;
    }

    @Override
    public String getMessage()
    {
        return error.getMessage();
    }
}

class RegistrationSuccess extends AbstractRegistrationStatus
{
    RegistrationSuccess(String[] registeredMetadata)
    {
    }

    @Override
    public boolean isError()
    {
        return false;
    }

    @Override
    public Throwable getError()
    {
        assert false : "getError() should not be called on a success object";

        return null;
    }

    @Override
    public String getMessage()
    {
        return "Success";
    }
}
