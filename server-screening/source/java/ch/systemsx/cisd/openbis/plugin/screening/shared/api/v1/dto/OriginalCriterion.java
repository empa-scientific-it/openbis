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

package ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * Criterion which filters image representation formats concerning whether they are original or not.
 *
 * @author Franz-Josef Elmer
 */
@JsonObject("OriginalCriterion")
public class OriginalCriterion extends AbstractFormatSelectionCriterion
{
    private static final long serialVersionUID = 1L;

    private final boolean original;

    /**
     * Creates an instance based on the specified flag. If the flag is <code>true</code> all original images fulfill this criterion. If the flag is
     * <code>false</code> all images which are not original (e.g. thumbnails) fulfill this criterion.
     */
    public OriginalCriterion(boolean original)
    {
        this.original = original;
    }

    @Override
    protected boolean accept(ImageRepresentationFormat format)
    {
        return format.isOriginal() == original;
    }

}
