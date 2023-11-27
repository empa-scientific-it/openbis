/*
 * Copyright ETH 2017 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.person;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.IObjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.create.PersonCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.PersonPermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.context.IProgress;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractCreateEntityExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.CommonUtils;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.CollectionBatch;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.CollectionBatchProcessor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch.MapBatch;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.entity.progress.CreateProgress;
import ch.systemsx.cisd.authentication.IAuthenticationService;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.common.spring.ExposablePropertyPlaceholderConfigurer;
import ch.systemsx.cisd.openbis.generic.server.AuthenticationServiceHolder;
import ch.systemsx.cisd.openbis.generic.server.business.bo.DataAccessExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DisplaySettings;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * @author Franz-Josef Elmer
 */
@Component
public class CreatePersonExecutor
        extends AbstractCreateEntityExecutor<PersonCreation, PersonPE, PersonPermId>
        implements ICreatePersonExecutor
{
    private static final String ALLOW_MISSING_USER_CREATION_PROPERTY = "allow-missing-user-creation";

    @Autowired
    private IDAOFactory daoFactory;

    @Autowired
    private ISetPersonSpaceExecutor setPersonSpaceExecutor;

    @Autowired
    private AuthenticationServiceHolder authenticationServiceHolder;

    @Autowired
    private IPersonAuthorizationExecutor authorizationExecutor;

    @Resource(name = ExposablePropertyPlaceholderConfigurer.PROPERTY_CONFIGURER_BEAN_NAME)
    private ExposablePropertyPlaceholderConfigurer configurer;

    @Override
    protected IObjectId getId(PersonPE entity)
    {
        return new PersonPermId(entity.getUserId());
    }

    @Override
    protected void checkData(IOperationContext context, PersonCreation creation)
    {
        if (StringUtils.isEmpty(creation.getUserId()))
        {
            throw new UserFailureException("Unspecified user id.");
        }
    }

    @Override
    protected void checkAccess(IOperationContext context)
    {
        authorizationExecutor.canCreate(context);
    }

    @Override
    protected void checkAccess(IOperationContext context, PersonPE entity)
    {
    }

    @Override
    protected List<PersonPE> createEntities(IOperationContext context, CollectionBatch<PersonCreation> batch)
    {
        final List<PersonPE> persons = new LinkedList<>();
        final List<String> unknownUsers = new ArrayList<String>();
        final DisplaySettings displaySettings = getDefaultDisplaySettings();
        final PersonPE registrator = context.getSession().tryGetPerson();
        boolean allowMissingUserCreation = PropertyUtils.getBoolean(configurer.getResolvedProps(),
                ALLOW_MISSING_USER_CREATION_PROPERTY, false);
        final IAuthenticationService authenticationService = authenticationServiceHolder.getAuthenticationService();
        new CollectionBatchProcessor<PersonCreation>(context, batch)
            {
                @Override
                public void process(PersonCreation personCreation)
                {
                    String userId = personCreation.getUserId();
                    PersonPE person = new PersonPE();
                    person.setDisplaySettings(displaySettings);
                    person.setActive(true);
                    person.setRegistrator(registrator);
                    try
                    {
                        Principal principal = authenticationService.getPrincipal(userId);
                        person.setUserId(principal.getUserId());
                        person.setFirstName(principal.getFirstName());
                        person.setLastName(principal.getLastName());
                        person.setEmail(principal.getEmail());
                        persons.add(person);
                    } catch (IllegalArgumentException ex)
                    {
                        if (allowMissingUserCreation)
                        {
                            person.setUserId(normalizeUserId(userId));
                            persons.add(person);
                        } else
                        {
                            unknownUsers.add(userId);
                        }
                    }
                }

                @Override
                public IProgress createProgress(PersonCreation object, int objectIndex, int totalObjectCount)
                {
                    return new CreateProgress(object, objectIndex, totalObjectCount);
                }
            };
        if (unknownUsers.size() > 0)
        {
            throw UserFailureException.fromTemplate(
                    "Following persons unknown by the authentication service: [%s]",
                    StringUtils.join(unknownUsers, ","));
        }
        return persons;
    }

    private String normalizeUserId(String userId)
    {
        return userId.replace("@", "_AT_");
    }

    private DisplaySettings getDefaultDisplaySettings()
    {
        PersonPE systemUser = daoFactory.getPersonDAO().tryFindPersonByUserId(PersonPE.SYSTEM_USER_ID);
        if (systemUser == null)
        {
            throw new UserFailureException(
                    "Couldn't find system user with default settings in the DB.");
        }
        return systemUser.getDisplaySettings();
    }

    @Override
    protected PersonPermId createPermId(IOperationContext context, PersonPE entity)
    {
        return new PersonPermId(entity.getUserId());
    }

    @Override
    protected void updateBatch(IOperationContext context, MapBatch<PersonCreation, PersonPE> batch)
    {
        setPersonSpaceExecutor.set(context, batch);
    }

    @Override
    protected void updateAll(IOperationContext context, MapBatch<PersonCreation, PersonPE> batch)
    {
    }

    @Override
    protected List<PersonPE> list(IOperationContext context, Collection<Long> ids)
    {
        return CommonUtils.listPersons(daoFactory, ids);
    }

    @Override
    protected void save(IOperationContext context, List<PersonPE> entities, boolean clearCache)
    {
        for (PersonPE person : entities)
        {
            try
            {
                PersonPE systemUser = daoFactory.getPersonDAO().tryFindPersonByUserId(PersonPE.SYSTEM_USER_ID);
                if (systemUser == null)
                {
                    throw new UserFailureException(
                            "Couldn't find system user with default settings in the DB.");
                }

                PersonPE existingPerson = daoFactory.getPersonDAO().tryFindPersonByUserId(person.getUserId());
                if (existingPerson != null){
                    throw new UserFailureException(
                            "User with User Id [" + person.getUserId() + "] already exists!"
                    );
                }
                daoFactory.getPersonDAO().createPerson(person);
            } catch (final DataAccessException e)
            {
                throw new UserFailureException(e.getMessage(), e);
            }
        }
    }

    @Override
    protected void handleException(DataAccessException e)
    {
        DataAccessExceptionTranslator.throwException(e, "person", null);
    }

}
