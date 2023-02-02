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

package ch.systemsx.cisd.openbis.dss.etl.dto.api;

import java.util.Arrays;

import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;

/**
 * Basic attributes of a dataset connected to a sample and optionally to one parent dataset.
 * 
 * @author Tomasz Pylak
 */
public class BasicDataSetInformation extends DataSetInformation
{
    private static final long serialVersionUID = IServer.VERSION;

    private String fileFormatTypeCode;

    // marks if a dataset is measured or derived from measured data
    private boolean isMeasured = true;

    /** Sets code of the dataset type */
    public void setDatasetTypeCode(String datasetTypeCode)
    {
        DataSetType dataSetType = new DataSetType();
        dataSetType.setCode(datasetTypeCode);
        super.setDataSetType(dataSetType);
    }

    /** Mandatory: sets file format code. */
    public void setFileFormatCode(String fileFormatCode)
    {
        this.fileFormatTypeCode = fileFormatCode;
    }

    /**
     * Sets attributes of the connected sample - optional.
     * <p>
     * Alternatively, call
     * {@link ch.systemsx.cisd.etlserver.registrator.api.v1.IDataSetUpdatable#setSample(ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.ISampleImmutable)}
     * on the object returned by {@link ch.systemsx.cisd.etlserver.registrator.api.v1.IDataSetRegistrationTransaction#createNewDataSet()}.
     */
    public void setSample(String sampleSpaceCode, String sampleProjectCode, String sampleCode)
    {
        super.setSpaceCode(sampleSpaceCode);
        super.setProjectCode(sampleProjectCode);
        super.setSampleCode(sampleCode);
    }

    /** attributes of the parent dataset - optional */
    public void setParentDatasetCode(String parentDatasetCode)
    {
        super.setParentDataSetCodes(Arrays.asList(parentDatasetCode));
    }

    /** marks if a dataset is measured or derived from measured data */
    public void setMeasured(boolean isMeasured)
    {
        this.isMeasured = isMeasured;
    }

    public String getFileFormatTypeCode()
    {
        return fileFormatTypeCode;
    }

    public boolean isMeasured()
    {
        return isMeasured;
    }

}
