/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.systemtest.plugin.generic;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;

import ch.systemsx.cisd.openbis.generic.client.web.server.UploadedFilesBean;
import ch.systemsx.cisd.openbis.generic.shared.ISessionWorkspaceProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientService;
import ch.systemsx.cisd.openbis.plugin.generic.shared.IGenericServer;
import ch.systemsx.cisd.openbis.systemtest.SystemTestCase;

/**
 * @author Franz-Josef Elmer
 */
public class GenericSystemTestCase extends SystemTestCase
{
    @SuppressWarnings("hiding")
    @Autowired
    protected IGenericClientService genericClientService;

    @SuppressWarnings("hiding")
    @Autowired
    protected IGenericServer genericServer;

    @Autowired
    protected ISessionWorkspaceProvider sessionWorkspaceProvider;

    protected void addMultiPartFile(String sessionToken, String sessionAttributeKey, String fileName, byte[] data)
    {
        HttpSession session = request.getSession();
        UploadedFilesBean uploadedFilesBean =
                (UploadedFilesBean) session.getAttribute(sessionAttributeKey);
        if (uploadedFilesBean == null)
        {
            uploadedFilesBean = new UploadedFilesBean();
            session.setAttribute(sessionAttributeKey, uploadedFilesBean);
        }
        MockMultipartFile multipartFile = new MockMultipartFile(fileName, fileName, "", data);
        uploadedFilesBean.addMultipartFile(sessionToken, multipartFile, sessionWorkspaceProvider);
    }

    protected IEntityProperty property(String type, String value)
    {
        EntityProperty property = new EntityProperty();
        PropertyType propertyType = new PropertyType();
        propertyType.setCode(type);
        property.setPropertyType(propertyType);
        property.setValue(value);
        return property;
    }
}
