/*
 * Copyright ETH 2014 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.server.asapi.v3.helper.generators;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.Attachment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.fetchoptions.AttachmentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.IObjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IAttachmentsHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.ICodeHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IDescriptionHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IExperimentHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IMaterialPropertiesHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IModificationDateHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IModifierHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPermIdHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPropertiesHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPropertyAssignmentsHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IRegistrationDateHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IRegistratorHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.ISampleHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.ISemanticAnnotationsHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.ISpaceHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.ITagsHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.Material;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.fetchoptions.MaterialFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.fetchoptions.PersonFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyAssignmentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.SemanticAnnotation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.fetchoptions.SemanticAnnotationFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.Tag;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.fetchoptions.TagFetchOptions;

public class AbstractGenerator
{

    public static void addModificationDate(DtoGenerator gen)
    {
        gen.addSimpleField(Date.class, "modificationDate")
                .withInterface(IModificationDateHolder.class);
    }

    public static void addModifier(DtoGenerator gen)
    {
        gen.addFetchedField(Person.class, "modifier", "Modifier", PersonFetchOptions.class)
                .withInterface(IModifierHolder.class);
    }

    public static void addRegistrationDate(DtoGenerator gen)
    {
        gen.addSimpleField(Date.class, "registrationDate")
                .withInterface(IRegistrationDateHolder.class);
    }

    public static void addRegistrator(DtoGenerator gen)
    {
        gen.addFetchedField(Person.class, "registrator", "Registrator", PersonFetchOptions.class)
                .withInterface(IRegistratorHolder.class);
    }

    public static void addCode(DtoGenerator gen)
    {
        gen.addSimpleField(String.class, "code").withInterface(ICodeHolder.class);
    }

    public static void addPermId(DtoGenerator gen, Class<? extends IObjectId> permIdClass)
    {
        gen.addSimpleField(permIdClass, "permId").withInterface(IPermIdHolder.class);
    }

    public static void addExperiment(DtoGenerator gen)
    {
        gen.addFetchedField(Experiment.class, "experiment", "Experiment",
                ExperimentFetchOptions.class).withInterface(IExperimentHolder.class);
    }

    public static void addSample(DtoGenerator gen)
    {
        gen.addFetchedField(Sample.class, "sample", "Sample", SampleFetchOptions.class)
                .withInterface(ISampleHolder.class);
    }

    public static void addSpace(DtoGenerator gen)
    {
        gen.addFetchedField(Space.class, "space", "Space", SpaceFetchOptions.class)
                .withInterface(ISpaceHolder.class);
    }

    public static void addTags(DtoGenerator gen)
    {
        gen.addPluralFetchedField("Set<Tag>", Set.class.getName(), "tags", "Tags",
                        TagFetchOptions.class)
                .withInterface(ITagsHolder.class);
        gen.addClassForImport(Tag.class);
        gen.addClassForImport(Set.class);
    }

    public static void addMetaData(DtoGenerator gen)
    {
        gen.addSimpleField("Map<String, String>", "MetaData", Map.class.getName(), "metaData");
        gen.addClassForImport(Map.class);
    }

    public static void addProperties(DtoGenerator gen)
    {
        gen.addPluralFetchedField("Map<String, String>", Map.class.getName(), "properties",
                        "Properties", PropertyFetchOptions.class)
                .withInterface(IPropertiesHolder.class);
        gen.addClassForImport(Map.class);
        gen.addPluralFetchedField("Map<String, Material>", Map.class.getName(),
                "materialProperties", "Material Properties",
                MaterialFetchOptions.class).withInterface(IMaterialPropertiesHolder.class);
        gen.addPluralFetchedField("Map<String, Sample>", Map.class.getName(), "sampleProperties",
                "Sample Properties",
                SampleFetchOptions.class);
        gen.addClassForImport(Map.class);
        gen.addClassForImport(HashMap.class);
        gen.addClassForImport(Material.class);
        gen.addClassForImport(Sample.class);

        gen.addAdditionalMethod("@Override\n"
                + "    public String getProperty(String propertyName)\n"
                + "    {\n"
                + "        return getProperties() != null ? getProperties().get(propertyName) : null;\n"
                + "    }");

        gen.addAdditionalMethod("@Override\n"
                + "    public void setProperty(String propertyName, String propertyValue)\n"
                + "    {\n"
                + "        if (properties == null)\n"
                + "        {\n"
                + "            properties = new HashMap<String, String>();\n"
                + "        }\n"
                + "        properties.put(propertyName, propertyValue);\n"
                + "    }");

        gen.addAdditionalMethod("@Override\n"
                + "    public Material getMaterialProperty(String propertyName)\n"
                + "    {\n"
                + "        return getMaterialProperties() != null ? getMaterialProperties().get(propertyName) : null;\n"
                + "    }");

        gen.addAdditionalMethod("@Override\n"
                + "    public void setMaterialProperty(String propertyName, Material propertyValue)\n"
                + "    {\n"
                + "        if (materialProperties == null)\n"
                + "        {\n"
                + "            materialProperties = new HashMap<String, Material>();\n"
                + "        }\n"
                + "        materialProperties.put(propertyName, propertyValue);\n"
                + "    }");

        gen.addClassForImport(Objects.class);
        gen.addClassForImport(ZonedDateTime.class);
        gen.addClassForImport(DateTimeFormatter.class);
        gen.addClassForImport(Arrays.class);

        // Integer
        gen.addAdditionalMethod(
                addPlaceholderTypedGetProperty("Long", "getIntegerProperty", "",
                        "Long.parseLong(propertyValue)"));
        gen.addAdditionalMethod(
                addPlaceholderTypedSetProperty("Long", "setIntegerProperty", "",
                        "Objects.toString(propertyValue, null)"));

        // Varchar
        gen.addAdditionalMethod("@Override\n"
                + "    public String getVarcharProperty(String propertyName)\n"
                + "    {\n"
                + "        return getProperty(propertyName);\n"
                + "    }");

        gen.addAdditionalMethod("@Override\n"
                + "    public void setVarcharProperty(String propertyName, String propertyValue)\n"
                + "    {\n"
                + "        setProperty(propertyName, propertyValue);\n"
                + "    }");

        // Multiline Varchar
        gen.addAdditionalMethod("@Override\n"
                + "    public String getMultilineVarcharProperty(String propertyName)\n"
                + "    {\n"
                + "        return getProperty(propertyName);\n"
                + "    }");

        gen.addAdditionalMethod("@Override\n"
                + "    public void setMultilineVarcharProperty(String propertyName, String propertyValue)\n"
                + "    {\n"
                + "        setProperty(propertyName, propertyValue);\n"
                + "    }");

        // Real
        gen.addAdditionalMethod(
                addPlaceholderTypedGetProperty("Double", "getRealProperty", "",
                        "Double.parseDouble(propertyValue)"));
        gen.addAdditionalMethod(
                addPlaceholderTypedSetProperty("Double", "setRealProperty", "",
                        "Objects.toString(propertyValue, null)"));

        // Timestamp
        gen.addAdditionalMethod("@Override\n"
                + "    public ZonedDateTime getTimestampProperty(String propertyName)\n"
                + "    {\n"
                + "        String propertyValue = getProperty(propertyName);\n"
                + "        return propertyValue == null ? null : ZonedDateTime.parse(getProperty(propertyName));\n"
                + "    }");

        gen.addAdditionalMethod("@Override\n"
                + "    public void setTimestampProperty(String propertyName, ZonedDateTime propertyValue)\n"
                + "    {\n"
                + "        String value = (propertyValue == null) ? null : propertyValue.format(DateTimeFormatter.ofPattern(\"yyyy-MM-dd HH:mm:ssX\"));\n"
                + "        setProperty(propertyName, value);\n"
                + "    }");


        // Boolean
        gen.addAdditionalMethod(
                addPlaceholderTypedGetProperty("Boolean", "getBooleanProperty", "",
                        "Boolean.parseBoolean(propertyValue)"));
        gen.addAdditionalMethod(
                addPlaceholderTypedSetProperty("Boolean", "setBooleanProperty", "",
                        "Objects.toString(propertyValue, null)"));


        // Controlled Vocabulary
        gen.addAdditionalMethod("@Override\n"
                + "    public String getControlledVocabularyProperty(String propertyName)\n"
                + "    {\n"
                + "        return getProperty(propertyName);\n"
                + "    }");

        gen.addAdditionalMethod("@Override\n"
                + "    public void setControlledVocabularyProperty(String propertyName, String propertyValue)\n"
                + "    {\n"
                + "        setProperty(propertyName, propertyValue);\n"
                + "    }");

        // Sample
        gen.addAdditionalMethod("@Override\n"
                + "    public SamplePermId getSampleProperty(String propertyName)\n"
                + "    {\n"
                + "        String propertyValue = getProperty(propertyName);\n"
                + "        return (propertyValue == null || propertyValue.trim().isEmpty()) ? null : new SamplePermId(propertyValue);\n"
                + "    }");

        gen.addAdditionalMethod("@Override\n"
                + "    public void setSampleProperty(String propertyName, SamplePermId propertyValue)\n"
                + "    {\n"
                + "        setProperty(propertyName, propertyValue == null ? null : propertyValue.getPermId());\n"
                + "    }");

        // Hyperlink
        gen.addAdditionalMethod("@Override\n"
                + "    public String getHyperlinkProperty(String propertyName)\n"
                + "    {\n"
                + "        return getProperty(propertyName);\n"
                + "    }");

        gen.addAdditionalMethod("@Override\n"
                + "    public void setHyperlinkProperty(String propertyName, String propertyValue)\n"
                + "    {\n"
                + "        setProperty(propertyName, propertyValue);\n"
                + "    }");

        // Xml
        gen.addAdditionalMethod("@Override\n"
                + "    public String getXmlProperty(String propertyName)\n"
                + "    {\n"
                + "        return getProperty(propertyName);\n"
                + "    }");

        gen.addAdditionalMethod("@Override\n"
                + "    public void setXmlProperty(String propertyName, String propertyValue)\n"
                + "    {\n"
                + "        setProperty(propertyName, propertyValue);\n"
                + "    }");

        // Integer array
        gen.addAdditionalMethod(
                addPlaceholderTypedGetProperty("Long[]", "getIntegerArrayProperty", "",
                        "Arrays.stream(propertyValue.split(\",\")).map(String::trim).map(Long::parseLong).toArray(Long[]::new)"));
        gen.addAdditionalMethod(
                addPlaceholderTypedSetProperty("Long[]", "setIntegerArrayProperty", "",
                        "propertyValue == null ? null : Arrays.stream(propertyValue).map(Object::toString).reduce((a,b) -> a + \", \" + b).get()"));

        // Real array
        gen.addAdditionalMethod(
                addPlaceholderTypedGetProperty("Double[]", "getRealArrayProperty", "",
                        "Arrays.stream(propertyValue.split(\",\")).map(String::trim).map(Double::parseDouble).toArray(Double[]::new)"));
        gen.addAdditionalMethod(
                addPlaceholderTypedSetProperty("Double[]", "setRealArrayProperty", "",
                        "propertyValue == null ? null : Arrays.stream(propertyValue).map(Object::toString).reduce((a,b) -> a + \", \" + b).get()"));

        // String array
        gen.addAdditionalMethod(
                addPlaceholderTypedGetProperty("String[]", "getStringArrayProperty", "",
                        "Arrays.stream(propertyValue.split(\",\")).map(String::trim).toArray(String[]::new)"));
        gen.addAdditionalMethod(
                addPlaceholderTypedSetProperty("String[]", "setStringArrayProperty", "",
                        "propertyValue == null ? null : Arrays.stream(propertyValue).reduce((a,b) -> a + \", \" + b).get()"));


        // Timestamp array
        gen.addAdditionalMethod("@Override\n"
                + "    public ZonedDateTime[] getTimestampArrayProperty(String propertyName)\n"
                + "    {\n"
                + "        String propertyValue = getProperty(propertyName);\n"
                + "        return propertyValue == null ? null : Arrays.stream(propertyValue.split(\",\"))\n"
                + "             .map(String::trim)\n"
                + "             .map(dateTime -> ZonedDateTime.parse(dateTime, DateTimeFormatter.ofPattern(\"yyyy-MM-dd HH:mm:ss X\")))\n"
                + "             .toArray(ZonedDateTime[]::new);\n"
                + "    }");

        gen.addAdditionalMethod("@Override\n"
                + "    public void setTimestampArrayProperty(String propertyName, ZonedDateTime[] propertyValue)\n"
                + "    {\n"
                + "        String value = (propertyValue == null) ? null : Arrays.stream(propertyValue)\n"
                + "                 .map(dateTime -> dateTime.format(DateTimeFormatter.ofPattern(\"yyyy-MM-dd HH:mm:ssX\")))\n"
                + "                 .reduce((a,b) -> a + \", \" + b)\n"
                + "                 .get();\n"
                + "        setProperty(propertyName, value);\n"
                + "    }");

        // Json
        gen.addAdditionalMethod("@Override\n"
                + "    public String getJsonProperty(String propertyName)\n"
                + "    {\n"
                + "        return getProperty(propertyName);\n"
                + "    }");

        gen.addAdditionalMethod("@Override\n"
                + "    public void setJsonProperty(String propertyName, String propertyValue)\n"
                + "    {\n"
                + "        setProperty(propertyName, propertyValue);\n"
                + "    }");
    }

    private static String addPlaceholderTypedGetProperty(String type, String methodName,
            String throwsOptional, String translationFunction)
    {
        return "@Override\n"
                + "    public " + type + " " + methodName + "(String propertyName)" + throwsOptional + "\n"
                + "    {\n"
                + "        String propertyValue = getProperty(propertyName);\n"
                + "        return (propertyValue == null || propertyValue.trim().isEmpty()) ? null : " + translationFunction + ";\n"
                + "    }";
    }

    private static String addPlaceholderTypedSetProperty(String type, String methodName,
            String throwsOptional, String translationFunction)
    {
        return "@Override\n"
                + "    public void " + methodName + "(String propertyName, " + type + " propertyValue)" + throwsOptional + "\n"
                + "    {\n"
                + "        setProperty(propertyName, " + translationFunction + ");\n"
                + "    }";
    }

    public static void addPropertyAssignments(DtoGenerator gen)
    {
        gen.addPluralFetchedField("List<PropertyAssignment>", List.class.getName(),
                        "propertyAssignments",
                        "Property assigments", PropertyAssignmentFetchOptions.class)
                .withInterface(IPropertyAssignmentsHolder.class);
        gen.addClassForImport(PropertyAssignment.class);
    }

    public static void addSemanticAnnotations(DtoGenerator gen)
    {
        gen.addPluralFetchedField("List<SemanticAnnotation>", List.class.getName(),
                        "semanticAnnotations",
                        "Semantic annotations", SemanticAnnotationFetchOptions.class)
                .withInterface(ISemanticAnnotationsHolder.class);
        gen.addClassForImport(SemanticAnnotation.class);
    }

    public static void addAttachments(DtoGenerator gen)
    {
        gen.addPluralFetchedField("List<Attachment>", List.class.getName(), "attachments",
                        "Attachments", AttachmentFetchOptions.class)
                .withInterface(IAttachmentsHolder.class);
        gen.addClassForImport(Attachment.class);
        gen.addClassForImport(List.class);
    }

    public static void addDescription(DtoGenerator gen)
    {
        gen.addSimpleField(String.class, "description").withInterface(IDescriptionHolder.class);
    }

}
