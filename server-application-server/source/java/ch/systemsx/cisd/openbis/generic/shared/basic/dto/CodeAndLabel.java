/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Value object which has a label and a normalized code. Normalized means that the original code arguments turn to upper case and any symbol which
 * isn't from A-Z or 0-9 is replaced by an underscore character.
 * 
 * @author Franz-Josef Elmer
 */
public class CodeAndLabel implements Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private String label;

    private String code;

    @SuppressWarnings("unused")
    // for GWT
    private CodeAndLabel()
    {
    }

    /**
     * Creates an instance for specified code and label. The code should be already be normalized.
     */
    public CodeAndLabel(String code, String label)
    {
        this.code = code;
        this.label = label.trim();
    }

    /**
     * Returns the label.
     */
    public final String getLabel()
    {
        return label;
    }

    /**
     * Returns the attribute.
     */
    public final String getCode()
    {
        return code;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj instanceof CodeAndLabel == false)
        {
            return false;
        }
        CodeAndLabel codeAndTitle = (CodeAndLabel) obj;
        return codeAndTitle.code.equals(code) && codeAndTitle.label.equals(label);
    }

    @Override
    public int hashCode()
    {
        return code.hashCode() * 37 + label.hashCode();
    }

    @Override
    public String toString()
    {
        return "<" + code + "> " + label;
    }

    // helper functions for convertions

    public static List<String> asLabels(List<CodeAndLabel> codesAndLabels)
    {
        final List<String> result = new ArrayList<String>();
        for (CodeAndLabel codeAndLabel : codesAndLabels)
        {
            result.add(codeAndLabel.getLabel());
        }
        return result;
    }

    public static List<String> asCodes(List<CodeAndLabel> codesAndLabels)
    {
        final List<String> result = new ArrayList<String>();
        for (CodeAndLabel codeAndLabel : codesAndLabels)
        {
            result.add(codeAndLabel.getCode());
        }
        return result;
    }

}
