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

import ch.systemsx.cisd.openbis.generic.shared.dto.ISampleRelationshipDAO;
import org.hibernate.SessionFactory;

import ch.systemsx.cisd.openbis.generic.shared.authorization.IAuthorizationConfig;

/**
 * Factory definition for all Data Access Objects which are needed for managing authorization.
 * 
 * @author Franz-Josef Elmer
 */
public interface IAuthorizationDAOFactory
{

    public IAuthorizationConfig getAuthorizationConfig();

    /** Returns the persistency resources used to create DAO's. */
    public PersistencyResources getPersistencyResources();

    /**
     * @param batchMode should be set to true if it is foreseen that many write operations interleaved with read operations will be executed in one
     *            block. Note that it causes that read operations will not see any changes made to the database in this session. It will bring big
     *            performance improvement. The batch mode should be set to false as soon as it is no longer needed.
     */
    public void setBatchUpdateMode(boolean batchMode);

    /**
     * Returns the Hibernate session factory.
     */
    public SessionFactory getSessionFactory();

    public IPersonDAO getPersonDAO();

    public ISpaceDAO getSpaceDAO();

    public IRoleAssignmentDAO getRoleAssignmentDAO();

    public IDataDAO getDataDAO();

    public IExperimentDAO getExperimentDAO();

    public IProjectDAO getProjectDAO();

    public ISampleDAO getSampleDAO();

    public ISampleRelationshipDAO getSampleRelationshipDAO();

    public IGridCustomFilterDAO getGridCustomFilterDAO();

    public IGridCustomColumnDAO getGridCustomColumnDAO();

    public IQueryDAO getQueryDAO();

    public IRelationshipTypeDAO getRelationshipTypeDAO();

    public IDeletionDAO getDeletionDAO();

    public IMetaprojectDAO getMetaprojectDAO();
}
