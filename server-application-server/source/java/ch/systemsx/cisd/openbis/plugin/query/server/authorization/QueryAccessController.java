/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.query.server.authorization;

import java.util.Map;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.plugin.query.server.authorization.resultfilter.QueryResultFilter;
import ch.systemsx.cisd.openbis.plugin.query.shared.DatabaseDefinition;

/**
 * @author Piotr Buczek
 */
public class QueryAccessController
{

    private static final Logger authorizationLog = LogFactory.getLogger(LogCategory.AUTH,
            QueryAccessController.class);

    private static IDAOFactory daoFactory;

    private static Map<String, DatabaseDefinition> definitionsByDbKey;

    public static void initialize(IDAOFactory factory, Map<String, DatabaseDefinition> definitions)
    {
        QueryAccessController.daoFactory = factory;
        QueryAccessController.definitionsByDbKey = definitions;
    }

    public static void checkWriteAccess(Session session, String dbKey, String operation)
    {
        DatabaseDefinition database = definitionsByDbKey.get(dbKey);
        PersonPE person = session.tryGetPerson();
        SpacePE dataSpaceOrNull = database.tryGetDataSpace();
        RoleWithHierarchy minimalRole = database.getCreatorMinimalRole();

        checkAuthorization(session, operation, database, person, dataSpaceOrNull, minimalRole);
    }

    public static void checkReadAccess(Session session, String dbKey)
    {
        DatabaseDefinition database = definitionsByDbKey.get(dbKey);
        PersonPE person = session.tryGetPerson();
        SpacePE dataSpaceOrNull = database.tryGetDataSpace();
        RoleWithHierarchy minimalRole = null;

        if (dataSpaceOrNull == null)
        {
            minimalRole = RoleWithHierarchy.PROJECT_OBSERVER;
        } else
        {
            minimalRole = RoleWithHierarchy.SPACE_OBSERVER;
        }

        checkAuthorization(session, "perform", database, person, dataSpaceOrNull, minimalRole);
    }

    private static void checkAuthorization(Session session, String operation,
            DatabaseDefinition database, PersonPE person, SpacePE dataSpaceOrNull,
            RoleWithHierarchy minimalRole)
    {
        if (isAuthorized(person, dataSpaceOrNull, minimalRole) == false)
        {
            String errorMsg =
                    createErrorMessage(operation, session.getUserName(), dataSpaceOrNull,
                            minimalRole, database.getLabel());
            throw createAuthorizationError(session, operation, errorMsg);
        }
    }

    static boolean isAuthorized(PersonPE person, SpacePE dataSpaceOrNull,
            RoleWithHierarchy minimalRole)
    {
        return new AuthorizationChecker(daoFactory.getAuthorizationConfig()).isAuthorized(person, dataSpaceOrNull, minimalRole);
    }

    private static String createErrorMessage(String operation, String userName,
            SpacePE dataSpaceOrNull, RoleWithHierarchy minimalRole, String database)
    {
        String minimalRoleDescription = minimalRole.name();
        if (dataSpaceOrNull != null)
        {
            minimalRoleDescription += " in space " + dataSpaceOrNull.getCode();
        }

        return String.format("User '%s' does not have enough privileges to %s "
                + "a query in database '%s'. One needs to be at least %s.", userName, operation,
                database, minimalRoleDescription);
    }

    private static AuthorizationFailureException createAuthorizationError(Session session,
            String operation, String errorMessage)
    {
        final String groupCode = session.tryGetHomeGroupCode();
        authorizationLog
                .info(String.format("[USER:'%s' SPACE:%s]: Authorization failure while "
                        + "trying to %s a query: %s", session.getUserName(),
                        groupCode == null ? "<UNDEFINED>" : "'" + groupCode + "'", operation,
                        errorMessage));
        return new AuthorizationFailureException(errorMessage);
    }

    public static TableModel filterResults(PersonPE person, String dbKey, IDAOFactory factory,
            TableModel table)
    {
        DatabaseDefinition database = definitionsByDbKey.get(dbKey);
        // If the data space has been configured, it is assumed that all data belongs to that
        // space and no further filtering is needed.
        if (database.tryGetDataSpace() != null)
        {
            return table;
        }
        return new QueryResultFilter(factory).filterResults(person, table);
    }

}
