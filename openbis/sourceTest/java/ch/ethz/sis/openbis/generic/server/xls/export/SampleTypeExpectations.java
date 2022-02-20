package ch.ethz.sis.openbis.generic.server.xls.export;

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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.fetchoptions.PluginFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.DataType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyAssignmentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.SampleType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleTypeFetchOptions;
import ch.systemsx.cisd.openbis.generic.server.business.bo.CollectionMatcher;

class SampleTypeExpectations extends Expectations
{

    public SampleTypeExpectations(final IApplicationServerApi api, final boolean exportReferred)
    {
        allowing(api).getSampleTypes(with(XLSExportTest.SESSION_TOKEN), with(new CollectionMatcher<>(
                        Collections.singletonList(new EntityTypePermId("ENTRY", EntityKind.SAMPLE)))),
                with(any(SampleTypeFetchOptions.class)));

        will(new CustomAction("getting sample types")
        {

            @Override
            public Object invoke(final Invocation invocation) throws Throwable
            {
                final SampleTypeFetchOptions fetchOptions = (SampleTypeFetchOptions) invocation.getParameter(2);
                final PluginFetchOptions pluginFetchOptions = fetchOptions.withValidationPlugin();

                final SampleType sampleType = new SampleType();
                sampleType.setFetchOptions(fetchOptions);
                sampleType.setCode("ENTRY");
                sampleType.setAutoGeneratedCode(true);
                sampleType.setGeneratedCodePrefix("ENTRY");
                sampleType.setPropertyAssignments(getPropertyAssignments(fetchOptions));

                final Plugin validationPlugin = new Plugin();
                validationPlugin.setScript("test.py");
                validationPlugin.setFetchOptions(pluginFetchOptions);

                sampleType.setValidationPlugin(validationPlugin);

                return Collections.singletonMap(new EntityTypePermId("ENTRY"), sampleType);
            }

            private List<PropertyAssignment> getPropertyAssignments(final SampleTypeFetchOptions fetchOptions)
            {
                final PropertyAssignmentFetchOptions propertyAssignmentFetchOptions =
                        fetchOptions.withPropertyAssignments();
                final PropertyTypeFetchOptions propertyTypeFetchOptions =
                        propertyAssignmentFetchOptions.withPropertyType();
                propertyTypeFetchOptions.withVocabulary();

                final PluginFetchOptions pluginFetchOptions = propertyAssignmentFetchOptions.withPlugin();
                pluginFetchOptions.withScript();

                final PropertyAssignment[] propertyAssignments = new PropertyAssignment[3];

                propertyAssignments[0] = new PropertyAssignment();
                propertyAssignments[0].setFetchOptions(propertyAssignmentFetchOptions);
                propertyAssignments[0].setPropertyType(new PropertyType());
                propertyAssignments[0].getPropertyType().setFetchOptions(propertyTypeFetchOptions);
                propertyAssignments[0].setPlugin(new Plugin());
                propertyAssignments[0].getPlugin().setFetchOptions(pluginFetchOptions);
                propertyAssignments[0].getPropertyType().setCode("$NAME");
                propertyAssignments[0].setMandatory(false);
                propertyAssignments[0].setShowInEditView(true);
                propertyAssignments[0].setSection("General info");
                propertyAssignments[0].getPropertyType().setLabel("Name");
                propertyAssignments[0].getPropertyType().setDataType(DataType.VARCHAR);
                propertyAssignments[0].getPropertyType().setDescription("Name");

                propertyAssignments[1] = new PropertyAssignment();
                propertyAssignments[1].setFetchOptions(propertyAssignmentFetchOptions);
                propertyAssignments[1].setPropertyType(new PropertyType());
                propertyAssignments[1].getPropertyType().setFetchOptions(propertyTypeFetchOptions);
                propertyAssignments[1].setPlugin(new Plugin());
                propertyAssignments[1].getPlugin().setFetchOptions(pluginFetchOptions);
                propertyAssignments[1].getPropertyType().setCode("$DOCUMENT");
                propertyAssignments[1].setMandatory(false);
                propertyAssignments[1].setShowInEditView(true);
                propertyAssignments[1].setSection("General info");
                propertyAssignments[1].getPropertyType().setLabel("Document");
                propertyAssignments[1].getPropertyType().setDataType(DataType.MULTILINE_VARCHAR);
                propertyAssignments[1].getPropertyType().setDescription("Document");
                propertyAssignments[1].getPropertyType().setMetaData(
                        Collections.singletonMap("custom_widget", "Word Processor"));

                propertyAssignments[2] = new PropertyAssignment();
                propertyAssignments[2].setFetchOptions(propertyAssignmentFetchOptions);
                propertyAssignments[2].setPropertyType(new PropertyType());
                propertyAssignments[2].getPropertyType().setFetchOptions(propertyTypeFetchOptions);
                propertyAssignments[2].setPlugin(new Plugin());
                propertyAssignments[2].getPlugin().setFetchOptions(pluginFetchOptions);
                propertyAssignments[2].getPropertyType().setCode("$ANNOTATIONS_STATE");
                propertyAssignments[2].setMandatory(false);
                propertyAssignments[2].setShowInEditView(false);
                propertyAssignments[2].getPropertyType().setLabel("Annotations State");
                propertyAssignments[2].getPropertyType().setDataType(DataType.XML);
                propertyAssignments[2].getPropertyType().setDescription("Annotations State");
                propertyAssignments[2].getPlugin().setScript("print(\"Hello world\");");

                return Arrays.asList(propertyAssignments);
            }

        });
    }

}
