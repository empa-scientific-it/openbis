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

import java.io.Serializable;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * Alignment match between a sequence and a query.
 *
 * @author Franz-Josef Elmer
 */
@JsonObject("AlignmentMatch")
public class AlignmentMatch implements Serializable
{
    private static final long serialVersionUID = 1L;

    private int sequenceStart;

    private int sequenceEnd;

    private int queryStart;

    private int queryEnd;

    private int numberOfMismatches;

    private int totalNumberOfGaps;

    public int getSequenceStart()
    {
        return sequenceStart;
    }

    public void setSequenceStart(int sequenceStart)
    {
        this.sequenceStart = sequenceStart;
    }

    public int getSequenceEnd()
    {
        return sequenceEnd;
    }

    public void setSequenceEnd(int sequenceEnd)
    {
        this.sequenceEnd = sequenceEnd;
    }

    public int getQueryStart()
    {
        return queryStart;
    }

    public void setQueryStart(int queryStart)
    {
        this.queryStart = queryStart;
    }

    public int getQueryEnd()
    {
        return queryEnd;
    }

    public void setQueryEnd(int queryEnd)
    {
        this.queryEnd = queryEnd;
    }

    public int getNumberOfMismatches()
    {
        return numberOfMismatches;
    }

    public void setNumberOfMismatches(int numberOfMismatches)
    {
        this.numberOfMismatches = numberOfMismatches;
    }

    public int getTotalNumberOfGaps()
    {
        return totalNumberOfGaps;
    }

    public void setTotalNumberOfGaps(int totalNumberOfGaps)
    {
        this.totalNumberOfGaps = totalNumberOfGaps;
    }

    @Override
    public String toString()
    {
        return "alignment in sequence: [" + getSequenceStart() + "-" + getSequenceEnd() + "], "
                + "alignment in query: [" + getQueryStart() + "-" + getQueryEnd() + "], number of mismatches: "
                + getNumberOfMismatches() + ", total number of gaps: " + getTotalNumberOfGaps();
    }

}
