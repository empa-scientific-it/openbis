/*
 * Copyright ETH 2010 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.dss.client.api.cli;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ch.systemsx.cisd.args4j.ExampleMode;
import ch.systemsx.cisd.args4j.Option;
import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IDataSetDss;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IDssComponent;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssBuilder;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO.DataSetOwner;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO.DataSetOwnerType;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.validation.ValidationError;

/**
 * Command that uploads a data set.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
class CommandPut extends AbstractDssCommand<CommandPut.CommandPutArguments>
{
    static class CommandPutArguments extends GlobalArguments
    {
        @Option(name = "t", longName = "type", usage = "Set the data set type")
        private String dataSetType;

        @Option(longName = "props", usage = "Set properties of the data set (format: code=val[,code=val]*")
        private String propertiesString;

        public String getDataSetType()
        {
            return dataSetType;
        }

        public DataSetOwnerType getOwnerType()
        {
            return DataSetOwnerType.valueOf(getArguments().get(0).toString().toUpperCase());
        }

        public String getOwnerIdentifier()
        {
            return getArguments().get(1);
        }

        public String getFilePath()
        {
            return getArguments().get(2);
        }

        public File getFile()
        {
            return new File(getFilePath());
        }

        public HashMap<String, Serializable> getProperties()
        {
            HashMap<String, Serializable> propsMap = new HashMap<String, Serializable>();
            String propsString = propertiesString;
            if (propsString == null || propsString.length() == 0)
            {
                return propsMap;
            }

            String[] propsArray = propsString.split(",");
            for (String propLine : propsArray)
            {
                String[] keyAndValue = propLine.split("=");
                assert keyAndValue.length == 2;
                propsMap.put(keyAndValue[0], keyAndValue[1]);
            }
            return propsMap;
        }

        @Override
        public boolean allAdditionalMandatoryArgumentsPresent()
        {
            if (getArguments().size() < 3)
                return false;

            try
            {
                getOwnerType();
            } catch (IllegalArgumentException e)
            {
                return false;
            }

            try
            {
                getProperties();
            } catch (Exception e)
            {
                System.err
                        .println("\nProprties must be specified using as code=value[,code=value]*\n");
                return false;
            }
            return true;
        }
    }

    private static class CommandPutExecutor extends AbstractExecutor<CommandPutArguments>
    {

        CommandPutExecutor(CommandPutArguments arguments,
                AbstractDssCommand<CommandPutArguments> command)
        {
            super(arguments, command);
        }

        @Override
        protected ResultCode doExecute(IDssComponent component)
        {
            try
            {
                NewDataSetDTO newDataSet = getNewDataSet();
                if (newDataSet.getFileInfos().isEmpty())
                {
                    File file = arguments.getFile();
                    if (false == file.exists())
                    {
                        System.err.println("Data set file does not exist");
                    } else if (false == file.isDirectory())
                    {
                        System.err.println("Must select a directory to upload.");
                    } else
                    {
                        System.err.println("Data set is empty.");
                    }
                    return ResultCode.INVALID_ARGS;
                }
                List<ValidationError> errors =
                        component.validateDataSet(newDataSet, arguments.getFile());
                if (errors.isEmpty())
                {
                    IDataSetDss dataSet = component.putDataSet(newDataSet, arguments.getFile());
                    System.out.println("Registered new data set " + dataSet.getCode());
                } else
                {
                    System.out.println("Data set has errors:");
                    for (ValidationError error : errors)
                    {
                        System.out.println("\t" + error.getErrorMessage());
                    }
                    return ResultCode.USER_ERROR;
                }
            } catch (IOException e)
            {
                throw new IOExceptionUnchecked(e);
            }

            return ResultCode.OK;
        }

        // TODO 2011-05-31, Piotr Buczek: support for creating new data set attached to a container
        private NewDataSetDTO getNewDataSet() throws IOException
        {
            // Get the owner
            // That the owner type is valid has already been checked by CmdPutArguments#isComplete
            DataSetOwnerType ownerType = arguments.getOwnerType();
            String ownerIdentifier = arguments.getOwnerIdentifier();
            DataSetOwner owner = new NewDataSetDTO.DataSetOwner(ownerType, ownerIdentifier);

            File file = arguments.getFile();
            ArrayList<FileInfoDssDTO> fileInfos = getFileInfosForPath(file);

            // Get the folder
            String folderNameOrNull = null;
            if (file.isDirectory())
            {
                folderNameOrNull = file.getName();
            }

            NewDataSetDTO dataSet = new NewDataSetDTO(owner, folderNameOrNull, fileInfos);
            // Set the data set type (may be null)
            dataSet.setDataSetTypeOrNull(arguments.getDataSetType());

            // Set the properties
            dataSet.setProperties(arguments.getProperties());

            return dataSet;
        }

        private ArrayList<FileInfoDssDTO> getFileInfosForPath(File file) throws IOException
        {
            ArrayList<FileInfoDssDTO> fileInfos = new ArrayList<FileInfoDssDTO>();
            if (false == file.exists())
            {
                return fileInfos;
            }

            String path = file.getCanonicalPath();
            if (false == file.isDirectory())
            {
                File parentFile = file.getParentFile();
                if (parentFile == null)
                {
                    parentFile = new File(".");
                }
                path = parentFile.getCanonicalPath();
            }

            FileInfoDssBuilder builder = new FileInfoDssBuilder(path, path);
            builder.appendFileInfosForFile(file, fileInfos, true);
            return fileInfos;
        }
    }

    CommandPut()
    {
        super(new CommandPutArguments());
    }

    @Override
    public ResultCode execute(String[] args) throws UserFailureException,
            EnvironmentFailureException
    {
        return new CommandPutExecutor(arguments, this).execute(args);
    }

    @Override
    public String getName()
    {
        return "put";
    }

    /**
     * Print usage information about the command.
     */
    @Override
    public void printUsage(PrintStream out)
    {
        out.println(getUsagePrefixString() + " [options] <owner type> <owner> <path>");
        parser.printUsage(out);
        out.println("  Examples : ");
        out.println("     " + getCommandCallString() + parser.printExample(ExampleMode.ALL)
                + " EXPERIMENT <experiment identifier> <path>");
        out.println("     " + getCommandCallString() + parser.printExample(ExampleMode.ALL)
                + " SAMPLE <sample identifier> <path>");
        out.println("     " + getCommandCallString() + parser.printExample(ExampleMode.ALL)
                + " DATA_SET <data set identifier> <path>");
    }
}
