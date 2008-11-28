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

package ch.systemsx.cisd.openbis.plugin.generic.server;

import java.util.Arrays;
import java.util.List;

import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.openbis.generic.server.AbstractServerLogger;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ListSampleCriteriaDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleGenerationDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleToRegisterDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SearchHit;
import ch.systemsx.cisd.openbis.generic.shared.dto.SearchableEntity;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.GroupIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.plugin.generic.shared.IGenericServer;

/**
 * Logger class for {@link GenericServer} which creates readable logs of method invocations.
 * 
 * @author Franz-Josef Elmer
 */
final class GenericServerLogger extends AbstractServerLogger implements IGenericServer
{
    /**
     * Creates an instance for the specified session manager and invocation status. The session
     * manager is used to retrieve user information which will be a part of the log message.
     */
    GenericServerLogger(final ISessionManager<Session> sessionManager,
            final boolean invocationSuccessful)
    {
        super(sessionManager, invocationSuccessful);
    }

    //
    // IGenericServer
    //

    public List<GroupPE> listGroups(final String sessionToken,
            final DatabaseInstanceIdentifier identifier)
    {
        final String command = "list_groups";
        if (identifier == null || identifier.getDatabaseInstanceCode() == null)
        {
            logAccess(sessionToken, command);
        } else
        {
            logAccess(sessionToken, command, "DATABASE-INSTANCE(%s)", identifier);
        }
        return null;
    }

    public void registerGroup(final String sessionToken, final String groupCode,
            final String descriptionOrNull, final String groupLeaderOrNull)
    {
        logTracking(sessionToken, "register_group", "CODE(%s)", groupCode);
    }

    public List<PersonPE> listPersons(final String sessionToken)
    {
        logAccess(sessionToken, "list_persons");
        return null;
    }

    public void registerPerson(final String sessionToken, final String userID)
    {
        logTracking(sessionToken, "register_person", "CODE(%s)", userID);

    }

    public List<RoleAssignmentPE> listRoles(final String sessionToken)
    {
        logAccess(sessionToken, "list_roles");
        return null;
    }

    public void registerGroupRole(final String sessionToken, final RoleCode roleCode,
            final GroupIdentifier groupIdentifier, final String person)
    {
        logTracking(sessionToken, "register_role", "ROLE(%s) GROUP(%s) PERSON(%s)", roleCode,
                groupIdentifier, person);

    }

    public void registerInstanceRole(final String sessionToken, final RoleCode roleCode,
            final String person)
    {
        logTracking(sessionToken, "register_role", "ROLE(%s)  PERSON(%s)", roleCode, person);

    }

    public void deleteGroupRole(final String sessionToken, final RoleCode roleCode,
            final GroupIdentifier groupIdentifier, final String person)
    {
        logTracking(sessionToken, "delete_role", "ROLE(%s) GROUP(%s) PERSON(%s)", roleCode,
                groupIdentifier, person);

    }

    public void deleteInstanceRole(final String sessionToken, final RoleCode roleCode,
            final String person)
    {
        logTracking(sessionToken, "delete_role", "ROLE(%s) PERSON(%s)", roleCode, person);

    }

    public final List<SampleTypePE> listSampleTypes(final String sessionToken)
    {
        logAccess(sessionToken, "list_sample_types");
        return null;
    }

    public final List<SamplePE> listSamples(final String sessionToken,
            final ListSampleCriteriaDTO criteria)
    {
        logAccess(sessionToken, "list_samples", "TYPE(%s) OWNERS(%s)", criteria.getSampleType(),
                criteria.getOwnerIdentifiers());
        return null;
    }

    public final List<SamplePropertyPE> listSamplesProperties(final String sessionToken,
            final ListSampleCriteriaDTO criteria, final List<PropertyTypePE> propertyCodes)
    {
        logAccess(sessionToken, "list_samples_properties", "CRITERIA(%s) PROPERTIES(%s)", criteria,
                propertyCodes.size());
        return null;
    }

    public final SampleGenerationDTO getSampleInfo(final String sessionToken,
            final SampleIdentifier identifier)
    {
        logTracking(sessionToken, "get_sample_info", "IDENTIFIER(%s)", identifier);
        return null;
    }

    public final List<ExternalDataPE> listExternalData(final String sessionToken,
            final SampleIdentifier identifier)
    {
        logTracking(sessionToken, "list_external_data", "IDENTIFIER(%s)", identifier);
        return null;
    }

    public final List<SearchHit> listMatchingEntities(final String sessionToken,
            final SearchableEntity[] searchableEntities, final String queryText)
    {
        logTracking(sessionToken, "list_matching_entities",
                "SEARCHABLE-ENTITIES(%s) QUERY-TEXT(%s)", Arrays.toString(searchableEntities),
                queryText);
        return null;
    }

    public void registerSample(String sessionToken, SampleToRegisterDTO newSample)
    {
        logTracking(sessionToken, "register_sample", "SAMPLE_TYPE(%s) SAMPLE(%S)", newSample
                .getSampleTypeCode(), newSample.getSampleIdentifier());
    }
}
