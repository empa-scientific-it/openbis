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

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.mail.EMailAddress;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.etlserver.IDataSetHandler;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * Global state for the SampleAndDatasetRegistration operation.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
class SampleAndDataSetRegistrationGlobalState
{
    enum SampleRegistrationMode
    {
        ACCEPT_ALL, IGNORE_EXISTING, REJECT_NONEXISTING
    }

    private final IDataSetHandler delegator;

    private final IEncapsulatedOpenBISService openbisService;

    private final SpaceIdentifier spaceIdentifierOrNull;

    private final SampleType sampleTypeOrNull;

    private final DataSetType dataSetTypeOrNull;

    private final DataSetKind dataSetKindOrNull;

    private final SampleRegistrationMode sampleRegistrationMode;

    private final List<String> errorEmailRecipientsOrNull;

    private final String controlFilePattern;

    private final boolean alwaysCleanupAfterProcessing;

    private final boolean unmentionedSubfolderIsFailure;

    private final boolean useIsFinishedMarkerFile;

    private final Logger operationLog;

    private IMailClient mailClient;

    SampleAndDataSetRegistrationGlobalState(IDataSetHandler delegator,
            IEncapsulatedOpenBISService openbisService, SpaceIdentifier spaceIdentifierOrNull,
            SampleType sampleTypeOrNull, DataSetType dataSetTypeOrNull, DataSetKind dataSetKindOrNull,
            SampleRegistrationMode sampleRegistrationMode, List<String> errorEmailRecipientsOrNull,
            String controlFilePattern, boolean alwaysCleanupAfterProcessing,
            boolean unmentionedSubfolderIsFailure, boolean useIsFinishedMarkerFile, Logger operationLog)
    {
        this.delegator = delegator;
        this.openbisService = openbisService;
        this.spaceIdentifierOrNull = spaceIdentifierOrNull;
        this.sampleTypeOrNull = sampleTypeOrNull;
        this.dataSetTypeOrNull = dataSetTypeOrNull;
        this.dataSetKindOrNull = dataSetKindOrNull;
        this.sampleRegistrationMode = sampleRegistrationMode;
        this.errorEmailRecipientsOrNull = errorEmailRecipientsOrNull;
        this.controlFilePattern = controlFilePattern;
        this.alwaysCleanupAfterProcessing = alwaysCleanupAfterProcessing;
        this.unmentionedSubfolderIsFailure = unmentionedSubfolderIsFailure;
        this.useIsFinishedMarkerFile = useIsFinishedMarkerFile;
        this.operationLog = operationLog;
    }

    public IDataSetHandler getDelegator()
    {
        return delegator;
    }

    /**
     * The connection to the Application Server.
     */
    public IEncapsulatedOpenBISService getOpenbisService()
    {
        return openbisService;
    }

    /**
     * Return true if the dropbox puts all samples/data sets in the specified space identifier
     */
    public boolean hasGlobalSpaceIdentifier()
    {
        return null != spaceIdentifierOrNull;
    }

    /**
     * Rreturn the space identifier for the drop box (if there is one).
     */
    public SpaceIdentifier trySpaceIdentifier()
    {
        return spaceIdentifierOrNull;
    }

    public SampleType trySampleType()
    {
        return sampleTypeOrNull;
    }

    public DataSetType tryDataSetType()
    {
        return dataSetTypeOrNull;
    }

    public DataSetKind tryDataSetKind()
    {
        return dataSetKindOrNull;
    }

    public SampleRegistrationMode getSampleRegistrationMode()
    {
        return sampleRegistrationMode;
    }

    public Logger getOperationLog()
    {
        return operationLog;
    }

    public IMailClient getMailClient()
    {
        return mailClient;
    }

    /**
     * Since the mail client is not known at object initialization time, we need to offer a setter for it.
     */
    public void initializeMailClient(IMailClient aMailClient)
    {
        this.mailClient = aMailClient;
    }

    public EMailAddress[] getErrorEmailRecipients()
    {
        ArrayList<EMailAddress> emailAddresses = new ArrayList<EMailAddress>();
        if (null == errorEmailRecipientsOrNull)
        {
            // Get the admin address from the server and use those
            List<Person> admins = openbisService.listAdministrators();
            for (Person admin : admins)
            {
                emailAddresses.add(new EMailAddress(admin.getEmail()));
            }
        } else
        {
            for (String useridOrEmail : errorEmailRecipientsOrNull)
            {
                Person personOrNull = openbisService.tryPersonWithUserIdOrEmail(useridOrEmail);
                if (null == personOrNull)
                {
                    emailAddresses.add(new EMailAddress(useridOrEmail));
                } else
                {
                    emailAddresses.add(new EMailAddress(personOrNull.getEmail()));
                }
            }
        }
        return emailAddresses.toArray(new EMailAddress[emailAddresses.size()]);
    }

    public String getControlFilePattern()
    {
        return controlFilePattern;
    }

    public boolean alwaysCleanUpAfterProcessing()
    {
        return alwaysCleanupAfterProcessing;
    }

    public boolean areUnmentionedFoldersAnError()
    {
        return unmentionedSubfolderIsFailure;
    }

    public boolean isUseIsFinishedMarkerFile()
    {
        return useIsFinishedMarkerFile;
    }
}
