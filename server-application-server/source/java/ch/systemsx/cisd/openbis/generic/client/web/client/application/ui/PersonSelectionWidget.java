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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.PersonModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.DropDownList;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;

/**
 * {@link ComboBox} containing list of persons loaded from the server.
 * 
 * @author Izabela Adamczyk
 */
public final class PersonSelectionWidget extends DropDownList<PersonModel, Person>
{
    public static final String SUFFIX = "person";

    private final IViewContext<?> viewContext;

    public PersonSelectionWidget(final IViewContext<?> viewContext, final String idSuffix)
    {
        super(viewContext, SUFFIX + idSuffix, Dict.PERSON, ModelDataPropertyNames.CODE, "person",
                "persons");
        this.viewContext = viewContext;
        setAutoSelectFirst(false);
    }

    /**
     * Returns the {@link Person} currently selected.
     * 
     * @return <code>null</code> if nothing is selected yet.
     */
    public final Person tryGetSelectedPerson()
    {
        return super.tryGetSelected();
    }

    public final String tryGetSelectedPersonCode()
    {
        Person person = super.tryGetSelected();
        return person == null ? null : person.getUserId();
    }

    @Override
    protected List<PersonModel> convertItems(List<Person> result)
    {
        return PersonModel.convert(result);
    }

    @Override
    protected void loadData(AbstractAsyncCallback<List<Person>> callback)
    {
        viewContext.getCommonService().listActivePersons(callback);
    }

    @Override
    public DatabaseModificationKind[] getRelevantModifications()
    {
        return DatabaseModificationKind.any(ObjectKind.PERSON);
    }
}
