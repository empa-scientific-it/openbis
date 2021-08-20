/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.ethz.bsse.cisd.dsu.tracking.email;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ch.ethz.bsse.cisd.dsu.tracking.dto.TrackedEntities;
import ch.systemsx.cisd.common.mail.EMailAddress;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;

/**
 * Manager that groups data about tracked entities into {@link EntityTrackingEmailData} objects.
 * 
 * @author Piotr Buczek
 * @author Manuel Kohler
 */
class EntityTrackingEmailDataManager
{
    private final static String CONTACT_PERSON_EMAIL = "CONTACT_PERSON_EMAIL";

    private final static String PRINCIPAL_INVESTIGATOR_EMAIL = "PRINCIPAL_INVESTIGATOR_EMAIL";

    private final static String CONTACT_DATA_MANAGER_EMAIL = "CONTACT_DATA_MANAGER_EMAIL";

    private static Map<String, String> recipientsBySpace;

    public static void initialize(final Map<String, String> recipients)
    {
        recipientsBySpace = recipients;
    }

    public static Collection<EntityTrackingEmailData> groupByRecipient(
            TrackedEntities trackedEntities)
    {
        assert recipientsBySpace != null : "recipientsBySpace not initialized";
        // <recipients email, email data>
        final Map<EMailAddress, EntityTrackingEmailData> dataByRecipient =
                new HashMap<EMailAddress, EntityTrackingEmailData>();
        groupSequencingSamplesToBeProcessed(dataByRecipient, trackedEntities);
        groupSequencingSamplesProcessed(dataByRecipient, trackedEntities);
        groupDataSetSamples(dataByRecipient, trackedEntities);
        return dataByRecipient.values();
    }

    public static Collection<EntityTrackingEmailData> groupByRecipientDataSets(
            TrackedEntities trackedEntities)
    {
        assert recipientsBySpace != null : "recipientsBySpace not initialized";
        // <recipients email, email data>
        final Map<EMailAddress, EntityTrackingEmailData> dataByRecipient =
                new HashMap<EMailAddress, EntityTrackingEmailData>();
        groupDataSetSamples(dataByRecipient, trackedEntities);
        return dataByRecipient.values();
    }

    /** Puts tracked sequencing samples to be processed grouped by recipient into <var>result</var>. */
    private static void groupSequencingSamplesToBeProcessed(
            Map<EMailAddress, EntityTrackingEmailData> result, TrackedEntities trackedEntities)
    {
        for (Sample sequencingSample : trackedEntities.getSequencingSamplesToBeProcessed())
        {
            for (EMailAddress recipient : getSequencingSampleTrackingRecipients(Collections
                    .singleton(sequencingSample)))
            {
                final EntityTrackingEmailData emailData =
                        getOrCreateRecipientEmailData(result, recipient);
                emailData.addSequencingSampleToBeProcessed(sequencingSample);
            }
        }
    }

    /** Puts tracked processed sequencing samples grouped by recipient into <var>result</var>. */
    private static void groupSequencingSamplesProcessed(
            Map<EMailAddress, EntityTrackingEmailData> result, TrackedEntities trackedEntities)
    {
        for (Sample sequencingSample : trackedEntities.getSequencingSamplesProcessed())
        {
            for (EMailAddress recipient : getSequencingSampleTrackingRecipients(Collections
                    .singleton(sequencingSample)))
            {
                final EntityTrackingEmailData emailData =
                        getOrCreateRecipientEmailData(result, recipient);
                emailData.addSequencingSampleProcessed(sequencingSample);
            }
        }
    }

    /** Puts tracked data sets grouped by recipient into <var>result</var>. */
    private static void groupDataSetSamples(Map<EMailAddress, EntityTrackingEmailData> result,
            TrackedEntities trackedEntities)
    {
        for (AbstractExternalData dataSet : trackedEntities.getDataSets())
        {
            for (EMailAddress recipient : getDataSetTrackingRecipients(dataSet))
            {
                final EntityTrackingEmailData emailData =
                        getOrCreateRecipientEmailData(result, recipient);
                emailData.addDataSet(dataSet);
            }
        }
    }

    private static EntityTrackingEmailData getOrCreateRecipientEmailData(
            Map<EMailAddress, EntityTrackingEmailData> dataByRecipient, EMailAddress recipient)
    {
        EntityTrackingEmailData emailDataOrNull = dataByRecipient.get(recipient);
        if (emailDataOrNull == null)
        {
            emailDataOrNull = new EntityTrackingEmailData(recipient);
            dataByRecipient.put(recipient, emailDataOrNull);
        }
        return emailDataOrNull;
    }

    /**
     * Returns a set of emails of recipients that should get a tracking information about given <var>sequencingSample</var>.<br>
     */
    // NOTE: Set is needed because one recipient can occur in many roles for one sample
    private static Set<EMailAddress> getSequencingSampleTrackingRecipients(
            Collection<Sample> sequencingSamples)
    {
        assert sequencingSamples != null;

        final Set<EMailAddress> recipients = new HashSet<EMailAddress>();

        // Recipients are taken from properties of the sequencing sample.
        final Set<String> recipientPropertyTypeCodes = new HashSet<String>();
        recipientPropertyTypeCodes.add(CONTACT_PERSON_EMAIL);
        recipientPropertyTypeCodes.add(PRINCIPAL_INVESTIGATOR_EMAIL);
        recipientPropertyTypeCodes.add(CONTACT_DATA_MANAGER_EMAIL);

        for (Sample sequencingSample : sequencingSamples)
        {
            for (Map.Entry<String, String> space : recipientsBySpace.entrySet())
            {
                if (sequencingSample.getIdentifier().split("/")[0].equals(space.getKey())) // 20.10 Bugfix to get Space Code
                {
                    String spaceRecipientOrNull =
                            recipientsBySpace.get(space.getKey());
                    if (spaceRecipientOrNull != null)
                    {
                        EMailAddress myEmail = new EMailAddress(spaceRecipientOrNull);
                        recipients.add(myEmail);
                    }
                }
            }

            for (IEntityProperty property : sequencingSample.getProperties())
            {
                final String propertyCode = property.getPropertyType().getCode();
                final String propertyValue = property.tryGetAsString();
                if (recipientPropertyTypeCodes.contains(propertyCode))
                {
                    EMailAddress myEmail = new EMailAddress(propertyValue);
                    recipients.add(myEmail);
                }
            }
        }

        return recipients;
    }

    /**
     * Returns a set of emails of recipients that should get a tracking information about given <var>flowLaneSample</var>.
     */
    private static Set<EMailAddress> getFlowLaneSampleTrackingRecipients(Sample flowLaneSample)
    {
        // Recipients are taken from properties of sequencing sample
        // that is a parent of the flow lane sample.
        assert flowLaneSample != null;
        return getSequencingSampleTrackingRecipients(flowLaneSample.getParents());
    }

    /**
     * Returns a set of emails of recipients that should get a tracking information about given <var>dataSet</var>.
     */
    private static Set<EMailAddress> getDataSetTrackingRecipients(AbstractExternalData dataSet)
    {
        // Recipients are taken from properties of sequencing sample
        // that is a parent of a flow lane sample connected directly with the data set.
        assert dataSet != null;
        return getFlowLaneSampleTrackingRecipients(dataSet.getSample());
    }

}