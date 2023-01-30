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

package ch.systemsx.cisd.openbis.generic.shared;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.util.Map;

/**
 * @author pkupczyk
 */
public interface ISessionWorkspaceProvider
{

    Map<String, File> getSessionWorkspaces();

    File getSessionWorkspace(String sessionToken);

    void deleteSessionWorkspace(String sessionToken);

    void write(String sessionToken, String relativePathToFile, InputStream inputStream) throws IOException;

    FileOutputStream getFileOutputStream(String sessionToken, String relativePathToFile) throws IOException;

    InputStream read(String sessionToken, String relativePathToFile) throws IOException;

    void delete(String sessionToken, String relativePathToFile) throws IOException;

}
