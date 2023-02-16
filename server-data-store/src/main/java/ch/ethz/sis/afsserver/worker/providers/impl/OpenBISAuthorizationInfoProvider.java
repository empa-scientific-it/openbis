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
import ch.ethz.sis.afsserver.worker.providers.AuthorizationInfoProvider;
import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.rights.Right;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.rights.Rights;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.rights.fetchoptions.RightsFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.shared.io.FilePermission;
import ch.ethz.sis.shared.startup.Configuration;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OpenBISAuthorizationInfoProvider implements AuthorizationInfoProvider {

    private IApplicationServerApi v3 = null;

    @Override
    public void init(Configuration initParameter) throws Exception {
        String openBISUrl = initParameter.getStringProperty(AtomicFileSystemServerParameter.openBISUrl);
        int openBISTimeout = initParameter.getIntegerProperty(AtomicFileSystemServerParameter.openBISTimeout);
        v3 = HttpInvokerUtils.createServiceStub(IApplicationServerApi.class, openBISUrl, openBISTimeout);
    }

    @Override
    public boolean doesSessionHaveRights(String sessionToken, String owner, Set<FilePermission> permissions) {
        Set<FilePermission> found = new HashSet<>();

        ISampleId identifier = null;
        if (owner.contains("/")) { // Is Identifier
            identifier = new SampleIdentifier(owner);
        } else { // Is permId
            identifier = new SamplePermId(owner);
        }
        Map<ISampleId, Sample> samples = v3.getSamples(sessionToken, List.of(identifier), new SampleFetchOptions());
        if (!samples.isEmpty()) {
            found.add(FilePermission.Read);
        }
        Rights rights = v3.getRights(sessionToken, List.of(identifier), new RightsFetchOptions()).get(identifier);
        if (rights.getRights().contains(Right.UPDATE)) {
            found.add(FilePermission.Write);
        }

        for (FilePermission permission:permissions) {
            if (!found.contains(permission)) {
                return false;
            }
        }
        return true;
    }
}
