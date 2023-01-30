/*
 * Copyright 2019 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.generic.server.jython.api.v1.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * Helper class to be used in initialize-master-data.py.
 *
 * @author Franz-Josef Elmer
 */
public class MasterDataRegistrationHelper {
    private static Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, MasterDataRegistrationHelper.class);

    private File masterDataFolder;

    public MasterDataRegistrationHelper(Collection<?> systemPaths) {
        for (Object systemPath : systemPaths) {
            if (systemPath != null) {
                String systemPathString = String.valueOf(systemPath);
                if (systemPathString.contains("core-plugins")) {
                    masterDataFolder = new File(new File(systemPathString), "master-data");
                    if (masterDataFolder.exists() == false) {
                        throw new IllegalArgumentException("Folder does not exist: " + masterDataFolder.getAbsolutePath());
                    }
                    if (masterDataFolder.isFile()) {
                        throw new IllegalArgumentException("Is not a folder but a file: " + masterDataFolder.getAbsolutePath());
                    }
                    operationLog.info("Master data folder: " + masterDataFolder.getAbsolutePath());
                    return;
                }
            }
        }
        throw new IllegalArgumentException("Does not contain path to the core plugin: " + systemPaths);
    }

    public List<byte[]> listXlsByteArrays() {
        List<byte[]> result = new ArrayList<>();
        for (File file : masterDataFolder.listFiles()) {
            String name = file.getName();
            if (name.endsWith(".xls") || name.endsWith(".xlsx")) {
                operationLog.info("load master data " + file.getName());
                result.add(FileUtilities.loadToByteArray(file));
            }
        }
        return result;
    }

    public List<byte[]> getByteArray(String findName) {
        for (File file : masterDataFolder.listFiles()) {
            String name = file.getName();
            if (name.equals(findName)) {
                operationLog.info("load master data " + file.getName());
                return Arrays.asList(FileUtilities.loadToByteArray(file));
            }
        }
        return null;
    }

    public List<byte[]> listCsvByteArrays() throws IOException {
        List<byte[]> result = new ArrayList<>();
        for (File file : masterDataFolder.listFiles()) {
            String name = file.getName();
            if (name.endsWith(".csv")) {
                operationLog.info("load master data " + file.getName());
                result.add(Files.readAllBytes(file.toPath()));
            }
        }
        return result;
    }

    public Map<String, String> getAllScripts() {
        Map<String, String> result = new TreeMap<>();
        File scriptsFolder = new File(masterDataFolder, "scripts");
        if (scriptsFolder.isDirectory()) {
            gatherScripts(result, scriptsFolder, scriptsFolder);
        }
        return result;
    }

    private void gatherScripts(Map<String, String> scripts, File rootFolder, File file) {
        if (file.isFile()) {
            String scriptPath = FileUtilities.getRelativeFilePath(rootFolder, file);
            scripts.put(scriptPath, FileUtilities.loadToString(file));
            operationLog.info("Script " + scriptPath + " loaded");
        }
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File child : files) {
                gatherScripts(scripts, rootFolder, child);
            }
        }
    }

}
