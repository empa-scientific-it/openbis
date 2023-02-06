/*
 * Copyright 2014 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.generic.shared.api.v1.dto;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * Extension of {@link EntityPropertySearchResultLocation} for BLAST search. Matching sequence/query start and end are available.
 * 
 * @author Franz-Josef Elmer
 */
@JsonObject("EntityPropertyBlastSearchResultLocation")
public class EntityPropertyBlastSearchResultLocation extends EntityPropertySearchResultLocation
{
    private static final long serialVersionUID = 1L;

    private AlignmentMatch alignmentMatch;

    public AlignmentMatch getAlignmentMatch()
    {
        return alignmentMatch;
    }

    public void setAlignmentMatch(AlignmentMatch alignmentMatch)
    {
        this.alignmentMatch = alignmentMatch;
        setPosition(alignmentMatch.getSequenceStart());
    }

    @Override
    protected String appendToString()
    {
        return alignmentMatch.toString();
    }
}
