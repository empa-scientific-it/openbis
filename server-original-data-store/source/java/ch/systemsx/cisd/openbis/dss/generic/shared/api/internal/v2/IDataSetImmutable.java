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

package ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2;

import java.util.List;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;

/**
 * A data set that has already been stored in openBIS.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public interface IDataSetImmutable extends IMetaprojectContent
{
    /**
     * Get the data set code of the data set
     * 
     * @return The code of this data set.
     */
    public String getDataSetCode();

    /**
     * Get the experiment for this data set. This can only be null at initialization time, and will be non-null for a valid data set.
     * 
     * @return The experiment for this data set (will be non-null for a valid data set)
     */
    IExperimentImmutable getExperiment();

    /**
     * Get the sample for this data set, if there is one.
     * 
     * @return A sample or null.
     */
    ISampleImmutable getSample();

    /**
     * The file format type of the data set. Defaults to the default specified in {@link FileFormatType}.
     * <p>
     * This property is undefined for container data sets.
     * 
     * @return The code of the {@link FileFormatType} for this data set.
     */
    public String getFileFormatType();

    /**
     * Return true if the data set is measured data. Defaults to true.
     * 
     * @return True if the data set is measured data, false otherwise.
     */
    public boolean isMeasuredData();

    /**
     * Get the data set type. This is only null during initialization and is non-null for a valid data set.
     */
    public String getDataSetType();

    public DataSetKind getDataSetKind();

    /**
     * Returns data set type with property types and vocabulary terms for all property types with vocabulary data type.
     * 
     * @throws UserFailureException if data set type code hasn't been defined.
     */
    public DataSetType getDataSetTypeWithPropertyTypes();

    /**
     * Returns the speed hint.
     * <p>
     * This property is undefined for container data sets.
     */
    public int getSpeedHint();

    /**
     * Get the value for a property.
     */
    public String getPropertyValue(String propertyCode);

    /**
     * Returns codes of all properties of this dataset. Each code can be used when calling {@link #getPropertyValue(String)}.
     */
    public List<String> getAllPropertyCodes();

    /** Gets the parents of the dataset. */
    public List<String> getParentDatasets();

    /**
     * Gets the children data sets. Only available for data sets existing prior the transaction start.
     */
    public List<IDataSetImmutable> getChildrenDataSets();

    /** Return true if this data set contains other data sets. */
    public boolean isContainerDataSet();

    /**
     * Get the codes for contained data sets. This is empty if {@link #isContainerDataSet()} returns false.
     */
    public List<String> getContainedDataSetCodes();

    /**
     * Return true if this data set is contained in other data set
     */
    public boolean isContainedDataSet();

    /**
     * Return the code of the container in which this data set is contained. If the data set is in more than one container only the code of one of
     * these containers is returned.
     * 
     * @deprecated Use {@link #getContainerDataSets()}.
     */
    @Deprecated
    public String getContainerDataSet();

    /**
     * Returns the codes of all containers in which this data set is contained.
     */
    public List<String> getContainerDataSets();

    /**
     * Returns the order of this data set in the specified container data set.
     * 
     * @return <code>null</code> if this data set is not a component of the specified container data set.
     */
    public Integer getOrderInContainer(String containerDataSetCode);

    /** @return true if this is a data set, that links to external data management system */
    public boolean isLinkDataSet();

    /**
     * @return true if this dataset doesn't contain any files. It can be for example container or link data set.
     */
    public boolean isNoFileDataSet();

    /**
     * @return true if, for this dataset, post registration has been finished.
     */
    public boolean isPostRegistered();

    /** Get the external data management system */
    public IExternalDataManagementSystemImmutable getExternalDataManagementSystem();

    /** @return the code of this link data set in the external data management system */
    public String getExternalCode();
}
