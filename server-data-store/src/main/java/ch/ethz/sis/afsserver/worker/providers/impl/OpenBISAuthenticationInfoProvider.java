package ch.ethz.sis.afsserver.worker.providers.impl;

import ch.ethz.sis.afsserver.startup.AtomicFileSystemServerParameter;
import ch.ethz.sis.afsserver.worker.providers.AuthenticationInfoProvider;
import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.shared.startup.Configuration;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;

public class OpenBISAuthenticationInfoProvider implements AuthenticationInfoProvider {

    private IApplicationServerApi v3 = null;

    @Override
    public void init(Configuration initParameter) throws Exception {
        String openBISUrl = initParameter.getStringProperty(AtomicFileSystemServerParameter.openBISUrl);
        int openBISTimeout = initParameter.getIntegerProperty(AtomicFileSystemServerParameter.openBISTimeout);
        v3 = HttpInvokerUtils.createServiceStub(IApplicationServerApi.class, openBISUrl, openBISTimeout);
    }

    @Override
    public String login(String userId, String password) {
        return v3.login(userId, password);
    }

    @Override
    public Boolean isSessionValid(String sessionToken) {
        return v3.isSessionActive(sessionToken);
    }

    @Override
    public Boolean logout(String sessionToken) {
        v3.logout(sessionToken);
        return Boolean.TRUE;
    }
}
