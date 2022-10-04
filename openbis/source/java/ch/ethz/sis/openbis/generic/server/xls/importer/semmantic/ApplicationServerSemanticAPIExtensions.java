package ch.ethz.sis.pat;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractEntitySearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyAssignmentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyAssignmentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.SemanticAnnotation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.create.SemanticAnnotationCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.fetchoptions.SemanticAnnotationFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.search.SemanticAnnotationSearchCriteria;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

import java.util.Map;

public abstract class ApplicationServerSemanticAPIExtensions {

    /**
     * This utility method provides a simplified API to create subject semantic annotations
     *
     */
    public static SemanticAnnotationCreation getSemanticSubjectCreation(    EntityKind subjectEntityKind,
                                                                            String subjectClass,
                                                                             String subjectClassOntologyId,
                                                                             String subjectClassOntologyVersion,
                                                                             String subjectClassId) {
        SemanticAnnotationCreation semanticAnnotationCreation = new SemanticAnnotationCreation();
        // Subject: Type matching an ontology class
        semanticAnnotationCreation.setEntityTypeId(new EntityTypePermId(subjectClass, subjectEntityKind));
        // Ontology URL
        semanticAnnotationCreation.setPredicateOntologyId(subjectClassOntologyId);
        semanticAnnotationCreation.setDescriptorOntologyId(subjectClassOntologyId);
        // Ontology Version URL
        semanticAnnotationCreation.setPredicateOntologyVersion(subjectClassOntologyVersion);
        semanticAnnotationCreation.setDescriptorOntologyVersion(subjectClassOntologyVersion);
        // Ontology Class URL
        semanticAnnotationCreation.setPredicateAccessionId(subjectClassId);
        semanticAnnotationCreation.setDescriptorAccessionId(subjectClassId);
        return semanticAnnotationCreation;
    }

    /**
     * This utility method provides a simplified API to create predicate semantic annotations
     *
     */
    public static SemanticAnnotationCreation getSemanticPredicateWithSubjectCreation( EntityKind subjectEntityKind,
                                                                            String subjectClass,
                                                                            String predicateProperty,
                                                                            String predicatePropertyOntologyId,
                                                                            String predicatePropertyOntologyVersion,
                                                                            String predicatePropertyId) {
        SemanticAnnotationCreation semanticAnnotationCreation = new SemanticAnnotationCreation();
        // Subject: Type matching an ontology class
        // Predicate: Property matching an ontology class property
        semanticAnnotationCreation.setPropertyAssignmentId(new PropertyAssignmentPermId(
                new EntityTypePermId(subjectClass, subjectEntityKind),
                new PropertyTypePermId(predicateProperty)));
        // Ontology URL
        semanticAnnotationCreation.setPredicateOntologyId(predicatePropertyOntologyId);
        // Ontology Version URL
        semanticAnnotationCreation.setPredicateOntologyVersion(predicatePropertyOntologyVersion);
        // Ontology Property URL
        semanticAnnotationCreation.setPredicateAccessionId(predicatePropertyId);
        return semanticAnnotationCreation;
    }

    /**
     * This utility method provides a simplified API to create predicate semantic annotations
     *
     */
    public static SemanticAnnotationCreation getSemanticPredicateCreation( String predicateProperty,
                                                                                      String predicatePropertyOntologyId,
                                                                                      String predicatePropertyOntologyVersion,
                                                                                      String predicatePropertyId) {
        SemanticAnnotationCreation semanticAnnotationCreation = new SemanticAnnotationCreation();
        // Predicate: Property matching an ontology class property
        semanticAnnotationCreation.setPropertyTypeId(new PropertyTypePermId(predicateProperty));
        // Ontology URL
        semanticAnnotationCreation.setPredicateOntologyId(predicatePropertyOntologyId);
        // Ontology Version URL
        semanticAnnotationCreation.setPredicateOntologyVersion(predicatePropertyOntologyVersion);
        // Ontology Property URL
        semanticAnnotationCreation.setPredicateAccessionId(predicatePropertyId);
        return semanticAnnotationCreation;
    }

    /**
     * This utility method provides a simplified API to search based on semantic subjects and predicates
     *
     * @throws UserFailureException in case of any problems
     */
    public static SearchResult searchEntityWithSemanticAnnotations(IApplicationServerApi v3,
                                                                           String sessionToken,
                                                                           EntityKind entityKind,
                                                                           String subjectClassIDOrNull,
                                                                           Map<String, String> predicatePropertyIDsOrNull,
                                                                           Integer fromOrNull,
                                                                           Integer countOrNull) {
        if (entityKind == null) {
            throw new UserFailureException("entityKind cannot be null");
        }
        if (entityKind == EntityKind.DATA_SET) {
            throw new UserFailureException("EntityKind.DATA_SET is not supported");
        }

        //
        // Part 1 : Translate semantic classes and properties into openBIS types and property types
        //

        SemanticAnnotationSearchCriteria semanticCriteria = new SemanticAnnotationSearchCriteria();
        semanticCriteria.withOrOperator();

        SemanticAnnotationFetchOptions semanticFetchOptions = new SemanticAnnotationFetchOptions();

        // Request and collect subjects
        if (subjectClassIDOrNull != null) {
            semanticCriteria.withPredicateAccessionId().thatEquals(subjectClassIDOrNull);
        }
        semanticFetchOptions.withEntityType();

        // Request and collect predicates
        if (predicatePropertyIDsOrNull != null) {
            for (String predicate : predicatePropertyIDsOrNull.keySet()) {
                semanticCriteria.withPredicateAccessionId().thatEquals(predicate);
            }
        }
        PropertyAssignmentFetchOptions propertyAssignmentFetchOptions = semanticFetchOptions.withPropertyAssignment();
        propertyAssignmentFetchOptions.withPropertyType();
        propertyAssignmentFetchOptions.withEntityType();

        SearchResult<SemanticAnnotation> semanticAnnotationSearchResult = v3.searchSemanticAnnotations(sessionToken, new SemanticAnnotationSearchCriteria(), semanticFetchOptions);

        //
        // Part 2 : Create openBIS search matching semantic results
        //

        AbstractEntitySearchCriteria criteria = getEntitySearchCriteria(entityKind);
        criteria.withAndOperator();

        // Set Subject
        String entityTypeCode = null;
        for (SemanticAnnotation semanticAnnotation:semanticAnnotationSearchResult.getObjects()) {
            if (semanticAnnotation.getEntityType() != null) {
                EntityTypePermId permId = (EntityTypePermId) semanticAnnotation.getEntityType().getPermId();
                if (permId.getEntityKind() == entityKind) {
                    entityTypeCode = semanticAnnotation.getEntityType().getCode();
                    setWithTypeThatEquals(entityKind, criteria, entityTypeCode);
                }
            }
        }

        if (entityTypeCode == null) {
            throw new UserFailureException("Entity Type matching Subject not found.");
        }

        // Set Predicates matching the Subject
        if (predicatePropertyIDsOrNull != null) {
            int predicatesFound = 0;
            for (SemanticAnnotation semanticAnnotation : semanticAnnotationSearchResult.getObjects()) {
                if (semanticAnnotation.getPropertyAssignment() != null &&
                        semanticAnnotation.getPropertyAssignment().getEntityType().getCode().equals(entityTypeCode)) {
                    EntityTypePermId permId = (EntityTypePermId) semanticAnnotation.getPropertyAssignment().getEntityType().getPermId();
                    if (permId.getEntityKind() == entityKind) {
                        String value = predicatePropertyIDsOrNull.get(semanticAnnotation.getPredicateAccessionId());
                        criteria.withProperty(semanticAnnotation.getPropertyAssignment().getPropertyType().getCode()).thatEquals(value);
                        predicatesFound++;
                    }
                }
            }

            if (predicatesFound != predicatePropertyIDsOrNull.size()) {
                throw new UserFailureException("Property Types matching Predicates not found.");
            }
        }

        FetchOptions fetchOptions = getEntityFetchOptions(entityKind);
        if (fromOrNull != null) {
            fetchOptions.from(fromOrNull);
        }
        if (countOrNull != null) {
            fetchOptions.count(countOrNull);
        }

        SearchResult searchResult = getSearchResult(v3, sessionToken, entityKind, criteria, fetchOptions);
        return searchResult;
    }

    private static void setWithTypeThatEquals(EntityKind entityKind, AbstractEntitySearchCriteria criteria, String entityTypeCode) {
        switch (entityKind) {
            case EXPERIMENT:
                ((ExperimentSearchCriteria) criteria).withType().withCode().thatEquals(entityTypeCode);
                break;
            case SAMPLE:
                ((SampleSearchCriteria) criteria).withType().withCode().thatEquals(entityTypeCode);
                break;
            case DATA_SET:
                ((DataSetSearchCriteria) criteria).withType().withCode().thatEquals(entityTypeCode);
                break;
        }
    }

    private static AbstractEntitySearchCriteria getEntitySearchCriteria(EntityKind entityKind) {
        AbstractEntitySearchCriteria criteria = null;
        switch (entityKind) {
            case EXPERIMENT:
                criteria = new ExperimentSearchCriteria();
                break;
            case SAMPLE:
                criteria = new SampleSearchCriteria();
                break;
            case DATA_SET:
                criteria = new DataSetSearchCriteria();
                break;
        }
        return criteria;
    }

    private static FetchOptions getEntityFetchOptions(EntityKind entityKind) {
        FetchOptions fetchOptions = null;
        switch (entityKind) {
            case EXPERIMENT:
                fetchOptions = new ExperimentFetchOptions();
                break;
            case SAMPLE:
                fetchOptions = new SampleFetchOptions();
                break;
            case DATA_SET:
                fetchOptions = new DataSetFetchOptions();
                break;
        }
        return fetchOptions;
    }

    private static SearchResult getSearchResult(IApplicationServerApi v3, String sessionToken, EntityKind entityKind, AbstractEntitySearchCriteria criteria, FetchOptions fetchOptions) {
        SearchResult searchResult = null;
        switch (entityKind) {
            case EXPERIMENT:
                searchResult = v3.searchExperiments(sessionToken, (ExperimentSearchCriteria) criteria, (ExperimentFetchOptions) fetchOptions);
                break;
            case SAMPLE:
                searchResult = v3.searchSamples(sessionToken, (SampleSearchCriteria) criteria, (SampleFetchOptions) fetchOptions);
                break;
            case DATA_SET:
                searchResult = v3.searchDataSets(sessionToken, (DataSetSearchCriteria) criteria, (DataSetFetchOptions) fetchOptions);
                break;
        }
        return searchResult;
    }

}