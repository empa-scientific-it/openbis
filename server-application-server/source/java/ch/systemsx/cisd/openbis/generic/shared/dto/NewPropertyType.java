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

package ch.systemsx.cisd.openbis.generic.shared.dto;

import ch.systemsx.cisd.common.parser.BeanProperty;
import ch.systemsx.cisd.openbis.generic.shared.IServer;

/**
 * <i>Bean</i> for registering new property type.
 * <p>
 * It is used by the parser. This explains the {@link BeanProperty} annotations.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public final class NewPropertyType extends Code<NewPropertyType>
{
    private static final long serialVersionUID = IServer.VERSION;

    public static final NewPropertyType[] EMPTY_ARRAY = new NewPropertyType[0];

    private String type;

    private String description;

    private String label;

    private String vocabularyCode;

    @BeanProperty(label = "data_type")
    public final void setType(final String type)
    {
        this.type = type;
    }

    @BeanProperty(label = "description")
    public final void setDescription(final String description)
    {
        this.description = description;
    }

    @BeanProperty(label = "label")
    public final void setLabel(final String label)
    {
        this.label = label;
    }

    @BeanProperty(optional = true, label = "vocabulary")
    public void setVocabularyCode(final String vocabularyCode)
    {
        this.vocabularyCode = vocabularyCode;
    }

    public final String getType()
    {
        return type;
    }

    public final String getDescription()
    {
        return description;
    }

    public final String getLabel()
    {
        return label;
    }

    public final String getVocabularyCode()
    {
        return vocabularyCode;
    }

    //
    // Code
    //

    @Override
    public final String toString()
    {
        return getCode();
    }
}
