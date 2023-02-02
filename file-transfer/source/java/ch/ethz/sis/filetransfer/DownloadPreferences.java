package ch.ethz.sis.filetransfer;

import java.io.Serializable;

/*
 * Copyright 2018 ETH Zuerich, CISD
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

/**
 * Additional download preferences, e.g. can be used to specify a wished number of download streams. Increasing the number of download streams usually
 * increases the overall download performance, still it requires additional resources at both download client and download server sides. Therefore, a
 * server may ignore a wish and permit fewer streams than requested (e.g. in case it is under a heavy load).
 * 
 * @author pkupczyk
 */
public class DownloadPreferences implements Serializable
{

    private static final long serialVersionUID = 1L;

    private Integer wishedNumberOfStreams;

    public DownloadPreferences()
    {
    }

    public DownloadPreferences(Integer wishedNumberOfStreams)
    {
        if (wishedNumberOfStreams != null && wishedNumberOfStreams <= 0)
        {
            throw new IllegalArgumentException("Wished number of streams must be > 0");
        }

        this.wishedNumberOfStreams = wishedNumberOfStreams;
    }

    public Integer getWishedNumberOfStreams()
    {
        return wishedNumberOfStreams;
    }

    @Override
    public String toString()
    {
        return new ToString(this).append("wishedNumberOfStreams", getWishedNumberOfStreams()).toString();
    }

}