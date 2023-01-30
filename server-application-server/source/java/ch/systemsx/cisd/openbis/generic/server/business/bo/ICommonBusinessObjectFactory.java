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

import ch.systemsx.cisd.openbis.generic.server.business.bo.datasetlister.IDatasetLister;
import ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.ISampleLister;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.IEntityResolverQuery;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.impl.IMasterDataScriptRegistrationRunner;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * The <i>generic</i> specific <i>Business Object</i> factory. Each method creates one kind of a business object.
 * 
 * @author Tomasz Pylak
 */
public interface ICommonBusinessObjectFactory extends IAbstractBussinessObjectFactory
{
    public IAttachmentBO createAttachmentBO(final Session session);

    public ISpaceBO createSpaceBO(final Session session);

    public IScriptBO createScriptBO(final Session session);

    public IRoleAssignmentTable createRoleAssignmentTable(final Session session);

    public ISampleTable createSampleTable(final Session session);

    public ISampleBO createSampleBO(final Session session);

    public IDataBO createDataBO(Session session);

    public IDataSetTable createDataSetTable(final Session session);

    public ISearchDomainSearcher createSearchDomainSearcher(Session session);

    public IDeletedDataSetTable createDeletedDataSetTable(final Session session);

    public IExperimentTable createExperimentTable(final Session session);

    public IExperimentBO createExperimentBO(final Session session);

    public IPropertyTypeTable createPropertyTypeTable(final Session session);

    public IPropertyTypeBO createPropertyTypeBO(final Session session);

    public IVocabularyBO createVocabularyBO(final Session session);

    public IVocabularyTermBO createVocabularyTermBO(final Session session);

    public IEntityTypePropertyTypeBO createEntityTypePropertyTypeBO(Session session,
            EntityKind entityKind);

    public IProjectBO createProjectBO(final Session session);

    public IEntityTypeBO createEntityTypeBO(Session session);

    public IAuthorizationGroupBO createAuthorizationGroupBO(Session session);

    public IGridCustomFilterOrColumnBO createGridCustomFilterBO(final Session session);

    public IGridCustomFilterOrColumnBO createGridCustomColumnBO(final Session session);

    public ITrashBO createTrashBO(final Session session);

    public IDeletionTable createDeletionTable(Session session);

    public ICorePluginTable createCorePluginTable(Session session,
            IMasterDataScriptRegistrationRunner masterDataScriptRunner);

    public IDataStoreBO createDataStoreBO(Session session);

    // Fast listing operations
    public ISampleLister createSampleLister(Session session);

    public ISampleLister createSampleLister(Session session, Long userId);

    public IDatasetLister createDatasetLister(Session session);

    public IDatasetLister createDatasetLister(Session session, Long userId);

    public IMetaprojectBO createMetaprojectBO(Session session);

    /**
     * Returns the entity resolver.
     */
    public IEntityResolverQuery getEntityResolver();

}
