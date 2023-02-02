/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto;

import java.io.Serializable;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * Describes images in one dataset and the way to access them.
 * 
 * @author Tomasz Pylak
 */
public class DatasetImagesReference implements Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    public static final DatasetImagesReference create(DatasetReference dataset,
            ImageDatasetParameters imageParams)
    {
        return new DatasetImagesReference(dataset, imageParams);
    }

    private DatasetReference dataset;

    private ImageDatasetParameters imageParameters;

    // GWT only
    protected DatasetImagesReference()
    {
    }

    protected DatasetImagesReference(DatasetReference dataset,
            ImageDatasetParameters imageParameters)
    {
        this.dataset = dataset;
        this.imageParameters = imageParameters;
    }

    public String getDatastoreCode()
    {
        return dataset.getDatastoreCode();
    }

    public String getDatastoreHostUrl()
    {
        return dataset.getDatastoreHostUrl();
    }

    public String getDatasetCode()
    {
        return dataset.getCode();
    }

    public Long getDatasetId()
    {
        return dataset.getId();
    }

    public DatasetReference getDatasetReference()
    {
        return dataset;
    }

    public ImageDatasetParameters getImageParameters()
    {
        return imageParameters;
    }

    @Override
    public int hashCode()
    {
        return getDatasetCode().hashCode();
    }

    @Override
    public String toString()
    {
        return "Image dataset " + getDatasetCode();
    }
}
