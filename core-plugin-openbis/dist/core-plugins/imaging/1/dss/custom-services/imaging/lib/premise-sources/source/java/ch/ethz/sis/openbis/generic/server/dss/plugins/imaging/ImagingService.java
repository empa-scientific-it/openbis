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
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.imaging.ImagingDataSetImage;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.imaging.ImagingDataSetPreview;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.imaging.ImagingDataSetPropertyConfig;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.service.CustomDSSServiceExecutionOptions;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.service.id.ICustomDSSServiceId;
import ch.ethz.sis.openbis.generic.server.dss.plugins.imaging.adaptor.IImagingDataSetAdaptor;
import ch.ethz.sis.openbis.generic.server.dss.plugins.imaging.container.ImagingDataContainer;
import ch.ethz.sis.openbis.generic.server.dss.plugins.imaging.container.ImagingExportContainer;
import ch.ethz.sis.openbis.generic.server.dss.plugins.imaging.container.ImagingMultiExportContainer;
import ch.ethz.sis.openbis.generic.server.dss.plugins.imaging.container.ImagingPreviewContainer;
import ch.ethz.sis.openbis.generic.dssapi.v3.plugin.service.ICustomDSSServiceExecutor;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.reflection.ClassUtils;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.generic.server.AbstractDataSetPackager;
import ch.systemsx.cisd.openbis.dss.generic.server.DataStoreServer;
import ch.systemsx.cisd.openbis.dss.generic.server.TarDataSetPackager;
import ch.systemsx.cisd.openbis.dss.generic.server.ZipDataSetPackager;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.ISessionWorkspaceProvider;
import ch.systemsx.cisd.openbis.generic.shared.dto.OpenBISSessionHolder;
import ch.systemsx.cisd.common.logging.LogFactory;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;

public class ImagingService implements ICustomDSSServiceExecutor
{

    private final Properties properties;

    private IHierarchicalContentProvider contentProvider;

    private static final Logger
            operationLog = LogFactory.getLogger(LogCategory.OPERATION, ImagingService.class);

    private static final int DEFAULT_BUFFER_SIZE = (int) (10 * FileUtils.ONE_MB);

    private static final String IMAGING_CONFIG_PROPERTY_NAME = "$IMAGING_DATA_CONFIG";

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

        if (adaptorName == null || adaptorName.isBlank())
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
                Util.readConfig(dataSet.getJsonProperty("$IMAGING_DATA_CONFIG"),
                        ImagingDataSetPropertyConfig.class);

        IImagingDataSetAdaptor adaptor = getAdaptor(config);
        File rootFile = getRootFile(sessionToken, dataSet);
        Map<String, Serializable> previewParams = data.getPreview().getConfig();
        Map<String, String> meta = data.getPreview().getMetaData();
        String format = data.getPreview().getFormat();

        int index = data.getIndex();
        if (config.getImages().size() <= index)
        {
            throw new UserFailureException("There is no image with index:" + index);
        }
        Map<String, Serializable> imageConfig = config.getImages().get(index).getConfig();

        if (format == null || format.isBlank())
        {
            throw new UserFailureException("Format can not be empty!");
        }

        previewParams.put("$COMMAND_TYPE", "PREVIEW");

        ImagingServiceContext context =
                new ImagingServiceContext(sessionToken, getApplicationServerApi(),
                        getDataStoreServerApi());

        data.getPreview().setBytes(
                adaptor.process(context,
                        rootFile, imageConfig, previewParams, meta, format).toString());
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

        String archiveFormat = exportConfig.get("archive-format").toString();
        Map<String, String> meta = data.getExport().getMetaData();
        Serializable[] exportTypes = (Serializable[]) exportConfig.get("include");
        String currentTimeMs = String.valueOf(System.currentTimeMillis());
        AbstractDataSetPackager packager;
        Function<InputStream, Long> checksumFunction;
        File archiveFile;
        String token = "export_" + currentTimeMs;
        ISessionWorkspaceProvider sessionWorkspaceProvider = getSessionWorkspaceProvider(sessionToken);
        File tempDirectory = sessionWorkspaceProvider.getSessionWorkspace();
        String url = DataStoreServer.getConfigParameters().getDownloadURL() + "/datastore_server/session_workspace_file_download?sessionID=" + sessionToken + "&filePath=";

        // Prepare temp directory for archive
        try
        {
            Path tempDir = Files.createDirectory(Path.of(tempDirectory.getAbsolutePath(), token));
            if (archiveFormat.equalsIgnoreCase("zip"))
            {
                archiveFile = Files.createFile(Path.of(tempDir.toAbsolutePath().toString(), "export.zip")).toFile();
                checksumFunction = Util::getCRC32Checksum;
                packager = new ZipDataSetPackager(archiveFile, true, null, null);
            } else if (archiveFormat.equalsIgnoreCase("tar"))
            {
                archiveFile =
                        Files.createFile(Path.of(tempDir.toAbsolutePath().toString(), "export.tar.gz")).toFile();
                checksumFunction = (x) -> 0L;
                packager = new TarDataSetPackager(archiveFile, null, null, DEFAULT_BUFFER_SIZE,
                        5L * DEFAULT_BUFFER_SIZE);
            } else
            {
                throw new UserFailureException("Unknown archive format!");
            }

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
                archiveImage(context, adaptor, image, exportConfig, packager, rootFile, "", checksumFunction, dataSet);
            } else if (exportType.toString().equalsIgnoreCase("raw data"))
            {
                archiveRawData(packager, rootFile, "", checksumFunction, dataSet);
            } else
            {
                throw new UserFailureException("Unknown export type!");
            }

        }

        packager.close();
        data.setUrl(url + Path.of(token, archiveFile.getName()));

        return data;
    }

    private Serializable processMultiExportFlow(String sessionToken, ImagingMultiExportContainer data)
    {

        {
            // multi export case
//            String archiveFormat;
//            for (ImagingDataSetExport export : data.getExport())
//            {
//                Map<String, Serializable> params = export.getConfig();
//                Map<String, String> meta = export.getMetaData();
//                String fullFormat = export.getFormat();
//
//                if (fullFormat == null || fullFormat.isBlank())
//                {
//                    throw new UserFailureException("Format can not be empty!");
//                }
//                String[] formats = fullFormat.split("/");
//                archiveFormat = formats[0];
//                String otherFormat = formats[1];
//
//            }

        }
        return null;
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

    private ISessionWorkspaceProvider getSessionWorkspaceProvider(String sessionToken)
    {
        return ServiceProvider.getDataStoreService().getSessionWorkspaceProvider(sessionToken);
    }

    private DataSet getDataSet(String sessionToken, String permId)
    {
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withProperties();
        fetchOptions.withType();
        fetchOptions.withDataStore();
        fetchOptions.withPhysicalData();
        Map<IDataSetId, DataSet> result = getApplicationServerApi()
                .getDataSets(sessionToken, List.of(new DataSetPermId(permId)),
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

    private void archiveRawData(AbstractDataSetPackager packager, File rootFile, String rootFolderName,
            Function<InputStream, Long> checksumFunction,  DataSet dataSet)
    {
        //Add dataset files to archive
        Util.archiveFiles(packager, rootFile, rootFolderName, checksumFunction);
        //Add dataset properties to archive
        Map<String, Serializable> properties = dataSet.getProperties();
        properties.remove(IMAGING_CONFIG_PROPERTY_NAME);
        byte[] json = Util.mapToJson(properties).getBytes(StandardCharsets.UTF_8);
        packager.addEntry("properties.txt",
                dataSet.getModificationDate().toInstant().toEpochMilli(),
                json.length,
                checksumFunction.apply(new ByteArrayInputStream(json)),
                new ByteArrayInputStream(json));
    }


    private void archiveImage(ImagingServiceContext context, IImagingDataSetAdaptor adaptor,
            ImagingDataSetImage image, Map<String, Serializable> exportConfig,
            AbstractDataSetPackager packager, File rootFile, String rootFolderName,
            Function<InputStream, Long> checksumFunction,  DataSet dataSet) {

        String imageFormat = exportConfig.get("image-format").toString();
        int previewIdx = 0;
        for(ImagingDataSetPreview preview : image.getPreviews())
        {
            Map<String, Serializable> params = preview.getConfig();
            params.put("resolution", exportConfig.get("resolution"));
            Serializable img = adaptor.process(context,
                    rootFile, image.getConfig(), params, image.getMetaData(), imageFormat);
            String imgString = img.toString();
            byte[] decoded = Base64.getDecoder().decode(imgString);
            long size = decoded.length;
            String name = "image" + previewIdx + "." + imageFormat;
            packager.addEntry(Paths.get(rootFolderName, name).toString(),
                    dataSet.getModificationDate().toInstant().toEpochMilli(),
                    size,
                    checksumFunction.apply(new ByteArrayInputStream(decoded)),
                    new ByteArrayInputStream(decoded));
            previewIdx++;
        }
    }

}
