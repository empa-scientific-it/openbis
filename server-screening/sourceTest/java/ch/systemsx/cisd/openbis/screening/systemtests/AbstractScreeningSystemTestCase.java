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

package ch.systemsx.cisd.openbis.screening.systemtests;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

import org.springframework.mock.web.MockHttpServletRequest;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.server.asapi.v3.ApplicationServerApi;
import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.image.ImageHistogram;
import ch.systemsx.cisd.common.servlet.SpringRequestContextProvider;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.SystemTestCase;
import ch.systemsx.cisd.openbis.dss.generic.server.Utils;
import ch.systemsx.cisd.openbis.generic.server.ICommonServerForInternalUse;
import ch.systemsx.cisd.openbis.generic.server.util.TestInitializer;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.URLMethodWithParameters;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Grantee;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.util.TestInstanceHostUtils;
import ch.systemsx.cisd.openbis.plugin.generic.shared.IGenericServer;
import ch.systemsx.cisd.openbis.plugin.screening.client.api.v1.IScreeningOpenbisServiceFacade;
import ch.systemsx.cisd.openbis.plugin.screening.client.api.v1.ScreeningOpenbisServiceFacade;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientService;
import ch.systemsx.cisd.openbis.plugin.screening.server.IAnalysisSettingSetter;
import ch.systemsx.cisd.openbis.plugin.screening.shared.IScreeningServer;
import ch.systemsx.cisd.openbis.plugin.screening.shared.ResourceNames;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.IScreeningApiServer;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants.ImageServletUrlParameters;

/**
 * System test case for screening. Starts both AS and DSS.
 * 
 * @author Kaloyan Enimanev
 */
public abstract class AbstractScreeningSystemTestCase extends SystemTestCase
{

    private static final String ADMIN_USER = "admin";

    private static final class FailureReport
    {
        private File referenceImage;

        private StringBuilder failureReport = new StringBuilder();

        FailureReport(File referenceImage)
        {
            this.referenceImage = referenceImage;
        }

        boolean isFailure()
        {
            return failureReport.length() > 0;
        }

        private void addFailureMessage(String failureMessage)
        {
            failureReport.append(failureMessage).append('\n');
        }

        @Override
        public String toString()
        {
            StringBuilder builder = new StringBuilder();
            builder.append("/===== Failure Report for image ").append(referenceImage.getAbsolutePath()).append('\n');
            builder.append(failureReport).append("\\============================\n");
            return builder.toString();
        }

    }

    private static interface IImageLoader
    {
        public BufferedImage load();

    }

    /**
     * Helper class to load an image from the image download servlet.
     *
     * @author Franz-Josef Elmer
     */
    protected static final class ImageLoader implements IImageLoader
    {
        private static final class OverlayChannel
        {
            private String dataSetCode;

            private String channel;

            OverlayChannel(String dataSetCode, String channel)
            {
                this.dataSetCode = dataSetCode;
                this.channel = channel;
            }
        }

        private final URLMethodWithParameters url;

        private final List<String> channels = new ArrayList<String>();

        private final List<OverlayChannel> overlayChannels = new ArrayList<OverlayChannel>();

        private final Map<String, String> transformationsByChannel = new HashMap<String, String>();

        private boolean mergeChannels = true;

        private boolean microscopy = false;

        private int wellRow = 1;

        private int wellColumn = 1;

        private int tileRow = 1;

        private int tileColumn = 1;

        private String mode = "thumbnail160x160";

        public ImageLoader(AbstractExternalData dataSet, String sessionToken)
        {
            url = new URLMethodWithParameters(dataSet.getDataStore().getHostUrl()
                    + "/" + ScreeningConstants.DATASTORE_SCREENING_SERVLET_URL);
            url.addParameter(Utils.SESSION_ID_PARAM, sessionToken);
            url.addParameter(ImageServletUrlParameters.DATASET_CODE_PARAM, dataSet.getCode());
        }

        @Override
        public BufferedImage load()
        {
            try
            {
                return ImageIO.read(createURL());
            } catch (IOException ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }
        }

        private URL createURL()
        {
            if (mergeChannels)
            {
                url.addParameter(ImageServletUrlParameters.MERGE_CHANNELS_PARAM, "true");
            } else
            {
                for (String channel : channels)
                {
                    url.addParameter(ImageServletUrlParameters.CHANNEL_PARAM, channel);
                }
            }
            for (OverlayChannel overlayChannel : overlayChannels)
            {
                String name = ImageServletUrlParameters.OVERLAY_CHANNEL_PREFIX_PARAM + overlayChannel.dataSetCode;
                url.addParameter(name, overlayChannel.channel);
            }
            if (microscopy == false)
            {
                url.addParameter(ImageServletUrlParameters.WELL_ROW_PARAM, Integer.toString(wellRow));
                url.addParameter(ImageServletUrlParameters.WELL_COLUMN_PARAM, Integer.toString(wellColumn));
            }
            url.addParameter(ImageServletUrlParameters.TILE_ROW_PARAM, Integer.toString(tileRow));
            url.addParameter(ImageServletUrlParameters.TILE_COL_PARAM, Integer.toString(tileColumn));
            for (Entry<String, String> entry : transformationsByChannel.entrySet())
            {
                url.addParameter(ImageServletUrlParameters.SINGLE_CHANNEL_TRANSFORMATION_CODE_PARAM + entry.getKey(),
                        entry.getValue());
            }
            url.addParameter("mode", mode);
            try
            {
                return new URL(url.toString());
            } catch (MalformedURLException ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }
        }

        public ImageLoader microscopy()
        {
            microscopy = true;
            return this;
        }

        public ImageLoader wellRow(int newWellRow)
        {
            wellRow = newWellRow;
            return this;
        }

        public ImageLoader wellColumn(int newWellColumn)
        {
            wellColumn = newWellColumn;
            return this;
        }

        public ImageLoader tileRow(int newTileRow)
        {
            tileRow = newTileRow;
            return this;
        }

        public ImageLoader tileColumn(int newTileColumn)
        {
            tileColumn = newTileColumn;
            return this;
        }

        public ImageLoader mode(String newMode)
        {
            mode = newMode;
            return this;
        }

        public ImageLoader channel(String channel)
        {
            mergeChannels = false;
            channels.add(channel);
            return this;
        }

        public ImageLoader overlay(String dataSetCode, String channel)
        {
            overlayChannels.add(new OverlayChannel(dataSetCode, channel));
            return this;
        }

        public ImageLoader rescaling(String channel, int low, int high)
        {
            transformationsByChannel.put(channel,
                    ScreeningConstants.USER_DEFINED_RESCALING_CODE + "(" + low + "," + high + ")");
            return this;
        }
    }

    /**
     * Helper class to load and check a reference image (from a file) and and actual image (from image download servlet). Several image pairs can be
     * tested. An extensive human readable failure report will be created.
     *
     * @author Franz-Josef Elmer
     */
    protected static final class ImageChecker
    {
        private static final int HISTOGRAM_HEIGHT = 19;

        private static final int HISTOGRAM_WIDTH = 151;

        private final StringBuilder failureReport = new StringBuilder();

        private final File folderForWrongImages;

        private boolean assertNoFailuresAlreadyInvoked;

        public ImageChecker(File folderForWrongImages)
        {
            if (folderForWrongImages.isFile())
            {
                throw new IllegalArgumentException("Folder for wrong images is a file: " + folderForWrongImages);
            }
            this.folderForWrongImages = folderForWrongImages;
        }

        /**
         * Asserts no failures occurred for all invocations of {@link #check(File, IImageLoader)}. Otherwise an {@link AssertionError} is thrown.
         */
        public void assertNoFailures()
        {
            if (assertNoFailuresAlreadyInvoked == false)
            {
                assertNoFailuresAlreadyInvoked = true;
                AssertJUnit.assertEquals("", failureReport.toString());
            }
        }

        /**
         * Checks that the specified reference image is equals to the image loaded from the specified file in the specified data set.
         */
        public void check(File referenceImage, final String sessionToken, final AbstractExternalData dataSet,
                final String pathInDataSet)
        {
            check(referenceImage, new IImageLoader()
                {

                    @Override
                    public BufferedImage load()
                    {
                        URLMethodWithParameters url = new URLMethodWithParameters(dataSet.getDataStore().getHostUrl()
                                + "/datastore_server/" + dataSet.getCode() + "/" + pathInDataSet);
                        url.addParameter(Utils.SESSION_ID_PARAM, sessionToken);
                        url.addParameter("mode", "simpleHtml");
                        try
                        {
                            return ImageIO.read(new URL(url.toString()));
                        } catch (Exception ex)
                        {
                            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
                        }
                    }
                });
        }

        /**
         * Checks that the specified reference image is equals to the image loaded by the specified image loader. A report is created in case of a
         * failure.
         */
        public void check(File referenceImage, IImageLoader imageLoader)
        {
            FailureReport report = new FailureReport(referenceImage);
            try
            {
                BufferedImage actualImage = imageLoader.load();
                checkEquals(report, referenceImage, actualImage);
                saveActualImageIfDifferent(actualImage, referenceImage, report);
            } catch (IOException ex)
            {
                report.addFailureMessage("Couldn't save image: " + ex);
            } finally
            {
                if (report.isFailure())
                {
                    failureReport.append(report);
                }
            }
        }

        private BufferedImage load(File referenceImage)
        {
            try
            {
                return ImageIO.read(referenceImage);
            } catch (IOException ex)
            {
                return null;
            }
        }

        private void saveActualImageIfDifferent(BufferedImage actualImage, File referenceImage, FailureReport report) throws IOException
        {
            if (report.isFailure() == false || actualImage == null)
            {
                return;
            }
            folderForWrongImages.mkdirs();
            File file = new File(folderForWrongImages, referenceImage.getName());
            boolean success = ImageIO.write(actualImage, "png", file);
            if (success == false)
            {
                report.addFailureMessage("Couldn't save actual image in file " + file.getAbsolutePath() + ".");
            } else
            {
                report.addFailureMessage("Actual image is saved in file " + file.getAbsolutePath() + ".");
            }
        }

        private void checkEquals(FailureReport report, File referenceImage, BufferedImage actualImage)
        {
            BufferedImage expectedImage = load(referenceImage);
            if (expectedImage == null)
            {
                report.addFailureMessage("Expected image is undefined");
            }
            if (actualImage == null)
            {
                report.addFailureMessage("Actual image is undefined");
            }
            if (expectedImage == null || actualImage == null)
            {
                return;
            }
            checkEquals(report, "Image height", expectedImage.getHeight(), actualImage.getHeight());
            checkEquals(report, "Image width", expectedImage.getWidth(), actualImage.getWidth());
            checkEquals(report, "Type", expectedImage.getType(), actualImage.getType());
            ColorModel expectedColorModel = expectedImage.getColorModel();
            ColorModel actualColorModel = actualImage.getColorModel();
            checkEquals(report, "Pixel size", expectedColorModel.getPixelSize(), actualColorModel.getPixelSize());
            ColorSpace expectedColorSpace = expectedColorModel.getColorSpace();
            ColorSpace actualColorSpace = actualColorModel.getColorSpace();
            checkEquals(report, "Color space type", expectedColorSpace.getType(), actualColorSpace.getType());
            ImageHistogram expectedHistogram = ImageHistogram.calculateHistogram(expectedImage);
            ImageHistogram actualHistogram = ImageHistogram.calculateHistogram(actualImage);
            String expectedRenderedHistogram = expectedHistogram.renderAsASCIIChart(HISTOGRAM_WIDTH, HISTOGRAM_HEIGHT);
            String actualRenderedHistogram = actualHistogram.renderAsASCIIChart(HISTOGRAM_WIDTH, HISTOGRAM_HEIGHT);
            if (expectedRenderedHistogram.equals(actualRenderedHistogram))
            {
                printComparisonMessage(referenceImage, expectedRenderedHistogram);
                return;
            }
            // difference can be caused by a value close to discretization border. Thus try another height.
            expectedRenderedHistogram = expectedHistogram.renderAsASCIIChart(HISTOGRAM_WIDTH, HISTOGRAM_HEIGHT + 1);
            actualRenderedHistogram = actualHistogram.renderAsASCIIChart(HISTOGRAM_WIDTH, HISTOGRAM_HEIGHT + 1);
            if (expectedRenderedHistogram.equals(actualRenderedHistogram))
            {
                printComparisonMessage(referenceImage, expectedRenderedHistogram);
                return;
            }
            report.addFailureMessage("expected histogram of " + expectedImage + ":\n" + expectedRenderedHistogram
                    + "actual histogram of " + actualImage + ":\n" + actualRenderedHistogram);
        }

        private void printComparisonMessage(File referenceImage,
                String expectedRenderedHistogram)
        {
            System.out.println("Histogram of reference image (" + referenceImage
                    + ") equals actual histogram:\n" + expectedRenderedHistogram);
        }

        private void checkEquals(FailureReport report, String message, Object expected, Object actual)
        {
            if (expected == null ? expected == actual : expected.equals(actual))
            {
                return;
            }
            report.addFailureMessage(message + ":\n  Expected: <" + expected + ">\n    Actual: <" + actual + ">");
        }
    }

    protected IScreeningClientService screeningClientService;

    protected IScreeningServer screeningServer;

    protected IScreeningApiServer screeningApiServer;

    protected IAnalysisSettingSetter analysisSettingServer;

    protected String sessionToken;

    protected IScreeningOpenbisServiceFacade screeningFacade;

    protected ICommonServer commonServer;

    protected IGenericServer genericServer;

    protected IApplicationServerApi v3api;

    @BeforeMethod
    public void setUpServices()
    {
        commonServer = (ICommonServer) applicationContext
                .getBean(ch.systemsx.cisd.openbis.generic.shared.ResourceNames.COMMON_SERVER);
        genericServer = (IGenericServer) applicationContext
                .getBean(ch.systemsx.cisd.openbis.plugin.generic.shared.ResourceNames.GENERIC_PLUGIN_SERVER);
        screeningClientService =
                (IScreeningClientService) applicationContext
                        .getBean(ResourceNames.SCREENING_PLUGIN_SERVICE);
        ((SpringRequestContextProvider) applicationContext.getBean("request-context-provider"))
                .setRequest(new MockHttpServletRequest());
        Object bean = applicationContext.getBean(ResourceNames.SCREENING_PLUGIN_SERVER);
        screeningServer = (IScreeningServer) bean;
        screeningApiServer = (IScreeningApiServer) bean;
        analysisSettingServer = (IAnalysisSettingSetter) bean;

        setUpUser();

        screeningFacade =
                ScreeningOpenbisServiceFacade.tryCreateForTest(sessionToken,
                        TestInstanceHostUtils.getOpenBISUrl(), screeningApiServer);

        v3api = (IApplicationServerApi) applicationContext
                .getBean(ApplicationServerApi.INTERNAL_SERVICE_NAME);
    }

    private void setUpUser()
    {
        ICommonServerForInternalUse commonServerInternal =
                (ICommonServerForInternalUse) applicationContext
                        .getBean(ch.systemsx.cisd.openbis.generic.shared.ResourceNames.COMMON_SERVER);
        String systemSessionToken = commonServerInternal.tryToAuthenticateAsSystem().getSessionToken();

        if (false == hasUser(systemSessionToken, ADMIN_USER))
        {
            commonServerInternal.registerPerson(systemSessionToken, ADMIN_USER);
            Grantee grantee = Grantee.createPerson(ADMIN_USER);
            commonServerInternal.registerInstanceRole(systemSessionToken, RoleCode.ADMIN, grantee);
        }

        sessionToken = screeningClientService.tryToLogin(ADMIN_USER, "a").getSessionID();
    }

    private boolean hasUser(String systemSessionToken, String userId)
    {
        List<Person> persons = commonServer.listPersons(systemSessionToken);
        for (Person person : persons)
        {
            if (person.getUserId().equals(userId))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Return the location of the openBIS application context config.
     */
    @Override
    protected String getApplicationContextLocation()
    {
        return "classpath:screening-applicationContext.xml";
    }

    /**
     * sets up the openbis database to be used by the tests.
     */
    @Override
    protected void setUpDatabaseProperties()
    {
        TestInitializer.initEmptyDbWithIndex();
    }

    @Override
    protected File getIncomingDirectory()
    {
        return new File(rootDir, "incoming-" + getClass().getSimpleName());
    }

    @Override
    protected int dataSetImportWaitDurationInSeconds()
    {
        return 600;
    }

}
