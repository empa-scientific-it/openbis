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

package ch.systemsx.cisd.openbis.generic.server.dataaccess;

import java.util.Date;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.ICodeSequenceDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.IPermIdDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * A <i>Data Access Object</i> factory.
 * 
 * @author Franz-Josef Elmer
 */
public interface IDAOFactory extends IAuthorizationDAOFactory
{
    public Date getTransactionTimestamp();

    /**
     * Returns the {@link ISampleTypeDAO} implementation.
     */
    public ISampleTypeDAO getSampleTypeDAO();

    /**
     * Returns the {@link IHibernateSearchDAO} implementation.
     */
    public IHibernateSearchDAO getHibernateSearchDAO();

    /**
     * Returns the {@link IPropertyTypeDAO} implementation.
     */
    public IPropertyTypeDAO getPropertyTypeDAO();

    /**
     * Returns the {@link IEntityTypeDAO} implementation.
     */
    public IEntityTypeDAO getEntityTypeDAO(EntityKind entityKind);

    /**
     * Returns the {@link IEntityPropertyTypeDAO} implementation.
     */
    public IEntityPropertyTypeDAO getEntityPropertyTypeDAO(EntityKind entityKind);

    public IEntityHistoryDAO getEntityPropertyHistoryDAO();

    /**
     * @return The implementation of the {@link IVocabularyDAO}.
     */
    public IVocabularyDAO getVocabularyDAO();

    /** Returns an implementation of {@link IVocabularyTermDAO}. */
    public IVocabularyTermDAO getVocabularyTermDAO();

    /** Returns an implementation of {@link IAttachmentDAO}. */
    public IAttachmentDAO getAttachmentDAO();

    /** Returns an implementation of {@link IDataSetTypeDAO}. */
    public IDataSetTypeDAO getDataSetTypeDAO();

    /** Returns an implementation of {@link IFileFormatTypeDAO}. */
    public IFileFormatTypeDAO getFileFormatTypeDAO();

    /** Returns an implementation of {@link ILocatorTypeDAO}. */
    public ILocatorTypeDAO getLocatorTypeDAO();

    /**
     * Returns the {@link IMaterialDAO} implementation.
     */
    public IMaterialDAO getMaterialDAO();

    /**
     * Returns the {@link IScriptDAO} implementation.
     */
    public IScriptDAO getScriptDAO();

    /** Returns an implementation of {@link ICodeSequenceDAO} */
    public ICodeSequenceDAO getCodeSequenceDAO();

    /** Returns an implementation of {@link IDataStoreDAO}. */
    public IDataStoreDAO getDataStoreDAO();

    /** Returns an implementation of {@link IPermIdDAO}. */
    public IPermIdDAO getPermIdDAO();

    /** Returns an implementation of {@link IEventDAO}. */
    public IEventDAO getEventDAO();

    /** Returns an implementation of {@link IEventsSearchDAO}. */
    public IEventsSearchDAO getEventsSearchDAO();

    /** Returns an implementation of {@link IAuthorizationGroupDAO}. */
    public IAuthorizationGroupDAO getAuthorizationGroupDAO();

    /** Returns an implementation of {@link IDeletionDAO}. */
    @Override
    public IDeletionDAO getDeletionDAO();

    /** Returns an implementation of {@link ICorePluginDAO}. */
    public ICorePluginDAO getCorePluginDAO();

    /** Returns an implementation of {@link IPostRegistrationDAO}. */
    public IPostRegistrationDAO getPostRegistrationDAO();

    /** Returns an implementation of {@link IEntityOperationsLogDAO}. */
    public IEntityOperationsLogDAO getEntityOperationsLogDAO();

    /** Returns an implementation of {@link IExternalDataManagementSystemDAO}. */
    public IExternalDataManagementSystemDAO getExternalDataManagementSystemDAO();

    /** Returns an implementation of {@link IOperationExecutionDAO}. */
    public IOperationExecutionDAO getOperationExecutionDAO();

    /** Returns an implementation of {@link ISemanticAnnotationDAO}. */
    public ISemanticAnnotationDAO getSemanticAnnotationDAO();

}
