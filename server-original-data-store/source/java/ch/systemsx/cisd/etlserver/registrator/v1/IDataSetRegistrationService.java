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

package ch.systemsx.cisd.etlserver.registrator.v1;

import java.io.File;

import ch.systemsx.cisd.etlserver.registrator.api.v1.IDataSetRegistrationTransaction;

/**
 * Functionality used in registering data sets.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public interface IDataSetRegistrationService
{

    /**
     * Create a new transaction that atomically performs file operations and registers entities, using the incoming data set as the root for resolving
     * relative paths.
     * 
     * @return A new transaction
     */
    public IDataSetRegistrationTransaction transaction();

    /**
     * Create a new transaction that atomically performs file operations and registers entities, using the file as the root for resolving relative
     * paths.
     * 
     * @param dataSetFile The file for resolving relative paths.
     * @return A new transaction
     */
    public IDataSetRegistrationTransaction transaction(File dataSetFile);

    /**
     * Move the incoming file to the error directory for the given data set type.
     * 
     * @param dataSetTypeCodeOrNull The code of the data set type. Pass null to have the framework default this value.
     */
    public File moveIncomingToError(String dataSetTypeCodeOrNull);

}