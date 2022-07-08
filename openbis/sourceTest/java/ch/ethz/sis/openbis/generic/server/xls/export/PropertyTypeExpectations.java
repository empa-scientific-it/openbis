package ch.ethz.sis.openbis.generic.server.xls.export;

import static ch.ethz.sis.openbis.generic.server.xls.export.ExportableKind.PROPERTY_TYPE;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyAssignmentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.Vocabulary;
import ch.systemsx.cisd.openbis.generic.server.business.bo.CollectionMatcher;

class PropertyTypeExpectations extends Expectations
{

    public PropertyTypeExpectations(final IApplicationServerApi api, final boolean exportReferred)
    {
        allowing(api).getPropertyTypes(with(XLSExportTest.SESSION_TOKEN), with(new CollectionMatcher<>(List.of(
                new PropertyTypePermId("ANNOTATION.SYSTEM.COMMENTS"),
                new PropertyTypePermId("ANNOTATION.SYSTEM.QUANTITY"),
                new PropertyTypePermId("ANNOTATION.SYSTEM.PLASMID_ANNOTATION"),
                new PropertyTypePermId("ANNOTATION.SYSTEM.PLASMID_RELATIONSHIP")))),
                with(any(PropertyTypeFetchOptions.class)));

        will(new CustomAction("getting property types")
        {

            @Override
            public Object invoke(final Invocation invocation) throws Throwable
            {
                final PropertyTypeFetchOptions fetchOptions = (PropertyTypeFetchOptions) invocation.getParameter(2);
                final PropertyType[] propertyTypes = new PropertyType[4];

                propertyTypes[0] = new PropertyType();
                propertyTypes[0].setFetchOptions(fetchOptions);
                propertyTypes[0].setCode("ANNOTATION.SYSTEM.COMMENTS");
                propertyTypes[0].setLabel("Comments");
                propertyTypes[0].setDataType(DataType.MULTILINE_VARCHAR);
                propertyTypes[0].setVocabulary(null);
                propertyTypes[0].setDescription("Comments");
                propertyTypes[0].setMetaData(null);

                propertyTypes[1] = new PropertyType();
                propertyTypes[1].setFetchOptions(fetchOptions);
                propertyTypes[1].setCode("ANNOTATION.SYSTEM.QUANTITY");
                propertyTypes[1].setLabel("Quantity");
                propertyTypes[1].setDataType(DataType.VARCHAR);
                propertyTypes[1].setVocabulary(null);
                propertyTypes[1].setDescription("Quantity");
                propertyTypes[1].setMetaData(Collections.singletonMap("a", "b"));

                propertyTypes[2] = new PropertyType();
                propertyTypes[2].setFetchOptions(fetchOptions);
                propertyTypes[2].setCode("ANNOTATION.SYSTEM.PLASMID_ANNOTATION");
                propertyTypes[2].setLabel("Plasmid Annotation");
                propertyTypes[2].setDataType(DataType.VARCHAR);
                propertyTypes[2].setVocabulary(null);
                propertyTypes[2].setDescription("Plasmid Annotation");
                propertyTypes[2].setMetaData(null);

                final Vocabulary vocabulary = new Vocabulary();
                vocabulary.setCode("ANNOTATION.PLASMID_RELATIONSHIP");

                propertyTypes[3] = new PropertyType();
                propertyTypes[3].setFetchOptions(fetchOptions);
                propertyTypes[3].setCode("ANNOTATION.SYSTEM.PLASMID_RELATIONSHIP");
                propertyTypes[3].setLabel("Plasmid Relationship");
                propertyTypes[3].setDataType(DataType.CONTROLLEDVOCABULARY);
                propertyTypes[3].setVocabulary(vocabulary);
                propertyTypes[3].setDescription("Plasmid Relationship");
                propertyTypes[3].setMetaData(Collections.singletonMap("a", "b"));

                return Arrays.stream(propertyTypes).collect(Collectors.toMap(PropertyType::getPermId,
                        Function.identity(), (propertyType1, propertyType2) -> propertyType2, LinkedHashMap::new));
            }

        });
    }

}
