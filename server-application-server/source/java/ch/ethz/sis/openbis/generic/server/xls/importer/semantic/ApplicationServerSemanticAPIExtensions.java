package ch.ethz.sis.openbis.generic.server.xls.importer.semantic;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyAssignmentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.create.SemanticAnnotationCreation;

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

}