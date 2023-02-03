package ch.ethz.sis.filetransfer;
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

import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * @author pkupczyk
 */
public class TestDownloadItemId implements IDownloadItemId
{

    private static final long serialVersionUID = 1L;

    private String filePath;

    public TestDownloadItemId(Path filePath)
    {
        this.filePath = filePath.toString();
    }

    @Override
    public String getId()
    {
        return filePath;
    }

    public Path getFilePath()
    {
        return Paths.get(filePath);
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder().append(filePath).toHashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        if (obj == this)
        {
            return true;
        }
        if (obj.getClass() != getClass())
        {
            return false;
        }

        TestDownloadItemId other = (TestDownloadItemId) obj;
        return new EqualsBuilder().append(filePath, other.filePath).isEquals();
    }

    @Override
    public String toString()
    {
        return new ToString(this).append("filePath", filePath).toString();
    }

}
