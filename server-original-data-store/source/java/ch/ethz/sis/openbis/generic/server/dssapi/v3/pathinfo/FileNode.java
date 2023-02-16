/*
 * Copyright ETH 2017 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.server.dssapi.v3.pathinfo;

class FileNode implements DataSetContentNode
{
    private final String fullPath;

    private final long length;

    private final Integer checksumCRC32;

    private final String checksum;

    public FileNode(String parentPath, String name, long length, Integer checksumCRC32, String checksum)
    {
        this.checksum = checksum;
        if (parentPath == null)
        {
            this.fullPath = name;
        } else
        {
            this.fullPath = parentPath + "/" + name;
        }
        this.length = length;
        this.checksumCRC32 = checksumCRC32;
    }

    @Override
    public long getLength()
    {
        return length;
    }

    @Override
    public Integer getChecksumCRC32()
    {
        return checksumCRC32;
    }

    @Override
    public String getChecksum()
    {
        return checksum;
    }

    @Override
    public String getFullPath()
    {
        return this.fullPath;
    }

    @Override
    public boolean isDirectory()
    {
        return false;
    }

}
