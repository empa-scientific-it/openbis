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

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.args4j.ExampleMode;
import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IDataSetDss;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IDssComponent;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssBuilder;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO.DataSetOwner;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO.DataSetOwnerType;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.validation.ValidationError;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.validation.ValidationScriptRunner;

/**
 * Command that runs a validation script and returns the error messages if the data set is not valid.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
class CommandTestValid extends AbstractDssCommand<CommandTestValid.CommandTestValidArguments>
{
    static class CommandTestValidArguments extends CommandPut.CommandPutArguments
    {
        public String[] getScriptPathsOrNull()
        {
            if (arguments.size() > 3)
            {
                String[] paths = new String[arguments.size() - 3];

                for (int i = 3; i < arguments.size(); i++)
                {
                    paths[i - 3] = arguments.get(i);
                }
                return paths;
            } else
            {
                return null;
            }
        }

        @Override
        public boolean allAdditionalMandatoryArgumentsPresent()
        {
            try
            {
                String[] paths = getScriptPathsOrNull();
                if (null == paths)
                {
                    return false;
                }

                for (String path : paths)
                {
                    File scriptFile = new File(path);
                    if (false == scriptFile.exists())
                    {
                        System.err.println("\n" + path
                                + " does not exist. Please specify a python (jython) script(s).");
                        return false;
                    }
                }
            } catch (Exception e)
            {
                System.err.println("\nThe script(s) must be a valid python (jython) script(s).");
            }
            return true;
        }
    }

    private static class CommandTestValidExecutor extends
            AbstractExecutor<CommandTestValidArguments>
    {

        CommandTestValidExecutor(CommandTestValidArguments arguments,
                AbstractDssCommand<CommandTestValidArguments> command)
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

                // If no script was provided, validate against the server's script
                List<ValidationError> errors;
                if (null == arguments.getScriptPathsOrNull())
                {
                    errors = component.validateDataSet(newDataSet, arguments.getFile());
                } else
                {
                    ValidationScriptRunner scriptRunner =
                            ValidationScriptRunner.createValidatorFromScriptPaths(arguments
                                    .getScriptPathsOrNull(), false);

                    errors = scriptRunner.validate(arguments.getFile());
                }
                for (ValidationError error : errors)
                {
                    System.err.println("ERROR: " + error.getErrorMessage());
                }
                if (errors.size() > 0)
                {
                    return ResultCode.USER_ERROR;
                } else
                {
                    System.out.println("OK: DataSet passed validation.");
                }
            } catch (IOException e)
            {
                throw new IOExceptionUnchecked(e);
            }

            return ResultCode.OK;
        }

        private NewDataSetDTO getNewDataSet() throws IOException
        {
            // Get the owner
            // That the owner type is valid has already been checked by CmdPutArguments#isComplete
            DataSetOwnerType ownerType = arguments.getOwnerType();
            String ownerIdentifier = arguments.getOwnerIdentifier();
            DataSetOwner owner = new NewDataSetDTO.DataSetOwner(ownerType, ownerIdentifier);

            File file = arguments.getFile();
            ArrayList<FileInfoDssDTO> fileInfos = getFileInfosForPath(file);

            // Get the parent
            String parentNameOrNull = null;
            if (file.isDirectory())
            {
                parentNameOrNull = file.getName();
            }

            NewDataSetDTO dataSet = new NewDataSetDTO(owner, parentNameOrNull, fileInfos);
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
                path = file.getParentFile().getCanonicalPath();
            }

            FileInfoDssBuilder builder = new FileInfoDssBuilder(path, path);
            builder.appendFileInfosForFile(file, fileInfos, true);
            return fileInfos;
        }
    }

    CommandTestValid()
    {
        super(new CommandTestValidArguments());
    }

    @Override
    public ResultCode execute(String[] args) throws UserFailureException,
            EnvironmentFailureException
    {
        return new CommandTestValidExecutor(arguments, this).execute(args);
    }

    @Override
    public String getName()
    {
        return "testvalid";
    }

    /**
     * Print usage information about the command.
     */
    @Override
    public void printUsage(PrintStream out)
    {
        out.println(getUsagePrefixString() + " [options] " + getRequiredArgumentsString());
        parser.printUsage(out);
        out.println("  Examples : ");
        out.println("     " + getCommandCallString() + parser.printExample(ExampleMode.ALL)
                + " EXPERIMENT <experiment identifier> <path> <script(s)>");
        out.println("     " + getCommandCallString() + parser.printExample(ExampleMode.ALL)
                + " SAMPLE <sample identifier> <path> <script(s)>");
    }

    @Override
    protected String getRequiredArgumentsString()
    {
        return "<owner type> <owner> <path> [<script(s)>]";
    }

    /**
     * Creates the DSS Component object and logs into the server.
     */
    @Override
    protected IDssComponent login(GlobalArguments args)
    {
        // The user wants to validate against the server's script, so we'll need to provide access
        // to the server.
        if (args.getArguments().size() < 4)
        {
            return super.login(args);
        }

        // If a script was specified, create a dummy component, since we don't need to access the
        // server.
        IDssComponent component = new IDssComponent()
            {

                @Override
                public void checkSession() throws InvalidSessionException
                {

                }

                @Override
                public String getSessionToken() throws IllegalStateException
                {
                    return null;
                }

                @Override
                public IDataSetDss getDataSet(String code) throws IllegalStateException,
                        EnvironmentFailureException
                {
                    return null;
                }

                @Override
                public IDataSetDss putDataSet(NewDataSetDTO newDataset, File dataSetFile)
                        throws IllegalStateException, EnvironmentFailureException
                {
                    return null;
                }

                @Override
                public void putFileToSessionWorkspace(String filePath, InputStream inputStream)
                        throws IOExceptionUnchecked
                {
                }

                @Override
                public void putFileToSessionWorkspace(String directory, File file)
                        throws IOExceptionUnchecked
                {
                }

                @Override
                public InputStream getFileFromSessionWorkspace(String filePath)
                        throws IOExceptionUnchecked
                {
                    return null;
                }

                @Override
                public void getFileFromSessionWorkspace(String filePath, File localFile)
                        throws IOExceptionUnchecked
                {
                }

                @Override
                public boolean deleteSessionWorkspaceFile(String path)
                {
                    return false;
                }

                @Override
                public void logout()
                {

                }

                @Override
                public List<ValidationError> validateDataSet(NewDataSetDTO newDataset,
                        File dataSetFile) throws IllegalStateException, EnvironmentFailureException
                {
                    return new ArrayList<ValidationError>();
                }

                @Override
                public Map<String, Serializable> extractMetadata(NewDataSetDTO newDataset,
                        File dataSetFile) throws IllegalStateException, EnvironmentFailureException
                {
                    return new HashMap<String, Serializable>();
                }

            };
        return component;
    }
}
