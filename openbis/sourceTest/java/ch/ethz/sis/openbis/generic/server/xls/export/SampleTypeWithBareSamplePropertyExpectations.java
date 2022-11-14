package ch.ethz.sis.openbis.generic.server.xls.export;

import static ch.ethz.sis.openbis.generic.server.xls.export.XLSExportTest.DATE_RANGE_VALIDATION_SCRIPT_CONTENT;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.Plugin;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.DataType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyAssignmentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.SampleType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleTypeFetchOptions;
import ch.systemsx.cisd.openbis.generic.server.business.bo.CollectionMatcher;

class SampleTypeWithBareSamplePropertyExpectations extends Expectations
{

    public SampleTypeWithBareSamplePropertyExpectations(final IApplicationServerApi api, final boolean exportReferred)
    {
        if (exportReferred)
        {
            allowing(api).getSampleTypes(with(XLSExportTest.SESSION_TOKEN), with(new CollectionMatcher<>(
                            Collections.singletonList(new EntityTypePermId("PERSON", EntityKind.SAMPLE)))),
                    with(any(SampleTypeFetchOptions.class)));
        }

        allowing(api).getSampleTypes(with(XLSExportTest.SESSION_TOKEN), with(new CollectionMatcher<>(
                        Collections.singletonList(new EntityTypePermId("COURSE", EntityKind.SAMPLE)))),
                with(any(SampleTypeFetchOptions.class)));

        will(new CustomAction("getting course sample types")
        {

            @Override
            public Object invoke(final Invocation invocation) throws Throwable
            {
                final SampleTypeFetchOptions fetchOptions = (SampleTypeFetchOptions) invocation.getParameter(2);
                final SampleType sampleType = new SampleType();
                sampleType.setFetchOptions(fetchOptions);
                sampleType.setPermId(new EntityTypePermId("COURSE", EntityKind.SAMPLE));
                sampleType.setCode("COURSE");
                sampleType.setDescription("Course");
                sampleType.setAutoGeneratedCode(false);
                sampleType.setPropertyAssignments(getCoursePropertyAssignments(fetchOptions));
                final Plugin validationPlugin = new Plugin();
                validationPlugin.setFetchOptions(fetchOptions.withValidationPlugin());
                validationPlugin.setName("date_range_validation");
                validationPlugin.setScript(DATE_RANGE_VALIDATION_SCRIPT_CONTENT);
                sampleType.setValidationPlugin(validationPlugin);
                return Collections.singletonMap(sampleType.getPermId(), sampleType);
            }

        });
    }

    private List<PropertyAssignment> getCoursePropertyAssignments(final SampleTypeFetchOptions fetchOptions)
    {
        final PropertyAssignmentFetchOptions propertyAssignmentFetchOptions = fetchOptions.withPropertyAssignments();
        final PropertyTypeFetchOptions propertyTypeFetchOptions = propertyAssignmentFetchOptions.withPropertyType();
        propertyTypeFetchOptions.withVocabulary();

        propertyAssignmentFetchOptions.withPlugin().withScript();

        final PropertyAssignment[] propertyAssignments = new PropertyAssignment[5];

        propertyAssignments[0] = PropertyAssignmentFactory.createPropertyAssignment(
                fetchOptions.withPropertyAssignments());
        propertyAssignments[0].getPropertyType().setCode("$NAME");
        propertyAssignments[0].setMandatory(true);
        propertyAssignments[0].setShowInEditView(true);
        propertyAssignments[0].setSection("General info");
        propertyAssignments[0].getPropertyType().setLabel("Name");
        propertyAssignments[0].getPropertyType().setDataType(DataType.VARCHAR);
        propertyAssignments[0].getPropertyType().setDescription("Name");

        propertyAssignments[1] = PropertyAssignmentFactory.createPropertyAssignment(
                fetchOptions.withPropertyAssignments());
        propertyAssignments[1].getPropertyType().setCode("OPEN");
        propertyAssignments[1].setMandatory(true);
        propertyAssignments[1].setShowInEditView(true);
        propertyAssignments[1].setSection("General info");
        propertyAssignments[1].getPropertyType().setLabel("Open");
        propertyAssignments[1].getPropertyType().setDataType(DataType.BOOLEAN);
        propertyAssignments[1].getPropertyType().setDescription("Marks the program open for assignment");

        final PropertyAssignmentFetchOptions propertyAssignmentFetchOptions3 =
                fetchOptions.withPropertyAssignments();
        propertyAssignments[2] = PropertyAssignmentFactory.createPropertyAssignment(propertyAssignmentFetchOptions3);
        propertyAssignments[2].getPropertyType().setCode("START_DATE");
        propertyAssignments[2].setMandatory(true);
        propertyAssignments[2].setShowInEditView(true);
        propertyAssignments[2].setSection("General info");
        propertyAssignments[2].getPropertyType().setLabel("Start date");
        propertyAssignments[2].getPropertyType().setDataType(DataType.TIMESTAMP);
        propertyAssignments[2].getPropertyType().setDescription("Start date");

        propertyAssignments[3] = PropertyAssignmentFactory.createPropertyAssignment(
                fetchOptions.withPropertyAssignments());
        propertyAssignments[3].getPropertyType().setCode("END_DATE");
        propertyAssignments[3].setMandatory(true);
        propertyAssignments[3].setShowInEditView(true);
        propertyAssignments[3].setSection("General info");
        propertyAssignments[3].getPropertyType().setLabel("End date");
        propertyAssignments[3].getPropertyType().setDataType(DataType.TIMESTAMP);
        propertyAssignments[3].getPropertyType().setDescription("End date");

        propertyAssignments[4] = PropertyAssignmentFactory.createPropertyAssignment(
                fetchOptions.withPropertyAssignments());
        propertyAssignments[4].getPropertyType().setCode("TEACHER");
        propertyAssignments[4].setMandatory(false);
        propertyAssignments[4].setShowInEditView(true);
        propertyAssignments[4].setSection("General info");
        propertyAssignments[4].getPropertyType().setLabel("Teacher");
        propertyAssignments[4].getPropertyType().setDataType(DataType.SAMPLE);
        propertyAssignments[4].getPropertyType().setDescription("Teacher");

        return Arrays.asList(propertyAssignments);
    }

}
