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

package ch.systemsx.cisd.openbis.dss.etl.dto.api.v2.impl;

import java.io.File;
import java.util.Collections;
import java.util.List;

import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationDetails;
import ch.systemsx.cisd.etlserver.registrator.api.v2.IDataSet;
import ch.systemsx.cisd.etlserver.registrator.api.v2.impl.DataSet;
import ch.systemsx.cisd.openbis.dss.etl.PlateGeometryOracle;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.impl.ImageDataSetInformation;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v2.IImageDataSet;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.IExperimentImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.ISampleImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;

/**
 * Represents an image data set for the registration API.
 * 
 * @author Tomasz Pylak
 */
public class ImageContainerDataSet extends DataSet<DataSetInformation> implements IImageDataSet
{
    private DataSet<ImageDataSetInformation> originalDataset;

    private List<IDataSet> thumbnailDatasets = Collections.emptyList();

    private boolean establishSampleLinkForContainedDataSets = false;

    public ImageContainerDataSet(
            DataSetRegistrationDetails<? extends DataSetInformation> registrationDetails,
            File dataSetFolder, IEncapsulatedOpenBISService service)
    {
        super(registrationDetails, dataSetFolder, service);
    }

    @Override
    public void setAnalysisProcedure(String analysisProcedure)
    {
        setPropertyValue(ScreeningConstants.ANALYSIS_PROCEDURE, analysisProcedure);
    }

    @Override
    public void establishSampleLinkForContainedDataSets()
    {
        establishSampleLinkForContainedDataSets = true;
    }

    public boolean sampleLinkForContainedDataSetsShouldBeEstablished()
    {
        return establishSampleLinkForContainedDataSets;
    }

    @Override
    public DataSet<ImageDataSetInformation> getOriginalDataset()
    {
        return originalDataset;
    }

    public void setOriginalDataset(DataSet<ImageDataSetInformation> originalDataset)
    {
        this.originalDataset = originalDataset;
    }

    @Override
    public List<IDataSet> getThumbnailDatasets()
    {
        return thumbnailDatasets;
    }

    public void setThumbnailDatasets(List<IDataSet> thumbnailDatasets)
    {
        this.thumbnailDatasets = thumbnailDatasets;
    }

    @Override
    public void setSample(ISampleImmutable sampleOrNull)
    {
        super.setSample(sampleOrNull);
        if (originalDataset != null)
        {
            originalDataset.setSample(sampleOrNull);
            originalDataset.getRegistrationDetails().getDataSetInformation()
                    .setLinkSample(establishSampleLinkForContainedDataSets);
        }
        for (IDataSet thumbnailDataset : thumbnailDatasets)
        {
            thumbnailDataset.setSample(sampleOrNull);
            if (thumbnailDataset instanceof DataSet)
            {
                ((DataSet<?>) thumbnailDataset).getRegistrationDetails().getDataSetInformation()
                        .setLinkSample(establishSampleLinkForContainedDataSets);
            }
        }
    }

    @Override
    public void setExperiment(IExperimentImmutable experimentOrNull)
    {
        super.setExperiment(experimentOrNull);
        if (originalDataset != null)
        {
            originalDataset.setExperiment(experimentOrNull);
        }
        for (IDataSet thumbnailDataset : thumbnailDatasets)
        {
            thumbnailDataset.setExperiment(experimentOrNull);
        }
    }

    @Override
    public String figureGeometry()
    {
        return PlateGeometryOracle.figureGeometry(getImageRegistrationDetails(), service);
    }

    @SuppressWarnings("unchecked")
    private DataSetRegistrationDetails<ImageDataSetInformation> getImageRegistrationDetails()
    {
        return (DataSetRegistrationDetails<ImageDataSetInformation>) originalDataset
                .getRegistrationDetails();
    }
}
