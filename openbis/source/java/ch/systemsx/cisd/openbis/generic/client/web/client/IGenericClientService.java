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

package ch.systemsx.cisd.openbis.generic.client.web.client;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Group;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Person;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.RoleAssignment;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Sample;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;

/**
 * Service interface for the generic GWT client.
 * 
 * @author Franz-Josef Elmer
 */
public interface IGenericClientService extends IClientService
{
    /**
     * Returns a list of all groups which belong to the specified database instance.
     */
    public List<Group> listGroups(String databaseInstanceCode) throws UserFailureException;

    /**
     * Registers a new group with specified code and optional description and group leader ID.
     */
    public void registerGroup(String groupCode, String descriptionOrNull, String groupLeaderOrNull)
            throws UserFailureException;

    /**
     * Returns a list of all persons which belong to the current database instance.
     */
    public List<Person> listPersons() throws UserFailureException;

    /**
     * Registers a new person with specified code.
     */
    public void registerPerson(String code) throws UserFailureException;

    /**
     * Returns a list of all roles.
     */
    public List<RoleAssignment> listRoles() throws UserFailureException;

    /**
     * Registers a new role from given role set code, group code and person code
     */
    public void registerGroupRole(String roleSetCode, String group, String person)
            throws UserFailureException;

    /**
     * Deletes the role described by given role set code, group code and person code
     */
    public void deleteGroupRole(String roleSetCode, String group, String person)
            throws UserFailureException;

    /**
     * Registers a new role from given role set code and person code
     */
    public void registerInstanceRole(String roleSetCode, String person) throws UserFailureException;

    /**
     * Deletes the role described by given role set code and person code
     */
    public void deleteInstanceRole(String roleSetCode, String person) throws UserFailureException;

    /**
     * Returns a list of sample types.
     */
    public List<SampleType> listSampleTypes() throws UserFailureException;

    /**
     * Returns a list of samples for given sample type.
     */
    public List<Sample> listSamples(SampleType sampleType, String groupCode, boolean includeGroup,
            boolean includeInstance, List<PropertyType> propertyCodes) throws UserFailureException;

    /** Returns a list of samples with updated properties */
    public List<Sample> updateSamples(List<Sample> samples, List<PropertyType> propertyCodes)
            throws UserFailureException;

    /**
     * For given <var>sampleIdentifier</var> returns corresponding {@link Sample}.
     */
    public Sample getSampleInfo(final String sampleIdentifier) throws UserFailureException;
}
