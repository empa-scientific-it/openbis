/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver;

import java.io.File;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * Uploads data to an additional project-specific database.
 * <p>
 * {@link IDataSetUploader} instances exist only within the scope of a storage processor transaction.
 * 
 * @author Franz-Josef Elmer
 */
public interface IDataSetUploader
{

    /** uploads files with recognized extensions to the additional database */
    void upload(File dataSet, DataSetInformation dataSetInformation)
            throws EnvironmentFailureException;

    /** commits the results of the last call of {@link #upload} */
    void commit();

    /** rollbacks the results of the last call of {@link #upload} */
    void rollback();
}