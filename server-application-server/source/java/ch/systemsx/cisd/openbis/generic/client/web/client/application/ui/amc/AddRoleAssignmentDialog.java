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

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.form.AdapterField;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AuthorizationGroupSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.PersonSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.SpaceSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.ProjectSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractRegistrationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.DialogWithOnlineHelpUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Grantee;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;

/**
 * {@link Window} containing role assignment registration form.
 * 
 * @author Izabela Adamczyk
 */
public class AddRoleAssignmentDialog extends AbstractRegistrationDialog
{
    private final IViewContext<ICommonClientServiceAsync> viewContext;

    static final String PREFIX = GenericConstants.ID_PREFIX + "add-role_";

    static final String ROLE_FIELD_ID = PREFIX + "role-field";

    static final String AUTH_GROUP_RADIO = PREFIX + "auth-group-rd";

    private final SpaceSelectionWidget group;

    private final ProjectSelectionWidget project;

    private final PersonSelectionWidget person;

    private final AdapterField roleBox;

    private Radio authGroupRadio;

    private Radio personRadio;

    private AuthorizationGroupSelectionWidget authGroup;

    public AddRoleAssignmentDialog(final IViewContext<ICommonClientServiceAsync> viewContext,
            final IDelegatedAction postRegistrationCallback)
    {
        super(viewContext, "Assign authorization role to the user or the group of users",
                postRegistrationCallback);
        this.viewContext = viewContext;

        group = new SpaceSelectionWidget(viewContext, PREFIX, false, false);
        group.setWidth(100);

        project = new ProjectSelectionWidget(viewContext, PREFIX);
        project.setWidth(100);

        roleBox = new AdapterField(new RoleListBox(viewContext, group, project));
        roleBox.setFieldLabel("Role");
        roleBox.setWidth(100);
        roleBox.setId(ROLE_FIELD_ID);
        addField(roleBox);
        addField(group);
        addField(project);

        RadioGroup radioGroup = new RadioGroup();
        radioGroup.setFieldLabel("Grantee Type");
        authGroupRadio = new Radio();
        authGroupRadio.setBoxLabel("Authorization Group");
        authGroupRadio.setId(AUTH_GROUP_RADIO);

        personRadio = new Radio();
        personRadio.setBoxLabel("Person");
        personRadio.setValue(true);

        radioGroup.add(authGroupRadio);
        radioGroup.add(personRadio);

        addField(radioGroup);

        person = new PersonSelectionWidget(viewContext, PREFIX);
        FieldUtil.markAsMandatory(person);
        addField(person);

        authGroup = new AuthorizationGroupSelectionWidget(viewContext, PREFIX);
        FieldUtil.markAsMandatory(authGroup);
        addField(authGroup);

        GWTUtils.updateVisibleField(personRadio.getValue(), authGroupRadio.getValue(), person,
                authGroup);

        radioGroup.addListener(Events.Change, new Listener<BaseEvent>()
            {
                @Override
                public void handleEvent(BaseEvent be)
                {
                    GWTUtils.updateVisibleField(personRadio.getValue(), authGroupRadio.getValue(),
                            person, authGroup);
                }
            });

        DialogWithOnlineHelpUtils.addHelpButton(viewContext, this, createHelpPageIdentifier());
    }

    @Override
    protected void register(AsyncCallback<Void> registrationCallback)
    {
        Grantee grantee =
                personRadio.getValue() ? Grantee.createPerson(person.tryGetSelectedPersonCode())
                        : Grantee.createAuthorizationGroup(authGroup
                                .tryGetSelectedAuthorizationGroupCode());

        RoleListBox roleWidget = (RoleListBox) roleBox.getWidget();
        RoleWithHierarchy role = roleWidget.getValue();

        if (role.isInstanceLevel())
        {
            viewContext.getService().registerInstanceRole(roleWidget.getValue(), grantee, registrationCallback);
        } else if (role.isSpaceLevel())
        {
            Space selectedSpace = group.tryGetSelectedSpace();
            viewContext.getService().registerSpaceRole(roleWidget.getValue(), selectedSpace.getCode(), grantee,
                    registrationCallback);
        } else if (role.isProjectLevel())
        {
            Project selectedProject = project.tryGetSelectedProject();
            viewContext.getService().registerProjectRole(roleWidget.getValue(), selectedProject.getIdentifier(), grantee,
                    registrationCallback);
        }
    }

    private HelpPageIdentifier createHelpPageIdentifier()
    {
        return new HelpPageIdentifier(HelpPageIdentifier.HelpPageDomain.ROLES,
                HelpPageIdentifier.HelpPageAction.REGISTER);
    }
}
