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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property_type;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.widget.form.ComboBox;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.SimplifiedBaseModelData;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.PropertyTypeRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.DropDownList;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;

/**
 * {@link ComboBox} containing list of property type codes assigned to specified entity type loaded from the server.
 * 
 * @author Piotr Buczek
 */
public final class EntityTypePropertyTypeSelectionWidget
        extends
        DropDownList<EntityTypePropertyTypeSelectionWidget.EntityTypePropertyTypeComboModel, EntityTypePropertyType<?>>
{
    public static final String TOP_ITEM_CODE = "(top)";

    private static final String EMPTY_RESULT_SUFFIX = "assigned property types";

    private static final String CHOOSE_SUFFIX = "assigned property type";

    private static final String SUFFIX = "entity-type-property-type";

    static class EntityTypePropertyTypeComboModel extends SimplifiedBaseModelData
    {
        private static final long serialVersionUID = 1L;

        private static final String ORDINAL = "ordinal";

        public EntityTypePropertyTypeComboModel(EntityTypePropertyType<?> entity)
        {
            set(ModelDataPropertyNames.CODE, entity == null ? TOP_ITEM_CODE : entity
                    .getPropertyType().getCode());
            set(ModelDataPropertyNames.CODE_WITH_LABEL, entity == null ? TOP_ITEM_CODE
                    : getDisplayName(entity));
            set(ORDINAL, entity == null ? 0L : entity.getOrdinal());
            set(ModelDataPropertyNames.TOOLTIP, entity == null ? null : getTooltip(entity));
            set(ModelDataPropertyNames.OBJECT, entity);
        }

        private Object getTooltip(EntityTypePropertyType<?> entity)
        {
            return PropertyTypeRenderer.renderAsTooltip(entity.getPropertyType(), entity
                    .getSection());
        }

        private String getDisplayName(EntityTypePropertyType<?> entity)
        {
            return (entity.getSection() != null ? entity.getSection() + ": " : "")
                    + entity.getPropertyType().getLabel();
        }

        public Long getOrdinal()
        {
            return get(ORDINAL);
        }
    }

    public EntityTypePropertyTypeSelectionWidget(
            final IViewContext<ICommonClientServiceAsync> viewContext, final String idSuffix,
            List<EntityTypePropertyType<?>> etpts, String initialValueOrNull)
    {
        super(viewContext, SUFFIX + idSuffix, Dict.POSITION_AFTER,
                ModelDataPropertyNames.CODE_WITH_LABEL, CHOOSE_SUFFIX, EMPTY_RESULT_SUFFIX);
        setETPTs(etpts);
        selectInitialValue(initialValueOrNull);
        setTemplate(GWTUtils.getTooltipTemplate(ModelDataPropertyNames.CODE_WITH_LABEL,
                ModelDataPropertyNames.TOOLTIP));
    }

    private void setETPTs(List<EntityTypePropertyType<?>> etpts)
    {
        final List<EntityTypePropertyTypeComboModel> models =
                new ArrayList<EntityTypePropertyTypeComboModel>();
        models.addAll(convertItems(etpts));
        updateStore(models);
        getPropertyEditor().setList(store.getModels());
    }

    private void selectInitialValue(String initialValueOrNull)
    {
        if (initialValueOrNull != null)
        {
            trySelectByCode(initialValueOrNull);
            updateOriginalValue();
        }
    }

    public void trySelectByCode(String termCode)
    {
        GWTUtils.setSelectedItem(this, ModelDataPropertyNames.CODE, termCode);
    }

    public final Long getSelectedEntityTypePropertyTypeOrdinal()
    {
        final EntityTypePropertyTypeComboModel selectedItem = getValue();
        assert selectedItem != null;
        return selectedItem.getOrdinal();
    }

    @Override
    protected List<EntityTypePropertyTypeComboModel> convertItems(
            List<EntityTypePropertyType<?>> etpts)
    {
        final List<EntityTypePropertyTypeComboModel> result =
                new ArrayList<EntityTypePropertyTypeComboModel>();
        for (final EntityTypePropertyType<?> etpt : etpts)
        {
            result.add(new EntityTypePropertyTypeComboModel(etpt));
        }
        return result;
    }

    @Override
    protected void loadData(AbstractAsyncCallback<List<EntityTypePropertyType<?>>> callback)
    {
        // nothing to do - list was injected in constructor
        callback.ignore();
    }

    @Override
    public DatabaseModificationKind[] getRelevantModifications()
    {
        return DatabaseModificationKind.any(ObjectKind.PROPERTY_TYPE);
    }
}
