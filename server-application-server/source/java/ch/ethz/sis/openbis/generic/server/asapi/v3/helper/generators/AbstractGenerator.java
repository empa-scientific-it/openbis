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
        gen.addClassForImport(Map.class);
        gen.addPluralFetchedField("Map<String, Material>", Map.class.getName(),
                "materialProperties", "Material Properties",
                MaterialFetchOptions.class).withInterface(IMaterialPropertiesHolder.class);
        gen.addPluralFetchedField("Map<String, Sample[]>", Map.class.getName(), "sampleProperties",
                "Sample Properties",
                SampleFetchOptions.class);
        gen.addClassForImport(Map.class);
        gen.addClassForImport(HashMap.class);
        gen.addClassForImport(Material.class);
        gen.addClassForImport(Sample.class);


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
