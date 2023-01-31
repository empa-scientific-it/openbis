/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.shared.dto;

import java.io.Serializable;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;

/**
 * @author Piotr Buczek
 */
public class DataSetCodesWithStatus implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final List<String> dataSetCodes;

    private final DataSetArchivingStatus status;

    private final boolean presentInArchive;

    public DataSetCodesWithStatus(List<String> dataSetCodes, DataSetArchivingStatus status,
            boolean presentInArchive)
    {
        this.dataSetCodes = dataSetCodes;
        this.status = status;
        this.presentInArchive = presentInArchive;
    }

    public List<String> getDataSetCodes()
    {
        return dataSetCodes;
    }

    public DataSetArchivingStatus getStatus()
    {
        return status;
    }

    public boolean isPresentInArchive()
    {
        return presentInArchive;
    }

    @Override
    public String toString()
    {
        return dataSetCodes + " - " + getStatus();
    }

}
