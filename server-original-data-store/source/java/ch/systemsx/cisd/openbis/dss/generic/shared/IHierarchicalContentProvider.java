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

package ch.systemsx.cisd.openbis.dss.generic.shared;

import java.io.File;

import ch.systemsx.cisd.common.server.ISessionTokenProvider;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IDatasetLocation;

/**
 * A provider of {@link IHierarchicalContent} for given data set.
 * <p>
 * <b>NOTE</b>{@link IHierarchicalContent#close()} needs to be called to release resources when working with the content is done. Otherwise data set
 * will remain locked in its share until timeout occurs.
 * 
 * @author Piotr Buczek
 * @author Chandrasekhar Ramakrishnan
 */
public interface IHierarchicalContentProvider
{

    /**
     * This method needs to access openBIS DB to retrieve information first.
     * 
     * @return {@link IHierarchicalContent} for the specified data set
     * @throws IllegalArgumentException if data set doesn't exist in openBIS DB
     */
    IHierarchicalContent asContent(String dataSetCode) throws IllegalArgumentException;

    /**
     * This method needs to access openBIS DB to retrieve information first. It does not modify the access timestamp of requested dataset.
     * 
     * @return {@link IHierarchicalContent} for the specified data set
     * @throws IllegalArgumentException if data set doesn't exist in openBIS DB
     */
    IHierarchicalContent asContentWithoutModifyingAccessTimestamp(String dataSetCode) throws IllegalArgumentException;

    /**
     * A faster alternative of {@link #asContent(String)} for the case when an {@link AbstractExternalData} object has already been fetched from the
     * openBIS AS.
     * 
     * @param dataSet a fully populated {@link AbstractExternalData} instance. For container data sets all physical (contained) data sets must be
     *            present.
     * @return {@link IHierarchicalContent} for the specified data set
     */
    IHierarchicalContent asContent(AbstractExternalData dataSet);

    /**
     * A faster alternative of {@link #asContent(String)} for the case when an {@link AbstractExternalData} object has already been fetched from the
     * openBIS AS. It does not modify the access timestamp of requested dataset.
     * 
     * @param dataSet a fully populated {@link AbstractExternalData} instance. For container data sets all physical (contained) data sets must be
     *            present.
     * @return {@link IHierarchicalContent} for the specified data set
     */
    IHierarchicalContent asContentWithoutModifyingAccessTimestamp(AbstractExternalData dataSet);

    /**
     * @return {@link IHierarchicalContent} for the specified data set
     * @deprecated doesn't support container data sets, use {@link #asContent(String)}
     */
    @Deprecated
    IHierarchicalContent asContent(IDatasetLocation datasetLocation);

    /**
     * <b>NOTE:</b> the data set is assumed to be locked when this method is called
     * 
     * @param datasetDirectory the directory file of the data set
     * @return {@link IHierarchicalContent} for the specified data set
     * @deprecated doesn't support container data sets, use {@link #asContent(String)}
     */
    @Deprecated
    IHierarchicalContent asContent(File datasetDirectory);

    /**
     * Creates a clone of this provider for the specified session token provider.
     */
    IHierarchicalContentProvider cloneFor(ISessionTokenProvider sessionTokenProvider);
}
