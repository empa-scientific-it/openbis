/*
 * Copyright ETH 2015 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.systemtest.deletion;

import java.util.Date;

class Change implements Comparable<Change>
{
    public final Date time;

    public final String userId;

    public final String value;

    public final boolean isRemoval;

    public final String key;

    public final String attachmentContent;

    public Change(Date time, String key, String userId, String value, String attachmentContent, boolean isRemoval)
    {
        this.time = time;
        this.key = key;
        this.userId = userId;
        this.value = value;
        this.attachmentContent = attachmentContent;
        this.isRemoval = isRemoval;
    }

    @Override
    public int compareTo(Change other)
    {
        if (this.time == null)
        {
            return other.time == null ? 0 : Long.compare(Long.MIN_VALUE, other.time.getTime());
        }
        int cmp = this.time.compareTo(other.time);
        if (cmp == 0)
        {
            if (this.isRemoval == other.isRemoval)
            {
                return 0;
            } else if (this.isRemoval)
            {
                return -1;
            } else
            {
                return 1;
            }
        } else
        {
            return cmp;
        }
    }

    @Override
    public String toString()
    {
        return key + " = " + value + " (" + time + ", removal " + isRemoval + ", userId: " + userId + ")";
    }
}