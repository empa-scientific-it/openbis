/*
 * Copyright 2014 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * @author Franz-Josef Elmer
 */
public interface IMultiDataSetArchiverDBTransaction
{

    public List<MultiDataSetArchiverDataSetDTO> getDataSetsForContainer(MultiDataSetArchiverContainerDTO container);

    /**
     * Creates a new container
     */
    public MultiDataSetArchiverContainerDTO createContainer(String path);

    public void deleteContainer(String container);

    public MultiDataSetArchiverDataSetDTO insertDataset(DatasetDescription dataSet,
            MultiDataSetArchiverContainerDTO container);

    public MultiDataSetArchiverDataSetDTO getDataSetForCode(String code);

    public void requestUnarchiving(List<String> dataSetCodes);

    public void resetRequestUnarchiving(long containerId);
    
    public void deleteContainer(long containerId);

    public void commit();

    public void rollback();

    public void close();

}