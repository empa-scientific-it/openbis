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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId;
import ch.ethz.sis.openbis.generic.dssapi.v3.IDataStoreServerApi;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.imaging.ImagingDataSetConfig;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.imaging.ImagingDataSetImage;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.service.CustomDSSServiceExecutionOptions;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.service.id.ICustomDSSServiceId;
import ch.ethz.sis.openbis.generic.server.dss.plugins.imaging.adaptor.IImagingDataSetAdaptor;
import ch.ethz.sis.openbis.generic.server.dss.plugins.imaging.container.ImagingDataContainer;
import ch.ethz.sis.openbis.generic.server.dss.plugins.imaging.container.ImagingExportContainer;
import ch.ethz.sis.openbis.generic.server.dss.plugins.imaging.container.ImagingMultiExportContainer;
import ch.ethz.sis.openbis.generic.server.dss.plugins.imaging.container.ImagingPreviewContainer;
import ch.ethz.sis.openbis.generic.server.dssapi.v3.executor.service.ICustomDSSServiceExecutor;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.common.reflection.ClassUtils;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.dto.OpenBISSessionHolder;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.Serializable;
import java.util.*;

public class ImagingService implements ICustomDSSServiceExecutor
{
    private final Properties properties;

    private IHierarchicalContentProvider contentProvider;

    private static final String SCRIPT_PATH = "script-path";

    private String scriptPath;

    public ImagingService(Properties properties)
    {
        this.properties = properties;
        scriptPath = PropertyUtils.getProperty(properties, SCRIPT_PATH);
    }

    @Override
    public Serializable executeService(String sessionToken, ICustomDSSServiceId serviceId,
            CustomDSSServiceExecutionOptions options)
    {
        ImagingDataContainer data = getDataFromParams(options.getParameters());
        try
        {
            if (data.getType().equalsIgnoreCase("preview"))
            {
                return processPreviewFlow(sessionToken, (ImagingPreviewContainer) data);
            } else
            {
                return processExportFlow(sessionToken, data);
            }
        } catch (Exception e)
        {
            data.setError(e.toString());
        }
        return data;
    }

    private ImagingPreviewContainer processPreviewFlow(String sessionToken,
            ImagingPreviewContainer data)
    {
        DataSet dataSet = getDataSet(sessionToken, data);

        Config config =
                readConfig(dataSet.getJsonProperty("$IMAGING_DATA_CONFIG"), Config.class);
        final String adaptorName = config.config.getAdaptor();

        IImagingDataSetAdaptor
                adaptor =
                ClassUtils.create(IImagingDataSetAdaptor.class, adaptorName, properties);

        IHierarchicalContent content =
                getHierarchicalContentProvider(sessionToken).asContent(
                        dataSet.getPermId().getPermId());
        IHierarchicalContentNode root = content.getRootNode();

        File rootFile = root.getFile();
        Map<String, Serializable> previewParams = data.getPreview().getConfig();
        Map<String, String> meta = data.getPreview().getMetaData();

        ImagingServiceContext context = new ImagingServiceContext(sessionToken, getApplicationServerApi(), getDataStoreServerApi());

        data.getPreview().setBytes(
                adaptor.process(context,
                        rootFile, previewParams, meta).toString());
        return data;
    }

    private Serializable processExportFlow(String sessionToken, ImagingDataContainer data)
    {
        //TODO
        if (data.getType().equalsIgnoreCase("export"))
        {
            // Single export case
            ImagingExportContainer input = (ImagingExportContainer) data;
        } else
        {
            // multi export case
            ImagingMultiExportContainer input = (ImagingMultiExportContainer) data;
        }
        return data;
    }

    private IHierarchicalContentProvider getHierarchicalContentProvider(String sessionToken)
    {
        if (contentProvider == null)
        {
            contentProvider = ServiceProvider.getHierarchicalContentProvider();
        }
        OpenBISSessionHolder sessionTokenHolder = new OpenBISSessionHolder();
        sessionTokenHolder.setSessionToken(sessionToken);
        return contentProvider.cloneFor(sessionTokenHolder);
    }

    private IApplicationServerApi getApplicationServerApi()
    {
        return ServiceProvider.getV3ApplicationService();
    }

    private IDataStoreServerApi getDataStoreServerApi()
    {
        return ServiceProvider.getV3DataStoreService();
    }

    private ObjectMapper getObjectMapper()
    {
        return ServiceProvider.getObjectMapperV3();
    }

    private <T> T readConfig(String val, Class<T> clazz)
    {
        try
        {
            ObjectMapper objectMapper = getObjectMapper();
            return objectMapper.readValue(new ByteArrayInputStream(val.getBytes()),
                    clazz);
        } catch (Exception e)
        {
            throw new UserFailureException("Could not read the parameters!", e);
        }
    }

    private String mapToJson(Map<String, Object> map)
    {
        try
        {
            ObjectMapper objectMapper = getObjectMapper();
            return objectMapper.writeValueAsString(map);
        } catch (Exception e)
        {
            throw new UserFailureException("Could not serialize the input parameters!", e);
        }
    }

    private DataSet getDataSet(String sessionToken, ImagingDataContainer data)
    {
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withType();
        fetchOptions.withDataStore();
        fetchOptions.withPhysicalData();
        Map<IDataSetId, DataSet> result = getApplicationServerApi()
                .getDataSets(sessionToken, List.of(new DataSetPermId(data.getPermId())),
                        fetchOptions);
        if (result.isEmpty())
        {
            throw new UserFailureException("Could not find Dataset:" + data.getPermId());
        }
        return result.get(new DataSetPermId(data.getPermId()));
    }

    public static final class Config
    {
        @JsonProperty
        ImagingDataSetConfig config;

        @JsonProperty
        List<ImagingDataSetImage> images;

        @JsonIgnore
        public ImagingDataSetConfig getConfig()
        {
            return config;
        }

        public void setConfig(ImagingDataSetConfig config)
        {
            this.config = config;
        }

        @JsonIgnore
        public List<ImagingDataSetImage> getImages()
        {
            return images;
        }

        public void setImages(
                List<ImagingDataSetImage> images)
        {
            this.images = images;
        }
    }

    private void validateInputParams(Map<String, Object> params)
    {
        if (!params.containsKey("permId"))
        {
            throw new UserFailureException("Missing dataset permId!");
        }
        if (!params.containsKey("type"))
        {
            throw new UserFailureException("Missing type!");
        }
        if (!params.containsKey("index"))
        {
            throw new UserFailureException("Missing index!");
        }
    }

    private ImagingDataContainer getDataFromParams(Map<String, Object> params)
    {
        validateInputParams(params);

        String type = (String) params.get("type");
        String json = mapToJson(params);

        switch (type.toLowerCase())
        {
            case "preview":
                return readConfig(json, ImagingPreviewContainer.class);
            case "export":
                return readConfig(json, ImagingExportContainer.class);
            case "multi-export":
                return readConfig(json, ImagingMultiExportContainer.class);
            default:
                throw new UserFailureException("Wrong type:" + type);
        }
    }

}
