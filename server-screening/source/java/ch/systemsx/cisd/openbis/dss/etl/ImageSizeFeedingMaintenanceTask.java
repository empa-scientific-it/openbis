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

package ch.systemsx.cisd.openbis.dss.etl;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import net.lemnik.eodsql.QueryTool;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.maintenance.IDataStoreLockingMaintenanceTask;
import ch.systemsx.cisd.common.utilities.Counters;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.dss.etl.dataaccess.IImagingQueryDAO;
import ch.systemsx.cisd.openbis.dss.etl.dataaccess.ImagingDatasetLoader;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.Size;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgImageDatasetDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgImageZoomLevelDTO;

/**
 * Task which feeds imageing database with images sizes of already registered data sets of type HCS_IMAGE*.
 * 
 * @author Franz-Josef Elmer
 */
public class ImageSizeFeedingMaintenanceTask implements IDataStoreLockingMaintenanceTask
{
    private static final String THUMBNAIL = "thumbnail";

    private static final String ORIGINAL = "original";

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            ImageSizeFeedingMaintenanceTask.class);

    private static final int MAX_NUMBER_OF_EXCEPTIONS_SHOWN = 10;

    private static final Pattern DATA_SET_TYPE_PATTERN = Pattern.compile("HCS_IMAGE.*");

    private IImagingQueryDAO dao;

    private IEncapsulatedOpenBISService service;

    private IHierarchicalContentProvider contentProvider;

    public ImageSizeFeedingMaintenanceTask()
    {
    }

    ImageSizeFeedingMaintenanceTask(IImagingQueryDAO dao, IEncapsulatedOpenBISService service,
            IHierarchicalContentProvider contentProvider)
    {
        this.dao = dao;
        this.service = service;
        this.contentProvider = contentProvider;
    }

    @Override
    public boolean requiresDataStoreLock()
    {
        return true;
    }

    @Override
    public void setUp(String pluginName, Properties properties)
    {
        DataSource dataSource = ServiceProvider.getDataSourceProvider().getDataSource(properties);
        dao = QueryTool.getQuery(dataSource, IImagingQueryDAO.class);
        dao.listSpots(0L); // testing correct database set up
        service = ServiceProvider.getOpenBISService();
        contentProvider = ServiceProvider.getHierarchicalContentProvider();
    }

    @Override
    public void execute()
    {
        List<SimpleDataSetInformationDTO> dataSets = service.listPhysicalDataSets();
        operationLog.info("Scan " + dataSets.size() + " data sets.");
        List<String> exceptions = new ArrayList<String>();
        Counters<String> counters = new Counters<String>();
        for (SimpleDataSetInformationDTO dataSet : dataSets)
        {
            if (matchingType(dataSet))
            {
                handleDataSet(dataSet.getDataSetCode(), exceptions, counters);
            }
        }
        logExceptionsIfAny(exceptions);
        operationLog.info(counters.getCountOf(ORIGINAL) + " original image sizes and "
                + counters.getCountOf(THUMBNAIL)
                + " thumbnail image sizes are added to the database.");
    }

    private void handleDataSet(String dataSetCode, List<String> exceptions,
            Counters<String> counters)
    {
        IHierarchicalContent content = null;
        try
        {
            content = contentProvider.asContentWithoutModifyingAccessTimestamp(dataSetCode);
            ImgImageDatasetDTO imageDataSet = dao.tryGetImageDatasetByPermId(dataSetCode);
            if (imageDataSet != null)
            {
                long dataSetId = imageDataSet.getId();
                List<ImgImageZoomLevelDTO> zoomLevels = dao.listImageZoomLevels(dataSetId);
                if (zoomLevels.isEmpty())
                {
                    IImagingDatasetLoader loader = createImageLoader(dataSetCode, content);
                    AbsoluteImageReference originalImage = loader.tryFindAnyOriginalImage();
                    String logEntryOriginal =
                            addZoomLevel(dataSetCode, dataSetId, originalImage, true);
                    AbsoluteImageReference thumbnail = loader.tryFindAnyThumbnail();
                    String logEntryThumbnail =
                            addZoomLevel(dataSetCode, dataSetId, thumbnail, false);
                    dao.commit();
                    if (logEntryOriginal != null)
                    {
                        operationLog.info(logEntryOriginal);
                        counters.count(ORIGINAL);
                    }
                    if (logEntryThumbnail != null)
                    {
                        operationLog.info(logEntryThumbnail);
                        counters.count(THUMBNAIL);
                    }
                }
            }
        } catch (Exception ex)
        {
            dao.rollback();
            exceptions.add("Data set " + dataSetCode + ": " + ex.toString());
        } finally
        {
            if (content != null)
            {
                content.close();
            }
        }
    }

    protected IImagingDatasetLoader createImageLoader(String dataSetCode,
            IHierarchicalContent content)
    {
        return ImagingDatasetLoader.tryCreate(dao, dataSetCode, content);
    }

    private void logExceptionsIfAny(List<String> exceptions)
    {
        if (exceptions.isEmpty() == false)
        {
            boolean more = exceptions.size() > MAX_NUMBER_OF_EXCEPTIONS_SHOWN;
            operationLog.error(exceptions.size()
                    + " exceptions occured"
                    + (more ? " (only the first " + MAX_NUMBER_OF_EXCEPTIONS_SHOWN
                            + " are logged):" : ":"));
            for (int i = 0; i < Math.min(MAX_NUMBER_OF_EXCEPTIONS_SHOWN, exceptions.size()); i++)
            {
                operationLog.error(exceptions.get(i));
            }
        }
    }

    private String addZoomLevel(String dataSetCode, long dataSetId, AbsoluteImageReference image,
            boolean original)
    {
        if (image == null)
        {
            return null;
        }
        Size size = image.getUnchangedImageSize();
        int width = size.getWidth();
        int height = size.getHeight();
        int colorDepth = image.getColorDepth();
        dao.addImageZoomLevel(new ImgImageZoomLevelDTO(dataSetCode, original, "", width, height,
                colorDepth, null, dataSetId));
        return (original ? "Original" : "Thumbnail") + " size " + width + "x" + height
                + " added for data set " + dataSetCode;
    }

    private boolean matchingType(SimpleDataSetInformationDTO dataSet)
    {
        String dataSetType = dataSet.getDataSetType();
        return DATA_SET_TYPE_PATTERN.matcher(dataSetType).matches();
    }

}
