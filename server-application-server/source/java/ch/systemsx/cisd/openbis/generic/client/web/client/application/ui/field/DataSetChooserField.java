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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.event.ComponentEvent;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.DataSetSearchHitGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.DisposableEntityChooser;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.AttributeSearchFieldKindProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriterion;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchField;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SearchCriteriaConnection;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;

/**
 * A field for selecting a data set from a list.
 * 
 * @author Izabela Adamczyk
 */
public class DataSetChooserField extends ChosenEntitySetter<AbstractExternalData>
{
    public static DataSetChooserField create(final String labelField, final boolean mandatory,
            final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        final DataSetChooserField field = new DataSetChooserField(mandatory, viewContext)
            {
                @Override
                protected void onTriggerClick(ComponentEvent ce)
                {
                    super.onTriggerClick(ce);
                    browse(viewContext, this);
                }
            };

        field.setFieldLabel(labelField);
        return field;
    }

    private static void browse(final IViewContext<ICommonClientServiceAsync> viewContext,
            final ChosenEntitySetter<AbstractExternalData> field)
    {
        ArrayList<DetailedSearchCriterion> criterionList = new ArrayList<DetailedSearchCriterion>();
        DetailedSearchCriteria searchCriteria = new DetailedSearchCriteria();
        searchCriteria.setConnection(SearchCriteriaConnection.MATCH_ANY);
        DetailedSearchCriterion searchCriterion =
                new DetailedSearchCriterion(
                        DetailedSearchField.createAttributeField(AttributeSearchFieldKindProvider
                                .getAttributeFieldKind(EntityKind.DATA_SET, "CODE")), "*");
        criterionList.add(searchCriterion);
        searchCriteria.setCriteria(criterionList);
        DisposableEntityChooser<TableModelRowWithObject<AbstractExternalData>> browser =
                DataSetSearchHitGrid.createWithInitialSearchCriteria(viewContext, searchCriteria,
                        true);
        new EntityChooserDialog<TableModelRowWithObject<AbstractExternalData>>(browser,
                new IChosenEntitiesSetter<TableModelRowWithObject<AbstractExternalData>>()
                    {

                        @Override
                        public void setChosenEntities(List<TableModelRowWithObject<AbstractExternalData>> row)
                        {
                            field.setChosenEntities(TableModelRowWithObject.getObjects(row));
                        }
                    }, "Choose data set", viewContext).show();
    }

    @Override
    public String renderEntity(AbstractExternalData entity)
    {
        return entity.getCode();
    }

    private DataSetChooserField(boolean mandatory,
            IViewContext<ICommonClientServiceAsync> viewContext)
    {
        FieldUtil.setMandatoryFlag(this, mandatory);
        setValidateOnBlur(true);
        setAutoValidate(true);
    }

}
