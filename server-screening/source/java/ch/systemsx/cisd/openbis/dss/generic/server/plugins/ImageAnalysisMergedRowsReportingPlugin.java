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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.AbstractTableModelReportingPlugin;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetProcessingContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.screening.server.util.FeatureVectorLoaderMetadataProviderFactory;
import ch.systemsx.cisd.openbis.dss.shared.DssScreeningUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.CodeNormalizer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CodeAndLabel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DoubleTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IntegerTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.StringTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.generic.shared.util.SimpleTableModelBuilder;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.PlateUtils;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.FeatureValue;
import ch.systemsx.cisd.openbis.plugin.screening.shared.dto.FeatureTableRow;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.FeatureVectorLoader;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.FeatureVectorLoader.IMetadataProvider;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.FeatureVectorLoader.WellFeatureCollection;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.IImagingReadonlyQueryDAO;

/**
 * Reporting plugin that concatenates rows of tabular files of all data sets (stripping the header lines of all but the first file) and delivers the
 * result back in the table model. Each row has additional Data Set code column.
 * 
 * @author Tomasz Pylak
 * @author Franz-Josef Elmer
 */
public class ImageAnalysisMergedRowsReportingPlugin extends AbstractTableModelReportingPlugin
{
    private static final long serialVersionUID = 1L;

    private static final String DATA_SET_CODE_TITLE = "Data Set Code";

    private static final String PLATE_IDENTIFIER_TITLE = "Plate Identifier";

    private static final String ROW_TITLE = "Row";

    private static final String COLUMN_TITLE = "Column";

    private static final ISerializableComparable EMPTY_CELL = new StringTableCell("");

    private IEncapsulatedOpenBISService service;

    private IImagingReadonlyQueryDAO dao;

    public ImageAnalysisMergedRowsReportingPlugin(Properties properties, File storeRoot)
    {
        this(properties, storeRoot, null, null);
    }

    ImageAnalysisMergedRowsReportingPlugin(Properties properties, File storeRoot,
            IEncapsulatedOpenBISService service, IImagingReadonlyQueryDAO dao)
    {
        super(properties, storeRoot);
        this.service = service;
        this.dao = dao;
    }

    @Override
    public TableModel createReport(List<DatasetDescription> datasets,
            DataSetProcessingContext context)
    {
        List<String> datasetCodes = extractDatasetCodes(datasets);
        ArrayList<String> featureCodes = new ArrayList<String>(); // fetch all
        WellFeatureCollection<FeatureTableRow> featuresCollection =
                FeatureVectorLoader.fetchDatasetFeatures(datasetCodes, featureCodes, getDAO(),
                        getMetadataProvider(datasetCodes));

        List<CodeAndLabel> codeAndLabels = featuresCollection.getFeatureCodesAndLabels();
        List<FeatureTableRow> rows = featuresCollection.getFeatures();
        SimpleTableModelBuilder builder = new SimpleTableModelBuilder(true);
        builder.addHeader(DATA_SET_CODE_TITLE, CodeNormalizer.normalize(DATA_SET_CODE_TITLE));
        builder.addHeader(PLATE_IDENTIFIER_TITLE, CodeNormalizer.normalize(PLATE_IDENTIFIER_TITLE));
        builder.addHeader(ROW_TITLE, CodeNormalizer.normalize(ROW_TITLE));
        builder.addHeader(COLUMN_TITLE, CodeNormalizer.normalize(COLUMN_TITLE));
        for (CodeAndLabel codeAndLabel : codeAndLabels)
        {
            builder.addHeader(codeAndLabel.getLabel(), "feature-" + codeAndLabel.getCode());
        }
        for (FeatureTableRow row : rows)
        {
            List<ISerializableComparable> values = new ArrayList<ISerializableComparable>();
            values.add(new StringTableCell(row.getDataSetCode()));
            values.add(new StringTableCell(row.getPlateIdentifier().toString()));
            values.add(new StringTableCell(PlateUtils.translateRowNumberIntoLetterCode(row
                    .getWellLocation().getRow())));
            values.add(new IntegerTableCell(row.getWellLocation().getColumn()));
            FeatureValue[] featureValues = row.getFeatureValues();
            for (FeatureValue value : featureValues)
            {
                values.add(createCell(value));
            }
            builder.addRow(values);
        }
        return builder.getTableModel();
    }

    private static ISerializableComparable createCell(FeatureValue value)
    {
        if (value.isFloat())
        {
            float floatValue = value.asFloat();
            if (Float.isNaN(floatValue))
            {
                return EMPTY_CELL;
            } else
            {
                return new DoubleTableCell(floatValue);
            }
        } else if (value.isVocabularyTerm())
        {
            String term = value.tryAsVocabularyTerm();
            if (term == null)
            {
                return EMPTY_CELL;
            } else
            {
                return new StringTableCell(term);
            }
        } else
        {
            throw new IllegalStateException("unknown value");
        }

    }

    private static List<String> extractDatasetCodes(List<DatasetDescription> datasets)
    {
        List<String> datasetCodes = new ArrayList<String>();
        for (DatasetDescription datasetDescription : datasets)
        {
            datasetCodes.add(datasetDescription.getDataSetCode());
        }
        return datasetCodes;
    }

    private IImagingReadonlyQueryDAO getDAO()
    {
        synchronized (this)
        {
            if (dao == null)
            {
                dao = DssScreeningUtils.getQuery();
            }
        }
        return dao;
    }

    private IMetadataProvider getMetadataProvider(final List<String> datasetCodes)
    {
        if (service == null)
        {
            service = ServiceProvider.getOpenBISService();
        }
        return FeatureVectorLoaderMetadataProviderFactory.createMetadataProvider(service,
                datasetCodes);
    }
}
