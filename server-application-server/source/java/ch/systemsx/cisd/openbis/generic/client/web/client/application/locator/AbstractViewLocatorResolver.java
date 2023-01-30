package ch.systemsx.cisd.openbis.generic.client.web.client.application.locator;

import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.PermlinkUtilities;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;

/**
 * Default implementation of the IViewLocatorHandler interface. Designed to be subclassed.
 * <p>
 * The default implementation is bound to one particular action. The method {@link #canHandleLocator(ViewLocator)} returns true if the locator's
 * action matches my handledAction.
 * 
 * @author Chandrasekhar Ramakrishnan
 * @author Piotr Buczek
 */
public abstract class AbstractViewLocatorResolver implements IViewLocatorResolver
{
    private final String handledAction;

    public AbstractViewLocatorResolver(String handledAction)
    {
        assert handledAction != null;
        this.handledAction = handledAction;
    }

    @Override
    public boolean canHandleLocator(ViewLocator locator)
    {
        return handledAction.equals(locator.tryGetAction());
    }

    @Override
    public void locatorExists(ViewLocator locator, AsyncCallback<Void> callback)
    {
        callback.onSuccess(null);
    }

    protected static final boolean getMandatoryBooleanParameter(ViewLocator locator,
            String paramName)
    {
        String value = getMandatoryParameter(locator, paramName);
        return new Boolean(value);
    }

    protected static final Boolean getOptionalBooleanParameter(ViewLocator locator, String paramName)
    {
        String value = getOptionalParameter(locator, paramName);
        if (value == null)
        {
            return null;
        }
        return new Boolean(value);
    }

    protected static final boolean getOptionalBooleanParameter(ViewLocator locator,
            String paramName, boolean defaultValue)
    {
        Boolean valueOrNull = getOptionalBooleanParameter(locator, paramName);
        if (valueOrNull == null)
        {
            return defaultValue;
        } else
        {
            return valueOrNull.booleanValue();
        }
    }

    protected static final String getMandatoryParameter(ViewLocator locator, String paramName)
    {
        String valueOrNull = getOptionalParameter(locator, paramName);
        if (valueOrNull == null)
        {
            throw createMissingParamException(paramName);
        }
        return valueOrNull;
    }

    protected static final String getOptionalParameter(ViewLocator locator, String paramName)
    {
        String valueOrNull = locator.getParameters().get(paramName);
        return valueOrNull;
    }

    /**
     * Utility method that throws an exception with a standard error message if the required paramter is not specified
     */
    protected static final void checkRequiredParameter(String valueOrNull, String paramName)
            throws UserFailureException
    {
        if (valueOrNull == null)
        {
            throw createMissingParamException(paramName);
        }
    }

    private static UserFailureException createMissingParamException(String paramName)
    {
        return new UserFailureException("Missing URL parameter: " + paramName);
    }

    protected static final EntityKind getEntityKind(ViewLocator locator)
    {
        String entityKindValueOrNull = locator.tryGetEntity();
        checkRequiredParameter(entityKindValueOrNull, PermlinkUtilities.ENTITY_KIND_PARAMETER_KEY);
        return getEntityKind(entityKindValueOrNull);
    }

    protected static final EntityKind getEntityKind(String entityKindValueOrNull)
    {
        try
        {
            return EntityKind.valueOf(entityKindValueOrNull);
        } catch (IllegalArgumentException exception)
        {
            throw new UserFailureException("Invalid '" + PermlinkUtilities.ENTITY_KIND_PARAMETER_KEY
                    + "' URL parameter value.");
        }
    }

    protected class LocatorExistsCallback<T> implements AsyncCallback<T>
    {

        private AsyncCallback<Void> callback;

        public LocatorExistsCallback(AsyncCallback<Void> callback)
        {
            this.callback = callback;
        }

        @Override
        public final void onSuccess(T result)
        {
            if (result != null)
            {
                callback.onSuccess(null);
            } else
            {
                callback.onFailure(null);
            }
        }

        @Override
        public final void onFailure(Throwable caught)
        {
            callback.onFailure(null);
        }
    }
}