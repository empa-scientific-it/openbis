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

package ch.systemsx.cisd.openbis.dss.client.api.v1;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

import ch.systemsx.cisd.common.api.retry.Retry;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.ControlledVocabularyPropertyType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet.Connections;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.NewVocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SampleFetchOption;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.WebAppSettings;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;

/**
 * Provides a fully-blown API for openBIS integration. The internals of the openBIS architecture are abstracted away from the API users (e.g. they do
 * not need to know openBIS is actually two servers - AS and DSS).
 * <p>
 * For the most basic operations (simple listing operations, data set upload/download) you can use instances of {@link IOpenbisServiceFacade} as
 * "simple" {@link ISimpleOpenbisServiceFacade}.
 * </p>
 * 
 * @author Kaloyan Enimanev
 */
public interface IOpenbisServiceFacade extends ISimpleOpenbisServiceFacade
{

    /**
     * Returns the persistent settings for a given custom web app.
     * 
     * @param webAppId The id of the custom web app to get the display settings for.
     */
    public WebAppSettings getWebAppSettings(String webAppId);

    /**
     * Sets the persistent settings for a given custom web app.
     * 
     * @param customDisplaySettings The new display settings
     */
    public void setWebAppSettings(WebAppSettings customDisplaySettings);

    /**
     * Returns all experiments matching specified search criteria.
     */
    public List<Experiment> searchForExperiments(SearchCriteria searchCriteria);

    /**
     * Return all samples that match the search criteria. This is a short cut for
     * 
     * <pre>
     * searchForSamples(searchCritera, EnumSet.of(SampleFetchOption.PROPERTIES))
     * </pre>
     * 
     * @param searchCriteria The sample metadata values to be matched against.
     */
    @Retry
    public List<Sample> searchForSamples(SearchCriteria searchCriteria);

    /**
     * Return all samples that match the search criteria.
     * 
     * @param searchCriteria The sample metadata values to be matched against.
     * @param fetchOptions Describes the amount of information about the sample that is needed. For more details see
     *            {@link IGeneralInformationService#searchForSamples(String, SearchCriteria, EnumSet)} .
     */
    @Retry
    public List<Sample> searchForSamples(SearchCriteria searchCriteria,
            EnumSet<SampleFetchOption> fetchOptions);

    /**
     * Return all data sets matching a specified search criteria.
     * 
     * @param searchCriteria the criteria used for searching.
     */
    @Retry
    public List<DataSet> searchForDataSets(SearchCriteria searchCriteria);

    /**
     * Return all data sets attached to the given samples with connections.
     * 
     * @param samples The samples for which we return attached data sets.
     */
    @Retry
    public List<DataSet> listDataSets(List<Sample> samples, EnumSet<Connections> connectionsToGet);

    /**
     * Adds new ad-hoc terms to a vocabulary starting from specified ordinal + 1.
     * 
     * @param vocabularyId The id of vocabulary which should be extended.
     * @param code Code of new vocabulary term.
     * @param label Label of new vocabulary term.
     * @param description Free text describing new vocabulary term.
     * @param previousTermOrdinal new vocabulary term will be placed right after vocabulary term with given ordinal number.
     * @deprecated Please use the {@link #addAdHocVocabularyTerm(Long, NewVocabularyTerm)} method instead.
     */
    @Deprecated
    public void addAdHocVocabularyTerm(TechId vocabularyId, String code, String label,
            String description, Long previousTermOrdinal);

    /**
     * Adds new ad-hoc terms to a vocabulary starting from specified ordinal + 1.
     * 
     * @param vocabularyId The id of vocabulary which should be extended.
     * @param term the vocabulary term to be added.
     */
    public void addAdHocVocabularyTerm(Long vocabularyId, NewVocabularyTerm term);

    /**
     * Returns map of avaialable vocabulary terms. Available since minor version 6.
     * 
     * @deprecated Please use the {@link #listVocabularies()} method instead.
     */
    @Deprecated
    @Retry
    public HashMap<Vocabulary, List<ControlledVocabularyPropertyType.VocabularyTerm>> getVocabularyTermsMap();
}
