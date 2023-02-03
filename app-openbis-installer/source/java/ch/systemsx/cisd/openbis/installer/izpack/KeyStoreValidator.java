/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.installer.izpack;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.izforge.izpack.api.data.AutomatedInstallData;

/**
 * @author Franz-Josef Elmer
 */
public class KeyStoreValidator extends AbstractDataValidator
{

    @Override
    public Status validateData(AutomatedInstallData data)
    {
        List<File> keyStoreFiles = new ArrayList<File>();
        String keyStoreFilePath = data.getVariable(GlobalInstallationContext.KEY_STORE_FILE_VARNAME);
        if (keyStoreFilePath != null && keyStoreFilePath.length() > 0)
        {
            keyStoreFiles.add(new File(keyStoreFilePath));
        } else
        {
            File installDir = GlobalInstallationContext.installDir;
            addKeyStoreFileIfItExists(keyStoreFiles, Utils.getKeystoreFileForAS(installDir));
            addKeyStoreFileIfItExists(keyStoreFiles, Utils.getKeystoreFileForDSS(installDir));
        }
        String keyStorePassword = data.getVariable(GlobalInstallationContext.KEY_STORE_PASSWORD_VARNAME);
        String keyPassword = data.getVariable(GlobalInstallationContext.KEY_PASSWORD_VARNAME);
        for (File keyStoreFile : keyStoreFiles)
        {
            InputStream input = null;
            try
            {
                KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                input = new FileInputStream(keyStoreFile);
                keyStore.load(input, keyStorePassword.toCharArray());
                Enumeration<String> aliases = keyStore.aliases();
                while (aliases.hasMoreElements())
                {
                    String alias = aliases.nextElement();
                    keyStore.getKey(alias, keyPassword.toCharArray());
                }
            } catch (Exception ex)
            {
                setErrorMessage("Error for key store " + keyStoreFile.getPath() + ":" + ex.getMessage());
                return Status.ERROR;
            } finally
            {
                IOUtils.closeQuietly(input);
            }
        }
        return Status.OK;
    }

    private void addKeyStoreFileIfItExists(List<File> keyStoreFiles, File keyStoreFile)
    {
        if (keyStoreFile.isFile())
        {
            keyStoreFiles.add(keyStoreFile);
        }
    }

    @Override
    public String getErrorMessageId()
    {
        return getErrorMessage();
    }

    @Override
    public String getWarningMessageId()
    {
        return getErrorMessageId();
    }

    @Override
    public boolean getDefaultAnswer()
    {
        return true;
    }

}
