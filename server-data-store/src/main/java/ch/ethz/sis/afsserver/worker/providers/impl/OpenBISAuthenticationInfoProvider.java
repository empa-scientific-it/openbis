/*
 * Copyright ETH 2022 - 2023 Zürich, Scientific IT Services
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
