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

import java.util.List;

import com.extjs.gxt.ui.client.widget.form.AdapterField;
import com.google.gwt.user.client.ui.ListBox;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.VocabularyTerm;

/**
 * A {@link AdapterField} extension for selecting a vocabulary term from a list.
 * 
 * @author Christian Ribeaud
 */
public final class ControlledVocabullaryField extends AdapterField
{

    public ControlledVocabullaryField(final String labelField, final boolean mandatory,
            final List<VocabularyTerm> terms)
    {
        super(createListBox(terms, mandatory));
        setFieldLabel(labelField);
        if (mandatory)
        {
            setLabelSeparator(GenericConstants.MANDATORY_LABEL_SEPARATOR);
        }
    }

    private static final ListBox createListBox(final List<VocabularyTerm> terms,
            final boolean mandatory)
    {
        final ListBox box = new ListBox();
        if (mandatory == false)
        {
            box.addItem(GWTUtils.NONE_LIST_ITEM);
        }
        for (final VocabularyTerm term : terms)
        {
            box.addItem(term.getCode());
        }
        return box;
    }

    //
    // AdapterField
    //

    @Override
    public final Object getValue()
    {
        final String stringValue = super.getValue().toString();
        if (GWTUtils.NONE_LIST_ITEM.equals(stringValue))
        {
            return null;
        }
        return stringValue;
    }

}