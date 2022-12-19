package ch.ethz.sis.openbis.generic;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.dssapi.v3.IDataStoreServerApi;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.dataset.create.UploadedDataSetCreation;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;

import java.nio.file.Path;

public class OpenBISAPI {
    private static final int DEFAULT_TIMEOUT_IN_MILLIS = 30000; //30 seconds

    private final IApplicationServerApi asFacade;
    private final IDataStoreServerApi dssFacade;
    private String sessionToken;

    public OpenBISAPI(String asURL, String dssURL, int timeout) {
        asFacade = HttpInvokerUtils.createServiceStub(IApplicationServerApi.class, asURL, timeout);
        dssFacade = HttpInvokerUtils.createServiceStub(IDataStoreServerApi.class, dssURL, timeout);
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public String uploadFileWorkspaceDSS(Path fileOrFolder) {
        // Upload file or folder to the DSS SessionWorkspaceFileUploadServlet and return the ID to be used by createUploadedDataSet
        // This method hides the complexities of uploading a folder with many files and does the uploads in chunks.
        return null;
    }

    public DataSetPermId createUploadedDataSet(UploadedDataSetCreation newDataSet) {
        return dssFacade.createUploadedDataSet(sessionToken, newDataSet);
    }

}
