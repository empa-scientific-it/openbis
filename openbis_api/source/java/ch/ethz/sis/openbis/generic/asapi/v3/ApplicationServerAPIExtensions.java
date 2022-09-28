package ch.ethz.sis.openbis.generic.asapi.v3;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.PersonalAccessToken;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.create.PersonalAccessTokenCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.fetchoptions.PersonalAccessTokenFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.id.PersonalAccessTokenPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.pat.search.PersonalAccessTokenSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyAssignmentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyAssignmentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.SemanticAnnotation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.create.SemanticAnnotationCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.fetchoptions.SemanticAnnotationFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.search.SemanticAnnotationSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.server.ServerInformation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.session.SessionInformation;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

public abstract class ApplicationServerAPIExtensions {

    /**
     * This utility method returns a well managed personal access token, creating one if no one is found and renews it if is close to expiration.
     * Requires are real session token since it uses other methods.
     *
     * @throws UserFailureException in case of any problems
     */
    public static PersonalAccessTokenPermId getManagedPersonalAccessToken(IApplicationServerApi v3, String sessionToken, String sessionName) {
        final int SECONDS_PER_DAY = 24 * 60 * 60;

        // Obtain servers renewal information
        Map<String, String> information = v3.getServerInformation(sessionToken);
        int personalAccessTokensRenewalPeriodInSeconds = Integer.parseInt(information.get(ServerInformation.PERSONAL_ACCESS_TOKENS_VALIDITY_WARNING_PERIOD));
        int personalAccessTokensRenewalPeriodInDays = personalAccessTokensRenewalPeriodInSeconds / SECONDS_PER_DAY;
        int personalAccessTokensMaxValidityPeriodInSeconds = Integer.parseInt(information.get(ServerInformation.PERSONAL_ACCESS_TOKENS_MAX_VALIDITY_PERIOD));
        int personalAccessTokensMaxValidityPeriodInDays = personalAccessTokensMaxValidityPeriodInSeconds / SECONDS_PER_DAY;

        // Obtain user id
        SessionInformation sessionInformation = v3.getSessionInformation(sessionToken);

        // Search for PAT for this user and application
        // NOTE: Standard users only get their PAT but admins get all, filtering with the user solves this corner case
        PersonalAccessTokenSearchCriteria personalAccessTokenSearchCriteria = new PersonalAccessTokenSearchCriteria();
        personalAccessTokenSearchCriteria.withSessionName().thatEquals(sessionName);
        personalAccessTokenSearchCriteria.withOwner().withUserId().thatEquals(sessionInformation.getPerson().getUserId());

        SearchResult<PersonalAccessToken> personalAccessTokenSearchResult = v3.searchPersonalAccessTokens(sessionToken, personalAccessTokenSearchCriteria, new PersonalAccessTokenFetchOptions());
        PersonalAccessToken bestTokenFound = null;
        PersonalAccessTokenPermId bestTokenFoundPermId = null;

        // Obtain longer lasting application token
        for (PersonalAccessToken personalAccessToken : personalAccessTokenSearchResult.getObjects()) {
            if (personalAccessToken.getValidToDate().after(new Date())) {
                if (bestTokenFound == null) {
                    bestTokenFound = personalAccessToken;
                } else if (personalAccessToken.getValidToDate().after(bestTokenFound.getValidToDate())) {
                    bestTokenFound = personalAccessToken;
                }
            }
        }

        // If best token doesn't exist, create
        if (bestTokenFound == null) {
            bestTokenFoundPermId = createManagedPersonalAccessToken(v3, sessionToken, sessionName, personalAccessTokensMaxValidityPeriodInDays);
        }

        // If best token is going to expire in less than the warning period, renew
        Calendar renewalDate = Calendar.getInstance();
        renewalDate.add(Calendar.DAY_OF_MONTH, personalAccessTokensRenewalPeriodInDays);
        if (bestTokenFound != null && bestTokenFound.getValidToDate().before(renewalDate.getTime())) {
            bestTokenFoundPermId = createManagedPersonalAccessToken(v3, sessionToken, sessionName, personalAccessTokensMaxValidityPeriodInDays);
        }

        // If we have not created or renewed, return current
        if (bestTokenFoundPermId == null) {
            bestTokenFoundPermId = bestTokenFound.getPermId();
        }

        return bestTokenFoundPermId;
    }

    private static PersonalAccessTokenPermId createManagedPersonalAccessToken(IApplicationServerApi v3, String sessionToken, String applicationName, int personalAccessTokensMaxValidityPeriodInDays) {
        final int SECONDS_PER_DAY = 24 * 60 * 60;
        final int MILLIS_PER_DAY = SECONDS_PER_DAY * 1000;

        PersonalAccessTokenCreation creation = new PersonalAccessTokenCreation();
        creation.setSessionName(applicationName);
        creation.setValidFromDate(new Date(System.currentTimeMillis() - MILLIS_PER_DAY));
        creation.setValidToDate(new Date(System.currentTimeMillis() + MILLIS_PER_DAY * personalAccessTokensMaxValidityPeriodInDays));
        List<PersonalAccessTokenPermId> personalAccessTokens = v3.createPersonalAccessTokens(sessionToken, List.of(creation));
        return personalAccessTokens.get(0);
    }

    /**
     * This utility method provides a simplified API to create subject semantic annotations
     *
     */
    public static SemanticAnnotationCreation getSemanticSubjectCreation(String subjectClass,
                                                                             String subjectClassOntologyId,
                                                                             String subjectClassOntologyVersion,
                                                                             String subjectClassId) {
        SemanticAnnotationCreation semanticAnnotationCreation = new SemanticAnnotationCreation();
        // Subject: Type matching an ontology class
        semanticAnnotationCreation.setEntityTypeId(new EntityTypePermId(subjectClass, EntityKind.SAMPLE));
        // Ontology URL
        semanticAnnotationCreation.setPredicateOntologyId(subjectClassOntologyId);
        // Ontology Version URL
        semanticAnnotationCreation.setPredicateOntologyVersion(subjectClassOntologyVersion);
        // Ontology Class URL
        semanticAnnotationCreation.setPredicateAccessionId(subjectClassId);
        return semanticAnnotationCreation;
    }

    /**
     * This utility method provides a simplified API to create predicate semantic annotations
     *
     */
    public static SemanticAnnotationCreation getSemanticPredicateCreation(String subjectClass,
                                                                            String predicateProperty,
                                                                            String predicatePropertyOntologyId,
                                                                            String predicatePropertyOntologyVersion,
                                                                            String predicatePropertyId) {
        SemanticAnnotationCreation semanticAnnotationCreation = new SemanticAnnotationCreation();
        // Subject: Type matching an ontology class
        // Predicate: Property matching an ontology class property
        semanticAnnotationCreation.setPropertyAssignmentId(new PropertyAssignmentPermId(
                new EntityTypePermId(subjectClass, EntityKind.SAMPLE),
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
     * This utility method provides a simplified API to search based on semantic subjects and predicates
     *
     * @throws UserFailureException in case of any problems
     */
    public static SearchResult<Sample> searchSamplesWithSemanticAnnotations(IApplicationServerApi v3,
                                                                        String sessionToken,
                                                                        String subjectClassOrNull,
                                                                        Map<String, String> predicatePropertiesOrNull,
                                                                        SampleFetchOptions sampleFetchOptions) {
        //
        // Part 1 : Translate semantic classes and properties into openBIS types and property types
        //

        SemanticAnnotationSearchCriteria semanticCriteria = new SemanticAnnotationSearchCriteria();
        semanticCriteria.withOrOperator();

        SemanticAnnotationFetchOptions semanticFetchOptions = new SemanticAnnotationFetchOptions();

        // Request and collect subjects
        if (subjectClassOrNull != null) {
            semanticCriteria.withPredicateAccessionId().thatEquals(subjectClassOrNull);
        }
        semanticFetchOptions.withEntityType();

        // Request and collect predicates
        for (String predicate:predicatePropertiesOrNull.keySet()) {
            semanticCriteria.withPredicateAccessionId().thatEquals(predicate);
        }
        PropertyAssignmentFetchOptions propertyAssignmentFetchOptions = semanticFetchOptions.withPropertyAssignment();
        propertyAssignmentFetchOptions.withPropertyType();
        propertyAssignmentFetchOptions.withEntityType();

        SearchResult<SemanticAnnotation> semanticAnnotationSearchResult = v3.searchSemanticAnnotations(sessionToken, new SemanticAnnotationSearchCriteria(), new SemanticAnnotationFetchOptions());

        //
        // Part 2 : Create openBIS search matching semantic results
        //

        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withAndOperator();

        // Set Subject
        String sampleTypeCode = null;
        for (SemanticAnnotation semanticAnnotation:semanticAnnotationSearchResult.getObjects()) {
            if (semanticAnnotation.getEntityType() != null) {
                EntityTypePermId permId = (EntityTypePermId) semanticAnnotation.getEntityType().getPermId();
                if (permId.getEntityKind() == EntityKind.SAMPLE) {
                    sampleTypeCode = semanticAnnotation.getEntityType().getCode();
                    criteria.withType().withCode().thatEquals(sampleTypeCode);
                }
            }
        }

        if (sampleTypeCode == null) {
            throw new UserFailureException("Sample Type matching Subject not found.");
        }

        // Set Predicates matching the Subject
        for (SemanticAnnotation semanticAnnotation:semanticAnnotationSearchResult.getObjects()) {
            if (semanticAnnotation.getPropertyAssignment() != null &&
                semanticAnnotation.getPropertyAssignment().getEntityType().getCode().equals(sampleTypeCode)) {
                EntityTypePermId permId = (EntityTypePermId) semanticAnnotation.getPropertyAssignment().getEntityType().getPermId();
                if (permId.getEntityKind() == EntityKind.SAMPLE) {
                    String value = predicatePropertiesOrNull.get(semanticAnnotation.getPredicateAccessionId());
                    criteria.withProperty(semanticAnnotation.getPropertyAssignment().getPropertyType().getCode()).thatEquals(value);
                }
            }
        }

        return v3.searchSamples(sessionToken, criteria, sampleFetchOptions);
    }

}