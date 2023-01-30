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

import com.extjs.gxt.ui.client.event.ComponentEvent;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.DisposableEntityChooser;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.material.MaterialBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.lang.StringEscapeUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;

/**
 * A field for selecting a material from a list or by specifying code and type.
 * 
 * @author Tomasz Pylak
 * @author Izabela Adamczyk
 */
public class MaterialChooserField extends ChosenEntitySetter<TableModelRowWithObject<Material>>
{
    public static MaterialChooserField create(final String labelField, final boolean mandatory,
            final MaterialType materialTypeOrNull, String initialValueOrNull,
            final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        final MaterialChooserField chosenMaterialField =
                new MaterialChooserField(mandatory, materialTypeOrNull, initialValueOrNull,
                        viewContext)
                    {
                        @Override
                        protected void onTriggerClick(ComponentEvent ce)
                        {
                            super.onTriggerClick(ce);
                            browseMaterials(viewContext, this, materialTypeOrNull);
                        }
                    };

        chosenMaterialField.setFieldLabel(labelField);
        return chosenMaterialField;
    }

    private static void browseMaterials(final IViewContext<ICommonClientServiceAsync> viewContext,
            final ChosenEntitySetter<TableModelRowWithObject<Material>> chosenMaterialField,
            MaterialType materialTypeOrNull)
    {
        DisposableEntityChooser<TableModelRowWithObject<Material>> materialBrowser =
                MaterialBrowserGrid.create(viewContext, materialTypeOrNull);
        String title = viewContext.getMessage(Dict.TITLE_CHOOSE_MATERIAL);
        new EntityChooserDialog<TableModelRowWithObject<Material>>(materialBrowser,
                chosenMaterialField, title, viewContext).show();
    }

    // ------------------

    // the pattern used to validate material identifier expression
    private final static String CODE_AND_TYPE_PATTERN = "^[^\\s]+ \\(" + CodeField.CODE_CHARS
            + "\\)$";

    @Override
    public String renderEntity(TableModelRowWithObject<Material> materialOrNull)
    {
        String identifier = materialOrNull.getObjectOrNull().getIdentifier();
        return StringEscapeUtils.unescapeHtml(identifier);
    }

    private MaterialChooserField(boolean mandatory, MaterialType materialTypeOrNull,
            String initialValueOrNull, IViewContext<ICommonClientServiceAsync> viewContext)
    {
        FieldUtil.setMandatoryFlag(this, mandatory);
        setValidateOnBlur(true);
        setAutoValidate(true);
        setRegex(CODE_AND_TYPE_PATTERN);
        getMessages().setRegexText(viewContext.getMessage(Dict.INCORRECT_MATERIAL_SYNTAX));
        if (initialValueOrNull != null)
        {
            setValue(initialValueOrNull);
        }
    }

}
