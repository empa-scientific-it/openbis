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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import com.izforge.izpack.api.data.Info;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.data.PackFile;
import com.izforge.izpack.api.data.ResourceManager;
import com.izforge.izpack.api.exception.InstallerException;
import com.izforge.izpack.util.OsConstraintHelper;

/**
 * Helper class to extract a package from an openBIS self installing JAR.
 * 
 * @author Franz-Josef Elmer
 */
public class Extractor
{
    private static final class VariableResolver
    {
        private final List<String[]> substitutions = new ArrayList<String[]>();

        VariableResolver(String rootPath)
        {
            substitutions.add(new String[] { "$INSTALL_PATH/servers", rootPath });
            substitutions.add(new String[] { "$INSTALL_PATH", rootPath });
            substitutions.add(new String[] { "$INSTALL_TMPEXTRACT", rootPath });
            substitutions.add(new String[] { "$DATA_TMPEXTRACT", rootPath + "/datastore_server" });
        }

        public String resolve(String string)
        {
            String result = string;
            for (String[] pair : substitutions)
            {
                result = result.replace(pair[0], pair[1]);
            }
            return result;
        }
    }

    private static final class Parameters
    {
        private static final String MISSING_PARAMETER_ERROR = "Missing parameters.";

        static Parameters parse(String[] args)
        {
            Parameters parameters = new Parameters();
            if (args.length < 2)
            {
                return setErrorMessage(parameters, MISSING_PARAMETER_ERROR);
            }
            String shortPackageName = args[0];
            parameters.installationPath = args[1];
            if (args[0].equals("-q"))
            {
                parameters.quiet = true;
                if (args.length < 3)
                {
                    return setErrorMessage(parameters, MISSING_PARAMETER_ERROR);
                }
                shortPackageName = args[1];
                parameters.installationPath = args[2];
            }
            if (shortPackageName.equals("as"))
            {
                parameters.packageName = "openBIS Server";
            } else if (shortPackageName.equals("dss"))
            {
                parameters.packageName = "Datastore Server";
            } else if (shortPackageName.equals("scripts"))
            {
                parameters.packageName = "Administration Scripts";
            } else
            {
                parameters.errorMessage = "Unkown package '" + shortPackageName + "'.";
            }
            return parameters;
        }

        private static Parameters setErrorMessage(Parameters parameters, String errorMessage)
        {
            parameters.errorMessage = errorMessage;
            return parameters;
        }

        private boolean quiet;

        private String packageName;

        private String installationPath;

        private String errorMessage;

        boolean isQuiet()
        {
            return quiet;
        }

        String getPackageName()
        {
            return packageName;
        }

        String getInstallationPath()
        {
            return installationPath;
        }

        String getErrorMessageOrNull()
        {
            return errorMessage;
        }
    }

    private static final class ConsoleOutput
    {
        private final boolean quiet;

        ConsoleOutput(boolean quiet)
        {
            this.quiet = quiet;
        }

        void println(String message)
        {
            if (quiet == false)
            {
                System.out.println(message);
            }
        }
    }

    public static void main(String[] args) throws Exception
    {
        Parameters parameters = Parameters.parse(args);
        if (parameters.getErrorMessageOrNull() != null)
        {
            System.err.println("ERROR: " + parameters.getErrorMessageOrNull());
            System.err.println("Usage: java " + Extractor.class.getName()
                    + " [-q] as|dss|scripts <installation path>");
            System.exit(1);
        }

        ConsoleOutput out = new ConsoleOutput(parameters.isQuiet());
        VariableResolver variableResolver = new VariableResolver(parameters.getInstallationPath());
        ResourceManager resourceManager = ResourceManager.getInstance();
        Info info = (Info) readObject(resourceManager, "info");
        out.println(info.getAppName() + " Version " + info.getAppVersion());
        List<Pack> availablePacks = getAvailablePacks(resourceManager);
        for (Pack pack : availablePacks)
        {
            if (parameters.getPackageName().equals(pack.name))
            {
                out.println("Extract package '" + pack.name + "' (" + pack.nbytes + " bytes)");
                extractPackage(pack, info, resourceManager, variableResolver, out);
                break;
            }
        }
    }

    private static void extractPackage(Pack pack, Info info, ResourceManager resourceManager,
            VariableResolver variableResolver, ConsoleOutput out) throws Exception
    {
        ObjectInputStream objectInputStream =
                new ObjectInputStream(getPackAsStream(resourceManager, pack.id, info));
        try
        {

            int numberOfFiles = objectInputStream.readInt();
            for (int j = 0; j < numberOfFiles; j++)
            {
                PackFile packFile = (PackFile) objectInputStream.readObject();
                String targetPath = variableResolver.resolve(packFile.getTargetPath());
                out.println(targetPath);
                File target = new File(targetPath);
                if (packFile.isDirectory())
                {
                    target.mkdirs();
                    continue;
                }
                target.getParentFile().mkdirs();
                extractFile(packFile, objectInputStream, target, resourceManager);
            }
        } finally
        {
            objectInputStream.close();
        }
    }

    private static void extractFile(PackFile packFile, ObjectInputStream objectInputStream,
            File target, ResourceManager resourceManager) throws Exception
    {
        OutputStream outputStream = null;
        try
        {
            outputStream = new FileOutputStream(target);
            if (packFile.isPack200Jar())
            {
                throw new RuntimeException("Pack200 no longer supported");
//                int key = objectInputStream.readInt();
//                Pack200.Unpacker unpacker = Pack200.newUnpacker();
//                InputStream pack200Input = resourceManager.getInputStream("/packs/pack200-" + key);
//                java.util.jar.JarOutputStream jarOutputStream = null;
//                try
//                {
//                    jarOutputStream = new java.util.jar.JarOutputStream(outputStream);
//                    unpacker.unpack(pack200Input, jarOutputStream);
//                } finally
//                {
//                    if (jarOutputStream != null)
//                    {
//                        jarOutputStream.close();
//                    }
//                }
            } else
            {
                byte[] buffer = new byte[5120];
                long numberOfBytesAlreadyRead = 0;
                while (numberOfBytesAlreadyRead < packFile.length())
                {
                    numberOfBytesAlreadyRead =
                            writeBuffer(packFile, buffer, outputStream, objectInputStream,
                                    numberOfBytesAlreadyRead);
                }
            }
        } finally
        {
            if (outputStream != null)
            {
                outputStream.close();
            }
        }
    }

    private static List<Pack> getAvailablePacks(ResourceManager resourceManager) throws Exception
    {
        InputStream in = resourceManager.getInputStream("packs.info");
        ObjectInputStream objectInputStream = null;
        try
        {
            objectInputStream = new ObjectInputStream(in);
            int size = objectInputStream.readInt();
            List<Pack> availablePacks = new ArrayList<Pack>();
            for (int i = 0; i < size; i++)
            {
                Pack pack = (Pack) objectInputStream.readObject();
                if (OsConstraintHelper.oneMatchesCurrentSystem(pack.osConstraints))
                {
                    availablePacks.add(pack);
                }
            }
            return availablePacks;
        } finally
        {
            if (objectInputStream != null)
            {
                objectInputStream.close();
            }
        }
    }

    private static long writeBuffer(PackFile packFile, byte[] buffer, OutputStream out,
            InputStream inputStream, long numberOfBytesAlreadyRead) throws IOException
    {
        int maxBytes = (int) Math.min(packFile.length() - numberOfBytesAlreadyRead, buffer.length);
        int numberOfBytesRead = inputStream.read(buffer, 0, maxBytes);
        if (numberOfBytesRead == -1)
        {
            throw new IOException("Unexpected end of input stream.");
        }
        out.write(buffer, 0, numberOfBytesRead);
        return numberOfBytesAlreadyRead + numberOfBytesRead;
    }

    private static InputStream getPackAsStream(ResourceManager resourceManager, String packid,
            Info info) throws Exception
    {
        InputStream in;
        in = resourceManager.getInputStream("packs/pack-" + packid);
        if (in == null)
        {
            throw new IOException("Unkown package '" + packid + "'.");
        }
        String decoderClassName = info.getPackDecoderClassName();
        if (decoderClassName == null)
        {
            return in;
        }
        @SuppressWarnings("unchecked")
        Class<Object> decoder = (Class<Object>) Class.forName(decoderClassName);
        Class<?>[] paramsClasses = new Class[1];
        paramsClasses[0] = Class.forName("java.io.InputStream");
        Constructor<Object> constructor = decoder.getDeclaredConstructor(paramsClasses);
        InputStream buffer = new BufferedInputStream(in);
        Object[] params =
        { buffer };
        Object instance = null;
        instance = constructor.newInstance(params);
        if (!InputStream.class.isInstance(instance))
        {
            throw new InstallerException("'" + decoderClassName + "' must be derived from "
                    + InputStream.class.toString());
        }
        return (InputStream) instance;
    }

    private static Object readObject(ResourceManager resourceManager, String resourceId)
            throws Exception
    {
        InputStream inputStream = resourceManager.getInputStream(resourceId);
        ObjectInputStream objectInputStream = null;
        try
        {
            objectInputStream = new ObjectInputStream(inputStream);
            Object model = objectInputStream.readObject();
            return model;
        } finally
        {
            if (objectInputStream != null)
            {
                objectInputStream.close();
            }
        }
    }

}
