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
package imaging.dataset.interceptor;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.CreateDataSetsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.DataSetCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update.DataSetUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update.UpdateDataSetsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;

import ch.ethz.sis.openbis.generic.imagingapi.v3.dto.ImagingDataSetConfig;
import ch.ethz.sis.openbis.generic.imagingapi.v3.dto.ImagingDataSetImage;
import ch.ethz.sis.openbis.generic.imagingapi.v3.dto.ImagingDataSetPreview;
import ch.ethz.sis.openbis.generic.imagingapi.v3.dto.ImagingDataSetPropertyConfig;
import ch.ethz.sis.openbis.generic.server.sharedapi.v3.json.GenericObjectMapper;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public final class ImagingDataSetInterceptorTest
{

    private static final String TEST_SESSION_TOKEN = "test-session-token";
    private static final String TEST_PERM_ID = "99999999-99";

    private ImagingDataSetInterceptor interceptor;

    private ImagingDataSetPropertyConfig properConfig;

    private Mockery context;

    private IApplicationServerApi asApi;

    private DataSet dataSet;



    @BeforeMethod
    public void setUp() {
        context = new Mockery();
        asApi = context.mock(IApplicationServerApi.class);

        interceptor = new ImagingDataSetInterceptor();

        properConfig = new ImagingDataSetPropertyConfig();
        ImagingDataSetConfig config = new ImagingDataSetConfig();
        config.setAdaptor("some.proper.adaptor.class.AdaptorName");
        properConfig.setConfig(config);
        ImagingDataSetImage image1 = new ImagingDataSetImage();
        ImagingDataSetPreview preview11 = new ImagingDataSetPreview();
        image1.setPreviews(List.of(preview11));
        ImagingDataSetImage image2 = new ImagingDataSetImage();
        ImagingDataSetPreview preview21 = new ImagingDataSetPreview();
        ImagingDataSetPreview preview22 = new ImagingDataSetPreview();
        image2.setPreviews(List.of(preview21, preview22));
        properConfig.setImages(List.of(image1, image2));

        dataSet = new DataSet();
        DataSetType type = new DataSetType();
        type.setPermId(new EntityTypePermId(ImagingDataSetInterceptor.IMAGING_TYPE, EntityKind.DATA_SET));
        dataSet.setType(type);
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withType();
        dataSet.setFetchOptions(fetchOptions);
        context.checking(new Expectations()
        {
            {
                allowing(asApi).getDataSets(with(TEST_SESSION_TOKEN), with(any(List.class)), with(any(
                        DataSetFetchOptions.class)));
                will(returnValue(Map.of(new DataSetPermId(TEST_PERM_ID), dataSet)));
            }
        });
    }

    private static String getJson(ImagingDataSetPropertyConfig config) {
        try {
            ObjectMapper mapper = new GenericObjectMapper();
            return mapper.writeValueAsString(config);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testCreate_missingProperty() {

        DataSetCreation creation = new DataSetCreation();
        creation.setTypeId(new EntityTypePermId(ImagingDataSetInterceptor.IMAGING_TYPE, EntityKind.DATA_SET));

        CreateDataSetsOperation operation = new CreateDataSetsOperation(creation);
        try
        {
            interceptor.beforeOperation(asApi, TEST_SESSION_TOKEN, operation);
            fail("It should fail!");
        } catch (UserFailureException e) {
            assertTrue(e.getMessage().contains("Imaging property config must not be empty!"));
        }

    }

    @Test
    public void testCreate_emptyProperty() {

        DataSetCreation creation = new DataSetCreation();
        creation.setTypeId(new EntityTypePermId(ImagingDataSetInterceptor.IMAGING_TYPE, EntityKind.DATA_SET));
        creation.setJsonProperty(ImagingDataSetInterceptor.IMAGING_CONFIG_PROPERTY_NAME, "{}");

        CreateDataSetsOperation operation = new CreateDataSetsOperation(creation);

        try
        {
            interceptor.beforeOperation(asApi, TEST_SESSION_TOKEN, operation);
            fail("It should fail!");
        } catch (UserFailureException e) {
           assertTrue(e.getMessage().contains("missing type id property '@type'"));
        }
    }

    @Test
    public void testCreate_basicFlow() {

        DataSetCreation creation = new DataSetCreation();
        creation.setTypeId(new EntityTypePermId(ImagingDataSetInterceptor.IMAGING_TYPE, EntityKind.DATA_SET));

        creation.setJsonProperty(ImagingDataSetInterceptor.IMAGING_CONFIG_PROPERTY_NAME, getJson(properConfig));
        CreateDataSetsOperation operation = new CreateDataSetsOperation(creation);

        interceptor.beforeOperation(asApi, TEST_SESSION_TOKEN, operation);
        assertTrue(creation.getMetaData().containsKey(ImagingDataSetInterceptor.PREVIEW_TOTAL_COUNT));
        assertEquals("3", creation.getMetaData().get(ImagingDataSetInterceptor.PREVIEW_TOTAL_COUNT));
    }

    @Test
    public void testCreate_missingImages() {

        DataSetCreation creation = new DataSetCreation();
        creation.setTypeId(new EntityTypePermId(ImagingDataSetInterceptor.IMAGING_TYPE, EntityKind.DATA_SET));

        properConfig.setImages(List.of());
        creation.setJsonProperty(ImagingDataSetInterceptor.IMAGING_CONFIG_PROPERTY_NAME, getJson(properConfig));
        CreateDataSetsOperation operation = new CreateDataSetsOperation(creation);

        try
        {
            interceptor.beforeOperation(asApi, TEST_SESSION_TOKEN, operation);
            fail("It should fail!");
        } catch (UserFailureException e) {
            assertTrue(e.getMessage().contains("At least one image must be included!"));
        }
    }

    @Test
    public void testCreate_missingPreviews() {

        DataSetCreation creation = new DataSetCreation();
        creation.setTypeId(new EntityTypePermId(ImagingDataSetInterceptor.IMAGING_TYPE, EntityKind.DATA_SET));

        properConfig.getImages().get(0).setPreviews(List.of());
        creation.setJsonProperty(ImagingDataSetInterceptor.IMAGING_CONFIG_PROPERTY_NAME, getJson(properConfig));
        CreateDataSetsOperation operation = new CreateDataSetsOperation(creation);

        try
        {
            interceptor.beforeOperation(asApi, TEST_SESSION_TOKEN, operation);
            fail("It should fail!");
        } catch (UserFailureException e) {
            assertTrue(e.getMessage().contains("At least one preview must be included!"));
        }
    }

    @Test
    public void testUpdate_missingProperty() {
        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(new DataSetPermId(TEST_PERM_ID));

        UpdateDataSetsOperation operation = new UpdateDataSetsOperation(update);
        try
        {
            interceptor.beforeOperation(asApi, TEST_SESSION_TOKEN, operation);
            fail("It should fail!");
        } catch (UserFailureException e) {
            assertTrue(e.getMessage().contains("Imaging property config must not be empty!"));
        }
    }

    @Test
    public void testUpdate_emptyProperty() {
        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(new DataSetPermId(TEST_PERM_ID));
        update.setJsonProperty(ImagingDataSetInterceptor.IMAGING_CONFIG_PROPERTY_NAME, "{}");

        UpdateDataSetsOperation operation = new UpdateDataSetsOperation(update);
        try
        {
            interceptor.beforeOperation(asApi, TEST_SESSION_TOKEN, operation);
            fail("It should fail!");
        } catch (UserFailureException e) {
            assertTrue(e.getMessage().contains("missing type id property '@type'"));
        }
    }

    @Test
    public void testUpdate_basicFlow() {
        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(new DataSetPermId(TEST_PERM_ID));
        update.setJsonProperty(ImagingDataSetInterceptor.IMAGING_CONFIG_PROPERTY_NAME, getJson(properConfig));

        UpdateDataSetsOperation operation = new UpdateDataSetsOperation(update);
        interceptor.beforeOperation(asApi, TEST_SESSION_TOKEN, operation);
        Map<String,String> map =
                (Map<String, String>) update.getMetaData().getAdded().stream().toArray()[0];

        assertTrue(map.containsKey(ImagingDataSetInterceptor.PREVIEW_TOTAL_COUNT));
        assertEquals("3", map.get(ImagingDataSetInterceptor.PREVIEW_TOTAL_COUNT));
    }

    @Test
    public void testUpdate_missingImages() {
        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(new DataSetPermId(TEST_PERM_ID));
        properConfig.setImages(List.of());
        update.setJsonProperty(ImagingDataSetInterceptor.IMAGING_CONFIG_PROPERTY_NAME, getJson(properConfig));

        UpdateDataSetsOperation operation = new UpdateDataSetsOperation(update);
        try
        {
            interceptor.beforeOperation(asApi, TEST_SESSION_TOKEN, operation);
            fail("It should fail!");
        } catch (UserFailureException e) {
            assertTrue(e.getMessage().contains("At least one image must be included!"));
        }
    }


    @Test
    public void testUpdate_missingPreview() {
        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(new DataSetPermId(TEST_PERM_ID));
        properConfig.getImages().get(0).setPreviews(List.of());
        update.setJsonProperty(ImagingDataSetInterceptor.IMAGING_CONFIG_PROPERTY_NAME, getJson(properConfig));

        UpdateDataSetsOperation operation = new UpdateDataSetsOperation(update);
        try
        {
            interceptor.beforeOperation(asApi, TEST_SESSION_TOKEN, operation);
            fail("It should fail!");
        } catch (UserFailureException e) {
            assertTrue(e.getMessage().contains("At least one preview must be included!"));
        }
    }

}
