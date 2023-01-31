/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.archiveverifier.pathinfo;

/**
 * Single file entry in pathinfo db.
 * 
 * @author anttil
 */
public class PathInfoEntry
{

    private final Long crc;

    private final Long size;

    public PathInfoEntry(Long crc, Long size)
    {
        this.crc = crc;
        this.size = size;
    }

    public Long getCrc()
    {
        return this.crc;
    }

    public Long getSize()
    {
        return size;
    }
}
