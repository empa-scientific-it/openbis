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
 * Wraps raw score, bit score and evalue of a BLAST search result.
 *
 * @author Franz-Josef Elmer
 */
@JsonObject("BlastScore")
public class BlastScore implements ISearchDomainResultScore
{
    private static final long serialVersionUID = 1L;

    private double score;

    private double bitScore;

    private double evalue;

    @Override
    public double getScore()
    {
        return score;
    }

    public void setScore(double score)
    {
        this.score = score;
    }

    public double getBitScore()
    {
        return bitScore;
    }

    public void setBitScore(double bitScore)
    {
        this.bitScore = bitScore;
    }

    public double getEvalue()
    {
        return evalue;
    }

    public void setEvalue(double evalue)
    {
        this.evalue = evalue;
    }

    @Override
    public String toString()
    {
        return "Score: " + score + ", bit score: " + bitScore + ", evalue: " + evalue;
    }

}
