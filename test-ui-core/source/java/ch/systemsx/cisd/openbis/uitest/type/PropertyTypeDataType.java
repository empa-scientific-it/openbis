/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.uitest.type;

import ch.systemsx.cisd.openbis.uitest.widget.Checkbox;
import ch.systemsx.cisd.openbis.uitest.widget.DropDown;
import ch.systemsx.cisd.openbis.uitest.widget.Fillable;
import ch.systemsx.cisd.openbis.uitest.widget.Text;
import ch.systemsx.cisd.openbis.uitest.widget.TextArea;
import ch.systemsx.cisd.openbis.uitest.widget.Widget;

/**
 * @author anttil
 */
public enum PropertyTypeDataType
{
    BOOLEAN("BOOLEAN", Checkbox.class),
    HYPERLINK("HYPERLINK", Text.class),
    INTEGER("INTEGER", Text.class),
    MATERIAL("MATERIAL", Text.class),
    MULTILINE_VARCHAR("MULTILINE_VARCHAR", TextArea.class),
    REAL("REAL", Text.class),
    TIMESTAMP("TIMESTAMP", Text.class),
    VARCHAR("VARCHAR", Text.class),
    XML("XML", TextArea.class),
    CONTROLLED_VOCABULARY("CONTROLLEDVOCABULARY", DropDown.class);

    private String name;

    private Class<?> widgetClass;

    private <T extends Widget & Fillable> PropertyTypeDataType(String name,
            Class<T> widgetClass)
    {
        this.name = name;
        this.widgetClass = widgetClass;
    }

    public String getName()
    {
        return this.name;
    }

    @SuppressWarnings("unchecked")
    public <T extends Widget> Class<T> representedAs()
    {
        return (Class<T>) widgetClass;
    }
}
