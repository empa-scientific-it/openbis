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
import ch.ethz.sis.openbis.generic.imagingapi.v3.dto.ImagingDataSetImage;
import ch.ethz.sis.openbis.generic.imagingapi.v3.dto.ImagingDataSetMultiExport;
import ch.ethz.sis.openbis.generic.imagingapi.v3.dto.ImagingDataSetPreview;
import ch.ethz.sis.openbis.generic.imagingapi.v3.dto.ImagingDataSetPropertyConfig;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.service.CustomDSSServiceExecutionOptions;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.service.id.ICustomDSSServiceId;
import ch.ethz.sis.openbis.generic.server.dss.plugins.imaging.adaptor.IImagingDataSetAdaptor;
import ch.ethz.sis.openbis.generic.imagingapi.v3.dto.ImagingDataContainer;
import ch.ethz.sis.openbis.generic.imagingapi.v3.dto.ImagingExportContainer;
import ch.ethz.sis.openbis.generic.imagingapi.v3.dto.ImagingMultiExportContainer;
import ch.ethz.sis.openbis.generic.imagingapi.v3.dto.ImagingPreviewContainer;
import ch.ethz.sis.openbis.generic.dssapi.v3.plugin.service.ICustomDSSServiceExecutor;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.reflection.ClassUtils;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.dto.OpenBISSessionHolder;
import ch.systemsx.cisd.common.logging.LogFactory;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ImagingService implements ICustomDSSServiceExecutor
{

    private final Properties properties;

    private IHierarchicalContentProvider contentProvider;

    private static final Logger
            operationLog = LogFactory.getLogger(LogCategory.OPERATION, ImagingService.class);

    static final String IMAGING_CONFIG_PROPERTY_NAME = "$IMAGING_DATA_CONFIG";

    public ImagingService(Properties properties)
    {
        this.properties = properties;
    }

    @Override
    public Serializable executeService(String sessionToken, ICustomDSSServiceId serviceId,
            CustomDSSServiceExecutionOptions options)
    {
        operationLog.info("Executing imaging service:" + serviceId);
        ImagingDataContainer data = getDataFromParams(options.getParameters());
        try
        {
            if (data.getType().equalsIgnoreCase("preview"))
            {
                return processPreviewFlow(sessionToken, (ImagingPreviewContainer) data);
            } else if (data.getType().equalsIgnoreCase("export"))
            {
                return processExportFlow(sessionToken, (ImagingExportContainer) data);
            } else if (data.getType().equalsIgnoreCase("multi-export"))
            {
                return processMultiExportFlow(sessionToken, (ImagingMultiExportContainer) data);
            } else
            {
                throw new UserFailureException("Unknown request type!");
            }
        } catch (Exception e)
        {
            data.setError(e.toString());
        }
        return data;
    }

    private IImagingDataSetAdaptor getAdaptor(ImagingDataSetPropertyConfig config)
    {
        final String adaptorName = config.getConfig().getAdaptor();

        if (adaptorName == null || adaptorName.trim().isEmpty())
        {
            throw new UserFailureException("Adaptor name is missing from the config!");
        }
        try
        {
            return ClassUtils.create(IImagingDataSetAdaptor.class, adaptorName, properties);
        } catch (Exception e)
        {
            throw new UserFailureException("Could not load adapter: " + adaptorName, e);
        }
    }

    private File getRootFile(String sessionToken, DataSet dataSet)
    {
        IHierarchicalContent content =
                getHierarchicalContentProvider(sessionToken).asContent(
                        dataSet.getPermId().getPermId());
        IHierarchicalContentNode root = content.getRootNode();
        return root.getFile();
    }

    private ImagingPreviewContainer processPreviewFlow(String sessionToken,
            ImagingPreviewContainer data)
    {
        DataSet dataSet = getDataSet(sessionToken, data.getPermId());

        ImagingDataSetPropertyConfig config =
                Util.readConfig(dataSet.getJsonProperty(IMAGING_CONFIG_PROPERTY_NAME),
                        ImagingDataSetPropertyConfig.class);

        IImagingDataSetAdaptor adaptor = getAdaptor(config);
        File rootFile = getRootFile(sessionToken, dataSet);
        String format = data.getPreview().getFormat();

        int index = data.getIndex();
        if (config.getImages().size() <= index)
        {
            throw new UserFailureException("There is no image with index:" + index);
        }
        ImagingDataSetImage image = config.getImages().get(index);

        if (format == null || format.trim().isEmpty())
        {
            throw new UserFailureException("Format can not be empty!");
        }

        ImagingServiceContext context =
                new ImagingServiceContext(sessionToken, getApplicationServerApi(),
                        getDataStoreServerApi());

        adaptor.computePreview(context, rootFile, image, data.getPreview());
        return data;
    }



    private Serializable processExportFlow(String sessionToken, ImagingExportContainer data)
    {
        // Get all parameters
        final DataSet dataSet = getDataSet(sessionToken, data.getPermId());
        final ImagingDataSetPropertyConfig config =
                Util.readConfig(dataSet.getJsonProperty(IMAGING_CONFIG_PROPERTY_NAME),
                        ImagingDataSetPropertyConfig.class);

        final File rootFile = getRootFile(sessionToken, dataSet);
        final int index = data.getIndex();
        if (config.getImages().size() <= index)
        {
            throw new UserFailureException("There is no image with index:" + index);
        }

        final ImagingDataSetImage image = config.getImages().get(index);

        Map<String, Serializable> exportConfig = data.getExport().getConfig();
        Validator.validateExportConfig(exportConfig);

        Serializable[] exportTypes = (Serializable[]) exportConfig.get("include");

        ImagingArchiver archiver;

        // Prepare archiver
        try
        {
            archiver = new ImagingArchiver(sessionToken, exportConfig.get("archive-format").toString());
        } catch (IOException exception)
        {
            throw new UserFailureException("Could not export data!", exception);
        }

        // For each export type, perform adequate action
        for (Serializable exportType : exportTypes)
        {
            if (exportType.toString().equalsIgnoreCase("image"))
            {
                ImagingServiceContext context =
                        new ImagingServiceContext(sessionToken, getApplicationServerApi(),
                                getDataStoreServerApi());
                IImagingDataSetAdaptor adaptor = getAdaptor(config);
                archiveImage(context, adaptor, image, index, exportConfig, rootFile, "", archiver);
            } else if (exportType.toString().equalsIgnoreCase("raw data"))
            {
                archiveRawData(rootFile, "", archiver, dataSet);
            } else
            {
                throw new UserFailureException("Unknown export type!");
            }

        }

        data.setUrl(archiver.build());
        return data;
    }

    private Serializable processMultiExportFlow(String sessionToken, ImagingMultiExportContainer data)
    {
            // multi export case
            final String archiveFormat = "zip";
            ImagingArchiver archiver;
            try
            {
                archiver = new ImagingArchiver(sessionToken, archiveFormat);
            } catch (IOException exception)
            {
                throw new UserFailureException("Could not export data!", exception);
            }

            for (ImagingDataSetMultiExport export : data.getExports())
            {
                DataSet dataSet = getDataSet(sessionToken, export.getPermId());
                ImagingDataSetPropertyConfig config =
                        Util.readConfig(dataSet.getJsonProperty(IMAGING_CONFIG_PROPERTY_NAME),
                                ImagingDataSetPropertyConfig.class);

                File rootFile = getRootFile(sessionToken, dataSet);

                final int index = export.getIndex();
                if (config.getImages().size() <= index)
                {
                    throw new UserFailureException("There is no image with index:" + index);
                }

                ImagingDataSetImage image = config.getImages().get(index);

                Map<String, Serializable> exportConfig = export.getConfig();
                Validator.validateExportConfig(exportConfig);

                Serializable[] exportTypes = (Serializable[]) exportConfig.get("include");

                // For each export type, perform adequate action
                for (Serializable exportType : exportTypes)
                {
                    if (exportType.toString().equalsIgnoreCase("image"))
                    {
                        ImagingServiceContext context =
                                new ImagingServiceContext(sessionToken, getApplicationServerApi(),
                                        getDataStoreServerApi());
                        IImagingDataSetAdaptor adaptor = getAdaptor(config);
                        archiveImage(context, adaptor, image, index, exportConfig, rootFile, export.getPermId(), archiver);
                    } else if (exportType.toString().equalsIgnoreCase("raw data"))
                    {
                        archiveRawData(rootFile, export.getPermId(), archiver, dataSet);
                    } else
                    {
                        throw new UserFailureException("Unknown export type!");
                    }

                }
            }
        data.setUrl(archiver.build());
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

    private DataSet getDataSet(String sessionToken, String permId)
    {
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withType();
        fetchOptions.withDataStore();
        fetchOptions.withPhysicalData();
        Map<IDataSetId, DataSet> result = getApplicationServerApi()
                .getDataSets(sessionToken, Arrays.asList(new DataSetPermId(permId)),
                        fetchOptions);
        if (result.isEmpty())
        {
            throw new UserFailureException("Could not find Dataset:" + permId);
        }
        return result.get(new DataSetPermId(permId));
    }



    private ImagingDataContainer getDataFromParams(Map<String, Object> params)
    {
        Validator.validateInputParams(params);

        String type = (String) params.get("type");
        String json = Util.mapToJson(params);

        switch (type.toLowerCase())
        {
            case "preview":
                return Util.readConfig(json, ImagingPreviewContainer.class);
            case "export":
                return Util.readConfig(json, ImagingExportContainer.class);
            case "multi-export":
                return Util.readConfig(json, ImagingMultiExportContainer.class);
            default:
                throw new UserFailureException("Wrong type:" + type);
        }
    }

    private void archiveRawData(File rootFile, String rootFolderName,
            ImagingArchiver archiver,  DataSet dataSet)
    {
        //Add dataset files to archive
        archiver.addToArchive(rootFolderName, rootFile);
        //Add dataset properties to archive
        Map<String, Serializable> properties = dataSet.getProperties();
        properties.remove(IMAGING_CONFIG_PROPERTY_NAME);
        if(!properties.isEmpty()) {
            byte[] json = Util.mapToJson(properties).getBytes(StandardCharsets.UTF_8);
            archiver.addToArchive(rootFolderName, "properties.txt", json);
        }
    }


    private void archiveImage(ImagingServiceContext context, IImagingDataSetAdaptor adaptor,
            ImagingDataSetImage image, int imageIdx, Map<String, Serializable> exportConfig,
            File rootFile, String rootFolderName, ImagingArchiver archiver) {

        String imageFormat = exportConfig.get("image-format").toString();
        int previewIdx = 0;
        for(ImagingDataSetPreview preview : image.getPreviews())
        {
            String format = imageFormat;
            if(imageFormat.equalsIgnoreCase("original")) {
                format = preview.getFormat();
            }
            Map<String, Serializable> params = preview.getConfig();
            params.put("resolution", exportConfig.get("resolution"));
            Map<String, Serializable> img = adaptor.process(context,
                    rootFile, format, image.getConfig(), image.getMetadata(), params, preview.getMetadata());
            String imgString = img.get("bytes").toString();
            byte[] decoded = Base64.getDecoder().decode(imgString);
            String fileName = "image" + imageIdx +"_preview" + previewIdx + "." + format;

            archiver.addToArchive(rootFolderName, fileName, decoded);
            previewIdx++;
        }
    }

}
