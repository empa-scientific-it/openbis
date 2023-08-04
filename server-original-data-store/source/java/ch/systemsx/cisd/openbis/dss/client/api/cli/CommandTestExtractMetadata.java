/*
 * Copyright ETH 2011 - 2023 Zürich, Scientific IT Services
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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IDssComponent;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssBuilder;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO.DataSetOwner;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO.DataSetOwnerType;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.validation.ValidationScriptRunner;

/**
 * Command that runs an extract metadata script and returns the extracted properties.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class CommandTestExtractMetadata extends CommandTestValid
{

    private static class CommandTestExtractMetadataExecutor extends
            AbstractExecutor<CommandTestValidArguments>
    {

        CommandTestExtractMetadataExecutor(CommandTestValidArguments arguments,
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
                Map<String, Serializable> properties;
                if (null == arguments.getScriptPathsOrNull())
                {
                    properties = component.extractMetadata(newDataSet, arguments.getFile());
                } else
                {
                    ValidationScriptRunner scriptRunner =
                            ValidationScriptRunner.createValidatorFromScriptPaths(arguments
                                    .getScriptPathsOrNull(), false);

                    properties = scriptRunner.extractMetadata(arguments.getFile());
                }
                String[] keys = properties.keySet().toArray(new String[0]);
                Arrays.sort(keys);
                System.out.println("PROPS:");
                for (String key : keys)
                {
                    System.out.println("\t" + key + " : " + properties.get(key));
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

    @Override
    public ResultCode execute(String[] args) throws UserFailureException,
            EnvironmentFailureException
    {
        return new CommandTestExtractMetadataExecutor(arguments, this).execute(args);
    }

    @Override
    public String getName()
    {
        return "testextract";
    }
}
