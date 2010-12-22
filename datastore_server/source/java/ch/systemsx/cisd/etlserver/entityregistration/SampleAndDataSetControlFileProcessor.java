/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.entityregistration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.util.ByteArrayDataSource;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.io.DelegatedReader;
import ch.systemsx.cisd.common.mail.EMailAddress;
import ch.systemsx.cisd.common.utilities.UnicodeUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.parser.BisTabFileLoader;
import ch.systemsx.cisd.openbis.generic.shared.parser.GlobalProperties;
import ch.systemsx.cisd.openbis.generic.shared.parser.GlobalPropertiesLoader;

/**
 * @author Chandrasekhar Ramakrishnan
 */
class SampleAndDataSetControlFileProcessor extends AbstractSampleAndDataSetProcessor
{
    private static final String SUCCESS_FILENAME = "registered.txt";

    private final File controlFile;

    private final HashMap<SampleDataSetPair, IRegistrationStatus> errorMap =
            new HashMap<SampleDataSetPair, IRegistrationStatus>();

    private final HashMap<SampleDataSetPair, IRegistrationStatus> successMap =
            new HashMap<SampleDataSetPair, IRegistrationStatus>();

    private final HashSet<File> processedDataSetFiles = new HashSet<File>();

    // State that is filled out as a result of processing
    private ControlFileRegistrationProperties properties;

    private String failureLinesResultSectionOrNull;

    private String unmentionedSubfoldersResultSectionOrNull;

    private String successLinesResultSectionOrNull;

    /**
     * Utility class for accessing the properties defined in a control file.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    static class ControlFileOverrideProperties
    {
        private final GlobalProperties properties;

        /**
         * Creates a wrapper on the properties. The FileNotFoundExcecption cannot happen in
         * practice, since we've already verified that the file exists.
         * 
         * @param controlFile
         * @throws FileNotFoundException
         */
        ControlFileOverrideProperties(File controlFile) throws FileNotFoundException
        {
            properties = GlobalPropertiesLoader.load(controlFile);
        }

        public SampleType trySampleType()
        {
            String sampleTypeCode =
                    properties
                            .get(SampleAndDataSetRegistrationHandler.SAMPLE_TYPE_CONTROL_FILE_KEY);
            if (null == sampleTypeCode)
            {
                return null;
            }
            SampleType sampleType = new SampleType();
            sampleType.setCode(sampleTypeCode);
            return sampleType;
        }

        public DataSetType tryDataSetType()
        {
            String dataSetTypeCode =
                    properties
                            .get(SampleAndDataSetRegistrationHandler.DATA_SET_TYPE_CONTROL_FILE_KEY);
            if (null == dataSetTypeCode)
            {
                return null;
            }
            DataSetType dataSetType = new DataSetType();
            dataSetType.setCode(dataSetTypeCode);
            return dataSetType;
        }

        public String tryUserString()
        {
            return properties.get(SampleAndDataSetRegistrationHandler.USER_CONTROL_FILE_KEY);
        }
    }

    /**
     * Utility class for accessing the properties defined in a control file.
     * <p>
     * After instantiating this object, you should call check validity to ensure that none of the
     * methods return null. If the object is invalid, you should check for null values, otherwise
     * all methods will return non-null values unless otherwise noted.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    static class ControlFileRegistrationProperties
    {
        private final ControlFileOverrideProperties overrideProperties;

        private final SampleAndDataSetRegistrationGlobalState globalProperties;

        private final Person user;

        /**
         * An object that gets the definitive properties for registration. The properties are
         * created by taking overrides when available, and defaults otherwise.
         */
        ControlFileRegistrationProperties(ControlFileOverrideProperties overrideProperties,
                SampleAndDataSetRegistrationGlobalState globalProperties)
                throws FileNotFoundException
        {
            this.overrideProperties = overrideProperties;
            this.globalProperties = globalProperties;

            String userIdOrEmail = overrideProperties.tryUserString();
            if (null == userIdOrEmail)
            {
                user = null;
            } else
            {
                user =
                        globalProperties.getOpenbisService().tryPersonWithUserIdOrEmail(
                                userIdOrEmail);
            }
        }

        public SampleAndDataSetRegistrationGlobalState getGlobalProperties()
        {
            return globalProperties;
        }

        public SampleType getSampleType()
        {
            SampleType sampleType = overrideProperties.trySampleType();
            if (null == sampleType)
            {
                sampleType = globalProperties.trySampleType();
            }
            return sampleType;
        }

        public DataSetType getDataSetType()
        {
            DataSetType dataSetType = overrideProperties.tryDataSetType();
            if (null == dataSetType)
            {
                dataSetType = globalProperties.tryDataSetType();
            }
            return dataSetType;
        }

        public Person getUser()
        {
            return user;
        }

        public void checkValidity()
        {
            SampleType sampleType = getSampleType();
            DataSetType dataSetType = getDataSetType();
            Person theUser = getUser();

            StringBuilder sb = new StringBuilder();
            boolean hasError = false;
            if (null == sampleType)
            {
                hasError = true;
                sb.append("\tNo default sample type has been specified, and no sample type was specified in the control file");
            }
            if (null == dataSetType)
            {
                hasError = true;
                sb.append("\tNo default data set type has been specified, and no data set type was specified in the control file");
            }
            if (null == theUser)
            {
                hasError = true;
                sb.append("\tNo user has been specified");
            }
            if (hasError)
            {
                throw new UserFailureException(sb.toString());
            }
        }
    }

    SampleAndDataSetControlFileProcessor(SampleAndDataSetRegistrationGlobalState globalState,
            File folder, File controlFile)
    {
        super(globalState, folder);
        this.controlFile = controlFile;
    }

    /**
     * Register the entities defined in the control file. Collect all the errors that happen and
     * notify the user. If the file itself has errors, then throw an exception to the parent.
     */
    public void register() throws UserFailureException, EnvironmentFailureException,
            FileNotFoundException
    {
        ControlFileOverrideProperties overrideProperties =
                new ControlFileOverrideProperties(controlFile);

        logControlFileOverridePropertiesExtracted(overrideProperties);

        properties = new ControlFileRegistrationProperties(overrideProperties, globalState);

        BisTabFileLoader<SampleDataSetPair> controlFileLoader =
                new BisTabFileLoader<SampleDataSetPair>(
                        SampleDataSetPairParserObjectFactory.createFactoryFactory(
                                properties.getSampleType(), properties.getDataSetType()), false);

        List<SampleDataSetPair> loadedSampleDataSetPairs = null;

        Reader reader = UnicodeUtils.createReader(new FileInputStream(controlFile));
        DelegatedReader delegatedReader = new DelegatedReader(reader, controlFile.getName());

        try
        {
            properties.checkValidity();
            loadedSampleDataSetPairs = controlFileLoader.load(delegatedReader);
        } catch (UserFailureException e)
        {
            // If we don't know which user to send the email to, don't handle this error -- leave it
            // to higher levels.
            if (null == properties.getUser())
            {
                throw e;
            }

            sendEmailWithErrorMessage(e.getMessage());
            return;
        } finally
        {
            try
            {
                delegatedReader.close();
            } catch (IOException ex)
            {
                // Ignore this failure
            }
        }

        String userId = properties.getUser().getUserId();
        // If we are here, we have successfully parsed the file
        for (SampleDataSetPair sampleDataSet : loadedSampleDataSetPairs)
        {
            sampleDataSet.getDataSetInformation().setUploadingUserId(userId);
            SampleAndDataSetRegistrator registrator =
                    new SampleAndDataSetRegistrator(folder, properties, sampleDataSet);
            IRegistrationStatus result = registrator.register();
            processedDataSetFiles.add(registrator.getDataSetFile());
            if (result.isError())
            {
                errorMap.put(sampleDataSet, result);
            } else
            {
                successMap.put(sampleDataSet, result);
            }
        }

        sendResultsEmail();
    }

    private void sendResultsEmail()
    {

        createFailureLinesSection();
        createUnmentionedFoldersSection();
        createSuccessfulLinesSection();

        boolean wasSuccessful = true;

        StringBuilder resultEmail = new StringBuilder();
        if (null != failureLinesResultSectionOrNull)
        {
            resultEmail.append(failureLinesResultSectionOrNull);
            resultEmail.append("\n");
            wasSuccessful = false;
        }
        if (null != unmentionedSubfoldersResultSectionOrNull)
        {
            resultEmail.append(unmentionedSubfoldersResultSectionOrNull);
            resultEmail.append("\n");
            wasSuccessful = false;
        }
        if (null != successLinesResultSectionOrNull)
        {
            resultEmail.append(successLinesResultSectionOrNull);
        }

        if (wasSuccessful)
        {
            sendEmailWithSuccessMessage(resultEmail.toString());
        } else
        {
            sendEmailWithErrorMessage(resultEmail.toString());
        }
    }

    private void createFailureLinesSection()
    {
        if (errorMap.isEmpty())
        {
            failureLinesResultSectionOrNull = null;
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Encountered errors in the following lines:\n");
        for (SampleDataSetPair pair : errorMap.keySet())
        {
            IRegistrationStatus error = errorMap.get(pair);
            sb.append("# ");
            sb.append(error.getMessage());
            sb.append("\n");

            String[] tokens = pair.getTokens();
            int i = 0;
            for (String token : tokens)
            {
                sb.append(token);
                if (++i < tokens.length)
                {
                    sb.append("\t");
                }
            }
            sb.append("\n");
        }
        failureLinesResultSectionOrNull = sb.toString();
    }

    private void createUnmentionedFoldersSection()
    {
        if (false == globalState.areUnmentionedFoldersAnError())
        {
            unmentionedSubfoldersResultSectionOrNull = null;
            return;
        }
        // Make sure all folders were processed
        ArrayList<File> unprocessedFiles = getUnprocessedDataSetList();

        if (unprocessedFiles.isEmpty())
        {
            unmentionedSubfoldersResultSectionOrNull = null;
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("The following subfolders were in the uploaded folder, but were not mentioned in the control file:\n");
        for (File file : unprocessedFiles)
        {
            sb.append(file.getName());
            sb.append(",");
        }

        // remove the final comma
        sb.deleteCharAt(sb.length() - 1);

        unmentionedSubfoldersResultSectionOrNull = sb.toString();
    }

    private void createSuccessfulLinesSection()
    {
        if (errorMap.isEmpty())
        {
            successLinesResultSectionOrNull = null;
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("The following lines were successfully registered:\n");
        for (SampleDataSetPair pair : successMap.keySet())
        {
            sb.append("# ");
            String[] tokens = pair.getTokens();
            int i = 0;
            for (String token : tokens)
            {
                sb.append(token);
                if (++i < tokens.length)
                {
                    sb.append("\t");
                }
            }
            sb.append("\n");
        }
        successLinesResultSectionOrNull = sb.toString();
    }

    private void logControlFileOverridePropertiesExtracted(
            ControlFileOverrideProperties overrideProperties)
    {
        String message =
                String.format(
                        "Global properties extracted from file '%s': SAMPLE_TYPE(%s) DATA_SET_TYPE(%s) USER(%s)",
                        controlFile.getName(), overrideProperties.trySampleType(),
                        overrideProperties.tryDataSetType(), overrideProperties.tryUserString());
        logInfo(message);
    }

    /**
     * Send an email message to the person who uploaded the file, telling them that everything went
     * ok.
     */
    private void sendEmailWithSuccessMessage(String message)
    {
        // Create an email and send it.
        try
        {
            String subject = createSuccessEmailSubject();
            String content = createSuccessEmailContent();
            String filename = SUCCESS_FILENAME;
            EMailAddress recipient = new EMailAddress(properties.getUser().getEmail());
            DataSource dataSource = new ByteArrayDataSource(message, "text/plain");

            globalState.getMailClient().sendEmailMessageWithAttachment(subject, content, filename,
                    new DataHandler(dataSource), null, null, recipient);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    /**
     * Send an email message to the person who uploaded the file. This method is only called if we
     * have a valid email address to contact. Otherwise, errors are forwarded to a higher level for
     * handling.
     */
    private void sendEmailWithErrorMessage(String message)
    {
        // Log it
        logError(message);

        // Create an email and send it.
        try
        {
            String subject = createErrorEmailSubject();
            String content = createErrorEmailContent();
            String filename = SampleAndDataSetFolderProcessor.ERRORS_FILENAME;
            EMailAddress recipient = new EMailAddress(properties.getUser().getEmail());
            DataSource dataSource = new ByteArrayDataSource(message, "text/plain");

            globalState.getMailClient().sendEmailMessageWithAttachment(subject, content, filename,
                    new DataHandler(dataSource), null, null, recipient);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    private String createErrorEmailSubject()
    {
        return String.format("Sample / Data Set Registration Error -- %s", controlFile);
    }

    private String createErrorEmailContent()
    {
        return String
                .format("Not all samples and data sets specified in the control file, %s, could be registered / updated. The errors are detailed in the attachment. Each faulty line is reproduced, preceded by a comment explaining the cause of the error.",
                        controlFile);
    }

    private String createSuccessEmailSubject()
    {
        return String.format("Sample / Data Set Registration Succeeded -- %s", controlFile);
    }

    private String createSuccessEmailContent()
    {
        return String
                .format("The registration/update of samples and the registration of data sets was successful specified in the control file, %s, was successful.",
                        controlFile);
    }

    private ArrayList<File> getUnprocessedDataSetList()
    {
        File[] files = folder.listFiles();
        ArrayList<File> unprcessedDataSets = new ArrayList<File>();
        for (File file : files)
        {
            if (controlFile.equals(file))
            {
                continue;
            }
            // See if it was handled already
            if (processedDataSetFiles.contains(file))
            {
                continue;
            }
            unprcessedDataSets.add(file);
        }

        return unprcessedDataSets;
    }
}
