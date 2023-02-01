package ch.ethz.sis.microservices.download.server.services.store;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ch.ethz.sis.microservices.download.server.logging.LogManager;
import ch.ethz.sis.microservices.download.server.logging.Logger;
import ch.ethz.sis.microservices.download.server.services.Service;
import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.ContentCopy;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId;
import ch.ethz.sis.openbis.generic.dssapi.v3.IDataStoreServerApi;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.DataSetFile;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.fetchoptions.DataSetFileFetchOptions;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.search.DataSetFileSearchCriteria;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;

public abstract class AbstractFileServiceHandler extends Service
{
    private static Logger logger = LogManager.getLogger(FileInfoHandler.class);

    protected void doAction(HttpServletRequest request,
            HttpServletResponse response) throws ServletException,
            IOException
    {
        // Service Configuration
        String openbisURL = this.getServiceConfig().getParameters().get("openbis-url");
        String datastoreURL = this.getServiceConfig().getParameters().get("datastore-url");
        int servicesTimeout = Integer.parseInt(this.getServiceConfig().getParameters().get("services-timeout"));
        String allowedExternalDMSCode = this.getServiceConfig().getParameters().get("allowedExternalDMSCode");
        String allowedContentCopyPath = this.getServiceConfig().getParameters().get("allowedContentCopyPath");

        // Request parameters
        String sessionToken = request.getParameter("sessionToken");
        String datasetPermId = request.getParameter("datasetPermId");
        String externalDMSCode = request.getParameter("externalDMSCode");
        String contentCopyPath = request.getParameter("contentCopyPath");
        String datasetPathToFile = request.getParameter("datasetPathToFile");

        if (!externalDMSCode.equals(allowedExternalDMSCode))
        {
            throw new RuntimeException("The requested externalDMSCode is not available.");
        }

        boolean userCanAccessDatasetFile =
                userCanAccessDatasetFile(openbisURL, datastoreURL, servicesTimeout, sessionToken, datasetPermId, externalDMSCode,
                        contentCopyPath, datasetPathToFile);
        boolean isFileAccessible = false;
        if (userCanAccessDatasetFile)
        {
            isFileAccessible = isDatasetFileAccessible(allowedContentCopyPath, datasetPermId, contentCopyPath, datasetPathToFile);
        }

        Path pathToFile = getPathToFile(contentCopyPath, datasetPathToFile);
        if (isFileAccessible)
        {
            success(pathToFile, request, response);
        } else
        {
            failure(pathToFile, request, response);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException,
            IOException
    {
        doAction(request, response);
    }

    protected abstract void success(Path pathToFile, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;

    protected abstract void failure(Path pathToFile, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;

    private DataSet getDataset(String openbisURL, int openbisTimeout, String sessionToken, String datasetPermId, String externalDMSCode,
            String contentCopyPath)
    {
        IApplicationServerApi v3 = HttpInvokerUtils.createServiceStub(IApplicationServerApi.class, openbisURL, openbisTimeout);
        DataSetFetchOptions dataSetFetchOptions = new DataSetFetchOptions();
        dataSetFetchOptions.withLinkedData().withExternalDms();
        DataSetPermId id = new DataSetPermId(datasetPermId);
        Map<IDataSetId, DataSet> datasets = v3.getDataSets(sessionToken, Arrays.asList(id), dataSetFetchOptions);
        DataSet dataset = datasets.get(id);
        if (dataset != null && !dataset.getLinkedData().getContentCopies().isEmpty())
        {
            for (ContentCopy contentCopy : dataset.getLinkedData().getContentCopies())
            {
                if (contentCopy.getExternalDms().getCode().equals(externalDMSCode) && contentCopy.getPath().equals(contentCopyPath))
                {
                    return dataset;
                }
            }
        }
        return null;
    }

    private DataSetFile getDatasetFile(String datastoreURL, int datastoreTimeout, String sessionToken, String datasetPermId,
            String datasetPathToFile)
    {
        IDataStoreServerApi v3Dss = HttpInvokerUtils.createServiceStub(IDataStoreServerApi.class, datastoreURL, datastoreTimeout);

        DataSetFileSearchCriteria searchCriteria = new DataSetFileSearchCriteria();
        searchCriteria.withDataSet().withCode().thatEquals(datasetPermId);
        DataSetFileFetchOptions fetchOptions = new DataSetFileFetchOptions();
        SearchResult<DataSetFile> files = v3Dss.searchFiles(sessionToken, searchCriteria, fetchOptions);
        for (DataSetFile file : files.getObjects())
        {
            if (file.getPath().equals(datasetPathToFile))
            {
                return file;
            }
        }
        return null;
    }

    private boolean userCanAccessDatasetFile(
            String openbisURL,
            String datastoreURL,
            int servicesTimeout,
            String sessionToken,
            String datasetPermId,
            String externalDMSCode,
            String contentCopyPath,
            String datasetPathToFile)
    {
        DataSet dataSet = getDataset(openbisURL, servicesTimeout, sessionToken, datasetPermId, externalDMSCode, contentCopyPath);
        DataSetFile file = getDatasetFile(datastoreURL, servicesTimeout, sessionToken, datasetPermId, datasetPathToFile);
        return (dataSet != null) && (file != null);
    }

    private Path getPathToFile(String contentCopyPath, String datasetPathToFile)
    {
        return Paths.get(contentCopyPath, datasetPathToFile);
    }

    private boolean isDatasetFileAccessible(String allowedContentCopyPath, String datasetPermId, String contentCopyPath, String datasetPathToFile)
    {
        boolean isFileAccessible = false;

        Path pathToFile = getPathToFile(contentCopyPath, datasetPathToFile);
        String normalizedPathToFile = pathToFile.normalize().toString();
        if (!normalizedPathToFile.startsWith(allowedContentCopyPath))
        {
            throw new RuntimeException("Given datasetPathToFile goes outside allowedContentCopyPath");
        }

        try
        {
            isFileAccessible = Files.exists(pathToFile);
        } catch (Exception ex)
        {
            logger.catching(ex);
        }

        return isFileAccessible;
    }
}
