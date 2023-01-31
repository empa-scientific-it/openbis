/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.common.action.IMapper;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.etlserver.registrator.api.v1.impl.AuthorizationHelper;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedBasicOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ManagedAuthentication;
import ch.systemsx.cisd.openbis.generic.shared.IServiceForDataStoreServer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetTypeWithVocabularyTerms;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListMaterialCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MetaprojectAssignments;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MetaprojectAssignmentsFetchOption;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.IObjectId;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * The basic version of encapsulated openbis service, that calls the etl service and filters the result to visible only to the specified user.
 * 
 * @author Jakub Straszewski
 */
public class EncapsulatedFilteredBasicOpenBISService implements IEncapsulatedBasicOpenBISService
{
    private final IServiceForDataStoreServer etlService;

    private final String systemSessionToken;

    private final IEncapsulatedOpenBISService encapsulatedService;

    private final String userName;

    public EncapsulatedFilteredBasicOpenBISService(String userName, IServiceForDataStoreServer etlService,
            IEncapsulatedOpenBISService encapsulatedService, String systemSessionToken)
    {
        this.etlService = etlService;
        this.encapsulatedService = encapsulatedService;

        this.systemSessionToken = systemSessionToken;

        this.userName = userName;
    }

    private final static IMapper<AbstractExternalData, String> externalDataCodeMapper =
            new IMapper<AbstractExternalData, String>()
                {
                    @Override
                    public String map(AbstractExternalData item)
                    {
                        return item == null ? null : item.getCode();
                    }
                };

    private final static IMapper<Sample, String> sampleIdMapper = new IMapper<Sample, String>()
        {
            @Override
            public String map(Sample item)
            {
                return item == null ? null : item.getIdentifier();
            }
        };

    private final static IMapper<Experiment, String> experimentIdMapper =
            new IMapper<Experiment, String>()
                {
                    @Override
                    public String map(Experiment item)
                    {
                        return item == null ? null : item.getIdentifier();
                    }
                };

    @Override
    public Project tryGetProject(ProjectIdentifier projectIdentifier) throws UserFailureException
    {
        return encapsulatedService.tryGetProject(projectIdentifier);
    }

    @Override
    public Space tryGetSpace(SpaceIdentifier spaceIdentifier) throws UserFailureException
    {
        return encapsulatedService.tryGetSpace(spaceIdentifier);
    }

    @Override
    public DataSetTypeWithVocabularyTerms getDataSetType(String dataSetTypeCode)
    {

        return etlService.getDataSetType(systemSessionToken, dataSetTypeCode);
    }

    @Override
    public List<Sample> searchForSamples(SearchCriteria searchCriteria)
    {

        List<Sample> samples = etlService.searchForSamples(systemSessionToken, searchCriteria);

        return AuthorizationHelper.filterToVisible(encapsulatedService, userName, samples,
                sampleIdMapper, AuthorizationHelper.EntityKind.SAMPLE);
    }

    @Override
    public List<Experiment> searchForExperiments(SearchCriteria searchCriteria)
    {
        List<Experiment> experiments = etlService.searchForExperiments(systemSessionToken, searchCriteria);

        return AuthorizationHelper.filterToVisible(encapsulatedService, userName, experiments, experimentIdMapper,
                AuthorizationHelper.EntityKind.EXPERIMENT);
    }

    @Override
    @ManagedAuthentication
    public Sample tryGetSampleWithExperiment(SampleIdentifier sampleIdentifier)
            throws UserFailureException
    {
        Sample sample = encapsulatedService.tryGetSampleWithExperiment(sampleIdentifier);
        return AuthorizationHelper.filterToVisible(encapsulatedService, userName, sample,
                sampleIdMapper, AuthorizationHelper.EntityKind.SAMPLE);
    }

    @Override
    public List<AbstractExternalData> searchForDataSets(SearchCriteria searchCriteria)
    {
        List<AbstractExternalData> datasets =
                etlService.searchForDataSets(systemSessionToken, searchCriteria);

        return AuthorizationHelper.filterToVisible(encapsulatedService, userName, datasets,
                externalDataCodeMapper, AuthorizationHelper.EntityKind.DATA_SET);
    }

    @Override
    public List<Experiment> listExperiments(ProjectIdentifier projectIdentifier)
    {

        List<Experiment> experiments =
                etlService.listExperiments(systemSessionToken, projectIdentifier);

        return AuthorizationHelper.filterToVisible(encapsulatedService, userName, experiments,
                experimentIdMapper, AuthorizationHelper.EntityKind.EXPERIMENT);
    }

    @Override
    @ManagedAuthentication
    public Experiment tryGetExperiment(ExperimentIdentifier experimentIdentifier)
            throws UserFailureException
    {
        Experiment experiment = encapsulatedService.tryGetExperiment(experimentIdentifier);
        return AuthorizationHelper.filterToVisible(encapsulatedService, userName, experiment,
                experimentIdMapper, AuthorizationHelper.EntityKind.EXPERIMENT);
    }

    @Override
    public Collection<VocabularyTerm> listVocabularyTerms(String vocabularyCode)
            throws UserFailureException
    {
        return etlService.listVocabularyTerms(systemSessionToken, vocabularyCode);
    }

    @Override
    public Vocabulary tryGetVocabulary(String code)
    {
        return etlService.tryGetVocabulary(systemSessionToken, code);
    }

    @Override
    public List<Material> listMaterials(ListMaterialCriteria criteria, boolean withProperties)
    {
        return etlService.listMaterials(systemSessionToken, criteria, withProperties);
    }

    @Override
    @ManagedAuthentication
    public Material tryGetMaterial(MaterialIdentifier materialIdentifier)
    {
        return encapsulatedService.tryGetMaterial(materialIdentifier);
    }

    @Override
    public List<? extends EntityTypePropertyType<?>> listPropertyDefinitionsForEntityType(
            String code, EntityKind entityKind)
    {
        return encapsulatedService.listPropertyDefinitionsForEntityType(code, entityKind);
    }

    @Override
    public List<Metaproject> listMetaprojects()
    {
        return etlService.listMetaprojects(systemSessionToken, userName);
    }

    @Override
    public Metaproject tryGetMetaproject(String name)
    {
        return etlService.tryGetMetaproject(systemSessionToken, name, userName);
    }

    @Override
    public MetaprojectAssignments getMetaprojectAssignments(String name)
    {
        return etlService.getMetaprojectAssignments(systemSessionToken, name, userName,
                EnumSet.allOf(MetaprojectAssignmentsFetchOption.class));
    }

    @Override
    public List<Metaproject> listMetaprojectsForEntity(IObjectId entityId)
    {
        return etlService.listMetaprojectsForEntity(systemSessionToken, userName, entityId);
    }

    @Override
    public Map<IObjectId, List<Metaproject>> listMetaprojectsForEntities(Collection<? extends IObjectId> entityIds)
    {
        return etlService.listMetaprojectsForEntities(systemSessionToken, userName, entityIds);
    }

    @Override
    public AbstractExternalData tryGetDataSet(String dataSetCode) throws UserFailureException
    {
        AbstractExternalData data = encapsulatedService.tryGetDataSet(dataSetCode);
        return AuthorizationHelper.filterToVisible(encapsulatedService, userName, data,
                externalDataCodeMapper, AuthorizationHelper.EntityKind.DATA_SET);
    }

    @Override
    public AbstractExternalData tryGetThinDataSet(String dataSetCode) throws UserFailureException
    {
        AbstractExternalData data = encapsulatedService.tryGetThinDataSet(dataSetCode);
        return AuthorizationHelper.filterToVisible(encapsulatedService, userName, data,
                externalDataCodeMapper, AuthorizationHelper.EntityKind.DATA_SET);
    }

    @Override
    public AbstractExternalData tryGetLocalDataSet(String dataSetCode) throws UserFailureException
    {
        AbstractExternalData data = encapsulatedService.tryGetLocalDataSet(dataSetCode);
        return AuthorizationHelper.filterToVisible(encapsulatedService, userName, data,
                externalDataCodeMapper, AuthorizationHelper.EntityKind.DATA_SET);
    }

    @Override
    public Experiment tryGetExperimentByPermId(String permId) throws UserFailureException
    {
        Experiment experiment = encapsulatedService.tryGetExperimentByPermId(permId);
        return AuthorizationHelper.filterToVisible(encapsulatedService, userName, experiment,
                experimentIdMapper, AuthorizationHelper.EntityKind.EXPERIMENT);
    }

    @Override
    public Project tryGetProjectByPermId(String permId) throws UserFailureException
    {
        return encapsulatedService.tryGetProjectByPermId(permId);
    }

    @Override
    public Sample tryGetSampleByPermId(String permId) throws UserFailureException
    {
        Sample sample = encapsulatedService.tryGetSampleByPermId(permId);
        return AuthorizationHelper.filterToVisible(encapsulatedService, userName, sample,
                sampleIdMapper, AuthorizationHelper.EntityKind.SAMPLE);
    }

    @Override
    public String getSessionToken()
    {
        return systemSessionToken;
    }
}
