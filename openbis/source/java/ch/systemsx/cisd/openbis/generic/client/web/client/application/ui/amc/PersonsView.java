package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.amc;

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

import static ch.systemsx.cisd.openbis.generic.client.web.client.application.util.ClientConstants.COL_DATE;
import static ch.systemsx.cisd.openbis.generic.client.web.client.application.util.ClientConstants.COL_EMAIL;
import static ch.systemsx.cisd.openbis.generic.client.web.client.application.util.ClientConstants.COL_FIRST_NAME;
import static ch.systemsx.cisd.openbis.generic.client.web.client.application.util.ClientConstants.COL_LAST_NAME;
import static ch.systemsx.cisd.openbis.generic.client.web.client.application.util.ClientConstants.COL_PERSON;
import static ch.systemsx.cisd.openbis.generic.client.web.client.application.util.ClientConstants.COL_PERSON_ID;
import static ch.systemsx.cisd.openbis.generic.client.web.client.application.util.ClientConstants.FIT_SIZE;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.AdapterToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.i18n.client.DateTimeFormat;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.PersonModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.ColumnFilter;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Person;

/**
 * {@link LayoutContainer} with persons functionality.
 * 
 * @author Izabela Adamczyk
 */
public class PersonsView extends LayoutContainer
{
    private static final String PREFIX = "persons-view_";

    static final String ADD_BUTTON_ID = GenericConstants.ID_PREFIX + PREFIX + "add-button";

    static final String TABLE_ID = GenericConstants.ID_PREFIX + PREFIX + "table";

    private final GenericViewContext viewContext;

    public PersonsView(final GenericViewContext viewContext)
    {
        this.viewContext = viewContext;
        setLayout(new FitLayout());

    }

    private void display(final List<Person> persons)
    {
        removeAll();

        final List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

        final ColumnConfig codeColumnConfig = new ColumnConfig();
        codeColumnConfig.setId(PersonModel.USER_ID);
        codeColumnConfig.setHeader("User ID");
        codeColumnConfig.setWidth(COL_PERSON_ID);
        configs.add(codeColumnConfig);

        final ColumnConfig firstNameColumnConfig = new ColumnConfig();
        firstNameColumnConfig.setId(PersonModel.FIRST_NAME);
        firstNameColumnConfig.setHeader("First Name");
        firstNameColumnConfig.setWidth(COL_FIRST_NAME);
        configs.add(firstNameColumnConfig);

        final ColumnConfig lastNameColumnConfig = new ColumnConfig();
        lastNameColumnConfig.setId(PersonModel.LAST_NAME);
        lastNameColumnConfig.setHeader("Last Name");
        lastNameColumnConfig.setWidth(COL_LAST_NAME);
        configs.add(lastNameColumnConfig);

        final ColumnConfig emailNameColumnConfig = new ColumnConfig();
        emailNameColumnConfig.setId(PersonModel.EMAIL);
        emailNameColumnConfig.setHeader("Email");
        emailNameColumnConfig.setWidth(COL_EMAIL);
        configs.add(emailNameColumnConfig);

        final ColumnConfig registratorColumnConfig = new ColumnConfig();
        registratorColumnConfig.setId(PersonModel.REGISTRATOR);
        registratorColumnConfig.setHeader("Registrator");
        registratorColumnConfig.setWidth(COL_PERSON);
        configs.add(registratorColumnConfig);

        final ColumnConfig registrationDateColumnConfig = new ColumnConfig();
        registrationDateColumnConfig.setId(PersonModel.REGISTRATION_DATE);
        registrationDateColumnConfig.setHeader("Registration Date");
        registrationDateColumnConfig.setWidth(COL_DATE);
        registrationDateColumnConfig.setAlignment(HorizontalAlignment.RIGHT);
        registrationDateColumnConfig.setDateTimeFormat(DateTimeFormat.getShortDateFormat());
        configs.add(registrationDateColumnConfig);

        final ColumnModel cm = new ColumnModel(configs);

        final ListStore<PersonModel> store = new ListStore<PersonModel>();
        store.add(getPersonModels(persons));

        final ContentPanel cp = new ContentPanel();
        cp.setBodyBorder(false);
        cp.setHeading("Person list");
        cp.setButtonAlign(HorizontalAlignment.CENTER);
        final PersonsView personList = this;

        cp.setLayout(new FitLayout());
        cp.setSize(FIT_SIZE, FIT_SIZE);

        final Grid<PersonModel> grid = new Grid<PersonModel>(store, cm);
        grid.setId(TABLE_ID);
        grid.setBorders(true);
        cp.add(grid);

        final Button addPersonButton =
                new Button("Add person", new SelectionListener<ComponentEvent>()
                    {
                        @Override
                        public void componentSelected(ComponentEvent ce)
                        {
                            new AddPersonDialog(viewContext, personList).show();
                        }
                    });
        addPersonButton.setId(ADD_BUTTON_ID);

        final ToolBar toolBar = new ToolBar();
        toolBar.add(new LabelToolItem("Filter:"));
        toolBar.add(new AdapterToolItem(new ColumnFilter<PersonModel>(store, PersonModel.USER_ID,
                "user id")));
        toolBar.add(new SeparatorToolItem());
        toolBar.add(new AdapterToolItem(addPersonButton));
        cp.setBottomComponent(toolBar);

        add(cp);
        layout();

    }

    List<PersonModel> getPersonModels(final List<Person> persons)
    {
        final List<PersonModel> pms = new ArrayList<PersonModel>();
        for (final Person p : persons)
        {
            pms.add(new PersonModel(p));
        }
        return pms;
    }

    public void refresh()
    {
        removeAll();
        add(new Text("data loading..."));
        viewContext.getService().listPersons(new ListPersonsCallback(viewContext));
    }

    //
    // Helper classes
    //

    final class ListPersonsCallback extends AbstractAsyncCallback<List<Person>>
    {
        private ListPersonsCallback(final GenericViewContext viewContext)
        {
            super(viewContext);
        }

        //
        // AbstractAsyncCallback
        //

        @Override
        public final void process(final List<Person> persons)
        {
            display(persons);
        }
    }
}
