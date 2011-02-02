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

package ch.systemsx.cisd.etlserver.registrator.api.v1;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LocatorType;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public interface IDataSet
{
    /**
     * Get the data set code of the data set
     */
    public String getDataSetCode();

    /**
     * Get the experiment for this data set. This can only be null at initialization time, and will
     * be non-null for a valid data set.
     */
    IExperimentImmutable getExperiment();

    /**
     * Set the experiment for this data set. The experiment may also be set by setting the sample.
     * 
     * @param experiment The experiment to use. Need not actually be immutable, but the immutable
     *            one is the supertype.
     */
    void setExperiment(IExperimentImmutable experiment);

    /**
     * Get the sample for this data set, if there is one.
     */
    ISampleImmutable getSampleOrNull();

    /**
     * Set the sample for this data set. Will also set the experiment, since the sample must have an
     * experiment.
     * 
     * @param sampleOrNull The sample to use. Need not actually be immutable, but the immutable one
     *            is the supertype.
     */
    void setSampleOrNull(ISampleImmutable sampleOrNull);

    /**
     * The file format type of the data set. Defaults to the default specified in
     * {@link FileFormatType}.
     */
    public String getFileFormatType();

    /**
     * Set the file format type.
     */
    public void setFileFormatType(String fileFormatTypeCode);

    /**
     * Return true if the data set is measured data. Defaults to true.
     */
    public boolean isMeasuredData();

    /**
     * Set whether the data is measured or not.
     */
    public void setMeasuredData(boolean measuredData);

    /**
     * Get the data set type. This is only null during initialization and is non-null for a valid
     * data set.
     */
    public String getDataSetType();

    /**
     * Set the data set type.
     */
    public void setDataSetType(String dataSetTypeCode);

    /**
     * Get the locator type. Defaults to the default specified in {@link LocatorType}.
     */
    public String getLocatorType();

    /**
     * Set the locator type.
     */
    public void setLocatorType(String locatorTypeCode);
}
