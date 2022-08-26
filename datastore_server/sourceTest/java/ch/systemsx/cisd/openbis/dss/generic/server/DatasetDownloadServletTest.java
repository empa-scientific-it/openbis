/*
 * Copyright 2008 ETH Zuerich, CISD
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

import static ch.systemsx.cisd.common.test.AssertionUtil.assertContains;
import static ch.systemsx.cisd.openbis.dss.generic.shared.Constants.DEFAULT_SHARE_ID;
import static ch.systemsx.cisd.openbis.generic.shared.basic.GenericSharedConstants.DATA_STORE_SERVER_WEB_APPLICATION_NAME;
import static org.testng.AssertJUnit.assertEquals;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Level;
import org.eclipse.jetty.util.URIUtil;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.base.utilities.OSUtilities;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.test.AssertionUtil;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.DefaultFileBasedHierarchicalContentFactory;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.IHierarchicalContentFactory;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.MockDataSetDirectoryProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.HierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetDirectoryProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DatasetLocationUtil;
import ch.systemsx.cisd.openbis.generic.shared.IServiceForDataStoreServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStore;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalDataLocationNode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LocatorType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PhysicalDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.pat.IPersonalAccessTokenConverter;
import ch.systemsx.cisd.openbis.util.LogRecordingUtils;

/**
 * @author Franz-Josef Elmer
 */
@Test
@Friend(toClasses = HierarchicalContentProvider.class)
public class DatasetDownloadServletTest
{
    private static final String REQUEST_URI_PREFIX = "/" + DATA_STORE_SERVER_WEB_APPLICATION_NAME
            + "/";

    private static final String EXPIRATION_MESSAGE =
            "<html><body>Download session expired.</body></html>";

    private static final String LOGGER_NAME = "OPERATION.AbstractDatasetDownloadServlet";

    private static final String CONTENT_PROVIDER_LOGGER_NAME =
            "OPERATION.HierarchicalContentProvider";

    private static final String LOG_INFO = "INFO  " + LOGGER_NAME + " - ";

    private static final String LOG_ERROR = "ERROR " + LOGGER_NAME + " - ";

    private static final String CONTENT_PROVIDER_LOG_ERROR = "ERROR "
            + CONTENT_PROVIDER_LOGGER_NAME + " - ";

    private static final String DATABASE_INSTANCE_UUID = "db-uuid";

    private static final File TEST_FOLDER = new File("targets/unit-test/store");

    private static final String EXAMPLE_DATA_SET_CODE = "1234-1";

    private static final File EXAMPLE_DATA_SET_FOLDER = getDatasetDirectoryLocation(TEST_FOLDER,
            EXAMPLE_DATA_SET_CODE);

    private static final String EXAMPLE_FILE_NAME = "read me @home.txt";

    private static final String ESCAPED_FILE_NAME_ENCODED = URIUtil.encodePath(EXAMPLE_FILE_NAME);

    private static final File EXAMPLE_FILE = new File(EXAMPLE_DATA_SET_FOLDER, EXAMPLE_FILE_NAME);

    private static final String EXAMPLE_FILE_CONTENT = "Hello world!";

    private static final String EXAMPLE_DATA_SET_SUB_FOLDER_NAME = "+ s % ! # @";

    private static final String ESCAPED_EXAMPLE_DATA_SET_SUB_FOLDER_NAME = URIUtil.encodePath(EXAMPLE_DATA_SET_SUB_FOLDER_NAME);

    private static final File EXAMPLE_DATA_SET_SUB_FOLDER = new File(EXAMPLE_DATA_SET_FOLDER, EXAMPLE_DATA_SET_SUB_FOLDER_NAME);

    private static final String EXAMPLE_SESSION_ID = "AV76CF";

    private static final String EXPERIMENT_CODE = "EPERIMENT-E";

    private static final String SPACE_CODE = "GROUP-G";

    private static final String PROJECT_CODE = "PROJECT-P";

    private BufferedAppender logRecorder;

    private Mockery context;

    private HttpServletRequest request;

    private HttpServletResponse response;

    private HttpSession httpSession;

    private IShareIdManager shareIdManager;

    private IEncapsulatedOpenBISService openbisService;

    private IHierarchicalContentProvider hierarchicalContentProvider;

    private IPersonalAccessTokenConverter personalAccessTokenConverter;

    private IServiceForDataStoreServer service;

    @BeforeMethod
    public void setUp() throws URISyntaxException
    {
        System.setProperty("java.awt.headless", "true");
        logRecorder = LogRecordingUtils.createRecorder("%-5p %c - %m%n", Level.DEBUG);
        context = new Mockery();
        request = context.mock(HttpServletRequest.class);
        response = context.mock(HttpServletResponse.class);
        shareIdManager = context.mock(IShareIdManager.class);
        openbisService = context.mock(IEncapsulatedOpenBISService.class);
        service = context.mock(IServiceForDataStoreServer.class);
        // test with DefaultFileBasedHierarchicalContentFactory to actually access files
        final IHierarchicalContentFactory fileBasedContentFactory =
                new DefaultFileBasedHierarchicalContentFactory();
        final IDataSetDirectoryProvider dummyDirectoryProvider =
                new MockDataSetDirectoryProvider(TEST_FOLDER, DEFAULT_SHARE_ID, shareIdManager);
        hierarchicalContentProvider =
                new HierarchicalContentProvider(openbisService, dummyDirectoryProvider,
                        fileBasedContentFactory, null, null, null, "STANDARD", null);
        personalAccessTokenConverter = new IPersonalAccessTokenConverter()
        {
            @Override public boolean shouldConvert(final String sessionTokenOrPAT)
            {
                return false;
            }

            @Override public String convert(final String sessionTokenOrPAT)
            {
                return sessionTokenOrPAT;
            }
        };

        httpSession = context.mock(HttpSession.class);
        TEST_FOLDER.mkdirs();
        EXAMPLE_DATA_SET_FOLDER.mkdirs();
        FileUtilities.writeToFile(EXAMPLE_FILE, EXAMPLE_FILE_CONTENT);
        EXAMPLE_DATA_SET_SUB_FOLDER.mkdir();
        context.checking(new Expectations()
        {
            {
                allowing(httpSession).setAttribute(with("openbis-session-id"), with(any(Object.class)));
                allowing(httpSession).getAttribute(with("openbis-session-id"));
                will(returnValue(EXAMPLE_SESSION_ID));
            }
        });

    }

    @AfterMethod
    public void tearDown()
    {
        logRecorder.reset();
        FileUtilities.deleteRecursively(TEST_FOLDER);
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void testGetMimetype()
    {
        assertEquals("image/tiff", Utils.getMimeType("image.tiff", false));
        assertEquals("binary", Utils.getMimeType("image.tiff", true));
        assertEquals("image/tiff", Utils.getMimeType("image.TIF", false));
        assertEquals("binary", Utils.getMimeType("image.TIF", true));
        assertEquals("application/pdf", Utils.getMimeType("doc.pdf", false));
        assertEquals("text/plain", Utils.getMimeType("filewithoutext", false));
    }

    @Test
    public void testInitialDoGet() throws Exception
    {
        final StringWriter writer = new StringWriter();
        final AbstractExternalData externalData = createDataSet();
        prepareParseRequestURL();
        prepareCheckSession();
        prepareForObtainingDataSetFromServer(externalData, true);
        prepareForGettingDataSetFromSession(externalData, "");
        prepareLocking();
        prepareForCreatingHTML(writer);

        DatasetDownloadServlet servlet = createServlet();
        servlet.doGet(request, response);

        String output = writer.toString();

        String directorySize = null;
        if (output.contains("4 KB"))
        {
            directorySize = "4 KB";
        } else
        {
            directorySize = "68 bytes";
        }

        assertEquals(
                "<html><head><style type='text/css'> * { margin: 3px; }html { height: 100%;  }"
                        + "body { height: 100%; font-family: verdana, tahoma, helvetica; "
                        + "font-size: 11px; text-align:left; }"
                        + "h1 { text-align: center; padding: 1em; color: #1E4E8F;}"
                        + ".td_hd { border: 1px solid #FFFFFF; padding 3px; "
                        + "background-color: #DDDDDD; height: 1.5em; }"
                        + ".div_hd { background-color: #1E4E8F; color: white; "
                        + "font-weight: bold; padding: 3px; }"
                        + "table { border-collapse: collapse; padding: 1em; }"
                        + "tr, td { font-family: verdana, tahoma, helvetica; font-size: 11px; }"
                        + ".td_file { font-family: verdana, tahoma, helvetica; "
                        + "font-size: 11px; height: 1.5em }"
                        + ".wrapper { min-height: 100%; height: auto !important; height: 100%;"
                        + " margin: 0em auto -4em; }"
                        + ".footer { height: 4em; text-align: center; }</style></head>"
                        + "<body><table> "
                        + OSUtilities.LINE_SEPARATOR
                        + "<tr><td class='td_file'>"
                        + "<a href='/datastore_server/1234-1/" + ESCAPED_EXAMPLE_DATA_SET_SUB_FOLDER_NAME
                        + "?disableLinks=false&mode=simpleHtml&sessionID=AV76CF'>"
                        + "+ s % ! # @/</a></td><td>" + directorySize + "</td><td></td></tr>"
                        + OSUtilities.LINE_SEPARATOR
                        + "<tr><td class='td_file'>"
                        + "<a href='/datastore_server/1234-1/" + ESCAPED_FILE_NAME_ENCODED + "?disableLinks=false&mode=simpleHtml&sessionID=AV76CF'>"
                        + "read me @home.txt</a></td><td>12 bytes</td><td></td></tr>"
                        + OSUtilities.LINE_SEPARATOR + "</table> </div> </body></html>"
                        + OSUtilities.LINE_SEPARATOR + "",
                output);

        String normalizedLogContent = getNormalizedLogContent();
        assertContains(getSessionCreationLogMessage() + OSUtilities.LINE_SEPARATOR + LOG_INFO
                + "For data set '1234-1' show directory ''", normalizedLogContent);

        context.assertIsSatisfied();
    }

    private void prepareParseRequestURL()
    {
        context.checking(new Expectations()
            {
                {
                    one(request).getParameter(Utils.SESSION_ID_PARAM);
                    will(returnValue(EXAMPLE_SESSION_ID));

                    one(request).getParameter(AbstractDatasetDownloadServlet.DISPLAY_MODE_PARAM);
                    will(returnValue(null));

                    one(request).getParameter(DatasetDownloadServlet.AUTO_RESOLVE_KEY);
                    will(returnValue(null));

                    one(request).getParameter(DatasetDownloadServlet.FORCE_AUTO_RESOLVE_KEY);
                    will(returnValue(null));

                    one(request).getParameter(DatasetDownloadServlet.MAIN_DATA_SET_PATH_KEY);
                    will(returnValue(null));

                    one(request).getParameter(DatasetDownloadServlet.MAIN_DATA_SET_PATTERN_KEY);
                    will(returnValue(null));

                    one(request).getParameter(DatasetDownloadServlet.DISABLE_LINKS);
                    will(returnValue(null));

                    one(request).getScheme();
                    one(request).getHeader("referer");

                    allowing(request).getParameter("is_link_data");
                    will(returnValue(null));
                }
            });
    }

    private void prepareCheckSession()
    {
        context.checking(new Expectations()
            {
                {
                    one(service).checkSession(EXAMPLE_SESSION_ID);
                }
            });
    }

    @Test()
    public void testInitialDoGetButDataSetNotFoundInStore() throws Exception
    {
        final StringWriter writer = new StringWriter();
        final AbstractExternalData externalData = createDataSet();
        prepareParseRequestURL();
        prepareCheckSession();
        prepareForObtainingDataSetFromServer(externalData, true);
        prepareLocking();
        context.checking(new Expectations()
            {
                {
                    prepareGetRequestURI(this, externalData, "blabla");

                    one(request).getRequestURL();
                    will(returnValue(new StringBuffer("requestURL")));

                    one(request).getQueryString();
                    will(returnValue("queryString"));

                    one(response).setContentType("text/html");
                    one(response).getWriter();
                    will(returnValue(new PrintWriter(writer)));
                }
            });

        DatasetDownloadServlet servlet = createServlet();
        servlet.doGet(request, response);
        String pageContent = writer.toString();
        String snippet = "Resource 'blabla' does not exist.";
        assertEquals("Text snippet >" + snippet + "< not found in following page content: "
                + pageContent, true, pageContent.indexOf(snippet) > 0);
        String logContent = logRecorder.getLogContent();
        assertEquals("Text snippet >" + snippet + "< not found in following page content: "
                + logContent, true, logContent.indexOf(snippet) > 0);

        context.assertIsSatisfied();
    }

    @Test()
    public void testDoGetButUnknownDataSetCode() throws Exception
    {
        final StringWriter writer = new StringWriter();
        prepareParseRequestURL();
        prepareCheckSession();
        prepareForObtainingDataSetFromServer(null, false);
        context.checking(new Expectations()
            {
                {
                    one(request).getRequestURI();
                    will(returnValue(REQUEST_URI_PREFIX + EXAMPLE_DATA_SET_CODE));

                    one(response).setContentType("text/html");
                    one(response).getWriter();
                    will(returnValue(new PrintWriter(writer)));
                }
            });

        DatasetDownloadServlet servlet = createServlet();
        servlet.doGet(request, response);
        AssertionUtil.assertContainsLines("<html><body><h1>Error</h1>" + OSUtilities.LINE_SEPARATOR
                + "Unknown data set: 1234-1" + OSUtilities.LINE_SEPARATOR + "</body></html>"
                + OSUtilities.LINE_SEPARATOR, writer.toString());
        String logContent = logRecorder.getLogContent();
        AssertionUtil.assertContainsLines(getSessionCreationLogMessage() + OSUtilities.LINE_SEPARATOR
                + CONTENT_PROVIDER_LOG_ERROR + "Data set '1234-1' not found in openBIS server."
                + OSUtilities.LINE_SEPARATOR + LOG_ERROR + "Unknown data set: 1234-1",
                logContent);

        context.assertIsSatisfied();
    }

    private String getSessionCreationLogMessage()
    {
        return LOG_INFO
                + "Creating a new session with the following parameters: [sessionID=AV76CF] Session Timeout: 120 sec";
    }

    @Test
    public void testDoGetSubFolder() throws Exception
    {
        final StringWriter writer = new StringWriter();
        final AbstractExternalData externalData = createDataSet();
        prepareCheckSession();
        prepareForObtainingDataSetFromServer(externalData, true);
        prepareForGettingDataSetFromSession(externalData, ESCAPED_EXAMPLE_DATA_SET_SUB_FOLDER_NAME);
        prepareLocking();
        context.checking(new Expectations()
            {
                {
                    prepareParseRequestURLNoSession(this);

                    allowing(request).getSession(false);
                    will(returnValue(httpSession));
                }
            });
        prepareForCreatingHTML(writer);

        DatasetDownloadServlet servlet = createServlet();
        servlet.doGet(request, response);
        assertEquals(
                "<html><head><style type='text/css'> * { margin: 3px; }html { height: 100%;  }body { height: 100%; font-family: verdana, tahoma, helvetica; font-size: 11px; text-align:left; }h1 { text-align: center; padding: 1em; color: #1E4E8F;}.td_hd { border: 1px solid #FFFFFF; padding 3px; background-color: #DDDDDD; height: 1.5em; }.div_hd { background-color: #1E4E8F; color: white; font-weight: bold; padding: 3px; }table { border-collapse: collapse; padding: 1em; }tr, td { font-family: verdana, tahoma, helvetica; font-size: 11px; }.td_file { font-family: verdana, tahoma, helvetica; font-size: 11px; height: 1.5em }.wrapper { min-height: 100%; height: auto !important; height: 100%; margin: 0em auto -4em; }.footer { height: 4em; text-align: center; }</style></head><body><table> <tr><td class='td_hd'>Folder:</td><td>+ s % ! # @</td></tr>"
                        + OSUtilities.LINE_SEPARATOR
                        + "<tr><td class='td_file'><a href='/datastore_server/1234-1/?disableLinks=false&mode=simpleHtml&sessionID=AV76CF'>..</a></td><td></td><td></td></tr>"
                        + OSUtilities.LINE_SEPARATOR
                        + "</table> </div> </body></html>"
                        + OSUtilities.LINE_SEPARATOR,
                writer.toString());
        assertContains(LOG_INFO + "For data set '1234-1' show directory '"
                + EXAMPLE_DATA_SET_SUB_FOLDER_NAME + "'", getNormalizedLogContent());

        context.assertIsSatisfied();
    }

    @Test()
    public void testDoGetFile() throws Exception
    {
        final AbstractExternalData externalData = createDataSet();

        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        prepareParseRequestURL();
        prepareCheckSession();
        prepareCheckDatasetAccess();
        prepareForObtainingDataSetFromServer(externalData, true);
        prepareLocking();

        context.checking(new Expectations()
            {
                {

                    prepareGetRequestURI(this, externalData, ESCAPED_FILE_NAME_ENCODED);

                    one(response).setContentType("text/plain");
                    one(response).setContentLength(EXAMPLE_FILE_CONTENT.length());
                    one(response).setHeader("Content-Disposition",
                            "inline; filename=\"" + EXAMPLE_FILE_NAME + "\"");
                    one(response).getOutputStream();
                    will(returnValue(new ServletOutputStream()
                        {
                            @Override
                            public void write(int b) throws IOException
                            {
                                outputStream.write(b);
                            }

                            @Override
                            public boolean isReady()
                            {
                                return true;
                            }

                            @Override
                            public void setWriteListener(WriteListener arg0)
                            {
                                // TODO Auto-generated method stub

                            }
                        }));
                }
            });

        DatasetDownloadServlet servlet = createServlet();
        servlet.doGet(request, response);
        assertEquals("Hello world!", outputStream.toString());

        String normalizedLogContent = getNormalizedLogContent();
        assertContains(getSessionCreationLogMessage() + OSUtilities.LINE_SEPARATOR + LOG_INFO
                + "Check access to the data set '1234-1' at openBIS server.", normalizedLogContent);
        assertContains(LOG_INFO + "For data set '1234-1' deliver file "
                + "'read me @home.txt' (12 bytes).", normalizedLogContent);

        context.assertIsSatisfied();
    }

    @Test(groups = "slow")
    public void testDoGetThumbnail() throws Exception
    {
        BufferedImage image = new BufferedImage(100, 200, BufferedImage.TYPE_INT_RGB);
        ImageIO.write(image, "png", EXAMPLE_FILE);
        prepareParseRequestURLForThumbnail(100, 50);
        prepareCheckSession();
        final AbstractExternalData externalData = createDataSet();
        prepareCheckDatasetAccess();
        prepareForObtainingDataSetFromServer(externalData, true);
        prepareLocking();
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        context.checking(new Expectations()
            {
                {
                    one(request).getRequestURI();
                    will(returnValue(REQUEST_URI_PREFIX + EXAMPLE_DATA_SET_CODE + "/"
                            + ESCAPED_FILE_NAME_ENCODED));

                    allowing(request).getParameter("is_link_data");
                    will(returnValue(null));
                    one(response).setContentType("image/png");
                    one(response).setContentLength(84);
                    one(response).addHeader("Cache-Control", "max-age=7200");
                    one(response).setHeader("Content-Disposition",
                            "inline; filename=\"" + EXAMPLE_FILE_NAME + "\"");
                    one(response).getOutputStream();
                    will(returnValue(new ServletOutputStream()
                        {
                            @Override
                            public void write(int b) throws IOException
                            {
                                outputStream.write(b);
                            }

                            @Override
                            public boolean isReady()
                            {
                                // TODO Auto-generated method stub
                                return true;
                            }

                            @Override
                            public void setWriteListener(WriteListener arg0)
                            {
                                // TODO Auto-generated method stub

                            }
                        }));
                }
            });

        DatasetDownloadServlet servlet = createServlet();
        servlet.doGet(request, response);
        BufferedImage thumbnail =
                ImageIO.read(new ByteArrayInputStream(outputStream.toByteArray()));
        assertEquals(25, thumbnail.getWidth());
        assertEquals(50, thumbnail.getHeight());
        String normalizedLogContent = getNormalizedLogContent();
        assertContains(getSessionCreationLogMessage() + OSUtilities.LINE_SEPARATOR + LOG_INFO
                + "Check access to the data set '1234-1' at openBIS server.", normalizedLogContent);
        AssertionUtil.assertContains(LOG_INFO
                + "For data set '1234-1' deliver file 'read me @home.txt' "
                + "(84 bytes) as a thumbnail.", normalizedLogContent);

        context.assertIsSatisfied();
    }

    @Test()
    public void testDoGetNonExistingFile() throws Exception
    {
        final StringWriter writer = new StringWriter();
        final AbstractExternalData externalData = createDataSet();
        prepareParseRequestURL();
        prepareCheckSession();
        prepareForObtainingDataSetFromServer(externalData, true);
        prepareLocking();
        context.checking(new Expectations()
            {
                {
                    prepareGetRequestURI(this, externalData, "blabla");

                    one(request).getRequestURL();
                    will(returnValue(new StringBuffer("requestURL")));

                    one(request).getQueryString();
                    will(returnValue("queryString"));

                    one(response).setContentType("text/html");
                    one(response).getWriter();
                    will(returnValue(new PrintWriter(writer)));
                }
            });

        DatasetDownloadServlet servlet = createServlet();
        servlet.doGet(request, response);
        assertEquals("<html><body><h1>Error</h1>" + OSUtilities.LINE_SEPARATOR
                + "Resource 'blabla' does not exist." + OSUtilities.LINE_SEPARATOR
                + "</body></html>" + OSUtilities.LINE_SEPARATOR, writer.toString());
        String logContent = getNormalizedLogContent();
        String[] logContentLines = logContent.split("\n");
        // Skip the first line which has information about session creation
        logContent = logContentLines[1];
        assertEquals(
                "The following string does not start as expected: " + logContent,
                true,
                logContent.startsWith(LOG_ERROR
                        + "Request requestURL?queryString caused an exception:"));

        context.assertIsSatisfied();
    }

    @Test()
    public void testDoGetForExpiredSession() throws Exception
    {
        final StringWriter writer = new StringWriter();
        context.checking(new Expectations()
            {
                {
                    prepareParseRequestURLNoSession(this);

                    one(request).getRequestURI();
                    will(returnValue(REQUEST_URI_PREFIX + EXAMPLE_DATA_SET_CODE));

                    allowing(request).getSession(false);
                    will(returnValue(null));

                    one(response).getWriter();
                    will(returnValue(new PrintWriter(writer)));

                    allowing(response).setContentType(with(any(String.class)));
                }
            });

        DatasetDownloadServlet servlet = createServlet();
        servlet.doGet(request, response);
        assertEquals(EXPIRATION_MESSAGE, writer.toString());
        assertEquals(
                LOG_ERROR
                        + "Could not create a servlet session since no existing servlet session is available, "
                        + "and the openBIS session ID was not provided as a parameter: [mode=html] "
                        + "Session Timeout: 120 sec",
                getNormalizedLogContent());

        context.assertIsSatisfied();
    }

    @Test()
    public void testDoGetRequestURINotStartingWithApplicationName() throws Exception
    {
        final StringWriter writer = new StringWriter();
        context.checking(new Expectations()
            {
                {
                    one(request).getRequestURI();
                    will(returnValue("blabla"));

                    one(response).setContentType("text");
                    one(response).getWriter();
                    will(returnValue(new PrintWriter(writer)));
                }
            });

        DatasetDownloadServlet servlet = createServlet();
        servlet.doGet(request, response);
        assertEquals("Error:" + OSUtilities.LINE_SEPARATOR
                + "Request URI 'blabla' expected to start with '/datastore_server/'."
                + OSUtilities.LINE_SEPARATOR, writer.toString());
        String logContent = getNormalizedLogContent();
        final String expected =
                LOG_ERROR + "Request URI 'blabla' expected to start with '/datastore_server/'.";
        AssertionUtil.assertContains(expected, logContent);

        context.assertIsSatisfied();
    }

    private void prepareForGettingDataSetFromSession(final AbstractExternalData externalData,
            final String path)
    {
        context.checking(new Expectations()
            {
                {
                    Map<String, Boolean> accessMap = new HashMap<String, Boolean>();
                    checkAndSetAttribute(this,
                            AbstractDatasetDownloadServlet.DATA_SET_ACCESS_SESSION_KEY, accessMap);

                    one(httpSession).getAttribute(
                            AbstractDatasetDownloadServlet.DATA_SET_SESSION_KEY);
                    Map<String, AbstractExternalData> map =
                            new HashMap<String, AbstractExternalData>();
                    map.put(externalData.getCode(), externalData);
                    will(Expectations.returnValue(map));

                    prepareGetRequestURI(this, externalData, path);

                    oneOf(response).setHeader("Pragma", "no-cache");
                    oneOf(response).setHeader("Cache-control",
                            "no-cache, no-store, must-revalidate");
                    allowing(response).setDateHeader(with(Expectations.any(String.class)),
                            with(Expectations.any(long.class)));
                    // oneOf(response).addDateHeader(with("Date"),
                    // with(Expectations.any(long.class)));
                    // oneOf(response).addDateHeader(with("Expires"),
                    // with(Expectations.any(long.class))); }
                }
            });
    }

    private void prepareGetRequestURI(Expectations exp, final AbstractExternalData externalData,
            final String path)
    {
        exp.one(request).getRequestURI();
        String codeAndPath = REQUEST_URI_PREFIX + externalData.getCode() + "/" + path;
        exp.will(Expectations.returnValue(codeAndPath));
    }

    private void prepareParseRequestURLNoSession(Expectations exp)
    {
        exp.one(request).getParameter(Utils.SESSION_ID_PARAM);
        exp.will(Expectations.returnValue(null));

        exp.allowing(request).getParameter(AbstractDatasetDownloadServlet.DISPLAY_MODE_PARAM);
        exp.will(Expectations.returnValue("html"));

        exp.one(request).getParameter(DatasetDownloadServlet.AUTO_RESOLVE_KEY);
        exp.will(Expectations.returnValue(null));
        exp.one(request).getParameter(DatasetDownloadServlet.FORCE_AUTO_RESOLVE_KEY);
        exp.will(Expectations.returnValue(null));
        exp.one(request).getParameter(DatasetDownloadServlet.MAIN_DATA_SET_PATH_KEY);
        exp.will(Expectations.returnValue(null));
        exp.one(request).getParameter(DatasetDownloadServlet.MAIN_DATA_SET_PATTERN_KEY);
        exp.will(Expectations.returnValue(null));
        exp.one(request).getParameter(DatasetDownloadServlet.DISABLE_LINKS);
        exp.will(Expectations.returnValue(null));

        exp.one(request).getScheme();
        exp.one(request).getHeader("referer");

        exp.allowing(request).getParameter("is_link_data");
        exp.will(Expectations.returnValue("false"));

        // For the logging of problem requests
        Vector<String> parameterNames = new Vector<String>();
        parameterNames.add(AbstractDatasetDownloadServlet.DISPLAY_MODE_PARAM);
        exp.allowing(request).getParameterNames();
        exp.will(Expectations.returnValue(parameterNames.elements()));
    }

    private void prepareParseRequestURLForThumbnail(final int width, final int height)
    {
        context.checking(new Expectations()
            {
                {
                    one(request).getParameter(Utils.SESSION_ID_PARAM);
                    will(returnValue(EXAMPLE_SESSION_ID));

                    one(request).getParameter(AbstractDatasetDownloadServlet.DISPLAY_MODE_PARAM);
                    will(returnValue("thumbnail" + width + "x" + height));

                    one(request).getParameter(DatasetDownloadServlet.AUTO_RESOLVE_KEY);
                    will(returnValue(null));

                    one(request).getParameter(DatasetDownloadServlet.FORCE_AUTO_RESOLVE_KEY);
                    will(returnValue(null));

                    one(request).getParameter(DatasetDownloadServlet.MAIN_DATA_SET_PATH_KEY);
                    will(returnValue(null));

                    one(request).getParameter(DatasetDownloadServlet.MAIN_DATA_SET_PATTERN_KEY);
                    will(returnValue(null));

                    one(request).getParameter(DatasetDownloadServlet.DISABLE_LINKS);
                    will(returnValue(null));

                    one(request).getScheme();
                    one(request).getHeader("referer");
                }
            });
    }

    private void prepareForObtainingDataSetFromServer(final AbstractExternalData externalData, final boolean accessed)
    {
        prepareCreateSession();
        prepareTryGetDatasetLocation(externalData);
        if (externalData != null && accessed)
        {
            prepareDatasetAccessed();
        }
    }

    private void prepareCreateSession()
    {
        context.checking(new Expectations()
            {
                {
                    allowing(request).getSession(false);
                    will(returnValue(null));

                    one(request).getSession(true);
                    will(returnValue(httpSession));

                    one(httpSession).setMaxInactiveInterval(120);

                    // For the logging of session-creating requests
                    Vector<String> parameterNames = new Vector<String>();
                    parameterNames.add(Utils.SESSION_ID_PARAM);
                    one(request).getParameterNames();
                    will(Expectations.returnValue(parameterNames.elements()));
                    one(request).getParameter(Utils.SESSION_ID_PARAM);
                    will(returnValue(EXAMPLE_SESSION_ID));
                }
            });
    }

    private void prepareTryGetDatasetLocation(final AbstractExternalData externalData)
    {
        context.checking(new Expectations()
            {
                {
                    one(openbisService).tryGetDataSetLocation(EXAMPLE_DATA_SET_CODE);
                    if (externalData == null)
                    {
                        will(returnValue(null));
                    } else
                    {
                        will(returnValue(new ExternalDataLocationNode(externalData)));
                    }
                }
            });
    }

    private void prepareLocking()
    {
        context.checking(new Expectations()
            {
                {
                    one(shareIdManager).lock(EXAMPLE_DATA_SET_CODE);
                    one(shareIdManager).releaseLock(EXAMPLE_DATA_SET_CODE);
                }
            });
    }

    private void prepareDatasetAccessed()
    {
        context.checking(new Expectations()
            {
                {
                    one(openbisService).notifyDatasetAccess(EXAMPLE_DATA_SET_CODE);
                }
            });
    }

    private void prepareCheckDatasetAccess()
    {
        context.checking(new Expectations()
            {
                {
                    getSessionAttribute(this, AbstractDatasetDownloadServlet.DATA_SET_SESSION_KEY,
                            new HashMap<String, ExternalDataPE>());

                    Map<String, Boolean> map = new HashMap<String, Boolean>();
                    checkAndSetAttribute(this,
                            AbstractDatasetDownloadServlet.DATA_SET_ACCESS_SESSION_KEY, map);

                    one(openbisService).checkDataSetAccess(EXAMPLE_SESSION_ID,
                            EXAMPLE_DATA_SET_CODE);

                    getSessionAttribute(this,
                            AbstractDatasetDownloadServlet.DATA_SET_ACCESS_SESSION_KEY, map);
                }
            });
    }

    private void checkAndSetAttribute(Expectations exp, String attributeKey, Object newValue)
    {
        getSessionAttribute(exp, attributeKey, null);
        exp.one(httpSession).setAttribute(attributeKey, newValue);
    }

    private void getSessionAttribute(Expectations exp, String attributeKey, Object value)
    {
        exp.one(httpSession).getAttribute(attributeKey);
        exp.will(Expectations.returnValue(value));
    }

    private void prepareForCreatingHTML(final StringWriter writer) throws IOException
    {
        context.checking(new Expectations()
            {
                {
                    one(response).getWriter();
                    will(returnValue(new PrintWriter(writer)));

                    one(response).setContentType("text/html");
                }
            });
    }

    private PhysicalDataSet createDataSet()
    {
        Space space = new Space();
        space.setCode(SPACE_CODE);
        Project project = new Project();
        project.setCode(PROJECT_CODE);
        project.setSpace(space);
        Experiment experiment = new Experiment();
        experiment.setCode(EXPERIMENT_CODE);
        experiment.setProject(project);
        final PhysicalDataSet dataSet = new PhysicalDataSet();
        dataSet.setExperiment(experiment);
        dataSet.setCode(EXAMPLE_DATA_SET_CODE);
        dataSet.setFileFormatType(new FileFormatType("DATA"));
        LocatorType locatorType = new LocatorType();
        locatorType.setCode(LocatorType.DEFAULT_LOCATOR_TYPE_CODE);
        dataSet.setLocatorType(locatorType);
        dataSet.setShareId(DEFAULT_SHARE_ID);
        dataSet.setLocation(DatasetLocationUtil.getDatasetLocationPath(EXAMPLE_DATA_SET_CODE,
                DATABASE_INSTANCE_UUID));
        DataStore dataStore = new DataStore();
        dataStore.setCode("STANDARD");
        dataSet.setDataStore(dataStore);
        return dataSet;
    }

    private static File getDatasetDirectoryLocation(final File baseDir, String dataSetCode)
    {
        return DatasetLocationUtil.getDatasetLocationPath(baseDir, dataSetCode, DEFAULT_SHARE_ID,
                DATABASE_INSTANCE_UUID);
    }

    private DatasetDownloadServlet createServlet()
    {
        Properties properties = new Properties();
        properties.setProperty(ConfigParameters.STOREROOT_DIR_KEY, TEST_FOLDER.toString());
        properties.setProperty(ConfigParameters.PORT_KEY, "8080");
        properties.setProperty(ConfigParameters.SERVER_URL_KEY, "http://localhost");
        properties.setProperty(ConfigParameters.SESSION_TIMEOUT_KEY, "2");
        properties.setProperty(ConfigParameters.KEYSTORE_PATH_KEY, "/");
        properties.setProperty(ConfigParameters.KEYSTORE_PASSWORD_KEY, "x");
        properties.setProperty(ConfigParameters.KEYSTORE_KEY_PASSWORD_KEY, "y");
        properties.setProperty(ConfigParameters.DOWNLOAD_URL, "http://localhost:8080");
        ConfigParameters configParameters = new ConfigParameters(properties);
        OpenbisSessionTokenCache sessionTokenCache = new OpenbisSessionTokenCache(service);
        return new DatasetDownloadServlet(new ApplicationContext(openbisService, sessionTokenCache,
                shareIdManager, hierarchicalContentProvider, configParameters, personalAccessTokenConverter));
    }

    private String getNormalizedLogContent()
    {
        String logContent = logRecorder.getLogContent();
        logContent = logContent.replace(TEST_FOLDER.getAbsolutePath(), "<wd>");
        logContent = logContent.replace('\\', '/');
        return logContent;
    }

}