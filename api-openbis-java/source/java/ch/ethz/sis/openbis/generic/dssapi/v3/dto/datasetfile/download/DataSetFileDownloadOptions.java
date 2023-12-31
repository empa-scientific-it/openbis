/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.download;

import java.io.Serializable;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.ObjectToString;

/**
 * @author pkupczyk
 */
public class DataSetFileDownloadOptions implements Serializable
{

    private static final long serialVersionUID = 1L;

    private boolean recursive = false;

    public boolean isRecursive()
    {
        return recursive;
    }

    public void setRecursive(boolean recursive)
    {
        this.recursive = recursive;
    }

    @Override
    public String toString()
    {
        return new ObjectToString(this).append("recursive", recursive).toString();
    }

}
