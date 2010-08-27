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

package ch.systemsx.cisd.openbis.dss.generic.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.ITabularData;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.CodeAndLabel;
import ch.systemsx.cisd.openbis.dss.shared.DssScreeningUtils;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.WellPosition;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.PlateUtils;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.IImagingReadonlyQueryDAO;

/**
 * Create a graph from the imaging database.
 * <p>
 * TODO 2010-08-09, CR, LMS-1692, This implementation is inefficient. The better way to implement
 * this would be to lazily get feature vectors necessary for generating the graph, not to egerly get
 * all feature vectors.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class TabularDataGraphServlet extends AbstractTabularDataGraphServlet
{

    private static final long serialVersionUID = 1L;

    private IImagingReadonlyQueryDAO imagingDbDao;

    private IEncapsulatedOpenBISService openBisService;

    final public static String WELL_ROW_COLUMN = "Row";

    final public static String WELL_COLUMN_COLUMN = "Column";

    /**
     * An CSV-file-like interface to feature data from the database.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    private final class ImagingTabularData implements ITabularData
    {
        private static final String WELL_NAME_COLUMN = "WellName";

        private final IImagingReadonlyQueryDAO dao;

        private final IEncapsulatedOpenBISService service;

        private final String dataSetCode;

        private String[] headerCodes;

        private String[] headerLabels;

        private ArrayList<String[]> lines;

        private ImagingTabularData(IImagingReadonlyQueryDAO dao,
                IEncapsulatedOpenBISService service, String dataSetCode)
        {
            this.dao = dao;
            this.service = service;
            this.dataSetCode = dataSetCode;
            initialize();
        }

        private void initialize()
        {
            final FeatureTableBuilder tableBuilder = new FeatureTableBuilder(dao, service);
            tableBuilder.addFeatureVectorsOfDataSet(dataSetCode);

            List<CodeAndLabel> featureCodeAndLabels = tableBuilder.getCodesAndLabels();
            int headerTokensLength = featureCodeAndLabels.size() + 3;
            headerLabels = new String[headerTokensLength];
            headerLabels[0] = WELL_NAME_COLUMN;
            headerLabels[1] = WELL_ROW_COLUMN;
            headerLabels[2] = WELL_COLUMN_COLUMN;
            headerCodes = new String[headerTokensLength];
            headerCodes[0] = CodeAndLabel.normalize(WELL_NAME_COLUMN);
            headerCodes[1] = CodeAndLabel.normalize(WELL_ROW_COLUMN);
            headerCodes[2] = CodeAndLabel.normalize(WELL_COLUMN_COLUMN);

            int i = 3;
            for (CodeAndLabel featureCodeAndLabel : featureCodeAndLabels)
            {
                headerCodes[i] = featureCodeAndLabel.getCode();
                headerLabels[i++] = featureCodeAndLabel.getLabel();
            }

            lines = new ArrayList<String[]>();

            final List<FeatureTableRow> rows = tableBuilder.createFeatureTableRows();
            for (FeatureTableRow row : rows)
            {
                String[] line = new String[headerTokensLength];
                WellPosition pos = row.getWellPosition();
                String rowLetter = PlateUtils.translateRowNumberIntoLetterCode(pos.getWellRow());
                String columnNumber = Integer.toString(row.getWellPosition().getWellColumn());
                line[0] = rowLetter + columnNumber;
                line[1] = Integer.toString(pos.getWellRow());
                line[2] = Integer.toString(pos.getWellColumn());
                i = 3;
                float[] values = row.getFeatureValues();
                for (float value : values)
                {
                    line[i++] = Float.toString(value);
                }
                lines.add(line);
            }
        }

        public List<String[]> getDataLines()
        {
            return lines;
        }

        public String[] getHeaderLabels()
        {
            return headerLabels;
        }

        public String[] getHeaderCodes()
        {
            return headerCodes;
        }

    }

    @Override
    protected ITabularData getDatasetLines(String dataSetCode, String filePathOrNull)
            throws IOException
    {
        return new ImagingTabularData(getDAO(), getService(), dataSetCode);
    }

    private IImagingReadonlyQueryDAO getDAO()
    {
        synchronized (this)
        {
            if (imagingDbDao == null)
            {
                imagingDbDao = DssScreeningUtils.getQuery();
            }
        }
        return imagingDbDao;
    }

    private IEncapsulatedOpenBISService getService()
    {
        synchronized (this)
        {
            if (openBisService == null)
            {
                openBisService = ServiceProvider.getOpenBISService();
            }
        }
        return openBisService;
    }

}
