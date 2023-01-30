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

package ch.systemsx.cisd.openbis.plugin.query.server;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.properties.PropertyParametersUtil;
import ch.systemsx.cisd.common.properties.PropertyParametersUtil.SectionProperties;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.common.spring.ExposablePropertyPlaceholderConfigurer;
import ch.systemsx.cisd.dbmigration.SimpleDatabaseConfigurationContext;
import ch.systemsx.cisd.openbis.generic.server.ComponentNames;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.authorization.AuthorizationConfigFacade;
import ch.systemsx.cisd.openbis.generic.shared.authorization.IAuthorizationConfig;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.plugin.query.server.authorization.QueryAccessController;
import ch.systemsx.cisd.openbis.plugin.query.shared.DatabaseDefinition;
import ch.systemsx.cisd.openbis.plugin.query.shared.IQueryDatabaseDefinitionProvider;
import ch.systemsx.cisd.openbis.plugin.query.shared.ResourceNames;

/**
 * Basic implementation of {@link IQueryDatabaseDefinitionProvider}
 * 
 * @author Piotr Buczek
 */
@Component(ResourceNames.QUERY_DATABASE_DEFINITION_PROVIDER)
public class QueryDatabaseDefinitionProvider implements IQueryDatabaseDefinitionProvider
{

    /** property with database keys/names separated by delimiter */
    private static final String DATABASE_KEYS = "query-databases";

    private static final String LABEL_PROPERTY_KEY = "label";

    private static final String CREATOR_MINIMAL_ROLE_KEY = "creator-minimal-role";

    private static final String DATA_SPACE_KEY = "data-space";

    private static final String DEFAULT_CREATOR_MINIMAL_ROLE_SPACE =
            RoleWithHierarchy.SPACE_POWER_USER.name();

    private static final String DEFAULT_CREATOR_MINIMAL_ROLE_INSTANCE =
            RoleWithHierarchy.INSTANCE_OBSERVER.name();

    @Resource(name = ExposablePropertyPlaceholderConfigurer.PROPERTY_CONFIGURER_BEAN_NAME)
    private ExposablePropertyPlaceholderConfigurer configurer;

    @Resource(name = ComponentNames.DAO_FACTORY)
    private IDAOFactory daoFactory;

    @Autowired
    private IAuthorizationConfig authorizationConfig;

    /**
     * map from dbKey to DatabaseDefinition
     */
    private Map<String, DatabaseDefinition> definitions;

    @Override
    public void initDatabaseDefinitions()
    {
        if (definitions != null)
        {
            return;
        }

        definitions = new HashMap<String, DatabaseDefinition>();
        Properties resolvedProps = configurer.getResolvedProps();
        SectionProperties[] sectionsProperties =
                PropertyParametersUtil
                        .extractSectionProperties(resolvedProps, DATABASE_KEYS, false);
        Set<String> labels = new HashSet<String>();
        for (int i = 0; i < sectionsProperties.length; i++)
        {
            final String databaseKey = sectionsProperties[i].getKey();
            final Properties databaseProperties = sectionsProperties[i].getProperties();

            final SimpleDatabaseConfigurationContext configurationContext =
                    new SimpleDatabaseConfigurationContext(databaseProperties);
            final String label =
                    PropertyUtils.getMandatoryProperty(databaseProperties, LABEL_PROPERTY_KEY);
            final String dataSpaceOrNullString =
                    PropertyUtils.getProperty(databaseProperties, DATA_SPACE_KEY);
            final String creatorMinimalRoleString =
                    PropertyUtils.getProperty(databaseProperties, CREATOR_MINIMAL_ROLE_KEY,
                            getDefaultRoleForDataSource(dataSpaceOrNullString));

            if (labels.contains(label))
            {
                throw new UnsupportedOperationException(
                        "Query databases need to have unique labels but '" + label
                                + "' label is used more than once.");
            }
            labels.add(label);
            SpacePE dataSpaceOrNull = null;
            if (dataSpaceOrNullString != null)
            {
                dataSpaceOrNull =
                        daoFactory.getSpaceDAO().tryFindSpaceByCode(
                                dataSpaceOrNullString);
                if (dataSpaceOrNull == null)
                {
                    throw new UnsupportedOperationException("Query database '" + databaseKey
                            + "' is not defined properly. Space '" + dataSpaceOrNullString
                            + "' doesn't exist.");
                }
            }
            try
            {
                final RoleWithHierarchy creatorMinimalRole =
                        RoleWithHierarchy.valueOf(creatorMinimalRoleString);

                AuthorizationConfigFacade configFacade = new AuthorizationConfigFacade(authorizationConfig);

                if (configFacade.isRoleEnabled(creatorMinimalRole))
                {
                    definitions.put(databaseKey, new DatabaseDefinition(configurationContext,
                            databaseKey, label, creatorMinimalRole, dataSpaceOrNull));
                } else
                {
                    throw new UnsupportedOperationException("Query database '" + databaseKey
                            + "' is not defined properly. '" + creatorMinimalRoleString
                            + "' is not a valid role.");
                }
            } catch (IllegalArgumentException ex)
            {
                throw new UnsupportedOperationException("Query database '" + databaseKey
                        + "' is not defined properly. '" + creatorMinimalRoleString
                        + "' is not a valid role.");
            }

        }
        QueryAccessController.initialize(daoFactory, definitions);
    }

    private static String getDefaultRoleForDataSource(final String dataSpaceOrNull)
    {
        if (dataSpaceOrNull == null) // database contains data for the whole instance
        {
            return DEFAULT_CREATOR_MINIMAL_ROLE_INSTANCE;
        } else
        {
            return DEFAULT_CREATOR_MINIMAL_ROLE_SPACE;
        }
    }

    @Override
    public DatabaseDefinition getDefinition(String dbKey)
    {
        checkInitialization();
        return definitions.get(dbKey);
    }

    @Override
    public Collection<DatabaseDefinition> getAllDefinitions() throws UserFailureException
    {
        checkInitialization();
        return definitions.values();
    }

    private void checkInitialization()
    {
        if (definitions == null)
        {
            throw UserFailureException.fromTemplate("Query databases were not initialized yet.");
        }
    }

}
