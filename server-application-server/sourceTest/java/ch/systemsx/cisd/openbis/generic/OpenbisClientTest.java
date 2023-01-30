package ch.systemsx.cisd.openbis.generic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.time.DateUtils;

import ch.systemsx.cisd.common.spring.HttpInvokerUtils;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.ITrackingServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleParentWithDerived;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TrackingDataSetCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TrackingSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;
import ch.systemsx.cisd.openbis.generic.shared.util.TestInstanceHostUtils;
import ch.systemsx.cisd.openbis.plugin.generic.shared.IGenericServer;

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

/**
 * Demo of the openBIS client which uses HTTPInvoker. This test is not run automatically.
 * 
 * @author Tomasz Pylak
 * @author Piotr Buczek
 */
public class OpenbisClientTest
{
    private static final String USER_ID = "test";

    private static final String USER_PASSWORD = "test";

    private static final String SERVER_URL = TestInstanceHostUtils.getOpenBISUrl() + "/openbis";

    // CommonServiceServer
    private static final String COMMON_SERVICE_PATH = SERVER_URL + "/rmi-common";

    // GenericServiceServer
    private static final String GENERIC_SERVICE_PATH = SERVER_URL + "/rmi-plugin-generic";

    // TrackingServiceServer
    private static final String TRACKING_SERVICE_PATH = SERVER_URL + "/rmi-tracking";

    private static final long SERVER_TIMEOUT = 5 * DateUtils.MILLIS_PER_MINUTE;

    public static void main(String[] args)
    {
        testCommonServerService(COMMON_SERVICE_PATH);
        testGenericServerService(GENERIC_SERVICE_PATH);
        testTrackingServerService(TRACKING_SERVICE_PATH);
    }

    private static void testCommonServerService(String serviceURL)
    {
        System.out.println("TEST CommonServerService: " + serviceURL);
        ICommonServer commonServer =
                HttpInvokerUtils.createServiceStub(ICommonServer.class, serviceURL, SERVER_TIMEOUT);
        SessionContextDTO session = commonServer.tryAuthenticate(USER_ID, USER_PASSWORD);

        List<Person> persons = commonServer.listPersons(session.getSessionToken());
        for (Person p : persons)
        {
            System.out.println(p.getUserId());
        }
    }

    private static void testGenericServerService(String serviceURL)
    {
        System.out.println("\nTEST GenericServerService: " + serviceURL);
        IGenericServer genericServer =
                HttpInvokerUtils
                        .createServiceStub(IGenericServer.class, serviceURL, SERVER_TIMEOUT);
        SessionContextDTO session = genericServer.tryAuthenticate(USER_ID, USER_PASSWORD);

        SampleParentWithDerived sampleInfo =
                genericServer.getSampleInfo(session.getSessionToken(), new TechId(1L));
        System.out.println(sampleInfo.getParent().getCode());
    }

    private static void testTrackingServerService(String serviceURL)
    {
        System.out.println("\nTEST TrackingServerService: " + serviceURL);
        ITrackingServer trackingServer =
                HttpInvokerUtils.createServiceStub(ITrackingServer.class, serviceURL,
                        SERVER_TIMEOUT);
        SessionContextDTO session = trackingServer.tryAuthenticate(USER_ID, USER_PASSWORD);

        final String sampleTypeCode = "CELL_PLATE";
        final int lastSeenDataSetId = 0; // compare with 3

        final TrackingDataSetCriteria dataSetCriteria =
                new TrackingDataSetCriteria(sampleTypeCode, lastSeenDataSetId);
        final List<AbstractExternalData> dataSets =
                trackingServer.listDataSets(session.getSessionToken(), dataSetCriteria);
        System.out
                .println(TrackingHelper.trackedEntitiesInformation(dataSets, EntityKind.DATA_SET));

        final String propertyTypeCode = "IS_VALID";
        final String propertyValue = "true";
        final Collection<Long> alreadyTracked = Arrays.asList(new Long[]
        { 986L, 987L });

        final TrackingSampleCriteria newSampleCriteria =
                new TrackingSampleCriteria(sampleTypeCode, propertyTypeCode, propertyValue,
                        alreadyTracked);
        final List<Sample> newSamples =
                trackingServer.listSamples(session.getSessionToken(), newSampleCriteria);
        System.out
                .println(TrackingHelper.trackedEntitiesInformation(newSamples, EntityKind.SAMPLE));
    }

    /**
     * Helper class encapsulating methods used for producing readable information about entities returned by {@link ITrackingServer} methods.
     * 
     * @author Piotr Buczek
     */
    private static class TrackingHelper
    {

        private static final String INDENT = "  ";

        private static String trackedEntitiesInformation(
                List<? extends IEntityInformationHolder> entities, EntityKind entityKind)
        {
            if (entities == null || entities.size() == 0)
            {
                return String.format("\nNo %ss tracked.", entityKind.getDescription());
            } else
            {
                List<String> entityInfo = new ArrayList<String>(entities.size());
                for (IEntityInformationHolder entity : entities)
                {
                    entityInfo.add(toString(entity));
                }
                return String.format("\nTracked %d %s(s): \n%s", entityInfo.size(),
                        entityKind.getDescription(), StringUtils.join(entityInfo, "\n"));
            }
        }

        private static String toString(IEntityInformationHolder entity)
        {
            switch (entity.getEntityKind())
            {
                case SAMPLE:
                    return toString((Sample) entity);
                case DATA_SET:
                    return toString((AbstractExternalData) entity);
                default:
                    throw new IllegalArgumentException(entity.getEntityKind()
                            + " is not supported ");
            }
        }

        private static String toString(Sample sample)
        {
            return toString(sample, INDENT);
        }

        private static String toString(Sample sample, String indent)
        {
            final StringBuilder sb = new StringBuilder();
            ToStringBuilder builder = new ToStringBuilder(sample, ToStringStyle.SHORT_PREFIX_STYLE);
            builder.append("id", sample.getId());
            builder.append(" code", sample.getCode());
            builder.append(" identifier", sample.getIdentifier());
            builder.append(" type", sample.getSampleType());
            builder.append(" permlink", sample.getPermlink());
            builder.append(" properties", toString(sample.getProperties()));
            sb.append(builder.toString());
            final String newIndent = indent + INDENT;
            if (sample.getContainer() != null)
            {
                sb.append("\n" + indent + "container");
                sb.append(toString(sample.getContainer(), newIndent));
            }
            if (sample.getGeneratedFrom() != null)
            {
                sb.append("\n" + indent + "parent");
                sb.append(toString(sample.getGeneratedFrom(), newIndent));
            }
            return sb.toString();
        }

        private static String toString(AbstractExternalData dataSet)
        {
            final StringBuilder sb = new StringBuilder();
            ToStringBuilder builder =
                    new ToStringBuilder(dataSet, ToStringStyle.SHORT_PREFIX_STYLE);
            builder.append("id", dataSet.getId());
            builder.append(" code", dataSet.getCode());
            builder.append(" type", dataSet.getDataSetType());
            builder.append(" permlink", dataSet.getPermlink());
            builder.append(" properties", toString(dataSet.getProperties()));
            sb.append(builder.toString());
            if (dataSet.getSample() != null)
            {
                final String indent = INDENT;
                final String newIndent = indent + INDENT;
                sb.append("\n" + indent + toString(dataSet.getSample(), newIndent));
            }
            return sb.toString();
        }

        private static String toString(List<IEntityProperty> properties)
        {
            if (properties == null)
            {
                return null;
            } else
            {
                final StringBuilder sb = new StringBuilder();
                sb.append("{");
                for (IEntityProperty property : properties)
                {
                    sb.append(property.getPropertyType() + "=" + property.getValue() + ",");
                }
                if (properties.size() > 0)
                {
                    sb.setLength(sb.length() - 1);
                }
                sb.append("}");
                return sb.toString();
            }
        }

    }
}
