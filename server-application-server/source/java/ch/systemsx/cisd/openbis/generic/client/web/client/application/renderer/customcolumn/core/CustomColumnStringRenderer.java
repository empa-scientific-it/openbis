/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.customcolumn.core;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.customcolumn.extension.link.CustomColumnLinkRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.lang.StringEscapeUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.PrimitiveValue;

/**
 * @author pkupczyk
 */
public class CustomColumnStringRenderer implements GridCellRenderer<BaseEntityModel<?>>
{

    @Override
    public Object render(BaseEntityModel<?> model, String property, ColumnData config,
            int rowIndex, int colIndex, ListStore<BaseEntityModel<?>> store,
            Grid<BaseEntityModel<?>> grid)
    {
        Object object = model.get(property);
        if (object == null)
        {
            return "";
        } else
        {
            String escapedString = ((PrimitiveValue) object).toString();

            if (maybeJSON(escapedString))
            {
                String unescapedString = StringEscapeUtils.unescapeHtml(escapedString);

                CustomColumnJSONClientData json = parseJSON(unescapedString);
                if (json == null)
                {
                    return renderNonJSON(escapedString);
                } else
                {
                    return renderJSON(json);
                }

            } else
            {
                return renderNonJSON(escapedString);
            }

        }
    }

    private boolean maybeJSON(String string)
    {
        return string.charAt(0) == '{';
    }

    private CustomColumnJSONClientData parseJSON(String string)
    {
        return CustomColumnJSONParser.parse(string);
    }

    private Object renderNonJSON(String string)
    {
        return string;
    }

    private Object renderJSON(CustomColumnJSONClientData data)
    {
        switch (data.getMethod())
        {
            case LINK:
                return new CustomColumnLinkRenderer(data).render();
            default:
                throw new IllegalArgumentException("No render defined for custom column method: "
                        + data.getMethod());
        }
    }

}
