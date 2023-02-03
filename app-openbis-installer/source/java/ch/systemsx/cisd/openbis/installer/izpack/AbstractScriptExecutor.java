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

package ch.systemsx.cisd.openbis.installer.izpack;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.handler.AbstractUIHandler;
import com.izforge.izpack.data.PanelAction;

/**
 * Abstract class that can execute admin scripts as part of the installation.
 * 
 * @author Kaloyan Enimanev
 */
public abstract class AbstractScriptExecutor implements PanelAction
{
    protected static final String INSTALL_BIN_PATH_VARNAME = "INSTALL_BIN_PATH";

    @Override
    public final synchronized void executeAction(AutomatedInstallData data, AbstractUIHandler handler)
    {
        try
        {
            executeActionWithHandler(data, handler);
        } catch (Exception ex)
        {
            ex.printStackTrace();
            handler.emitErrorAndBlockNext("Error", ex.toString());
        }
    }
    
    protected void executeActionWithHandler(AutomatedInstallData data, AbstractUIHandler handler)
    {
        executeAction(data);
    }

    protected void executeAction(AutomatedInstallData data)
    {
    }

    protected String getAdminScript(AutomatedInstallData data, String scriptFileName)
    {
        File adminScriptFile = getAdminScriptFile(data, scriptFileName);
        return adminScriptFile.getAbsolutePath();
    }

    protected File getAdminScriptFile(AutomatedInstallData data, String scriptFileName)
    {
        return new File(data.getVariable(INSTALL_BIN_PATH_VARNAME), scriptFileName);
    }

    protected void executeAdminScript(Map<String, String> customEnv, String... command)
    {
        executeAdminScript(customEnv, System.out, System.err, command);
    }

    protected void executeAdminScript(Map<String, String> customEnv, OutputStream out,
            OutputStream err, String... command)
    {
        ProcessBuilder pb = new ProcessBuilder(command);
        Map<String, String> environment = pb.environment();
        environment.putAll(System.getenv());
        if (customEnv != null)
        {
            Set<Entry<String, String>> entrySet = customEnv.entrySet();
            for (Entry<String, String> entry : entrySet)
            {
                String key = entry.getKey();
                String value = entry.getValue();
                if (key != null)
                {
                    if (value != null)
                    {
                        environment.put(key,  value);
                    } else
                    {
                        System.out.println("Warning: custom environment variable '" + key + "' is null.");
                    }
                }
            }
        }
        try
        {
            ByteArrayOutputStream stdOutput = new ByteArrayOutputStream();
            ByteArrayOutputStream errOutput = new ByteArrayOutputStream();
            Process process = pb.start();
            pipe(process.getErrorStream(), new DelegatingOutputStream(errOutput, err));
            pipe(process.getInputStream(), new DelegatingOutputStream(stdOutput, out));
            int returnValue = process.waitFor();
            if (returnValue != 0)
            {
                System.err.println("Executing of command " + pb.command() + " has failed. Aborting ...");
                throw new RuntimeException("Executing of command " + pb.command() + " has failed:\n"
                        + "Exit value: " + returnValue + "\n"
                        + "-------- Std out ---------\n" + stdOutput + "\n"
                        + "-------- Err out ---------\n" + errOutput);
            }
        } catch (Exception e)
        {
            e.printStackTrace();
            if (e instanceof RuntimeException)
            {
                throw (RuntimeException) e;
            }
            throw new RuntimeException("Error executing " + command[0] + ": " + e.getMessage(), e);
        }
    }

    private void pipe(final InputStream src, final OutputStream dest)
    {
        new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        byte[] buffer = new byte[1024];
                        for (int n = 0; n != -1; n = src.read(buffer))
                        {
                            dest.write(buffer, 0, n);
                        }
                    } catch (IOException e)
                    {
                    }
                }
            }).start();
    }

    private static final class DelegatingOutputStream extends OutputStream
    {
        private OutputStream[] outputStreams;

        DelegatingOutputStream(OutputStream... outputStreams)
        {
            this.outputStreams = outputStreams;
        }

        @Override
        public void write(int b) throws IOException
        {
            for (OutputStream outputStream : outputStreams)
            {
                outputStream.write(b);
            }
        }

    }

}
