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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractDataConfirmationDialog;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AuthorizationGroup;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;

public final class PersonListDeletionConfirmationDialog extends
        AbstractDataConfirmationDialog<List<Person>>
{
    private static final int LABEL_WIDTH = 60;

    private static final int FIELD_WIDTH = 180;

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    private final AbstractAsyncCallback<Void> callback;

    private final AuthorizationGroup authorizationGroup;

    public PersonListDeletionConfirmationDialog(
            IViewContext<ICommonClientServiceAsync> viewContext, List<Person> persons,
            AuthorizationGroup authorizationGroup, AbstractAsyncCallback<Void> callback)
    {
        super(viewContext, persons, viewContext
                .getMessage(Dict.REMOVE_PERSONS_FROM_AUTHORIZATION_GROUP_CONFIRMATION_TITLE));
        assert authorizationGroup != null;
        this.viewContext = viewContext;
        this.authorizationGroup = authorizationGroup;
        this.callback = callback;
    }

    @Override
    protected void executeConfirmedAction()
    {
        viewContext.getCommonService().removePersonsFromAuthorizationGroup(
                TechId.create(authorizationGroup), extractCodes(data), callback);
    }

    private List<String> extractCodes(List<Person> persons)
    {
        ArrayList<String> result = new ArrayList<String>();
        for (Person p : persons)
        {
            result.add(p.getUserId());
        }
        return result;
    }

    @Override
    protected String createMessage()
    {
        List<String> codes = extractCodes(data);
        return viewContext.getMessage(
                Dict.REMOVE_PERSONS_FROM_AUTHORIZATION_GROUP_CONFIRMATION_MESSAGE, codes.size(),
                StringUtils.abbreviate(StringUtils.joinList(codes), 1000),
                authorizationGroup.getCode());
    }

    @Override
    protected void extendForm()
    {
        formPanel.setLabelWidth(LABEL_WIDTH);
        formPanel.setFieldWidth(FIELD_WIDTH);
    }
}
