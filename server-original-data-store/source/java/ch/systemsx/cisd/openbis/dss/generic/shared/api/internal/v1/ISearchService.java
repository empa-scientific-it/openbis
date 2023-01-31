/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public interface ISearchService
{
    /**
     * List all experiments for a given project.
     * 
     * @param projectIdentifier The project identifier as a string (e.g., /SPACE-CODE/PROJECT-CODE).
     * @return A list of experiments for the specified project.
     */
    public List<IExperimentImmutable> listExperiments(String projectIdentifier);

    /**
     * List all data sets with a given value for a particular property, optionally restricted to a specific type.
     * 
     * @param property The property of interest.
     * @param value The value the property should have. This may contain wildcards.
     * @return A list of matching data sets.
     */
    public List<IDataSetImmutable> searchForDataSets(String property, String value,
            String typeOrNull);

    /**
     * List all data sets with a given value for a particular property, optionally restricted to a specific type.
     * 
     * @param property The property of interest.
     * @param value The value the property should have. This may contain wildcards if <var>escape</var> is set to <code>false</code>.
     * @param escape If <code>true</code>, escape the <var>value</var> to search for it verbatimly.
     * @return A list of matching data sets.
     */
    public List<IDataSetImmutable> searchForDataSets(String property, String value,
            String typeOrNull, boolean escape);

    /**
     * List all samples with a given value for a particular property, optionally restricted to a specific type.
     * 
     * @param property The property of interest.
     * @param value The value the property should have. This may contain wildcards.
     * @return A list of matching samples.
     */
    public List<ISampleImmutable> searchForSamples(String property, String value, String typeOrNull);

    /**
     * List all samples with a given value for a particular property, optionally restricted to a specific type.
     * 
     * @param property The property of interest.
     * @param value The value the property should have. This may contain wildcards if <var>escape</var> is set to <code>false</code>.
     * @param escape If <code>true</code>, escape the <var>value</var> to search for it verbatimly.
     * @return A list of matching samples.
     */
    public List<ISampleImmutable> searchForSamples(String property, String value,
            String typeOrNull, boolean escape);

    /**
     * List all data sets that match the given searchCriteria.
     * 
     * @param searchCriteria The criteria to match against.
     * @return A list of matching data sets.
     */
    public List<IDataSetImmutable> searchForDataSets(SearchCriteria searchCriteria);

    /**
     * List all samples that match the given searchCriteria.
     * 
     * @param searchCriteria The criteria to match against.
     * @return A list of matching samples.
     */
    public List<ISampleImmutable> searchForSamples(SearchCriteria searchCriteria);

    /**
     * @param identifierCollection a collection containing the identifiers of the matching materials. Identifiers that do not exist in the openBIS
     *            database are ignored.
     * @return a list of materials matching the specified collection.
     */
    public List<IMaterialImmutable> listMaterials(MaterialIdentifierCollection identifierCollection);

    /**
     * @return a controlled vocabulary with the given code. Returns null if the vocabulary with given code is not found.
     * @deprecated use {@link #getVocabulary(String)} instead
     */
    @Deprecated
    public IVocabularyImmutable searchForVocabulary(String code);

    /**
     * @return a controlled vocabulary with the given code. Returns null if the vocabulary with given code is not found.
     */
    public IVocabularyImmutable getVocabulary(String code);

    /**
     * @return the list of property definitions for a data set type.
     */
    public List<IPropertyAssignmentImmutable> listPropertiesDefinitionsForDataSetType(String code);

    /**
     * @return the list of property definitions for a sample type.
     */
    public List<IPropertyAssignmentImmutable> listPropertiesDefinitionsForSampleType(String code);

    /**
     * @return the list of property definitions for an experiment type.
     */
    public List<IPropertyAssignmentImmutable> listPropertiesDefinitionsForExperimentType(String code);

    /**
     * @return the list of property definitions for a material type.
     */
    public List<IPropertyAssignmentImmutable> listPropertiesDefinitionsForMaterialType(String code);

    /**
     * @return the list of metaprojects for the current user.
     */
    public List<IMetaprojectImmutable> listMetaprojects();

    /**
     * @return the assignments for the given metaproject for current user.
     */
    public IMetaprojectAssignments getMetaprojectAssignments(String name);

    /**
     * @return metaprojects for current user, which are assigned to the given entity
     */
    public List<IMetaprojectImmutable> listMetaprojectsForEntity(IMetaprojectContent entity);

    /**
     * Get a sample from the openBIS AS by its perm id. Returns null if the sample does not exist.
     * 
     * @return A sample or null
     */
    ISampleImmutable getSampleByPermId(String permId);

    /**
     * Get a project from the openBIS AS by its perm id.
     */
    IProjectImmutable getProjectByPermId(String permId);

    /**
     * Get an experiment from the openBIS AS by its perm id.
     */
    IExperimentImmutable getExperimentByPermId(String permId);
}
