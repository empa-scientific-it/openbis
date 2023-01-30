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

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.SearchableEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.DropDownList;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SearchableEntity;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;

/**
 * A {@link ComboBox} extension for searching entities.
 * 
 * @author Christian Ribeaud
 */
final class SearchableEntitySelectionWidget extends
        DropDownList<SearchableEntityModel, SearchableEntity>
{

    private final IViewContext<ICommonClientServiceAsync> commonContext;

    SearchableEntitySelectionWidget(final IViewContext<ICommonClientServiceAsync> commonContext)
    {
        super(commonContext, "", "", ModelDataPropertyNames.DESCRIPTION, "", "");
        this.commonContext = commonContext;
        setTemplate(getTemplateWithDividers(ModelDataPropertyNames.DESCRIPTION));
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

    private final class ListSearchableEntities extends
            AbstractAsyncCallback<List<SearchableEntity>>
    {

        ListSearchableEntities(final IViewContext<?> viewContext)
        {
            super(viewContext);
        }

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

    @Override
    protected List<SearchableEntityModel> convertItems(List<SearchableEntity> result)
    {
        return SearchableEntityModel.convert(result);
    }

    @Override
    protected void loadData(AbstractAsyncCallback<List<SearchableEntity>> callback)
    {
        ListSearchableEntities entities = new ListSearchableEntities(commonContext);
        commonContext.getService().listSearchableEntities(entities);
        callback.ignore();
    }

    @Override
    public DatabaseModificationKind[] getRelevantModifications()
    {
        return DatabaseModificationKind.EMPTY_ARRAY;
    }
    
    public final static native String getTemplateWithDividers(String displayField)
    /*-{ 
       return  [ 
       '<tpl for=".">', 
       '<tpl if="xindex == 1"><div class="unselectableItem">Entities<\/div><\/tpl>',
       '<tpl if="xindex == 6"><div class="unselectableItem">Search Domains<\/div><\/tpl>',
       '<div class="x-combo-list-item">{[values.',displayField,']}</div>', 
       '</tpl>' 
       ].join(""); 
     }-*/;

}
