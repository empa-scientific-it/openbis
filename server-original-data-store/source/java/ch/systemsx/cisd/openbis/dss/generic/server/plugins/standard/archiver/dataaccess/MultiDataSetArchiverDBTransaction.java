/*
 * Copyright 2014 ETH Zuerich, CISD
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
 * @author Jakub Straszewski
 */
public class MultiDataSetArchiverDBTransaction implements IMultiDataSetArchiverDBTransaction
{

    private IMultiDataSetArchiverQueryDAO transaction;

    public MultiDataSetArchiverDBTransaction()
    {
        this.transaction = MultiDataSetArchiverDataSourceUtil.getTransactionalQuery();
    }

    @Override
    public List<MultiDataSetArchiverDataSetDTO> getDataSetsForContainer(MultiDataSetArchiverContainerDTO container)
    {
        return transaction.listDataSetsForContainerId(container.getId());
    }

    /**
     * Creates a new container
     */
    @Override
    public MultiDataSetArchiverContainerDTO createContainer(String path)
    {

        MultiDataSetArchiverContainerDTO container =
                new MultiDataSetArchiverContainerDTO(0, path);

        long id = transaction.addContainer(container);
        container.setId(id);

        return container;
    }

    @Override
    public void deleteContainer(long containerId)
    {
        transaction.deleteContainer(containerId);
    }

    @Override
    public void deleteContainer(String container)
    {
        transaction.deleteContainer(container);
    }

    @Override
    public MultiDataSetArchiverDataSetDTO insertDataset(DatasetDescription dataSet,
            MultiDataSetArchiverContainerDTO container)
    {
        String code = dataSet.getDataSetCode();

        MultiDataSetArchiverDataSetDTO mads = getDataSetForCode(code);

        if (mads != null)
        {
            throw new IllegalStateException("Dataset " + dataSet.getDataSetCode() + "is already archived in other container");
        }

        mads = new MultiDataSetArchiverDataSetDTO(0, code, container.getId(), dataSet.getDataSetSize());

        long id = transaction.addDataSet(mads);
        mads.setId(id);

        return mads;
    }

    @Override
    public MultiDataSetArchiverDataSetDTO getDataSetForCode(String code)
    {
        return transaction.getDataSetForCode(code);
    }

    @Override
    public void requestUnarchiving(List<String> dataSetCodes)
    {
        transaction.requestUnarchiving(dataSetCodes.toArray(new String[dataSetCodes.size()]));
    }

    @Override
    public void resetRequestUnarchiving(long containerId)
    {
        transaction.resetRequestUnarchiving(containerId);
    }

    /**
     * @see net.lemnik.eodsql.TransactionQuery#commit()
     */
    @Override
    public void commit()
    {
        transaction.commit();
    }

    /**
     * @see net.lemnik.eodsql.TransactionQuery#rollback()
     */
    @Override
    public void rollback()
    {
        transaction.rollback();
    }

    /**
     * @see net.lemnik.eodsql.TransactionQuery#close()
     */
    @Override
    public void close()
    {
        transaction.close();
    }
}
