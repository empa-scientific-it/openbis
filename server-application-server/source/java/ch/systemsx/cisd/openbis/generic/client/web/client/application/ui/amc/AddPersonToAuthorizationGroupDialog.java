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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.amc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.PersonSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractRegistrationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AuthorizationGroup;

/**
 * {@link Window} containing a form allowing to choose a person or a few of them.
 * 
 * @author Izabela Adamczyk
 */
public class AddPersonToAuthorizationGroupDialog extends AbstractRegistrationDialog
{

    public static final String ID_MULTIPLE_PERSON_RADIO = "multiple-person-rd";

    public static final String ID_MULTIPLE_PERSON_FIELD = "multiple-person-field";

    public static final String ID_SINGLE_PERSON_FIELD = "single-person-field";

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    private final PersonSelectionWidget singlePersonField;

    private final AuthorizationGroup authorizationGroup;

    private final TextArea multiplePersonsField;

    private final Radio singlePersonRadio;

    private final Radio multiplePersonsRadio;

    public AddPersonToAuthorizationGroupDialog(
            final IViewContext<ICommonClientServiceAsync> viewContext,
            AuthorizationGroup authorizationGroupOrNull,
            final IDelegatedAction postRegistrationCallback)
    {
        super(viewContext, viewContext.getMessage(Dict.ADD_PERSON_TO_AUTHORIZATION_GROUP_TITLE,
                authorizationGroupOrNull.getCode()), postRegistrationCallback);
        this.viewContext = viewContext;
        this.authorizationGroup = authorizationGroupOrNull;

        RadioGroup radioGroup = new RadioGroup();
        radioGroup.setLabelSeparator("");
        multiplePersonsRadio = new Radio();
        multiplePersonsRadio.setId(createId(authorizationGroupOrNull, ID_MULTIPLE_PERSON_RADIO));
        multiplePersonsRadio.setBoxLabel(viewContext.getMessage(Dict.RADIO_MANY_USERS));
        singlePersonRadio = new Radio();
        singlePersonRadio.setBoxLabel(viewContext.getMessage(Dict.RADIO_ONE_USER));
        singlePersonRadio.setValue(true);
        radioGroup.add(singlePersonRadio);
        radioGroup.add(multiplePersonsRadio);
        addField(radioGroup);

        this.singlePersonField =
                new PersonSelectionWidget(viewContext, createId(authorizationGroupOrNull,
                        ID_SINGLE_PERSON_FIELD));
        FieldUtil.setMandatoryFlag(singlePersonField, true);
        addField(singlePersonField);
        this.multiplePersonsField = createMultiplePersonField(authorizationGroupOrNull);
        addField(multiplePersonsField);
        GWTUtils.updateVisibleField(singlePersonRadio.getValue(), multiplePersonsRadio.getValue(),
                singlePersonField, multiplePersonsField);
        radioGroup.addListener(Events.Change, new Listener<BaseEvent>()
            {
                @Override
                public void handleEvent(BaseEvent be)
                {
                    GWTUtils.updateVisibleField(singlePersonRadio.getValue(), multiplePersonsRadio
                            .getValue(), singlePersonField, multiplePersonsField);
                }
            });
    }

    private TextArea createMultiplePersonField(AuthorizationGroup authorizationGroupOrNull)
    {
        TextArea field = new TextArea();
        field.setId(createId(authorizationGroupOrNull, ID_MULTIPLE_PERSON_FIELD));
        field.setHeight("20em");
        field.setWidth(500);
        field.setFieldLabel(viewContext.getMessage(Dict.PERSONS_IDS_LABEL));
        field.setEmptyText(viewContext.getMessage(Dict.PERSON_IDS_LIST));
        FieldUtil.setMandatoryFlag(field, true);
        return field;
    }

    public static final String createId(AuthorizationGroup authorizationGroupOrNull, String suffix)
    {
        return GenericConstants.ID_PREFIX + "_add-person-to-user-group-dialog_"
                + authorizationGroupOrNull.getId() + "_" + suffix;
    }

    public final List<String> getUserCodes()
    {
        List<String> codes = new ArrayList<String>();
        if (singlePersonRadio.getValue())
        {
            String codeOrNull = singlePersonField.tryGetSelectedPersonCode();
            if (codeOrNull != null)
            {
                codes.add(codeOrNull);
            }
        } else
        {
            String text = multiplePersonsField.getValue();
            if (StringUtils.isBlank(text) == false)
            {
                codes.addAll(Arrays.asList(text.split(GenericConstants.ITEMS_TEXTAREA_REGEX)));
            }
        }
        return codes;
    }

    @Override
    protected void register(AsyncCallback<Void> registrationCallback)
    {
        List<String> personsCodes = getUserCodes();
        if (personsCodes.size() > 0)
        {
            TechId authGroupId = TechId.create(authorizationGroup);
            viewContext.getService().addPersonsToAuthorizationGroup(authGroupId, personsCodes,
                    registrationCallback);
        } else
        {
            GWTUtils.alert("Info", "No users have been selected.");
        }
    }
}
