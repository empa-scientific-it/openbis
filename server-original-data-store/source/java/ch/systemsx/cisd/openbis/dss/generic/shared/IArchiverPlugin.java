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

package ch.systemsx.cisd.openbis.dss.generic.shared;

import java.io.Serializable;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatasetLocation;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * Interface of the archiver task.
 * 
 * @author Piotr Buczek
 * @author Kaloyan Enimanev
 * @author Jakub Straszewski
 */
public interface IArchiverPlugin extends Serializable
{
    /**
     * Returns <code>false</code> if archiving is currently <b>not</b> possible.
     */
    boolean isArchivingPossible();

    /**
     * Asynchronously processes archiving of the specified datasets.
     * 
     * @returns {@link ProcessingStatus} of the finished processing with statuses of processing for all scheduled data sets or null if processing
     *          succeeded for all datasets and no additional information is provided.
     */
    ProcessingStatus archive(List<DatasetDescription> datasets, ArchiverTaskContext context,
            boolean removeFromDataStore);

    /**
     * Asynchronously processes unarchiving of the specified datasets.
     * 
     * @returns {@link ProcessingStatus} of the finished processing with statuses of processing for all scheduled data sets or null if processing
     *          succeeded for all datasets and no additional information is provided.
     */
    ProcessingStatus unarchive(List<DatasetDescription> datasets, ArchiverTaskContext context);

    /**
     * Enhances the list of data set codes, so that it contains all datasets that should be unarchived together in one batch.
     * 
     * @returns the list that should be a superset {@code dataSetCodes}
     */
    List<String> getDataSetCodesForUnarchiving(List<String> dataSetCodes);

    /**
     * Delete data sets from the archive.
     * 
     * @returns {@link ProcessingStatus} containing the deletion statuses for all data sets or null if processing succeeded for all datasets and no
     *          additional information is provided.
     */
    ProcessingStatus deleteFromArchive(List<DatasetLocation> datasets);
}
