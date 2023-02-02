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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * Criterion based on the color depth.
 *
 * @author Franz-Josef Elmer
 */
@JsonObject("ColorDepthCriterion")
public class ColorDepthCriterion extends AbstractFormatSelectionCriterion
{
    private static final long serialVersionUID = 1L;

    private final Set<Integer> colorDepth;

    /**
     * Creates an instance which allows all image representation formats with specified color depths.
     */
    public ColorDepthCriterion(Integer... colorDepths)
    {
        this.colorDepth = new HashSet<Integer>(Arrays.asList(colorDepths));
    }

    @Override
    protected boolean accept(ImageRepresentationFormat format)
    {
        return colorDepth.contains(format.getColorDepth());
    }

}
