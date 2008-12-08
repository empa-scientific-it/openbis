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

package ch.systemsx.cisd.openbis.generic.client.web.client.application;

import java.util.List;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.google.gwt.user.client.Element;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.SearchableEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SearchableEntity;

/**
 * A {@link ComboBox} extension for searching entities.
 * 
 * @author Christian Ribeaud
 */
final class EntityChooser extends ComboBox<SearchableEntityModel>
{
    private final IViewContext<ICommonClientServiceAsync> commonContext;

    EntityChooser(final IViewContext<ICommonClientServiceAsync> commonContext)
    {
        this.commonContext = commonContext;
        setEditable(false);
        setDisplayField(ModelDataPropertyNames.DESCRIPTION);
        setWidth(100);
        setStore(new ListStore<SearchableEntityModel>());
    }

    /**
     * Returns the {@link SearchableEntity} currently selected.
     * 
     * @return never <code>null</code> but be sure not to call this method if nothing is selected.
     */
    public final SearchableEntity getSelectedSearchableEntity()
    {
        final List<SearchableEntityModel> selection = getSelection();
        assert selection.size() == 1 : "Selection is empty.";
        return selection.get(0).get(ModelDataPropertyNames.OBJECT);
    }

    //
    // ComboBox
    //

    @Override
    protected final void onRender(final Element parent, final int index)
    {
        super.onRender(parent, index);
        commonContext.getService()
                .listSearchableEntities(new ListSearchableEntities(commonContext));
    }

    //
    // Helper classes
    //

    public final class ListSearchableEntities extends AbstractAsyncCallback<List<SearchableEntity>>
    {

        ListSearchableEntities(final IViewContext<?> viewContext)
        {
            super(viewContext);
        }

        //
        // AbstractAsyncCallback
        //

        @Override
        protected final void process(final List<SearchableEntity> result)
        {
            final ListStore<SearchableEntityModel> searchableEntityStore = getStore();
            searchableEntityStore.removeAll();
            searchableEntityStore.add(SearchableEntityModel.NULL_SEARCHABLE_ENTITY_MODEL);
            searchableEntityStore.add(SearchableEntityModel.convert(result));
            setValue(searchableEntityStore.getAt(0));
        }
    }

}