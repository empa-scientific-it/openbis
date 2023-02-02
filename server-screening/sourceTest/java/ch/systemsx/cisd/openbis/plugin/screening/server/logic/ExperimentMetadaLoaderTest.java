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

package ch.systemsx.cisd.openbis.plugin.screening.server.logic;

import static org.testng.AssertJUnit.assertEquals;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import net.lemnik.eodsql.QueryTool;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.dss.etl.dataaccess.IImagingQueryDAO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Geometry;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageChannel;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageSize;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.AbstractDBTest;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.IImagingReadonlyQueryDAO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgChannelDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgContainerDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgImageDatasetDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgImageZoomLevelDTO;

/**
 * @author Kaloyan Enimanev
 */
public class ExperimentMetadaLoaderTest extends AbstractDBTest
{

    private IImagingQueryDAO dao;

    @BeforeClass(alwaysRun = true)
    public void init() throws SQLException
    {
        dao = QueryTool.getQuery(datasource, IImagingQueryDAO.class);
    }

    @Test
    public void testTwoEqualContainers()
    {
        Geometry plateGeometry = Geometry.createFromRowColDimensions(15, 10);
        Geometry tileGeometry = Geometry.createFromRowColDimensions(2, 3);
        long experimentId = createExperiment();
        createContainerWithDataSets(container(plateGeometry, experimentId), dataset(tileGeometry),
                dataset(tileGeometry));
        createContainerWithDataSets(container(plateGeometry, experimentId), dataset(tileGeometry),
                dataset(tileGeometry));

        ExperimentMetadaLoader loader = createLoader(experimentId);

        assertEquals(plateGeometry, loader.tryGetPlateGeometry());
        assertEquals(tileGeometry, loader.tryGetTileGeometry());

    }

    @Test
    public void testNoPlateGeometry()
    {
        Geometry plateGeometry1 = Geometry.createFromRowColDimensions(15, 10);
        Geometry plateGeometry2 = Geometry.createFromRowColDimensions(16, 10);
        Geometry tileGeometry = Geometry.createFromRowColDimensions(2, 3);
        long experimentId = createExperiment();
        createContainerWithDataSets(container(plateGeometry1, experimentId), dataset(tileGeometry),
                dataset(tileGeometry));
        createContainerWithDataSets(container(plateGeometry2, experimentId), dataset(tileGeometry),
                dataset(tileGeometry));

        ExperimentMetadaLoader loader = createLoader(experimentId);

        assertEquals(null, loader.tryGetPlateGeometry());
        assertEquals(tileGeometry, loader.tryGetTileGeometry());

    }

    @Test
    public void testNoTileGeometry()
    {
        Geometry plateGeometry = Geometry.createFromRowColDimensions(15, 10);
        Geometry tileGeometry1 = Geometry.createFromRowColDimensions(2, 3);
        Geometry tileGeometry2 = Geometry.createFromRowColDimensions(3, 2);
        long experimentId = createExperiment();
        createContainerWithDataSets(container(plateGeometry, experimentId), dataset(tileGeometry1),
                dataset(tileGeometry1));
        createContainerWithDataSets(container(plateGeometry, experimentId), dataset(tileGeometry2),
                dataset(tileGeometry2));

        ExperimentMetadaLoader loader = createLoader(experimentId);

        assertEquals(plateGeometry, loader.tryGetPlateGeometry());
        assertEquals(null, loader.tryGetTileGeometry());
    }

    @Test
    public void testNonUniqueChannelsFiltered()
    {
        Mockery context = new Mockery();
        final IImagingReadonlyQueryDAO dao1 = context.mock(IImagingReadonlyQueryDAO.class, "dao1");
        final IImagingReadonlyQueryDAO dao2 = context.mock(IImagingReadonlyQueryDAO.class, "dao2");

        final Long experimentId = 0L;
        final ImgChannelDTO channel1 = channel("C1");
        final ImgChannelDTO channel2 = channel("C2");
        context.checking(new Expectations()
            {
                {
                    one(dao1).getChannelsByExperimentId(experimentId);
                    will(returnValue(Arrays.asList(channel1)));

                    one(dao2).getChannelsByExperimentId(experimentId);
                    will(returnValue(Arrays.asList(channel1, channel2)));
                }
            });
        ExperimentMetadaLoader loader = createLoader(0L, dao1, dao2);

        List<ImageChannel> channels = loader.getImageChannels();
        assertEquals(2, channels.size());
        assertEquals("C1", channels.get(0).getCode());
        assertEquals("C2", channels.get(1).getCode());

        context.assertIsSatisfied();
    }

    @Test
    public void testAllSameOriginalImageSize()
    {
        Geometry plateGeometry = Geometry.createFromRowColDimensions(15, 10);
        Geometry tileGeometry = Geometry.createFromRowColDimensions(2, 3);
        long experimentId = createExperiment();
        ImgContainerDTO plate1 = container(plateGeometry, experimentId);
        ImgImageDatasetDTO ds1 = dataset(tileGeometry);
        ImgImageDatasetDTO ds2 = dataset(tileGeometry);
        List<Long> dataSetIds = createContainerWithDataSets(plate1, ds1, ds2);
        createZoomLevels(dataSetIds.get(0), zoomLevel(400, 300, true), zoomLevel(40, 30, false));
        createZoomLevels(dataSetIds.get(1), zoomLevel(400, 300, true), zoomLevel(80, 60, false));

        ExperimentMetadaLoader loader = createLoader(experimentId);
        ImageSize imageSize = loader.tryGetOriginalImageSize();

        assertEquals("400x300", imageSize.toString());
    }

    @Test
    public void testDifferentOriginalImageSize()
    {
        Geometry plateGeometry = Geometry.createFromRowColDimensions(15, 10);
        Geometry tileGeometry = Geometry.createFromRowColDimensions(2, 3);
        long experimentId = createExperiment();
        ImgContainerDTO plate1 = container(plateGeometry, experimentId);
        ImgImageDatasetDTO ds1 = dataset(tileGeometry);
        ImgImageDatasetDTO ds2 = dataset(tileGeometry);
        List<Long> dataSetIds = createContainerWithDataSets(plate1, ds1, ds2);
        createZoomLevels(dataSetIds.get(0), zoomLevel(400, 300, true), zoomLevel(40, 30, false));
        createZoomLevels(dataSetIds.get(1), zoomLevel(400, 200, true), zoomLevel(80, 60, false));

        ExperimentMetadaLoader loader = createLoader(experimentId);
        ImageSize imageSize = loader.tryGetOriginalImageSize();

        assertEquals(null, imageSize);
    }

    @Test
    public void testMissingOriginalImageSize()
    {
        Geometry plateGeometry = Geometry.createFromRowColDimensions(15, 10);
        Geometry tileGeometry = Geometry.createFromRowColDimensions(2, 3);
        long experimentId = createExperiment();
        ImgContainerDTO plate1 = container(plateGeometry, experimentId);
        ImgImageDatasetDTO ds1 = dataset(tileGeometry);
        createContainerWithDataSets(plate1, ds1);

        ExperimentMetadaLoader loader = createLoader(experimentId);
        ImageSize imageSize = loader.tryGetOriginalImageSize();

        assertEquals(null, imageSize);
    }

    @Test
    public void testCommonThumbnailImageSizes()
    {
        Geometry plateGeometry = Geometry.createFromRowColDimensions(15, 10);
        Geometry tileGeometry = Geometry.createFromRowColDimensions(2, 3);
        long experimentId = createExperiment();
        ImgContainerDTO plate1 = container(plateGeometry, experimentId);
        ImgImageDatasetDTO ds1 = dataset(tileGeometry);
        ImgImageDatasetDTO ds2 = dataset(tileGeometry);
        ImgImageDatasetDTO ds3 = dataset(tileGeometry);
        List<Long> dataSetIds = createContainerWithDataSets(plate1, ds1, ds2, ds3);
        createZoomLevels(dataSetIds.get(0), zoomLevel(4, 3, false), zoomLevel(40, 30, false),
                zoomLevel(80, 60, false), zoomLevel(400, 300, true));
        createZoomLevels(dataSetIds.get(1), zoomLevel(40, 30, false), zoomLevel(80, 60, false),
                zoomLevel(400, 300, true));
        createZoomLevels(dataSetIds.get(2), zoomLevel(40, 30, false), zoomLevel(80, 60, false),
                zoomLevel(200, 150, false), zoomLevel(400, 300, true));

        ExperimentMetadaLoader loader = createLoader(experimentId);
        List<ImageSize> sizes = loader.getThumbnailImageSizes();

        assertEquals("[40x30, 80x60]", sizes.toString());
    }

    @Test
    public void testNoCommonThumbnailImageSizes()
    {
        Geometry plateGeometry = Geometry.createFromRowColDimensions(15, 10);
        Geometry tileGeometry = Geometry.createFromRowColDimensions(2, 3);
        long experimentId = createExperiment();
        ImgContainerDTO plate1 = container(plateGeometry, experimentId);
        ImgImageDatasetDTO ds1 = dataset(tileGeometry);
        ImgImageDatasetDTO ds2 = dataset(tileGeometry);
        ImgImageDatasetDTO ds3 = dataset(tileGeometry);
        List<Long> dataSetIds = createContainerWithDataSets(plate1, ds1, ds2, ds3);
        createZoomLevels(dataSetIds.get(0), zoomLevel(40, 30, false), zoomLevel(400, 300, true));
        createZoomLevels(dataSetIds.get(1), zoomLevel(40, 30, false), zoomLevel(400, 300, true));
        createZoomLevels(dataSetIds.get(2), zoomLevel(80, 60, false), zoomLevel(400, 300, true));

        ExperimentMetadaLoader loader = createLoader(experimentId);
        List<ImageSize> sizes = loader.getThumbnailImageSizes();

        assertEquals("[]", sizes.toString());
    }

    private ImgChannelDTO channel(String code)
    {
        return new ImgChannelDTO(code, null, null, null, 0L, null, 0, 1, 2);
    }

    private long createExperiment()
    {
        return dao.addExperiment(generatePermId());
    }

    private List<Long> createContainerWithDataSets(ImgContainerDTO container,
            ImgImageDatasetDTO... datasets)
    {
        long containerId = dao.addContainer(container);
        List<Long> dataSetIds = new ArrayList<Long>();
        for (ImgImageDatasetDTO dataset : datasets)
        {
            dataset.setContainerId(containerId);
            dataSetIds.add(dao.addImageDataset(dataset));
        }
        return dataSetIds;
    }

    private void createZoomLevels(long dataSetId, ImgImageZoomLevelDTO... zoomLevels)
    {
        for (ImgImageZoomLevelDTO zoomLevel : zoomLevels)
        {
            zoomLevel.setContainerDatasetId(dataSetId);
            dao.addImageZoomLevel(zoomLevel);
        }
    }

    private ImgContainerDTO container(Geometry plateGeometry, long experimentId)
    {
        return new ImgContainerDTO(generatePermId(), plateGeometry.getNumberOfRows(),
                plateGeometry.getNumberOfColumns(), experimentId);
    }

    private ImgImageDatasetDTO dataset(Geometry tileGeometry)
    {
        return new ImgImageDatasetDTO(generatePermId(), tileGeometry.getNumberOfRows(),
                tileGeometry.getNumberOfColumns(), 0L, true, null, null);
    }

    private ImgImageZoomLevelDTO zoomLevel(int width, int height, boolean original)
    {
        // FIXME! null values for color depth and file type
        return new ImgImageZoomLevelDTO(generatePermId(), original, "", width, height, null, null,
                0);
    }

    private ExperimentMetadaLoader createLoader(long experimentId)
    {
        return createLoader(experimentId, dao);
    }

    private ExperimentMetadaLoader createLoader(long experimentId, IImagingReadonlyQueryDAO... daos)
    {
        return new ExperimentMetadaLoader(experimentId,
                Arrays.<IImagingReadonlyQueryDAO> asList(daos));
    }

    private String generatePermId()
    {
        return UUID.randomUUID().toString();
    }
}
