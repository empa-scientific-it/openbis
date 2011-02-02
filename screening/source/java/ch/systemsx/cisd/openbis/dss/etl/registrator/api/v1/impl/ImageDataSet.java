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

package ch.systemsx.cisd.openbis.dss.etl.registrator.api.v1.impl;

import java.io.File;

import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationDetails;
import ch.systemsx.cisd.etlserver.registrator.api.v1.impl.DataSet;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.ImageDataSetInformation;
import ch.systemsx.cisd.openbis.dss.etl.registrator.api.v1.IImageDataSet;

/**
 * Implementation of an {@link IImageDataSet} based on {@link DataSet}.
 * 
 * @author Franz-Josef Elmer
 */
// TODO 2011-02-02, Tomasz Pylak: We do not use it, is it really needed? For sure it's not complete.
public class ImageDataSet extends DataSet<ImageDataSetInformation> implements IImageDataSet
{

    public ImageDataSet(DataSetRegistrationDetails<ImageDataSetInformation> registrationDetails,
            File dataSetFolder)
    {
        super(registrationDetails, dataSetFolder);
    }

    public void setTileGeometry(int numberOfRows, int numberOfColumns)
    {
        ImageDataSetInformation dataSetInformation =
                getRegistrationDetails().getDataSetInformation();
        dataSetInformation.setTileGeometry(numberOfRows, numberOfColumns);
    }
}
