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

package ch.systemsx.cisd.openbis.dss.generic.shared.utils;

import ch.systemsx.cisd.openbis.generic.shared.basic.CodeNormalizer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CodeAndLabel;

/**
 * Utility class to operate on codes and labels.
 * 
 * @author Tomasz Pylak
 */
public class CodeAndLabelUtil
{
    /**
     * Creates an instance from specified label with optional code prefix in form of <code>&lt;code&gt;</code>. The code will be normalized.
     */
    public static CodeAndLabel create(String labelWithOptionalCode)
    {
        String t = labelWithOptionalCode;
        String c = labelWithOptionalCode;
        if (labelWithOptionalCode.startsWith("<"))
        {
            int indexOfClosing = labelWithOptionalCode.indexOf('>');
            if (indexOfClosing > 0)
            {
                c = labelWithOptionalCode.substring(1, indexOfClosing).trim();
                t = labelWithOptionalCode.substring(indexOfClosing + 1).trim();
            }
        }
        String code = CodeNormalizer.normalize(c);
        String rest = t.trim();
        String label = rest.length() == 0 ? code : rest;
        return new CodeAndLabel(code, label);
    }

}
