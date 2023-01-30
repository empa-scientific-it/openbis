/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.model;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.data.ModelData;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AuthorizationGroup;

/**
 * A {@link ModelData} implementation for {@link AuthorizationGroup}.
 * 
 * @author Izabela Adamczyk
 */
public class AuthorizationGroupModel extends SimplifiedBaseModel
{

    private static final long serialVersionUID = 1L;

    public AuthorizationGroupModel()
    {
    }

    public AuthorizationGroupModel(final AuthorizationGroup authGroup)
    {
        set(ModelDataPropertyNames.CODE, authGroup.getCode());
        set(ModelDataPropertyNames.OBJECT, authGroup);
    }

    public final static List<AuthorizationGroupModel> convert(final List<AuthorizationGroup> groups)
    {
        final List<AuthorizationGroupModel> result = new ArrayList<AuthorizationGroupModel>();
        for (final AuthorizationGroup g : groups)
        {
            result.add(new AuthorizationGroupModel(g));
        }
        return result;
    }

    public final AuthorizationGroup getBaseObject()
    {
        return get(ModelDataPropertyNames.OBJECT);
    }
}
