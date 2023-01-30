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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.amc;

import java.util.Arrays;
import java.util.List;

import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.PersonSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractSaveDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.DropDownList;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractDefaultTestCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.GWTTestUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AuthorizationGroup;

/**
 * Wait until all fields are loaded and fill add person form.
 * 
 * @author Izabela Adamczyk
 */
public class FillAddPersonForm extends AbstractDefaultTestCommand
{

    private final boolean singleUser;

    private final List<String> codes;

    private AuthorizationGroup authorizationGroup;

    public static final FillAddPersonForm singleUser(String code, AuthorizationGroup authGroup)
    {
        return new FillAddPersonForm(true, Arrays.asList(code), authGroup);
    }

    public static final FillAddPersonForm multipleUsers(List<String> codes,
            AuthorizationGroup authGroup)
    {
        return new FillAddPersonForm(false, codes, authGroup);
    }

    private FillAddPersonForm(final boolean singleUser, final List<String> codes,
            AuthorizationGroup authGroup)
    {
        super();
        assert codes.size() == 1 || singleUser == false;
        authorizationGroup = authGroup;
        this.singleUser = singleUser;
        this.codes = codes;
    }

    @Override
    public void execute()
    {
        if (singleUser)
        {
            GWTTestUtil.selectValueInSelectionWidget(DropDownList.ID
                    + PersonSelectionWidget.SUFFIX
                    + AddPersonToAuthorizationGroupDialog.createId(authorizationGroup,
                            AddPersonToAuthorizationGroupDialog.ID_SINGLE_PERSON_FIELD),
                    ModelDataPropertyNames.CODE, codes.get(0));
        } else
        {
            GWTTestUtil.setRadioValue(AddPersonToAuthorizationGroupDialog.createId(
                    authorizationGroup,
                    AddPersonToAuthorizationGroupDialog.ID_MULTIPLE_PERSON_RADIO), true);
            GWTTestUtil.getTextFieldWithID(
                    AddPersonToAuthorizationGroupDialog.createId(authorizationGroup,
                            AddPersonToAuthorizationGroupDialog.ID_MULTIPLE_PERSON_FIELD))
                    .setValue(StringUtils.joinList(codes));
        }
        GWTTestUtil.clickButtonWithID(AbstractSaveDialog.SAVE_BUTTON_ID);
    }
}
