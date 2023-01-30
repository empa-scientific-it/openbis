/*
 * Copyright 2008 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.systemsx.cisd.openbis.generic.server.authorization;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.collection.FilteredList;
import ch.systemsx.cisd.common.collection.IModifiable;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.reflection.MethodUtils;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.ReturnValueFilter;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.ICollectionValidator;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.IValidator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IAuthorizationDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSession;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * A default <code>IReturnValueFilter</code> implementation
 * 
 * @author Christian Ribeaud
 */
public final class DefaultReturnValueFilter implements IReturnValueFilter
{

    @Private
    static final String FILTER_APPLIED_ON_VALUE =
            "Filter applied on method '%s': return value filtered out.";

    @Private
    static final String FILTER_APPLIED_ON_LIST =
            "Filter applied on method '%s': %d items filtered out.";

    @Private
    static final String FILTER_APPLIED_ON_ARRAY =
            "Filter applied on method '%s': %d items filtered out.";

    @Private
    static final String NO_ANNOTATION_FOUND_MSG_FORMAT =
            "No filter applied on method '%s': no annotation '%s' found.";

    @Private
    static final String NULL_RETURN_VALUE_MSG_FORMAT =
            "No filter applied on method '%s': return value is null.";

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            DefaultReturnValueFilter.class);

    private IAuthorizationDataProvider authorizationDataProvider;

    /**
     * @param daoFactory
     */
    public DefaultReturnValueFilter(IAuthorizationDAOFactory daoFactory)
    {
        authorizationDataProvider = new AuthorizationDataProvider(daoFactory);
    }

    @SuppressWarnings("unchecked")
    private final <T> IValidator<T> getValidator(final ReturnValueFilter annotation)
    {
        return ValidatorStore.getValidatorForClass(
                (Class<? extends IValidator<T>>) annotation.validatorClass(),
                authorizationDataProvider);
    }

    @SuppressWarnings("unchecked")
    private final static <T> T[] castToArray(final Object value)
    {
        return (T[]) value;
    }

    @SuppressWarnings("unchecked")
    private final static <T> List<T> castToList(final Object value)
    {
        return (List<T>) value;
    }

    @SuppressWarnings("unchecked")
    private final static <T> Collection<T> castToCollection(final Object value)
    {
        return (Collection<T>) value;
    }

    @SuppressWarnings("unchecked")
    private final static <T> T cast(final Object value)
    {
        return (T) value;
    }

    private final static <T> List<T> proceedList(final PersonPE person, final Method method,
            final List<T> returnValue, final IValidator<T> validator)
    {
        if (returnValue.size() == 0)
        {
            return returnValue;
        }

        final List<T> list;

        if (returnValue instanceof IModifiable)
        {
            list = returnValue;
        } else
        {
            // We are on the safe side if we wrap the list in a new one (could be an
            // unmodifiable one).
            list = new ArrayList<T>(returnValue);
        }

        final int oldSize = list.size();
        // Does not create a new list but might modify the original one.
        FilteredList.decorate(list, new ValidatorAdapter<T>(validator, person));
        int diff = oldSize - list.size();
        if (diff > 0)
        {
            operationLog.info(String.format(FILTER_APPLIED_ON_LIST,
                    MethodUtils.describeMethod(method), diff));
        }
        return list;
    }

    private final static <T> T[] proceedArray(final PersonPE person, final Method method,
            final IValidator<T> validator, final T[] returnValue)
    {
        if (returnValue.length == 0)
        {
            return returnValue;
        }
        final List<T> list =
                FilteredList.decorate(returnValue, new ValidatorAdapter<T>(validator, person));
        final T[] array =
                castToArray(Array.newInstance(returnValue.getClass().getComponentType(),
                        list.size()));
        final T[] newValue = castToArray(list.toArray(array));
        int diff = returnValue.length - newValue.length;
        if (diff > 0)
        {
            operationLog.info(String.format(FILTER_APPLIED_ON_ARRAY,
                    MethodUtils.describeMethod(method), diff));
        }
        return newValue;
    }

    private final static <T> Object proceedValue(final PersonPE person, final Method method,
            final Object returnValue, final IValidator<T> validator, final T value)
    {
        if (validator.isValid(person, value))
        {
            return returnValue;
        }
        operationLog
                .info(String.format(FILTER_APPLIED_ON_VALUE, MethodUtils.describeMethod(method)));
        return null;
    }

    private final static void logTimeTaken(final StopWatch stopWatch, final Method method)
    {
        stopWatch.stop();
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("Return value filtering of method '%s' took %s",
                    MethodUtils.describeMethod(method), stopWatch));
        }
    }

    @Private
    final <T> Object proceed(final PersonPE person, final Method method, final Object returnValue,
            final IValidator<T> validator)
    {
        final String validatorClassName = validator.getClass().getName();
        final Class<?> returnValueClass = returnValue.getClass();
        if (returnValue instanceof List<?>)
        {
            final List<T> list = castToList(returnValue);
            try
            {
                return proceedList(person, method, list, validator);
            } catch (final ClassCastException ex)
            {
                throw new IllegalArgumentException(String.format("Given validator class '%s' "
                        + "and list type '%s' are not compatible.", validatorClassName, list.get(0)
                                .getClass().getName()));
            }
        } else if (returnValueClass.isArray())
        {
            final T[] array = castToArray(returnValue);
            try
            {
                return proceedArray(person, method, validator, array);
            } catch (final ClassCastException e)
            {
                throw new IllegalArgumentException(String.format("Given validator class '%s' "
                        + "and array type '%s' are not compatible.", validatorClassName,
                        returnValueClass.getComponentType().getName()));
            }
        } else
        {
            final T value = cast(returnValue);
            try
            {
                return proceedValue(person, method, returnValue, validator, value);
            } catch (final ClassCastException ex)
            {
                throw new IllegalArgumentException(String.format("Given validator class '%s' "
                        + "and return value type '%s' are not compatible.", validatorClassName,
                        returnValueClass.getName()));
            }
        }
    }

    private Object proceedCollection(PersonPE person, Method method, Object returnValue, ICollectionValidator<?> validator)
    {
        if (returnValue instanceof Collection<?>)
        {
            return validator.getValid(person, castToCollection(returnValue));
        } else
        {
            throw new IllegalArgumentException(
                    String.format("Returned value '%s' is not a collection and cannot be validated by a collection validator '%s'.",
                            returnValue.getClass(), validator.getClass().getName()));
        }
    }

    //
    // IReturnValueFilter
    //

    @Override
    public final Object applyFilter(final IAuthSession session, final Method method,
            final Object returnValueOrNull)
    {
        assert session != null : "Unspecified session";
        assert method != null : "Unspecified method";
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try
        {
            if (returnValueOrNull == null)
            {
                operationLog.debug(String.format(NULL_RETURN_VALUE_MSG_FORMAT,
                        MethodUtils.describeMethod(method)));
                return returnValueOrNull;
            }
            final ReturnValueFilter annotation = method.getAnnotation(ReturnValueFilter.class);
            if (annotation == null)
            {
                operationLog
                        .debug(String.format(NO_ANNOTATION_FOUND_MSG_FORMAT,
                                MethodUtils.describeMethod(method),
                                ReturnValueFilter.class.getSimpleName()));
                return returnValueOrNull;
            }

            final IValidator<?> validator = getValidator(annotation);

            if (validator instanceof ICollectionValidator<?>)
            {
                return proceedCollection(session.tryGetPerson(), method, returnValueOrNull, (ICollectionValidator<?>) validator);
            } else
            {
                return proceed(session.tryGetPerson(), method, returnValueOrNull, validator);
            }
        } finally
        {
            logTimeTaken(stopWatch, method);
        }
    }

    //
    // Helper classes
    //

    private final static class ValidatorAdapter<T> implements
            ch.systemsx.cisd.common.collection.IValidator<T>
    {

        private final IValidator<T> validator;

        private final PersonPE person;

        ValidatorAdapter(final IValidator<T> validator, final PersonPE person)
        {
            this.validator = validator;
            this.person = person;
        }

        //
        // IValidator
        //

        @Override
        public final boolean isValid(final T object)
        {
            return validator.isValid(person, object);
        }
    }
}
