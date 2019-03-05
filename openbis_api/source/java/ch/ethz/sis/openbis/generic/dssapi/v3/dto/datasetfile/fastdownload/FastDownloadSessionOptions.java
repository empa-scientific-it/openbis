/*
 * Copyright 2019 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.fastdownload;

import java.io.Serializable;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.ObjectToString;

/**
 * Options for fast downloading.
 * 
 * @author Franz-Josef Elmer
 */
public class FastDownloadSessionOptions implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Integer wishedNumberOfStreams;

    public Integer getWishedNumberOfStreams()
    {
        return wishedNumberOfStreams;
    }

    /**
     * Sets the wished number of streams for parallel downloading. The actual number can be less than the wished number.
     * 
     * @param wishedNumberOfStreams can be <code>null</code> if there is no wish (default behavior)
     * @return this instance.
     */
    public FastDownloadSessionOptions wishedNumberOfStreams(Integer wishedNumberOfStreams)
    {
        if (wishedNumberOfStreams != null && wishedNumberOfStreams <= 0)
        {
            throw new IllegalArgumentException("Wished number of streams must be > 0: " + wishedNumberOfStreams);
        }
        this.wishedNumberOfStreams = wishedNumberOfStreams;
        return this;
    }

    @Override
    public String toString()
    {
        return new ObjectToString(this).append("wishedNumberOfStreams", wishedNumberOfStreams).toString();
    }

}
