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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;

import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTypePropertyType;

/**
 * A {@link ComboBox} extension using simple strings for selecting section name of {@link EntityTypePropertyType}.
 * 
 * @author Piotr Buczek
 */
public final class SectionSelectionWidget extends SimpleComboBox<String>
{
    public static SectionSelectionWidget create(
            final IViewContext<ICommonClientServiceAsync> viewContext,
            List<? extends EntityTypePropertyType<?>> etpts)
    {
        final Set<String> sections = new LinkedHashSet<String>(); // linked set preserves order
        for (EntityTypePropertyType<?> currentETPT : etpts)
        {
            final String section = currentETPT.getSection();
            if (section != null)
            {
                sections.add(currentETPT.getSection());
            }
        }
        return new SectionSelectionWidget(viewContext.getMessage(Dict.SECTION), viewContext
                .getMessage(Dict.SECTION_TOOLTIP), viewContext.getMessage(Dict.COMBO_BOX_EMPTY,
                "sections"), viewContext.getMessage(Dict.COMBO_BOX_CHOOSE, "section"),
                new ArrayList<String>(sections));
    }

    private SectionSelectionWidget(final String fieldLabel, final String toolTip,
            final String emptyText, final String chooseText, final List<String> sections)
    {
        setFieldLabel(fieldLabel);
        GWTUtils.setToolTip(this, toolTip);
        GWTUtils.setupAutoWidth(this);
        if (sections.size() == 0)
        {
            setEmptyText(emptyText);
        } else
        {
            setEmptyText(chooseText);
        }
        add(sections);
    }

    @Override
    public String getSimpleValue()
    {
        return StringUtils.trimToNull(super.getSimpleValue());
    }
}
