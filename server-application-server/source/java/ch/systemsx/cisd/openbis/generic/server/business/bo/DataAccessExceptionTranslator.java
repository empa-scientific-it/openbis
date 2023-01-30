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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import java.sql.SQLException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.common.db.SQLStateUtils;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * This class converts the low-level {@link DataAccessException} into a high-level exception {@link UserFailureException}.
 * <p>
 * This class can only be used on the business layer side as, only there, we dispose of enough information to decide whether or not a
 * <code>DataAccessException</code> should be translated into an high-level exception.
 * </p>
 * <p>
 * Do not try to put this logic on the <i>DAO</i> level or do not try to automate this conversion as we want full control on this translation and we
 * prefer to call it manually.
 * </p>
 * 
 * @author Tomasz Pylak
 */
public final class DataAccessExceptionTranslator
{

    /** Message format for unique violation. */
    public final static String UNIQUE_VIOLATION_FORMAT =
            "%s already exists in the database and needs to be unique.";

    /** Message format for foreign key violation. */
    public final static String FOREIGN_KEY_VIOLATION_FORMAT =
            "%s is being used. Delete all connected data %s first.";

    public final static String DETAILED_FOREIGN_KEY_VIOLATION_FORMAT = FOREIGN_KEY_VIOLATION_FORMAT
            + " To find out which exactly objects are connected to this object "
            + "go to its Detail view or use Search.";

    private DataAccessExceptionTranslator()
    {
        // Can not be instantiated.
    }

    /**
     * Analyzes given <code>DataAccessException</code> and converts it into a <code>UserFailureException</code>.
     * <p>
     * This method is typically used by <i>creator</i> methods (methods which inserts a new object into the database) and <i>deletion</i> methods.
     * </p>
     * 
     * @param subject short description of the object that got blessed by the unique/foreign key violation constraint.
     * @param entityKindOrNull entity kind of the subject object (if specified can make the error message more detailed especially for foreign key
     *            violation upon deletion)
     */
    public final static void throwException(final DataAccessException exception,
            final String subject, final EntityKind entityKindOrNull) throws UserFailureException
    {
        assert StringUtils.isNotBlank(subject) : "Given subject can not be blank.";
        throwExceptionWithMsg(exception, StringUtils.capitalize(subject), entityKindOrNull);
    }

    private final static void throwExceptionWithMsg(final DataAccessException exception,
            final String subject, final EntityKind entityKindOrNull) throws UserFailureException
    {
        assert exception != null : "DataAccessException not specified.";
        final SQLException sqlException =
                SQLStateUtils.tryGetNextExceptionWithNonNullState(exception);
        Throwable throwable = exception;
        if (sqlException != null)
        {
            final String sqlState = sqlException.getSQLState();
            assert sqlState != null : "SQL state is null";
            if (SQLStateUtils.isUniqueViolation(sqlState))
            {
                throw new UserFailureException(String.format(UNIQUE_VIOLATION_FORMAT, subject),
                        exception);
            } else if (SQLStateUtils.isForeignKeyViolation(sqlState))
            {
                throwForeignKeyViolationException(subject, entityKindOrNull, exception);
            } else
            {
                throwable = sqlException;
            }
        }
        throw new UserFailureException(throwable.getMessage(), exception);
    }

    public static void throwForeignKeyViolationException(final String subject,
            EntityKind entityKindOrNull) throws UserFailureException
    {
        throwForeignKeyViolationException(subject, entityKindOrNull, null);
    }

    private static void throwForeignKeyViolationException(final String subject,
            EntityKind entityKindOrNull, Exception exception) throws UserFailureException
    {
        throw new UserFailureException(String.format(
                getForeignKeyViolationFormat(entityKindOrNull), subject,
                getForeignKeyViolationDetailedDescription(entityKindOrNull)), exception);
    }

    private static String getForeignKeyViolationFormat(EntityKind entityKindOrNull)
    {
        return entityKindOrNull == null ? FOREIGN_KEY_VIOLATION_FORMAT
                : DETAILED_FOREIGN_KEY_VIOLATION_FORMAT;
    }

    private static String getForeignKeyViolationDetailedDescription(EntityKind entityKindOrNull)
    {

        if (entityKindOrNull == null)
        {
            return "";
        }
        switch (entityKindOrNull)
        {
            case DATA_SET:
                return "";
            case EXPERIMENT:
                return "(data sets, samples)";
            case MATERIAL:
                return "";
            case SAMPLE:
                return "(data sets, contained and generated samples)";
        }
        return "";
    }
}
