/*
 * Copyright 2012 ETH Zuerich, CISD
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
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;

/**
 * @author Pawel Glyzewski
 */
public class PersonListDeactivationConfirmationDialog extends
        AbstractDataConfirmationDialog<List<Person>>
{
    private static final int LABEL_WIDTH = 60;

    private static final int FIELD_WIDTH = 180;

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    private final AbstractAsyncCallback<Void> callback;

    public PersonListDeactivationConfirmationDialog(
            IViewContext<ICommonClientServiceAsync> viewContext, List<Person> persons,
            AbstractAsyncCallback<Void> callback)
    {
        super(viewContext, persons, viewContext
                .getMessage(Dict.DEACTIVATE_PERSONS_CONFIRMATION_TITLE));
        this.viewContext = viewContext;
        this.callback = callback;
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
        return viewContext.getMessage(Dict.DEACTIVATE_PERSONS_CONFIRMATION_MESSAGE, codes.size(),
                StringUtils.abbreviate(StringUtils.joinList(codes), 1000));
    }

    @Override
    protected void extendForm()
    {
        formPanel.setLabelWidth(LABEL_WIDTH);
        formPanel.setFieldWidth(FIELD_WIDTH);
    }

    @Override
    protected void executeConfirmedAction()
    {
        viewContext.getCommonService().deactivatePersons(extractCodes(data), callback);
    }
}
