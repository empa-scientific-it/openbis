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

package ch.systemsx.cisd.openbis.dss.etl;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.openbis.dss.etl.PlateStorageProcessor.DatasetOwnerInformation;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Geometry;
import ch.systemsx.cisd.openbis.plugin.screening.shared.dto.PlateDimension;
import ch.systemsx.cisd.openbis.plugin.screening.shared.dto.PlateDimensionParser;

/**
 * Describes one HCS dataset container (e.g. plate) with images.
 * 
 * @author Tomasz Pylak
 */
public class HCSContainerDatasetInfo
{
    private String experimentPermId;

    private String containerSamplePermId;

    private int containerSampleRows, containerSampleColumns;

    private String datasetPermId;

    public String getExperimentPermId()
    {
        return experimentPermId;
    }

    public void setExperimentPermId(String experimentPermId)
    {
        this.experimentPermId = experimentPermId;
    }

    public String getContainerSamplePermId()
    {
        return containerSamplePermId;
    }

    public void setContainerSamplePermId(String containerSamplePermId)
    {
        this.containerSamplePermId = containerSamplePermId;
    }

    public String getDatasetPermId()
    {
        return datasetPermId;
    }

    public void setDatasetPermId(String datasetPermId)
    {
        this.datasetPermId = datasetPermId;
    }

    public int getContainerRows()
    {
        return containerSampleRows;
    }

    public void setContainerRows(int containerRows)
    {
        this.containerSampleRows = containerRows;
    }

    public int getContainerColumns()
    {
        return containerSampleColumns;
    }

    public void setContainerColumns(int containerColumns)
    {
        this.containerSampleColumns = containerColumns;
    }

    public Geometry getContainerGeometry()
    {
        return Geometry.createFromRowColDimensions(containerSampleRows, containerSampleColumns);
    }

    public static HCSContainerDatasetInfo createScreeningDatasetInfoWithSample(
            DataSetInformation dataSetInformation, Sample sampleOrNull)
    {
        return createScreeningDatasetInfoWithSample(
                DatasetOwnerInformation.create(dataSetInformation), sampleOrNull);
    }

    public static HCSContainerDatasetInfo createScreeningDatasetInfo(
            DatasetOwnerInformation dataSetInformation)
    {
        Sample sample = dataSetInformation.tryGetSample();
        assert sample != null : "no sample connected to a dataset";
        PlateDimension plateGeometry = getPlateGeometry(dataSetInformation);
        HCSContainerDatasetInfo info =
                createBasicScreeningDataSetInfo(dataSetInformation, sample, plateGeometry);
        return info;
    }

    /**
     * Create a screening data set info given sample.
     */
    public static HCSContainerDatasetInfo createScreeningDatasetInfoWithSample(
            DatasetOwnerInformation dataSetInformation, Sample containingSample)
    {
        Sample sample = containingSample;
        assert sample != null : "no sample connected to a dataset";
        PlateDimension plateGeometry = getPlateGeometry(sample);
        HCSContainerDatasetInfo info =
                createBasicScreeningDataSetInfo(dataSetInformation, sample, plateGeometry);
        return info;
    }

    private static HCSContainerDatasetInfo createBasicScreeningDataSetInfo(
            DatasetOwnerInformation dataSetInformation, Sample sample, PlateDimension plateGeometry)
    {
        Experiment experiment = dataSetInformation.tryGetExperiment();
        HCSContainerDatasetInfo info = new HCSContainerDatasetInfo();
        info.setExperimentPermId(experiment.getPermId());
        info.setContainerSamplePermId(sample.getPermId());
        info.setDatasetPermId(dataSetInformation.getDataSetCode());
        info.setContainerRows(plateGeometry.getRowsNum());
        info.setContainerColumns(plateGeometry.getColsNum());
        return info;
    }

    private static PlateDimension getPlateGeometry(Sample sample)
    {
        final IEntityProperty[] sampleProperties =
                (sample.getProperties()).toArray(new IEntityProperty[0]);
        final PlateDimension plateDimension =
                PlateDimensionParser.tryToGetPlateDimension(sampleProperties);
        if (plateDimension == null)
        {
            throw new EnvironmentFailureException(
                    "Missing plate geometry for the plate registered for sample identifier '"
                            + sample.getIdentifier() + "'.");
        }
        return plateDimension;
    }

    public static PlateDimension getPlateGeometry(final DatasetOwnerInformation dataSetInformation)
    {
        IEntityProperty[] sampleProperties = dataSetInformation.getSampleProperties();
        if ((sampleProperties == null || sampleProperties.length == 0)
                && dataSetInformation.tryGetSample() != null)
        {
            sampleProperties =
                    dataSetInformation.tryGetSample().getProperties()
                            .toArray(new IEntityProperty[0]);
        }
        final PlateDimension plateDimension =
                PlateDimensionParser.tryToGetPlateDimension(sampleProperties);
        if (plateDimension == null)
        {
            throw new EnvironmentFailureException(
                    "Missing plate geometry for the plate registered for sample identifier '"
                            + dataSetInformation.getSampleIdentifier() + "'.");
        }
        return plateDimension;
    }
}