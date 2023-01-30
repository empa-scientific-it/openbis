/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui;

import java.util.List;

import com.extjs.gxt.ui.client.widget.form.ComboBox;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.AuthorizationGroupModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.DropDownList;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AuthorizationGroup;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;

/**
 * {@link ComboBox} containing list of persons loaded from the server.
 * 
 * @author Izabela Adamczyk
 */
public final class AuthorizationGroupSelectionWidget extends
        DropDownList<AuthorizationGroupModel, AuthorizationGroup>
{
    public static final String SUFFIX = "authorization_group";

    private final IViewContext<?> viewContext;

    public AuthorizationGroupSelectionWidget(final IViewContext<?> viewContext,
            final String idSuffix)
    {
        super(viewContext, SUFFIX + idSuffix, Dict.AUTHORIZATION_GROUP,
                ModelDataPropertyNames.CODE, "Authorization Group", "Authorization Groups");
        this.viewContext = viewContext;
        setAutoSelectFirst(false);
    }

    public final String tryGetSelectedAuthorizationGroupCode()
    {
        AuthorizationGroup authGroup = super.tryGetSelected();
        return authGroup == null ? null : authGroup.getCode();
    }

    @Override
    protected List<AuthorizationGroupModel> convertItems(List<AuthorizationGroup> result)
    {
        return AuthorizationGroupModel.convert(result);
    }

    @Override
    protected void loadData(AbstractAsyncCallback<List<AuthorizationGroup>> callback)
    {
        viewContext.getCommonService().listAuthorizationGroups(callback);
    }

    @Override
    public DatabaseModificationKind[] getRelevantModifications()
    {
        return DatabaseModificationKind.any(ObjectKind.AUTHORIZATION_GROUP);
    }
}
