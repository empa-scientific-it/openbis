/*
 * Copyright ETH 2022 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.server.xls.importer.helper;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.SemanticAnnotation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.create.SemanticAnnotationCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.fetchoptions.SemanticAnnotationFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.search.SemanticAnnotationSearchCriteria;
import ch.ethz.sis.openbis.generic.server.xls.importer.ImportOptions;
import ch.ethz.sis.openbis.generic.server.xls.importer.delay.DelayedExecutionDecorator;
import ch.ethz.sis.openbis.generic.server.xls.importer.enums.ImportModes;
import ch.ethz.sis.openbis.generic.server.xls.importer.enums.ImportTypes;
import ch.ethz.sis.openbis.generic.server.xls.importer.semantic.ApplicationServerSemanticAPIExtensions;
import ch.ethz.sis.openbis.generic.server.xls.importer.utils.IAttribute;

import java.util.List;
import java.util.Map;

public class SemanticAnnotationImportHelper extends BasicImportHelper
{

    public enum SemanticAnnotationType { EntityType, PropertyType, EntityTypeProperty }

    private enum Attribute implements IAttribute {
        Code("Code", true),
        OntologyId("Ontology Id", false),
        OntologyVersion("Ontology Version", false),
        OntologyAnnotationId("Ontology Annotation Id", false);

        private final String headerName;

        private final boolean mandatory;

        Attribute(String headerName, boolean mandatory) {
            this.headerName = headerName;
            this.mandatory = mandatory;
        }

        public String getHeaderName() {
            return headerName;
        }
        public boolean isMandatory() {
            return mandatory;
        }
    }

    private final DelayedExecutionDecorator delayedExecutor;

    private SemanticAnnotationType type;

    private EntityTypePermId permIdOrNull;

    public SemanticAnnotationImportHelper(DelayedExecutionDecorator delayedExecutor, ImportModes mode, ImportOptions options)
    {
        super(mode, options);
        this.delayedExecutor = delayedExecutor;
    }

    @Override protected ImportTypes getTypeName()
    {
        return ImportTypes.SEMANTIC_ANNOTATION;
    }

    @Override protected boolean isObjectExist(Map<String, Integer> header, List<String> values)
    {
        boolean insertSemanticAnnotation = false; // Initially we don't need to insert a semantic annotation

        String code = getValueByColumnName(header, values, Attribute.Code);

        String ontologyId = getValueByColumnName(header, values, Attribute.OntologyId);
        String ontologyVersion = getValueByColumnName(header, values, Attribute.OntologyVersion);
        String ontologyAnnotationId = getValueByColumnName(header, values, Attribute.OntologyAnnotationId);

        if (ontologyId != null && !ontologyId.isEmpty() &&
                ontologyVersion != null && !ontologyVersion.isEmpty() &&
                ontologyAnnotationId != null && !ontologyAnnotationId.isEmpty())
        {
            SemanticAnnotationSearchCriteria criteria = new SemanticAnnotationSearchCriteria();
            if (type == SemanticAnnotationType.EntityTypeProperty || type == SemanticAnnotationType.EntityType) {
                criteria.withEntityType().withKind().thatEquals(this.permIdOrNull.getEntityKind());
                criteria.withEntityType().withCode().thatEquals(this.permIdOrNull.getPermId());
            }
            if (type == SemanticAnnotationType.EntityTypeProperty || type == SemanticAnnotationType.PropertyType) {
                criteria.withPropertyType().withCode().thatEquals(code);
            }
            criteria.withPredicateOntologyId().thatEquals(ontologyId);
            criteria.withPredicateOntologyVersion().thatEquals(ontologyVersion);
            criteria.withPredicateAccessionId().thatEquals(ontologyAnnotationId);
            SemanticAnnotation semanticAnnotation = delayedExecutor.getSemanticAnnotation(criteria, new SemanticAnnotationFetchOptions());
            insertSemanticAnnotation = (semanticAnnotation == null); // We insert a semantic annotation
        }

        return !insertSemanticAnnotation; // insertSemanticAnnotation == !isObjectExist
    }

    @Override protected void createObject(Map<String, Integer> headers, List<String> values, int page, int line)
    {
        String code = getValueByColumnName(headers, values, Attribute.Code);

        String ontologyId = getValueByColumnName(headers, values, Attribute.OntologyId);
        String ontologyVersion = getValueByColumnName(headers, values, Attribute.OntologyVersion);
        String ontologyAnnotationId = getValueByColumnName(headers, values, Attribute.OntologyAnnotationId);

        SemanticAnnotationCreation creation = null;

        switch (type) {
            case EntityType:
                creation = ApplicationServerSemanticAPIExtensions.getSemanticSubjectCreation(
                        this.permIdOrNull.getEntityKind(),
                        this.permIdOrNull.getPermId(), // == code
                        ontologyId,
                        ontologyVersion,
                        ontologyAnnotationId);
                break;
            case PropertyType:
                creation = ApplicationServerSemanticAPIExtensions.getSemanticPredicateCreation(
                        code, // Property Code
                        ontologyId,
                        ontologyVersion,
                        ontologyAnnotationId);
                break;
            case EntityTypeProperty:
                creation = ApplicationServerSemanticAPIExtensions.getSemanticPredicateWithSubjectCreation(
                        this.permIdOrNull.getEntityKind(),
                        this.permIdOrNull.getPermId(),
                        code, // Property Code
                        ontologyId,
                        ontologyVersion,
                        ontologyAnnotationId);
                break;
        }
        delayedExecutor.createSemanticAnnotation(creation, page, line);
    }

    @Override protected void updateObject(Map<String, Integer> header, List<String> values, int page, int line)
    {
        // do only create
    }

    @Override protected void validateHeader(Map<String, Integer> headers)
    {
        // not validated here
    }

    public void importBlockForEntityType(List<List<String>> page, int pageIndex, int start, int end, ImportTypes importTypes)
    {
        type = SemanticAnnotationType.EntityType;
        Map<String, Integer> header = parseHeader(page.get(start), false);
        String code = getValueByColumnName(header, page.get(start + 1), Attribute.Code);

        switch (importTypes)
        {
            case EXPERIMENT_TYPE:
                this.permIdOrNull = new EntityTypePermId(code, EntityKind.EXPERIMENT);
                break;
            case SAMPLE_TYPE:
                this.permIdOrNull = new EntityTypePermId(code, EntityKind.SAMPLE);
                break;
            case DATASET_TYPE:
                this.permIdOrNull = new EntityTypePermId(code, EntityKind.DATA_SET);
                break;
            default:
                throw new RuntimeException("Should never happen!");
        }

        super.importBlock(page, pageIndex, start, end);
    }

    public void importBlockForEntityTypeProperty(List<List<String>> page, int pageIndex, int start, int end, ImportTypes importTypes)
    {
        type = SemanticAnnotationType.EntityTypeProperty;
        Map<String, Integer> header = parseHeader(page.get(start), false);
        String code = getValueByColumnName(header, page.get(start + 1), Attribute.Code);

        switch (importTypes)
        {
            case EXPERIMENT_TYPE:
                this.permIdOrNull = new EntityTypePermId(code, EntityKind.EXPERIMENT);
                break;
            case SAMPLE_TYPE:
                this.permIdOrNull = new EntityTypePermId(code, EntityKind.SAMPLE);
                break;
            case DATASET_TYPE:
                this.permIdOrNull = new EntityTypePermId(code, EntityKind.DATA_SET);
                break;
            default:
                throw new RuntimeException("Should never happen!");
        }

        super.importBlock(page, pageIndex, start + 2, end);
    }

    public void importBlockForPropertyType(List<List<String>> page, int pageIndex, int start, int end)
    {
        type = SemanticAnnotationType.PropertyType;
        this.permIdOrNull = null;
        super.importBlock(page, pageIndex, start, end);
    }

    @Override public void importBlock(List<List<String>> page, int pageIndex, int start, int end)
    {
        throw new IllegalStateException("This method should have never been called.");
    }
}
