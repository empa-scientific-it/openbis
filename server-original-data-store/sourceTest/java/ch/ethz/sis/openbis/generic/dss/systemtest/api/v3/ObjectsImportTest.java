/*
 * Copyright 2018 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.dss.systemtest.api.v3;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.apache.commons.io.FileUtils;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentProvider;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.MultiPartContentProvider;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.IObjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IModificationDateHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.IExperimentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.fetchoptions.MaterialFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.id.IMaterialId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.CustomASServiceExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.id.CustomASServiceCode;
import ch.ethz.sis.openbis.systemtest.asapi.v3.util.EmailUtil;
import ch.ethz.sis.openbis.systemtest.asapi.v3.util.EmailUtil.Email;
import ch.systemsx.cisd.common.http.JettyHttpClientFactory;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.util.TestInstanceHostUtils;

/**
 * @author pkupczyk
 */
public class ObjectsImportTest extends AbstractFileTest
{

    protected static final String SERVICE_URL = TestInstanceHostUtils.getOpenBISUrl() + "/openbis/upload";

    protected static final String TEST_UPLOAD_KEY = "test-import";

    protected static final String TEST_EMAIL = "test@email.com";

    protected static final long DEFAULT_TIMEOUT = 30000;

    protected static final String FALSE_TRUE_PROVIDER = "sync-async";

    protected static final String PARAM_UPLOAD_KEY = "uploadKey";

    protected static final String PARAM_TYPE_CODE = "typeCode";

    protected static final String PARAM_ASYNC = "async";

    protected static final String PARAM_USER_EMAIL = "userEmail";

    protected static final String PARAM_DEFAULT_SPACE_IDENTIFIER = "defaultSpaceIdentifier";

    protected static final String PARAM_SPACE_IDENTIFIER_OVERRIDE = "spaceIdentifierOverride";

    protected static final String PARAM_EXPERIMENT_IDENTIFIER_OVERRIDE = "experimentIdentifierOverride";

    protected static final String PARAM_UPDATE_EXISTING = "updateExisting";

    protected static final String PARAM_IGNORE_UNREGISTERED = "ignoreUnregistered";

    protected static final String PARAM_CUSTOM_IMPORT_CODE = "customImportCode";

    protected IApplicationServerApi as;

    @BeforeClass
    protected void beforeClass() throws Exception
    {
        super.beforeClass();
        as = ServiceProvider.getV3ApplicationService();
    }

    protected ContentResponse uploadFiles(String sessionToken, String uploadSessionKey, MultiPartContentProvider multiPart)
            throws InterruptedException, TimeoutException, ExecutionException
    {
        HttpClient client = JettyHttpClientFactory.getHttpClient();
        Request request = client.newRequest(SERVICE_URL).method(HttpMethod.POST);
        request.param("sessionID", sessionToken);
        request.param("sessionKeysNumber", "1");
        request.param("sessionKey_0", uploadSessionKey);
        request.content(multiPart);

        return request.send();
    }

    protected ContentResponse uploadFiles(String sessionToken, String uploadSessionKey, String... filesContent)
            throws InterruptedException, TimeoutException, ExecutionException
    {
        MultiPartContentProvider multiPart = new MultiPartContentProvider();

        for (int i = 0; i < filesContent.length; i++)
        {
            ContentProvider contentProvider = new StringContentProvider(filesContent[i]);

            String fieldName = uploadSessionKey + "_" + i;
            String fileName = "fileName_" + i;
            multiPart.addFilePart(fieldName, fileName, contentProvider, null);
        }

        multiPart.close();

        return uploadFiles(sessionToken, uploadSessionKey, multiPart);
    }

    protected void assertUploadedFiles(String sessionToken, String... fileContents) throws Exception
    {
        File sessionWorkspaceRootDir = new File("targets/sessionWorkspace");
        File sessionWorkspace = new File(sessionWorkspaceRootDir, sessionToken);
        File[] files = sessionWorkspace.listFiles();

        List<String> expectedContents = new ArrayList<String>(Arrays.asList(fileContents));
        List<String> actualContents = new ArrayList<String>();

        if (files != null)
        {
            for (File file : files)
            {
                actualContents.add(FileUtils.readFileToString(file));
            }
        }

        expectedContents.sort(String.CASE_INSENSITIVE_ORDER);
        actualContents.sort(String.CASE_INSENSITIVE_ORDER);

        assertEquals(expectedContents, actualContents);
    }

    protected String executeImport(String sessionToken, String operation, Map<String, Object> parameters)
    {
        CustomASServiceCode serviceId = new CustomASServiceCode("import-test");
        CustomASServiceExecutionOptions options = new CustomASServiceExecutionOptions();
        options.withParameter("operation", operation);
        for (String name : parameters.keySet())
        {
            options.withParameter(name, parameters.get(name));
        }
        return (String) as.executeCustomASService(sessionToken, serviceId, options);
    }

    protected <T extends IModificationDateHolder> T getObject(String sessionToken, IObjectId objectId)
    {
        return getObject(sessionToken, objectId, 0, 0);
    }

    protected <T extends IModificationDateHolder> T getObject(String sessionToken, IObjectId objectId, long modifiedAfterTimestamp,
            long timeoutAfterMillis)
    {
        long startMillis = System.currentTimeMillis();

        while (true)
        {
            Map<IObjectId, T> objects = getObjects(sessionToken, objectId);
            T object = objects.get(objectId);

            if (object != null && object.getModificationDate() != null && object.getModificationDate().getTime() >= modifiedAfterTimestamp)
            {
                return object;
            }

            if (timeoutAfterMillis > 0 && System.currentTimeMillis() < startMillis + timeoutAfterMillis)
            {
                try
                {
                    Thread.sleep(100);
                } catch (InterruptedException e)
                {
                    throw new RuntimeException(e);
                }
            } else
            {
                return null;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <K, V> Map<K, V> getObjects(String sessionToken, IObjectId objectId)
    {
        if (objectId instanceof IExperimentId)
        {
            ExperimentFetchOptions fo = new ExperimentFetchOptions();
            fo.withProperties();
            return (Map<K, V>) as.getExperiments(sessionToken, Arrays.asList((IExperimentId) objectId), (ExperimentFetchOptions) fo);
        } else if (objectId instanceof ISampleId)
        {
            SampleFetchOptions fo = new SampleFetchOptions();
            fo.withProperties();
            return (Map<K, V>) as.getSamples(sessionToken, Arrays.asList((ISampleId) objectId), (SampleFetchOptions) fo);
        } else if (objectId instanceof IDataSetId)
        {
            DataSetFetchOptions fo = new DataSetFetchOptions();
            fo.withProperties();
            return (Map<K, V>) as.getDataSets(sessionToken, Arrays.asList((IDataSetId) objectId), (DataSetFetchOptions) fo);
        } else if (objectId instanceof IMaterialId)
        {
            MaterialFetchOptions fo = new MaterialFetchOptions();
            fo.withProperties();
            return (Map<K, V>) as.getMaterials(sessionToken, Arrays.asList((IMaterialId) objectId), (MaterialFetchOptions) fo);
        } else
        {
            throw new IllegalArgumentException("Unsupported object id " + objectId);
        }
    }

    protected void assertNoEmails(long timestamp)
    {
        Email latestEmail = EmailUtil.findLatestEmail();
        assertTrue("Timestamp: " + timestamp + ", Latest email: " + latestEmail, latestEmail == null || latestEmail.timestamp < timestamp);
    }

    protected void assertEmail(long timestamp, String expectedEmail, String expectedSubject)
    {
        Email latestEmail = EmailUtil.findLatestEmail();
        assertTrue("Timestamp: " + timestamp + ", Latest email: " + latestEmail, latestEmail != null && latestEmail.timestamp >= timestamp);
        assertEquals(expectedEmail, latestEmail.to);
        assertTrue(latestEmail.subject, latestEmail.subject.contains(expectedSubject));
    }

    protected long getTimestampAndWaitASecond()
    {
        long timestamp = System.currentTimeMillis();
        try
        {
            Thread.sleep(1000);
        } catch (InterruptedException e)
        {
            // silently ignored
        }
        return timestamp;
    }

    public static class ImportFile
    {

        private List<String> columns;

        private List<List<String>> lines = new ArrayList<List<String>>();

        public ImportFile(String... columns)
        {
            this.columns = Arrays.asList(columns);
        }

        public void addLine(String... values)
        {
            lines.add(Arrays.asList(values));
        }

        @Override
        public String toString()
        {
            StringBuilder content = new StringBuilder();
            content.append(String.join("\t", columns) + "\n");

            for (List<String> line : lines)
            {
                content.append(String.join("\t", line) + "\n");
            }

            return content.toString();
        }
    }

    @DataProvider(name = FALSE_TRUE_PROVIDER)
    public static Object[][] provideFalseTrue()
    {
        return new Object[][] { { false }, { true } };
    }

}
