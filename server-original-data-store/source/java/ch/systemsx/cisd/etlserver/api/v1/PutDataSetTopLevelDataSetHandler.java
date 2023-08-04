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
package ch.systemsx.cisd.etlserver.api.v1;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.io.ConcatenatedFileOutputStreamWriter;
import ch.systemsx.cisd.etlserver.ITopLevelDataSetRegistrator;
import ch.systemsx.cisd.etlserver.ITopLevelDataSetRegistratorDelegate;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationPreStagingBehavior;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO.DataSetOwner;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;

/**
 * A helper class for carrying out the put command for creating data sets.
 * <p>
 * It is a data set handler that allows overriding information obtained from the extractors.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
class PutDataSetTopLevelDataSetHandler
{
    private static class DataSetRegistratorDelegate implements ITopLevelDataSetRegistratorDelegate
    {
        private final ArrayList<DataSetInformation> registeredDataSets =
                new ArrayList<DataSetInformation>();

        @Override
        public void didRegisterDataSets(List<DataSetInformation> dataSetInformations)
        {
            registeredDataSets.addAll(dataSetInformations);
        }

        @Override
        public DataSetRegistrationPreStagingBehavior getPrestagingBehavior()
        {
            return DataSetRegistrationPreStagingBehavior.USE_ORIGINAL;
        }

    }

    // General State
    private final PutDataSetService service;

    // Command Context State
    private final ITopLevelDataSetRegistrator registrator;

    private final String sessionToken;

    private final NewDataSetDTO newDataSet;

    private final InputStream inputStream;

    private final File temporaryIncomingDir;

    private final File dataSetDir;

    private final File dataSet;

    PutDataSetTopLevelDataSetHandler(PutDataSetService service,
            ITopLevelDataSetRegistrator registrator, String sessionToken, NewDataSetDTO newDataSet, File temporaryIncomingDir, File dataSet)
    {
        this.service = service;
        this.registrator = registrator;
        this.sessionToken = sessionToken;
        this.newDataSet = newDataSet;
        this.temporaryIncomingDir = temporaryIncomingDir;
        this.dataSet = dataSet;
        this.inputStream = null;
        this.dataSetDir = null;
    }

    PutDataSetTopLevelDataSetHandler(PutDataSetService service,
            ITopLevelDataSetRegistrator registrator, String sessionToken, NewDataSetDTO newDataSet,
            InputStream inputStream)
    {
        this.service = service;
        this.registrator = registrator;
        this.sessionToken = sessionToken;
        this.newDataSet = newDataSet;
        this.inputStream = inputStream;
        this.temporaryIncomingDir = service.createTemporaryIncomingDir(newDataSet.tryDataSetType());
        String dataSetFolderName = newDataSet.getDataSetFolderName();
        boolean dataSetIsASingleFile =
                NewDataSetDTO.DEFAULT_DATA_SET_FOLDER_NAME.equals(dataSetFolderName)
                        && newDataSet.getFileInfos().size() == 1;
        if (dataSetIsASingleFile)
        {
            dataSetDir = temporaryIncomingDir;
            dataSet =
                    new File(temporaryIncomingDir, newDataSet.getFileInfos().get(0)
                            .getPathInDataSet());
        } else
        {
            this.dataSetDir = new File(temporaryIncomingDir, dataSetFolderName);
            dataSet = dataSetDir;
        }
        if (dataSetDir.exists())
        {
            deleteDataSetDir();
        }
        if (false == this.dataSetDir.mkdir())
        {
            throw new EnvironmentFailureException("Could not create directory for data set "
                    + dataSet.getName());
        }

    }

    /**
     * Run the put command; does *not* close the input stream &mdash; clients of the executor are expected to close the input stream when appropriate.
     * 
     * @param noOwnerAllowed <code>true</code> if missing data set owner is allowed. This is the case for custom import.
     * @throws IOException
     */
    public List<DataSetInformation> execute(boolean noOwnerAllowed) throws UserFailureException, IOException
    {
        PutDataSetUtil.checkAccess(sessionToken, getOpenBisService(), newDataSet, noOwnerAllowed);

        writeDataSetToTempDirectory();

        // Register the data set
        try
        {
            DataSetRegistratorDelegate delegate = new DataSetRegistratorDelegate();
            registrator.handle(dataSet, sessionToken, getCallerDataSetInformation(), delegate);
            return delegate.registeredDataSets;
        } finally
        {
            deleteDataSetDir();
        }
    }

    /**
     * Run the put command; this method assumes the data set is already in the rpc-icoming folder in the share.
     */
    public List<DataSetInformation> executeWithoutWriting() throws UserFailureException
    {
        PutDataSetUtil.checkAccess(sessionToken, getOpenBisService(), newDataSet, false);

        // Register the data set
        try
        {
            DataSetRegistratorDelegate delegate = new DataSetRegistratorDelegate();
            registrator.handle(dataSet, sessionToken, getCallerDataSetInformation(), delegate);
            return delegate.registeredDataSets;
        } finally
        {
            deleteDataSetDir();
        }

    }

    public SessionContextDTO getSessionContext()
    {
        return getOpenBisService().tryGetSession(sessionToken);
    }

    public DataSetOwner getDataSetOwner()
    {
        return newDataSet.getDataSetOwner();
    }

    public DataSetInformation getCallerDataSetInformation()
    {
        DataSetInformation dataSetInfo = new DataSetInformation();
        SessionContextDTO sessionContext = getSessionContext();
        dataSetInfo.setUploadingUserEmail(sessionContext.getUserEmail());
        dataSetInfo.setUploadingUserId(sessionContext.getUserName());
        DataSetOwner owner = getDataSetOwner();
        if (owner != null)
        {
            switch (owner.getType())
            {
                case EXPERIMENT:
                    dataSetInfo.setExperimentIdentifier(tryExperimentIdentifier());
                    break;
                case SAMPLE:
                    SampleIdentifier sampleId = trySampleIdentifier();
                    dataSetInfo.setSampleIdentifier(sampleId);
                    break;
                case DATA_SET:
                    String dataSetCode = tryGetDataSetCode();

                    AbstractExternalData parentDataSet = getOpenBisService().tryGetDataSet(dataSetCode);
                    if (parentDataSet != null)
                    {
                        if (parentDataSet.getExperiment() != null)
                        {
                            dataSetInfo.setExperiment(parentDataSet.getExperiment());
                        }
                        if (parentDataSet.getSample() != null)
                        {
                            dataSetInfo.setSample(parentDataSet.getSample());
                        }

                        ArrayList<String> parentDataSetCodes = new ArrayList<String>();
                        // Add this parent as the first parent
                        parentDataSetCodes.add(parentDataSet.getCode());
                        parentDataSetCodes.addAll(dataSetInfo.getParentDataSetCodes());
                        dataSetInfo.setParentDataSetCodes(parentDataSetCodes);
                    }
                    break;
            }
        }
        String typeCode = newDataSet.tryDataSetType();
        if (null != typeCode)
        {
            dataSetInfo.setDataSetType(new DataSetType(typeCode));
        }
        dataSetInfo.setDataSetKind(DataSetKind.PHYSICAL);

        Map<String, Serializable> primitiveProps = newDataSet.getProperties();
        if (false == primitiveProps.isEmpty())
        {
            ArrayList<NewProperty> properties = new ArrayList<NewProperty>();
            for (String key : primitiveProps.keySet())
            {
                properties.add(new NewProperty(key, primitiveProps.get(key)));
            }
            dataSetInfo.setDataSetProperties(properties);
        }

        // Add any parents to the end of the list of parents
        ArrayList<String> parentDataSetCodes = new ArrayList<String>();
        parentDataSetCodes.addAll(dataSetInfo.getParentDataSetCodes());
        parentDataSetCodes.addAll(newDataSet.getParentDataSetCodes());
        dataSetInfo.setParentDataSetCodes(parentDataSetCodes);

        return dataSetInfo;
    }

    private void writeDataSetToTempDirectory() throws IOException
    {
        ConcatenatedFileOutputStreamWriter imagesWriter =
                new ConcatenatedFileOutputStreamWriter(inputStream);
        for (FileInfoDssDTO fileInfo : newDataSet.getFileInfos())
        {
            if (fileInfo.isDirectory())
            {
                // Just make the directory
                File file = new File(dataSetDir, fileInfo.getPathInDataSet());
                file.mkdir();
            } else
            {
                // Download the file -- the directory should have already been made
                OutputStream output = getOutputStream(fileInfo);
                imagesWriter.writeNextBlock(output);
                output.flush();
                output.close();
            }
        }
    }

    private OutputStream getOutputStream(FileInfoDssDTO fileInfo)
    {
        File file = new File(dataSetDir, fileInfo.getPathInDataSet());

        FileOutputStream fos;
        try
        {
            fos = new FileOutputStream(file);
        } catch (FileNotFoundException ex)
        {
            // This should not happen since we just created the directory to store the new file in
            throw new EnvironmentFailureException("Could not save file " + file, ex);
        }
        return new BufferedOutputStream(fos);
    }

    private void deleteDataSetDir()
    {
        deleteDir(temporaryIncomingDir);
    }

    private void deleteDir(File dirToDelete)
    {
        try
        {
            FileUtils.deleteDirectory(dirToDelete);
        } catch (IOException ex)
        {
            getOperationLog().error("Could not delete data set directory " + dirToDelete, ex);
            ex.printStackTrace();
        }
    }

    private ExperimentIdentifier tryExperimentIdentifier()
    {
        DataSetOwner owner = getDataSetOwner();
        switch (owner.getType())
        {
            case EXPERIMENT:
                return new ExperimentIdentifierFactory(owner.getIdentifier()).createIdentifier();
            default:
                return null;
        }
    }

    private SampleIdentifier trySampleIdentifier()
    {
        DataSetOwner owner = getDataSetOwner();
        switch (owner.getType())
        {
            case SAMPLE:
                return new SampleIdentifierFactory(owner.getIdentifier()).createIdentifier();
            default:
                return null;
        }
    }

    private String tryGetDataSetCode()
    {
        DataSetOwner owner = getDataSetOwner();
        switch (owner.getType())
        {
            case DATA_SET:
                return owner.getIdentifier();
            default:
                return null;
        }
    }

    private IEncapsulatedOpenBISService getOpenBisService()
    {
        return service.getOpenBisService();
    }

    private Logger getOperationLog()
    {
        return service.getOperationLog();
    }
}
