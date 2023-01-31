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

package ch.systemsx.cisd.etlserver.registrator.api.v2;

import java.util.List;

import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.IDataSetImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.IExperimentImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.IExternalDataManagementSystemImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.ISampleImmutable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public interface IDataSetUpdatable extends IDataSetImmutable
{

    /**
     * Set the experiment for this data set. The experiment may also be set by setting the sample.
     * 
     * @param experiment The experiment for this data set. Need not actually be immutable, but the immutable one is the supertype.
     */
    void setExperiment(IExperimentImmutable experiment);

    /**
     * Set the sample for this data set. Will also set the experiment, since the sample must have an experiment.
     * 
     * @param sampleOrNull The sample to use. Need not actually be immutable, but the immutable one is the supertype.
     */
    void setSample(ISampleImmutable sampleOrNull);

    /**
     * Set the file format type.
     * <p>
     * This property is undefined for container data sets.
     * 
     * @param fileFormatTypeCode The code of the desired {@link FileFormatType}.
     */
    public void setFileFormatType(String fileFormatTypeCode);

    /**
     * Set the value for a property.
     */
    public void setPropertyValue(String propertyCode, String propertyValue);

    /** Sets the parents of the dataset. */
    public void setParentDatasets(List<String> parentDatasetCodes);

    /** Set the codes for contained data sets. */
    public void setContainedDataSetCodes(List<String> containedDataSetCodes);

    /** Set the external data management system */
    public void setExternalDataManagementSystem(
            IExternalDataManagementSystemImmutable externalDataManagementSystem);

    /** Set the code from the external data management system */
    public void setExternalCode(String externalCode);
}
