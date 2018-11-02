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
 * @author pkupczyk
 */
public class DownloadRange implements Serializable
{

    private static final long serialVersionUID = 1L;

    private int start;

    private int end;

    public DownloadRange(int start, int end)
    {
        if (start < 0)
        {
            throw new IllegalArgumentException("Start cannot be < 0");
        }

        if (end < 0)
        {
            throw new IllegalArgumentException("End cannot be < 0");
        }

        if (start > end)
        {
            throw new IllegalArgumentException("Start cannot be > end");
        }

        this.start = start;
        this.end = end;
    }

    public int getStart()
    {
        return start;
    }

    public int getEnd()
    {
        return end;
    }

    @Override
    public String toString()
    {
        return new ToString(this).append("start", start).append("end", end).toString();
    }

}