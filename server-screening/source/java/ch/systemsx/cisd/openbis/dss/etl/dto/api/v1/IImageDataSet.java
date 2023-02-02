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

package ch.systemsx.cisd.openbis.dss.etl.dto.api.v1;

import java.util.List;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IDataSet;

/**
 * Container image dataset which contains original and thumbnail physical datasets.
 * 
 * @author Tomasz Pylak
 */
public interface IImageDataSet extends IDataSet
{
    IDataSet getOriginalDataset();

    List<IDataSet> getThumbnailDatasets();

    /** Sets analysis procedure. Makes sense only for segmentation datasets. */
    void setAnalysisProcedure(String analysisProcedure);

    /**
     * If this method is called the contained data sets (original data set and thumbnails) will automatically linked to the sample specified by
     * {@link IDataSet#setSample(ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.ISampleImmutable)} . By default there will be no such
     * links.
     * 
     * @deprecated this method is for backwards compatibility only
     */
    @Deprecated
    void establishSampleLinkForContainedDataSets();

    /**
     * Utility method to find out the plate geometry by looking for which wells images are available.
     * 
     * @return a constant which can be used as a vocabulary term value for $PLATE_GEOMETRY property of a plate/
     * @throws UserFailureException if all available geometries in openBIS are too small (there is a well outside).
     */
    String figureGeometry();

}
