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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.ComboBox;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.DetailedSearchFieldComboModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.PropertyTypeRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.entity.PropertyTypesFilterUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.DropDownList;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.AttributeSearchFieldKindProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchField;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchFieldKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IAttributeSearchFieldKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISearchFieldAvailability;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISearchFieldKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PersonRoles;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;

/**
 * {@link ComboBox} containing list of detailed search fields loaded from the server (property types) and static ones.
 * 
 * @author Izabela Adamczyk
 * @author Piotr Buczek
 */
// TODO 2009-02-13, Tomasz Pylak: fetching of property types is done every time a new widget is
// created, although all of them are identical. It should be done outside of this class and passed
// to it.
public final class DetailedSearchFieldsSelectionWidget extends
        DropDownList<DetailedSearchFieldComboModel, PropertyType>
{
    private static final int WIDTH = 200;

    private static final String EMPTY_RESULT_SUFFIX = "search fields";

    private static final String CHOOSE_SUFFIX = "search field";

    static final String SUFFIX = "data-set-search";

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    private final EntityKind entityKind;

    private boolean dataLoaded;

    private List<PropertyType> propertyTypes;

    public DetailedSearchFieldsSelectionWidget(
            final IViewContext<ICommonClientServiceAsync> viewContext, final String idSuffix,
            final EntityKind entityKind)
    {
        super(viewContext, SUFFIX + idSuffix, Dict.PROPERTY_TYPE, ModelDataPropertyNames.CODE,
                CHOOSE_SUFFIX, EMPTY_RESULT_SUFFIX);
        this.viewContext = viewContext;
        this.entityKind = entityKind;
        dataLoaded = false;
        setAllowBlank(true);
    }

    public EntityKind getEntityKind()
    {
        return entityKind;
    }

    @Override
    /*
     * * Always returns null.
     */
    public PropertyType tryGetSelected()
    {
        return null;
    }

    /**
     * Returns {@link DetailedSearchField} connected with selected option, or null - if nothing selected.
     */
    public DetailedSearchField tryGetSelectedField()
    {
        return (DetailedSearchField) GWTUtils.tryGetSingleSelected(this);
    }

    /**
     * Returns code of the selected option, or null - if nothing is selected.
     */
    public String tryGetSelectedCode()
    {
        return GWTUtils.tryGetSingleSelectedCode(this);
    }

    public ISearchFieldKind tryGetSelectedKind()
    {
        DetailedSearchFieldComboModel model = GWTUtils.tryGetSingleSelectedModel(this);
        return model != null ? model.getKind() : null;
    }

    // NOTE: should be removed. See the todo for the whole class.
    public List<PropertyType> getAvailablePropertyTypes()
    {
        return propertyTypes;
    }

    public DetailedSearchFieldsSelectionWidget(DetailedSearchFieldsSelectionWidget source,
            final String idSuffix, final EntityKind entityKind)
    {
        this(source.viewContext, idSuffix, entityKind);
        ListStore<DetailedSearchFieldComboModel> sourceStore = source.getStore();
        sourceStore.clearFilters();
        this.setValue(findAnyFieldItem(sourceStore.getModels()));
        setWidth(WIDTH);
        setStore(sourceStore);
        dataLoaded = true;
    }

    private final class ListPropertyTypesCallback extends
            AbstractAsyncCallback<TypedTableResultSet<PropertyType>>
    {
        ListPropertyTypesCallback(final IViewContext<ICommonClientServiceAsync> viewContext)
        {
            super(viewContext);
        }

        @Override
        protected void process(final TypedTableResultSet<PropertyType> result)
        {
            ResultSet<TableModelRowWithObject<PropertyType>> resultSet = result.getResultSet();
            resultSetKey = resultSet.getResultSetKey();
            List<TableModelRowWithObject<PropertyType>> rows =
                    result.getResultSet().getList().extractOriginalObjects();
            propertyTypes = new ArrayList<PropertyType>();
            for (TableModelRowWithObject<PropertyType> row : rows)
            {
                propertyTypes.add(row.getObjectOrNull());
            }

            final ListStore<DetailedSearchFieldComboModel> propertyTypeStore = getStore();
            propertyTypeStore.removeAll();
            List<DetailedSearchFieldComboModel> items = convertItems(propertyTypes);
            propertyTypeStore.add(items);
            if (propertyTypeStore.getCount() > 0)
            {
                setEmptyText(viewContext.getMessage(Dict.COMBO_BOX_CHOOSE, CHOOSE_SUFFIX));
                setReadOnly(false);
            } else
            {
                setEmptyText(viewContext.getMessage(Dict.COMBO_BOX_EMPTY, EMPTY_RESULT_SUFFIX));
                setReadOnly(true);
            }
            setValue(findAnyFieldItem(items));

            dataLoaded = true;
            removeResultSetFromCache();
        }
    }

    private static DetailedSearchFieldComboModel findAnyFieldItem(
            List<DetailedSearchFieldComboModel> items)
    {
        return findFieldItem(items, DetailedSearchFieldKind.ANY_FIELD);
    }

    private static DetailedSearchFieldComboModel findFieldItem(
            List<DetailedSearchFieldComboModel> items, DetailedSearchFieldKind fieldKind)
    {
        for (DetailedSearchFieldComboModel item : items)
        {
            DetailedSearchField field = item.getField();
            if (field.getKind() == fieldKind)
            {
                return item;
            }

        }
        throw new IllegalStateException("field not found: " + fieldKind);
    }

    @Override
    protected List<DetailedSearchFieldComboModel> convertItems(List<PropertyType> types)
    {
        final List<DetailedSearchFieldComboModel> result =
                new ArrayList<DetailedSearchFieldComboModel>();
        for (IAttributeSearchFieldKind attributeFieldKind : getAllAttributeFieldKinds(entityKind))
        {
            DetailedSearchField attributeField =
                    DetailedSearchField.createAttributeField(attributeFieldKind);
            addAttributeFieldComboModel(result, attributeField, attributeFieldKind);
        }
        Collections.sort(types);

        List<String> allEntityPropertyCodes = addEntityPropertyTypes(result, types);

        DetailedSearchField anyPropertyField =
                DetailedSearchField.createAnyPropertyField(allEntityPropertyCodes);
        addComplexFieldComboModel(result, anyPropertyField);

        DetailedSearchField anyField = DetailedSearchField.createAnyField(allEntityPropertyCodes);
        addComplexFieldComboModel(result, anyField);

        Iterator<DetailedSearchFieldComboModel> iterator = result.iterator();
        while (iterator.hasNext())
        {
            DetailedSearchFieldComboModel field = iterator.next();
            ISearchFieldAvailability availability =
                    field.getKind() != null ? field.getKind().getAvailability() : null;

            if (availability != null
                    && availability.isAvailable(field.getField(), getPerson(), getPersonRoles()) == false)
            {
                iterator.remove();
            }
        }

        return result;
    }

    private static IAttributeSearchFieldKind[] getAllAttributeFieldKinds(EntityKind entityKind)
    {
        return AttributeSearchFieldKindProvider.getAllAttributeFieldKinds(entityKind);
    }

    private void addComplexFieldComboModel(List<DetailedSearchFieldComboModel> result,
            DetailedSearchField field)
    {
        assert field.getKind() != DetailedSearchFieldKind.ATTRIBUTE : "attribute field not allowed";
        result.add(createComplexFieldComboModel(field));
    }

    private void addAttributeFieldComboModel(List<DetailedSearchFieldComboModel> result,
            DetailedSearchField attributeField, IAttributeSearchFieldKind attributeFieldKind)
    {
        assert attributeField.getKind() == DetailedSearchFieldKind.ATTRIBUTE : "attribute field required";
        result.add(createAttributeFieldComboModel(viewContext, attributeField, attributeFieldKind));
    }

    private static DetailedSearchFieldComboModel createComplexFieldComboModel(
            DetailedSearchField complexField)
    {
        assert complexField.getKind() != DetailedSearchFieldKind.ATTRIBUTE : "attribute field not allowed";
        return new DetailedSearchFieldComboModel(getDisplayName(complexField), complexField, null);
    }

    private static DetailedSearchFieldComboModel createAttributeFieldComboModel(
            IViewContext<ICommonClientServiceAsync> viewContext, DetailedSearchField attributeField, 
            IAttributeSearchFieldKind attributeFieldKind)
    {
        assert attributeField.getKind() == DetailedSearchFieldKind.ATTRIBUTE : "attribute field required";
        return new DetailedSearchFieldComboModel(getDisplayName(viewContext, attributeFieldKind),
                attributeField, attributeFieldKind);
    }

    private List<String> addEntityPropertyTypes(final List<DetailedSearchFieldComboModel> result,
            List<PropertyType> allPropertyTypes)
    {
        List<PropertyType> relevantPropertyTypes =
                PropertyTypesFilterUtil.filterPropertyTypesForEntityKind(allPropertyTypes,
                        entityKind);
        return addPropertyTypes(result, relevantPropertyTypes);
    }

    // returns codes of added properties
    private static List<String> addPropertyTypes(final List<DetailedSearchFieldComboModel> result,
            List<PropertyType> types)
    {
        final List<String> allProps = new ArrayList<String>();
        for (final PropertyType st : types)
        {
            final String propertyCode = st.getCode();
            allProps.add(propertyCode);
            final DetailedSearchField field = DetailedSearchField.createPropertyField(propertyCode);
            final DetailedSearchFieldComboModel comboModel =
                    createPropertyComboModel(st, field, types);
            result.add(comboModel);
        }
        return allProps;
    }

    private static DetailedSearchFieldComboModel createPropertyComboModel(
            final PropertyType propertyType, DetailedSearchField searchField,
            List<PropertyType> types)
    {
        String prefix = getDisplayName(searchField);
        String property = PropertyTypeRenderer.getDisplayName(propertyType, types);
        String code = prefix + " \'" + property + "\'";
        return new DetailedSearchFieldComboModel(code, searchField, null);
    }

    private static String getDisplayName(DetailedSearchField complexField)
    {
        assert complexField.getKind() != DetailedSearchFieldKind.ATTRIBUTE : "attribute field not allowed";
        return complexField.getKind().getDescription();
    }

    static String getDisplayName(IViewContext<ICommonClientServiceAsync> viewContext, IAttributeSearchFieldKind attributefieldKind)
    {
        String description = attributefieldKind.getDescription();
        description = description.replace("Sample", viewContext.getMessage(Dict.SAMPLE));
        description = description.replace("Experiment", viewContext.getMessage(Dict.EXPERIMENT));
        return description;
    }

    @Override
    protected void loadData(AbstractAsyncCallback<List<PropertyType>> callback)
    {
        if (dataLoaded == false)
        {
            DefaultResultSetConfig<String, TableModelRowWithObject<PropertyType>> config =
                    DefaultResultSetConfig.createFetchAll();
            viewContext.getService().listPropertyTypes(config,
                    new ListPropertyTypesCallback(viewContext));
        }
        callback.ignore();
    }

    @Override
    public DatabaseModificationKind[] getRelevantModifications()
    {
        return DatabaseModificationKind.any(ObjectKind.PROPERTY_TYPE);
    }

    private Person getPerson()
    {
        return viewContext.getModel().getSessionContext().getUser().getUserPersonObject();
    }

    private PersonRoles getPersonRoles()
    {
        return viewContext.getModel().getSessionContext().getUser().getUserPersonRoles();
    }
}
