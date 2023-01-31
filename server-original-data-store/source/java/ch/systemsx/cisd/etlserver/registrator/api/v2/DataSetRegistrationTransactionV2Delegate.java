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

package ch.systemsx.cisd.etlserver.registrator.api.v2;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import net.lemnik.eodsql.DynamicTransactionQuery;

import ch.systemsx.cisd.etlserver.TopLevelDataSetRegistratorGlobalState;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationContext;
import ch.systemsx.cisd.etlserver.registrator.api.v2.impl.DataSetRegistrationTransaction;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.IAttachmentImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.IDataSetImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.IExperimentImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.IExternalDataManagementSystemImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.IMaterialImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.IProjectImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.ISampleImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.ISearchService;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.ISpaceImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.IVocabularyImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.authorization.IAuthorizationService;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;

/**
 * @author Jakub Straszewski
 */
public class DataSetRegistrationTransactionV2Delegate implements IDataSetRegistrationTransactionV2
{
    private DataSetRegistrationTransaction<?> transaction;

    public DataSetRegistrationTransactionV2Delegate(DataSetRegistrationTransaction<?> transaction)
    {
        this.transaction = transaction;
    }

    @Override
    public String getOpenBisServiceSessionToken()
    {
        return transaction.getOpenBisServiceSessionToken();
    }

    @Override
    public IDataSet createNewDataSet()
    {
        return transaction.createNewDataSet();
    }

    @Override
    public IDataSet createNewDataSet(String dataSetType)
    {
        return transaction.createNewDataSet(dataSetType);
    }

	@Override
	public IDataSet createNewDataSet(String dataSetType, DataSetKind datasetKindOrNull)
	{
        return transaction.createNewDataSet(dataSetType, datasetKindOrNull);		
	}

	@Override
    public IDataSet createNewDataSet(String dataSetType, String dataSetCode)
    {
        return transaction.createNewDataSet(dataSetType, dataSetCode);
    }

	@Override
	public IDataSet createNewDataSet(String dataSetType, String dataSetCode, DataSetKind datasetKindOrNull)
	{
        return transaction.createNewDataSet(dataSetType, dataSetCode, datasetKindOrNull);
	}

    @Override
    public IDataSetImmutable getDataSet(String dataSetCode)
    {
        return transaction.getDataSet(dataSetCode);
    }

    @Override
    public IDataSetUpdatable getDataSetForUpdate(String dataSetCode)
    {
        return transaction.getDataSetForUpdate(dataSetCode);
    }

    @Override
    public IDataSetUpdatable makeDataSetMutable(IDataSetImmutable dataSet)
    {
        return transaction.makeDataSetMutable(dataSet);
    }

    @Override
    public ISampleImmutable getSample(String sampleIdentifierString)
    {
        return transaction.getSample(sampleIdentifierString);
    }

    @Override
    public ISample getSampleForUpdate(String sampleIdentifierString)
    {
        return transaction.getSampleForUpdate(sampleIdentifierString);
    }

    @Override
    public ISample makeSampleMutable(ISampleImmutable sample)
    {
        return transaction.makeSampleMutable(sample);
    }

    @Override
    public ISample createNewSample(String sampleIdentifierString, String sampleTypeCode)
    {
        return transaction.createNewSample(sampleIdentifierString, sampleTypeCode);
    }

    @Override
    public ISample createNewSampleWithGeneratedCode(String spaceCode, String sampleTypeCode)
    {
        return transaction.createNewSampleWithGeneratedCode(spaceCode, sampleTypeCode);
    }

    @Override
    public ISample createNewProjectSampleWithGeneratedCode(String projectIdentifier, String sampleTypeCode)
    {
        return transaction.createNewProjectSampleWithGeneratedCode(projectIdentifier, sampleTypeCode);
    }

    @Override
    public IExperimentImmutable getExperiment(String experimentIdentifierString)
    {
        return transaction.getExperiment(experimentIdentifierString);
    }

    @Override
    public IExperimentUpdatable getExperimentForUpdate(String experimentIdentifierString)
    {
        return transaction.getExperimentForUpdate(experimentIdentifierString);
    }

    @Override
    public IExperimentUpdatable makeExperimentMutable(IExperimentImmutable experiment)
    {
        return transaction.makeExperimentMutable(experiment);
    }

    @Override
    public IExperiment createNewExperiment(String experimentIdentifierString,
            String experimentTypeCode)
    {
        return transaction.createNewExperiment(experimentIdentifierString, experimentTypeCode);
    }

    @Override
    public IProject createNewProject(String projectIdentifier)
    {
        return transaction.createNewProject(projectIdentifier);
    }

    @Override
    public IProjectImmutable getProject(String projectIdentifier)
    {
        return transaction.getProject(projectIdentifier);
    }

    @Override
    public IProject getProjectForUpdate(String projectIdentifierString)
    {
        return transaction.getProjectForUpdate(projectIdentifierString);
    }

    @Override
    public IProject makeProjectMutable(IProjectImmutable project)
    {
        return transaction.makeProjectMutable(project);
    }

    @Override
    public ISpace createNewSpace(String spaceCode, String spaceAdminUserIdOrNull)
    {
        return transaction.createNewSpace(spaceCode, spaceAdminUserIdOrNull);
    }

    @Override
    public ISpaceImmutable getSpace(String spaceCode)
    {
        return transaction.getSpace(spaceCode);
    }

    @Override
    public IMaterialImmutable getMaterial(String materialCode, String materialType)
    {
        return transaction.getMaterial(materialCode, materialType);
    }

    @Override
    public IMaterialImmutable getMaterial(String identifier)
    {
        return transaction.getMaterial(identifier);
    }

    @Override
    public IMaterial getMaterialForUpdate(String materialCode, String materialType)
    {
        return transaction.getMaterialForUpdate(materialCode, materialType);
    }

    @Override
    public IMaterial getMaterialForUpdate(String identifier)
    {
        return transaction.getMaterialForUpdate(identifier);
    }

    @Override
    public IMaterial makeMaterialMutable(IMaterialImmutable material)
    {
        return transaction.makeMaterialMutable(material);
    }

    @Override
    public IMaterial createNewMaterial(String materialCode, String materialType)
    {
        return transaction.createNewMaterial(materialCode, materialType);
    }

    @Override
    public IMetaproject createNewMetaproject(String name, String description)
    {
        return transaction.createNewMetaproject(name, description);
    }

    @Override
    public IMetaproject createNewMetaproject(String name, String description, String ownerId)
    {
        return transaction.createNewMetaproject(name, description, ownerId);
    }

    @Override
    public IMetaproject getMetaproject(String name)
    {
        return transaction.getMetaproject(name);
    }

    @Override
    public IMetaproject getMetaproject(String name, String ownerId)
    {
        return transaction.getMetaproject(name, ownerId);
    }

    @Override
    public IVocabularyImmutable getVocabulary(String code)
    {
        return transaction.getVocabulary(code);
    }

    @Override
    public IVocabulary getVocabularyForUpdate(String code)
    {
        return transaction.getVocabularyForUpdate(code);
    }

    @Override
    public IVocabularyTerm createNewVocabularyTerm()
    {
        return transaction.createNewVocabularyTerm();
    }

    @Override
    public String moveFile(String src, IDataSet dst)
    {
        return transaction.moveFile(src, dst);
    }

    @Override
    public String moveFile(String src, IDataSet dst, String dstInDataset)
    {
        return transaction.moveFile(src, dst, dstInDataset);
    }

    @Override
    public String createNewDirectory(IDataSet dst, String dirName)
    {
        return transaction.createNewDirectory(dst, dirName);
    }

    @Override
    public String createNewFile(IDataSet dst, String fileName)
    {
        return transaction.createNewFile(dst, fileName);
    }

    @Override
    public String createNewFile(IDataSet dst, String dstInDataset, String fileName)
    {
        return transaction.createNewFile(dst, dstInDataset, fileName);
    }

    @Override
    public ISearchService getSearchService()
    {
        return transaction.getSearchService();
    }

    @Override
    public ISearchService getSearchServiceUnfiltered()
    {
        return transaction.getSearchServiceUnfiltered();
    }

    @Override
    public ISearchService getSearchServiceFilteredForUser(String userId)
    {
        return transaction.getSearchServiceFilteredForUser(userId);
    }

    @Override
    public IAuthorizationService getAuthorizationService()
    {
        return transaction.getAuthorizationService();
    }

    @Override
    public DynamicTransactionQuery getDatabaseQuery(String dataSourceName)
            throws IllegalArgumentException
    {
        return transaction.getDatabaseQuery(dataSourceName);
    }

    @Override
    public DataSetRegistrationContext getRegistrationContext()
    {
        return transaction.getRegistrationContext();
    }

    @Override
    public TopLevelDataSetRegistratorGlobalState getGlobalState()
    {
        return transaction.getGlobalState();
    }

    @Override
    public Map<String, String> getServerInformation()
    {
        return transaction.getServerInformation();
    }

    @Override
    public File getIncoming()
    {
        return transaction.getIncoming();
    }

    @Override
    public String getUserId()
    {
        return transaction.getUserId();
    }

    @Override
    public void setUserId(String userIdOrNull)
    {
        transaction.setUserId(userIdOrNull);
    }

    @Override
    public IExternalDataManagementSystemImmutable getExternalDataManagementSystem(
            String externalDataManagementSystemCode)
    {
        return transaction.getExternalDataManagementSystem(externalDataManagementSystemCode);
    }

    @Override
    public void assignRoleToSpace(RoleCode role, ISpaceImmutable space, List<String> userIds, List<String> groupCodes)
    {
        transaction.assignRoleToSpace(role, space, userIds, groupCodes);
    }

    @Override
    public void revokeRoleFromSpace(RoleCode role, ISpaceImmutable space, List<String> userIds, List<String> groupCodes)
    {
        transaction.revokeRoleFromSpace(role, space, userIds, groupCodes);
    }

    @Override
    public InputStream getAttachmentContent(IProjectImmutable project, String fileName, Integer versionOrNull)
    {
        return transaction.getAttachmentContent(project, fileName, versionOrNull);
    }

    @Override
    public InputStream getAttachmentContent(IExperimentImmutable experiment, String fileName, Integer versionOrNull)
    {
        return transaction.getAttachmentContent(experiment, fileName, versionOrNull);
    }

    @Override
    public InputStream getAttachmentContent(ISampleImmutable sample, String fileName, Integer versionOrNull)
    {
        return transaction.getAttachmentContent(sample, fileName, versionOrNull);
    }

    @Override
    public List<IAttachmentImmutable> listAttachments(IProjectImmutable project)
    {
        return transaction.listAttachments(project);
    }

    @Override
    public List<IAttachmentImmutable> listAttachments(IExperimentImmutable experiment)
    {
        return transaction.listAttachments(experiment);
    }

    @Override
    public List<IAttachmentImmutable> listAttachments(ISampleImmutable sample)
    {
        return transaction.listAttachments(sample);
    }

    @Override
    public String createNewLink(IDataSet dst, String dstInDataset, String linkName, String linkTarget)
    {
        return transaction.createNewLink(dst, dstInDataset, linkName, linkTarget);
    }

    @Override
    public IDSSRegistrationLogger getLogger()
    {
        return transaction.getLogger();
    }

}
