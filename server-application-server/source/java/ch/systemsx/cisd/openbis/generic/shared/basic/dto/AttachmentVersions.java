/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * Encapsulation of all versions of an {@link Attachment} file.
 * 
 * @author Piotr Buczek
 */
public class AttachmentVersions implements Comparable<AttachmentVersions>, Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    /** sorted list of all versions of an {@link Attachment} file */
    private List<Attachment> versions;

    private Attachment current;

    public AttachmentVersions(List<Attachment> versions)
    {
        assert versions != null : "versions not set!";
        assert versions.isEmpty() == false : "versions empty!";
        Collections.sort(versions);
        this.versions = versions;
        this.current = versions.get(versions.size() - 1);
    }

    @Override
    public int compareTo(final AttachmentVersions o)
    {
        return getCurrent().getFileName().compareTo(o.getCurrent().getFileName());
    }

    public Attachment getCurrent()
    {
        return current;
    }

    public List<Attachment> getVersions()
    {
        return versions;
    }

    public String getPermlink()
    {
        return getCurrent().getPermlink();
    }

    public String getLatestVersionPermlink()
    {
        return getCurrent().getLatestVersionPermlink();
    }

    // serialization

    @SuppressWarnings("unused")
    private AttachmentVersions()
    {

    }

    @SuppressWarnings("unused")
    private void setCurrent(Attachment current)
    {
        this.current = current;
    }

    @SuppressWarnings("unused")
    private void setVersions(List<Attachment> versions)
    {
        this.versions = versions;
    }

}
