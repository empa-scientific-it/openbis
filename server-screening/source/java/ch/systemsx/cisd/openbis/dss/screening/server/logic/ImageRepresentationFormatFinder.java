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

package ch.systemsx.cisd.openbis.dss.screening.server.logic;

import java.util.List;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IImageRepresentationFormatSelectionCriterion;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageRepresentationFormat;

/**
 * Find {@link ImageRepresentationFormat} instances which fulfill some criteria.
 *
 * @author Franz-Josef Elmer
 */
public class ImageRepresentationFormatFinder
{
    private final IImageRepresentationFormatSelectionCriterion[] criteria;

    public ImageRepresentationFormatFinder(IImageRepresentationFormatSelectionCriterion... criteria)
    {
        if (criteria == null || criteria.length == 0)
        {
            throw new UserFailureException("No criterion specified.");
        }
        this.criteria = criteria;
    }

    public List<ImageRepresentationFormat> find(List<ImageRepresentationFormat> formats)
    {
        List<ImageRepresentationFormat> filteredFormats = formats;
        for (IImageRepresentationFormatSelectionCriterion criterion : criteria)
        {
            filteredFormats = criterion.getMatching(filteredFormats);
            if (filteredFormats.size() == 1)
            {
                break;
            }
        }
        return filteredFormats;
    }
}
