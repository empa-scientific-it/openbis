/*
 *  Copyright ETH 2023 Zürich, Scientific IT Services
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package ch.ethz.sis.openbis.generic.server.dss.plugins.imaging;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.imagingapi.v3.dto.ImagingDataSetConfig;
import ch.ethz.sis.openbis.generic.imagingapi.v3.dto.ImagingDataSetImage;
import ch.ethz.sis.openbis.generic.imagingapi.v3.dto.ImagingDataSetPreview;
import ch.ethz.sis.openbis.generic.imagingapi.v3.dto.ImagingDataSetPropertyConfig;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.service.CustomDSSServiceExecutionOptions;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.service.id.CustomDssServiceCode;
import ch.ethz.sis.openbis.generic.imagingapi.v3.dto.ImagingPreviewContainer;
import ch.ethz.sis.openbis.generic.server.sharedapi.v3.json.GenericObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.springframework.beans.factory.BeanFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.Serializable;
import java.util.*;

import static org.junit.Assert.*;

public class ImagingServiceTest
{

    private static final String TEST_SESSION_TOKEN = "test-session-token";
    private static final String TEST_PERM_ID = "99999999-99";

    protected Mockery context;

    private IApplicationServerApi asApi;

    private static final ObjectMapper MAPPER = new GenericObjectMapper();

    @BeforeMethod
    public void setUp() {
        context = new Mockery();
        final BeanFactory beanFactory = context.mock(BeanFactory.class);
        ServiceProviderTestWrapper.setApplicationContext(beanFactory);

        asApi = context.mock(IApplicationServerApi.class);

        context.checking(new Expectations()
        {
            {
                allowing(beanFactory).getBean("objectMapper-v3");
                will(returnValue(MAPPER));

                allowing(beanFactory).getBean("v3-application-service");
                will(returnValue(asApi));
            }
        });

    }

    @Test
    public void testMissingAdaptor() throws Exception
    {
        Properties properties = new Properties();
        ImagingService imagingService = new ImagingService(properties);

        CustomDssServiceCode code = new CustomDssServiceCode("imaging");

        CustomDSSServiceExecutionOptions options = new CustomDSSServiceExecutionOptions();
        options.getParameters().put("type", "preview");
        options.getParameters().put("permId", TEST_PERM_ID);
        ImagingDataSetPreview preview = new ImagingDataSetPreview();
        preview.setIndex(0);
        preview.setFormat("png");
        preview.setConfig(Map.of("param", "value"));

        options.getParameters().put("preview", preview);

        ImagingDataSetPropertyConfig propertyConfig = new ImagingDataSetPropertyConfig();
        ImagingDataSetConfig config = new ImagingDataSetConfig();
        ImagingDataSetImage image = new ImagingDataSetImage();
        image.setPreviews(Arrays.asList(new ImagingDataSetPreview()));
        propertyConfig.setConfig(config);
        propertyConfig.setImages(Arrays.asList(image));

        DataSet dataSet = new DataSet();

        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withProperties();
        dataSet.setFetchOptions(fetchOptions);

        dataSet.setPermId(new DataSetPermId(TEST_PERM_ID));
        dataSet.setProperties(Map.of(
                ImagingService.IMAGING_CONFIG_PROPERTY_NAME, MAPPER.writeValueAsString(propertyConfig)));

        context.checking(new Expectations()
         {
             {
                 allowing(asApi).getDataSets(with(TEST_SESSION_TOKEN), with(any(List.class)), with(any(
                         DataSetFetchOptions.class)));
                 will(returnValue(Map.of(new DataSetPermId(TEST_PERM_ID), dataSet)));
             }
         });


        Serializable result = imagingService.executeService(TEST_SESSION_TOKEN, code, options);
        ImagingPreviewContainer outputPreview = (ImagingPreviewContainer) result;

        assertNotNull(outputPreview.getError());
        assertEquals("ch.systemsx.cisd.common.exceptions.UserFailureException: Adaptor name is missing from the config!",
                outputPreview.getError());

    }












}
