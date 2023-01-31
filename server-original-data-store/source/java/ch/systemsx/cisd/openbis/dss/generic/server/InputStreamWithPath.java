/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server;

import java.io.InputStream;

/**
 * Bean which holds an {@link InputStream} and a path.
 *
 * @author Franz-Josef Elmer
 */
public class InputStreamWithPath
{
    private final InputStream inputStream;

    private final String path;

    InputStreamWithPath(InputStream inputStream, String path)
    {
        this.inputStream = inputStream;
        this.path = path;
    }

    public InputStream getInputStream()
    {
        return inputStream;
    }

    public String getPath()
    {
        return path;
    }

}
