/*
 * Copyright 2015 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Franz-Josef Elmer
 */
class MockCleaner implements IMultiDataSetArchiveCleaner
{
    private final List<File> deletedFiles = new ArrayList<File>();

    @Override
    public void delete(File file)
    {
        deletedFiles.add(file);
        file.delete();
    }

    @Override
    public String toString()
    {
        return deletedFiles.toString();
    }

}
