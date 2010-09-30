package ch.systemsx.cisd.openbis.generic.client.web.client.application.locator;

import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;

/**
 * Default implementation of the IViewLocatorHandler interface. Designed to be subclassed.
 * <p>
 * The default implementation is bound to one particular action. The method
 * {@link #canHandleLocator(ViewLocator)} returns true if the locator's action matches my
 * handledAction.
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

    public boolean canHandleLocator(ViewLocator locator)
    {
        return handledAction.equals(locator.tryGetAction());
    }

    protected final static boolean getMandatoryBooleanParameter(ViewLocator locator,
            String paramName)
    {
        String value = getMandatoryParameter(locator, paramName);
        return new Boolean(value);
    }

    protected final static String getMandatoryParameter(ViewLocator locator, String paramName)
    {
        String valueOrNull = locator.getParameters().get(paramName);
        if (valueOrNull == null)
        {
            throw createMissingParamException(paramName);
        }
        return valueOrNull;
    }

    /**
     * Utility method that throws an exception with a standard error message if the required
     * paramter is not specified
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
        checkRequiredParameter(entityKindValueOrNull, ViewLocator.ENTITY_PARAMETER);
        return getEntityKind(entityKindValueOrNull);
    }

    protected static final EntityKind getEntityKind(String entityKindValueOrNull)
    {
        try
        {
            return EntityKind.valueOf(entityKindValueOrNull);
        } catch (IllegalArgumentException exception)
        {
            throw new UserFailureException("Invalid '" + ViewLocator.ENTITY_PARAMETER
                    + "' URL parameter value.");
        }
    }
}