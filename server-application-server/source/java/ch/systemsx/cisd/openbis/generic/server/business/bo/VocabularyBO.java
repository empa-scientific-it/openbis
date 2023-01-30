/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DataRetrievalFailureException;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.collection.IKeyExtractor;
import ch.systemsx.cisd.common.collection.TableMap;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.IRelationshipService;
import ch.systemsx.cisd.openbis.generic.server.business.bo.util.DataSetTypeWithoutExperimentChecker;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.DynamicPropertyEvaluationOperation;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEntityPropertyTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.util.KeyExtractorFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.ICodeHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IVocabularyUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewVocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.UpdatedVocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTermBatchUpdateDetails;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTermReplacement;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventType;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityInformationWithPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewVocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermWithStats;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IManagedPropertyEvaluatorFactory;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * The only productive implementation of {@link IVocabularyBO}.
 * 
 * @author Christian Ribeaud
 */
public class VocabularyBO extends AbstractBusinessObject implements IVocabularyBO
{
    private static final String UNSPECIFIED_VOCABULARY = "Unspecified vocabulary";

    private static final int MAX_NUMBER_OF_INVAID_TERMS_IN_ERROR_MESSAGE = 10;

    private VocabularyPE vocabularyPE;

    private Map<Class<? extends IEntityInformationWithPropertiesHolder>, List<Long>> changedEntitiesMap =
            new HashMap<Class<? extends IEntityInformationWithPropertiesHolder>, List<Long>>();

    public VocabularyBO(final IDAOFactory daoFactory, final Session session,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory,
            DataSetTypeWithoutExperimentChecker dataSetTypeChecker,
            IRelationshipService relationshipService)
    {
        super(daoFactory, session, managedPropertyEvaluatorFactory, dataSetTypeChecker, relationshipService);
    }

    // For tests only
    @Private
    VocabularyBO(final IDAOFactory daoFactory, final Session session, VocabularyPE vocabulary,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory,
            DataSetTypeWithoutExperimentChecker dataSetTypeChecker,
            IRelationshipService relationshipService)
    {
        super(daoFactory, session, managedPropertyEvaluatorFactory, dataSetTypeChecker, relationshipService);
        vocabularyPE = vocabulary;
    }

    //
    // AbstractVocabularyBusinessObject
    //

    @Override
    public final void define(final NewVocabulary vocabulary) throws UserFailureException
    {
        assert vocabulary != null : "Unspecified vocabulary.";

        vocabularyPE = new VocabularyPE();
        vocabularyPE.setCode(vocabulary.getCode());
        vocabularyPE.setRegistrator(findPerson());
        vocabularyPE.setDescription(vocabulary.getDescription());
        vocabularyPE.setChosenFromList(vocabulary.isChosenFromList());
        vocabularyPE.setURLTemplate(vocabulary.getURLTemplate());
        vocabularyPE.setManagedInternally(vocabulary.isManagedInternally());

        new InternalVocabularyAuthorization().canCreateVocabulary(session, vocabularyPE);

        Long currentTermOrdinal = 1L;
        for (final VocabularyTerm term : vocabulary.getTerms())
        {
            addTerm(term, currentTermOrdinal++, term.isOfficial());
        }
    }

    private List<VocabularyTermPE> addNewTerms(List<VocabularyTerm> newTerms, Long previousTermOrdinal,
            boolean isOfficial)
    {
        assert vocabularyPE != null : UNSPECIFIED_VOCABULARY;

        Long currentTermOrdinal;
        if (previousTermOrdinal == null)
        {
            currentTermOrdinal = getVocabularyTermDAO().getMaximumOrdinal(vocabularyPE) + 1;
        } else
        {
            currentTermOrdinal = previousTermOrdinal + 1;
            // need to shift existing terms to create space for new terms
            increaseVocabularyTermOrdinals(currentTermOrdinal, newTerms.size());
        }

        List<VocabularyTermPE> results = new ArrayList<VocabularyTermPE>();

        for (VocabularyTerm newTerm : newTerms)
        {
            VocabularyTermPE result = addTerm(newTerm, currentTermOrdinal++, isOfficial);
            results.add(result);
        }

        return results;
    }

    @Override
    public List<VocabularyTermPE> addNewTerms(List<VocabularyTerm> newTermCodes, Long previousTermOrdinal)
    {
        return addNewTerms(newTermCodes, previousTermOrdinal, true);
    }

    @Override
    public VocabularyTermPE addNewUnofficialTerm(String code, String label, String description,
            Long previousTermOrdinal)
    {
        assert vocabularyPE != null : UNSPECIFIED_VOCABULARY;
        assert code != null : "Unspecified vocabulary term code";

        Long currentTermOrdinal;
        if (previousTermOrdinal == null)
        {
            currentTermOrdinal = getVocabularyTermDAO().getMaximumOrdinal(vocabularyPE) + 1;
        } else
        {
            currentTermOrdinal = previousTermOrdinal + 1;
            // need to shift existing terms to create space for new terms
            increaseVocabularyTermOrdinals(currentTermOrdinal, 1);
        }

        return addTerm(code, description, label, currentTermOrdinal, false);
    }

    /** shift terms in vocabulary by specified increment starting from term with specified ordinal */
    private void increaseVocabularyTermOrdinals(Long startOrdinal, int increment)
    {
        getVocabularyTermDAO()
                .increaseVocabularyTermOrdinals(vocabularyPE, startOrdinal, increment);
    }

    private VocabularyTermPE addTerm(String code, String description, String label, Long ordinal,
            Boolean isOfficial)
    {
        final VocabularyTermPE existingTermPE = vocabularyPE.tryGetVocabularyTerm(code);

        if (existingTermPE != null)
        {
            PersonPE user = session.tryGetPerson();

            if (user != null && user.isSystemUser() && vocabularyPE.isManagedInternally())
            {
                existingTermPE.setDescription(description);
                existingTermPE.setLabel(label);
                existingTermPE.setOrdinal(ordinal);
                existingTermPE.setOfficial(isOfficial);
                getVocabularyTermDAO().updateRegistrator(existingTermPE, user);

                return existingTermPE;
            }
        }

        final VocabularyTermPE vocabularyTermPE = new VocabularyTermPE();
        vocabularyTermPE.setCode(code);
        vocabularyTermPE.setDescription(description);
        if (label != null && label.length() > 0)
        {
            vocabularyTermPE.setLabel(label);
        }
        vocabularyTermPE.setRegistrator(findPerson());
        vocabularyTermPE.setOrdinal(ordinal);
        vocabularyTermPE.setOfficial(isOfficial);

        new InternalVocabularyAuthorization().canCreateTerm(session, vocabularyPE, vocabularyTermPE);

        vocabularyPE.addTerm(vocabularyTermPE);

        return vocabularyTermPE;
    }

    private VocabularyTermPE addTerm(VocabularyTerm term, Long ordinal, Boolean isOfficial)
    {
        return addTerm(term.getCode(), term.getDescription(), term.getLabel(), ordinal, isOfficial);
    }

    @Override
    public void delete(List<VocabularyTerm> termsToBeDeleted,
            List<VocabularyTermReplacement> termsToBeReplaced)
    {
        assert vocabularyPE != null : UNSPECIFIED_VOCABULARY;

        Set<VocabularyTermPE> terms = vocabularyPE.getTerms();
        IKeyExtractor<String, VocabularyTermPE> keyExtractor =
                KeyExtractorFactory.<VocabularyTermPE> createCodeKeyExtractor();
        TableMap<String, VocabularyTermPE> termsMap =
                new TableMap<String, VocabularyTermPE>(terms, keyExtractor);
        Set<String> remainingTerms = new HashSet<String>(termsMap.keySet());
        for (VocabularyTerm termToBeDeleted : termsToBeDeleted)
        {
            remainingTerms.remove(termToBeDeleted.getCode());
        }
        for (VocabularyTermReplacement termToBeReplaced : termsToBeReplaced)
        {
            remainingTerms.remove(termToBeReplaced.getTerm().getCode());
        }
        for (VocabularyTermReplacement termToBeReplaced : termsToBeReplaced)
        {
            String code = termToBeReplaced.getTerm().getCode();
            Long id = termToBeReplaced.getTerm().getId();
            String replacement = termToBeReplaced.getReplacementCode();
            VocabularyTermPE term = termsMap.tryGet(replacement);
            if (term == null || remainingTerms.contains(replacement) == false)
            {
                throw new IllegalArgumentException(
                        "Invalid vocabulary replacement because of unknown replacement: "
                                + termToBeReplaced);
            }
            for (EntityKind entityKind : EntityKind.values())
            {
                IEntityPropertyTypeDAO dao = getEntityPropertyTypeDAO(entityKind);
                List<EntityPropertyPE> properties = dao.listPropertiesByVocabularyTerm(id);
                for (EntityPropertyPE entityProperty : properties)
                {
                    addToChangedEntities(entityProperty.getEntity());
                    entityProperty.setVocabularyTerm(term);
                }
                try
                {
                    dao.updateProperties(properties);
                } catch (DataAccessException e)
                {
                    throwException(e, "Couldn't replace in " + entityKind.toString().toLowerCase()
                            + "s vocabulary term '" + code + "' by '" + term.getCode() + "'.");
                }
            }
        }
        for (VocabularyTerm termToBeDeleted : termsToBeDeleted)
        {
            removeTerm(termsMap, termToBeDeleted.getCode());
        }
        for (VocabularyTermReplacement termToBeReplaced : termsToBeReplaced)
        {
            removeTerm(termsMap, termToBeReplaced.getTerm().getCode());
        }
    }

    private void removeTerm(TableMap<String, VocabularyTermPE> termsMap, String termCode)
    {
        VocabularyTermPE term = termsMap.remove(termCode);

        new InternalVocabularyAuthorization().canDeleteTerm(session, vocabularyPE, term);

        term.setVocabulary(null);
    }

    @Override
    public void save() throws UserFailureException
    {
        assert vocabularyPE != null : UNSPECIFIED_VOCABULARY;
        try
        {
            StringBuilder builder = new StringBuilder();
            int numberOfInvalidTerms = 0;
            for (VocabularyTermPE term : vocabularyPE.getTerms())
            {
                try
                {
                    getVocabularyTermDAO().validate(term);
                } catch (DataIntegrityViolationException ex)
                {
                    numberOfInvalidTerms++;
                    if (numberOfInvalidTerms <= MAX_NUMBER_OF_INVAID_TERMS_IN_ERROR_MESSAGE)
                    {
                        builder.append('\n').append(ex.getMessage());
                    }
                }
            }
            if (builder.length() > 0)
            {
                builder.insert(0, "Invalid terms:");
                int additionalTerms =
                        numberOfInvalidTerms - MAX_NUMBER_OF_INVAID_TERMS_IN_ERROR_MESSAGE;
                if (additionalTerms > 0)
                {
                    builder.append("\n").append("and ").append(additionalTerms);
                    builder.append(" more invalid terms.");
                }
                throw new UserFailureException("Invalid terms:" + builder);
            }
            getVocabularyDAO().createOrUpdateVocabulary(vocabularyPE);

            reindexChangedEntities();

        } catch (final DataAccessException e)
        {
            throwException(e, String.format("Vocabulary '%s'.", vocabularyPE.getCode()));
        }
    }

    @Override
    public void update(IVocabularyUpdates updates)
    {
        loadDataByTechId(TechId.create(updates));

        if (false == vocabularyPE.getModificationDate().equals(updates.getModificationDate()))
        {
            throwModifiedEntityException("Vocabulary");
        }

        new InternalVocabularyAuthorization().canUpdateVocabulary(session, vocabularyPE);

        vocabularyPE.setCode(updates.getCode());
        vocabularyPE.setDescription(updates.getDescription());
        vocabularyPE.setURLTemplate(updates.getURLTemplate());
        vocabularyPE.setChosenFromList(updates.isChosenFromList());

        validateAndSave();
    }

    @Override
    public void update(VocabularyUpdatesDTO updates)
    {
        loadDataByTechId(TechId.create(updates));

        boolean updateNeeded = new EqualsBuilder().append(vocabularyPE.getCode(), updates.getCode())
                .append(vocabularyPE.getDescription(), updates.getDescription()).append(vocabularyPE.getURLTemplate(), updates.getUrlTemplate())
                .append(vocabularyPE.isManagedInternally(), updates.isManagedInternally()).isEquals() == false;

        if (updateNeeded)
        {
            new InternalVocabularyAuthorization().canUpdateVocabulary(session, vocabularyPE);

            vocabularyPE.setCode(updates.getCode());
            vocabularyPE.setDescription(updates.getDescription());
            vocabularyPE.setURLTemplate(updates.getUrlTemplate());
            vocabularyPE.setChosenFromList(updates.isChosenFromList());
            vocabularyPE.setManagedInternally(updates.isManagedInternally());

            new InternalVocabularyAuthorization().canUpdateVocabulary(session, vocabularyPE);
        }

        for (NewVocabularyTerm t : updates.getNewTerms())
        {
            addTerm(t.getCode(), t.getDescription(), t.getLabel(), t.getOrdinal(), true);
        }

        validateAndSave();
    }

    private void validateAndSave()
    {
        getVocabularyDAO().validateAndSaveUpdatedEntity(vocabularyPE);
    }

    @Override
    public final VocabularyPE getVocabulary()
    {
        assert vocabularyPE != null : UNSPECIFIED_VOCABULARY;
        return vocabularyPE;
    }

    protected final VocabularyPE tryGetVocabulary()
    {
        return vocabularyPE;
    }

    @Override
    public List<VocabularyTermWithStats> countTermsUsageStatistics()
    {
        assert vocabularyPE != null : UNSPECIFIED_VOCABULARY;
        enrichWithTerms();
        Set<VocabularyTermPE> terms = vocabularyPE.getTerms();
        return createTermsWithStatistics(terms);
    }

    private List<VocabularyTermWithStats> createTermsWithStatistics(Set<VocabularyTermPE> terms)
    {
        List<VocabularyTermWithStats> results = new ArrayList<VocabularyTermWithStats>();
        for (VocabularyTermPE term : terms)
        {
            results.add(new VocabularyTermWithStats(term));
        }
        Collections.sort(results);
        for (EntityKind entityKind : EntityKind.values())
        {
            getEntityPropertyTypeDAO(entityKind).fillTermUsageStatistics(results, vocabularyPE);
        }
        return results;
    }

    @Override
    public void load(String vocabularyCode) throws UserFailureException
    {
        tryLoad(vocabularyCode);
        if (vocabularyPE == null)
        {
            throw UserFailureException.fromTemplate("Vocabulary '%s' does not exist.",
                    vocabularyCode);
        }
    }

    public void tryLoad(String vocabularyCode) throws UserFailureException
    {
        vocabularyPE = getVocabularyDAO().tryFindVocabularyByCode(vocabularyCode);
    }

    @Override
    public void loadDataByTechId(TechId vocabularyId)
    {
        try
        {
            vocabularyPE = getVocabularyDAO().getByTechId(vocabularyId);
        } catch (DataRetrievalFailureException exception)
        {
            throw new UserFailureException(exception.getMessage());
        }
    }

    @Override
    public Set<VocabularyTermPE> enrichWithTerms()
    {
        HibernateUtils.initialize(vocabularyPE.getTerms());
        return vocabularyPE.getTerms();
    }

    @Override
    public void deleteByTechId(TechId vocabularyId, String reason) throws UserFailureException
    {
        loadDataByTechId(vocabularyId);

        new InternalVocabularyAuthorization().canDeleteVocabulary(session, vocabularyPE);

        try
        {
            getVocabularyDAO().delete(vocabularyPE);
            getEventDAO()
                    .persist(createDeletionEvent(vocabularyPE, session.tryGetPerson(), reason));
        } catch (final DataAccessException ex)
        {
            throwException(ex, String.format("Vocabulary '%s'", vocabularyPE.getCode()));
        }
    }

    public static EventPE createDeletionEvent(VocabularyPE vocabularyPE, PersonPE registrator,
            String reason)
    {
        EventPE event = new EventPE();
        event.setEventType(EventType.DELETION);
        event.setEntityType(EntityType.VOCABULARY);
        event.setIdentifiers(Collections.singletonList(vocabularyPE.getCode()));
        event.setDescription(getDeletionDescription(vocabularyPE));
        event.setReason(reason);
        event.setRegistrator(registrator);

        return event;
    }

    private static String getDeletionDescription(VocabularyPE vocabularyPE)
    {
        return String.format("%s", vocabularyPE.getCode());
    }

    @Override
    public void updateTerms(List<VocabularyTerm> terms)
    {
        assert vocabularyPE != null : UNSPECIFIED_VOCABULARY;

        checkAllTermsPresent(vocabularyPE.getTerms(), terms);
        Map<String, UpdatedVocabularyTerm> newTermsMap = prepareUpdateMap(terms);
        updateExistingTermsAndRemoveFromMap(newTermsMap);
        addNewTerms(newTermsMap);
    }

    private Map<String, UpdatedVocabularyTerm> prepareUpdateMap(List<VocabularyTerm> terms)
    {
        // additionally check if all terms are present only once
        Map<String, UpdatedVocabularyTerm> newTermsMap =
                new HashMap<String, UpdatedVocabularyTerm>();
        Set<String> multipliedCodes = new LinkedHashSet<String>(); // keep order
        for (VocabularyTerm v : terms)
        {
            UpdatedVocabularyTerm previousTermOrNull =
                    newTermsMap.put(v.getCode(), (UpdatedVocabularyTerm) v);
            if (previousTermOrNull != null)
            {
                multipliedCodes.add(v.getCode());
            }
        }
        if (multipliedCodes.size() > 0)
        {
            throw new UserFailureException(String.format("Mulitiple rows found for terms: [%s]",
                    StringUtils.join(multipliedCodes, ",")));
        }
        return newTermsMap;
    }

    private void updateExistingTermsAndRemoveFromMap(Map<String, UpdatedVocabularyTerm> newTermsMap)
    {
        for (VocabularyTermPE oldTerm : vocabularyPE.getTerms())
        {
            String code = oldTerm.getCode();
            UpdatedVocabularyTerm update = newTermsMap.get(code);
            VocabularyTermBatchUpdateDetails batchUpdateDetails = update.getBatchUpdateDetails();

            new InternalVocabularyAuthorization().canUpdateTerm(session, vocabularyPE, oldTerm);

            if (batchUpdateDetails.isDescriptionUpdateRequested())
            {
                oldTerm.setDescription(update.getDescription());
            }
            if (batchUpdateDetails.isLabelUpdateRequested())
            {
                oldTerm.setLabel(update.getLabel());
            }
            oldTerm.setOrdinal(update.getOrdinal()); // ordinal is always updated

            makeSystemInternalIfSystemUser(oldTerm);

            newTermsMap.remove(code);
        }
    }

    private void addNewTerms(Map<String, UpdatedVocabularyTerm> newTermsMap)
    {
        for (VocabularyTerm newTerm : newTermsMap.values())
        {
            addTerm(newTerm, newTerm.getOrdinal(), newTerm.isOfficial());
        }
    }

    private void checkAllTermsPresent(Set<VocabularyTermPE> oldTerms, List<VocabularyTerm> newTerms)
    {
        Collection<String> undetected = convert(oldTerms);
        undetected.removeAll(convert(newTerms));
        if (undetected.size() > 0)
        {
            throw new UserFailureException(String.format("Missing vocabulary terms: [%s]",
                    StringUtils.join(undetected, ",")));
        }
    }

    private <T extends ICodeHolder> Collection<String> convert(Collection<T> terms)
    {
        ArrayList<String> list = new ArrayList<String>();
        for (ICodeHolder t : terms)
        {
            list.add(t.getCode());
        }
        return list;
    }

    private void addToChangedEntities(IEntityPropertiesHolder entity)
    {
        Class<? extends IEntityInformationWithPropertiesHolder> entityClass;

        // We do the instanceof check to keep all the subclasses under the same key,
        // e.g. DataPE, ExternalDataPE and LinkDataPE all under DataPE.class key.
        // Even though the other classes do not have subclasses yet we still do the same check
        // to make it work even in the future when such subclasses might be created.

        if (entity instanceof ExperimentPE)
        {
            entityClass = ExperimentPE.class;
        } else if (entity instanceof SamplePE)
        {
            entityClass = SamplePE.class;
        } else if (entity instanceof DataPE)
        {
            entityClass = DataPE.class;
        } else if (entity instanceof MaterialPE)
        {
            entityClass = MaterialPE.class;
        } else
        {
            throw new IllegalArgumentException("Unsupported entity class: " + entity.getClass());
        }

        List<Long> ids = changedEntitiesMap.get(entityClass);
        if (ids == null)
        {
            ids = new ArrayList<Long>();
            changedEntitiesMap.put(entityClass, ids);
        }

        ids.add(entity.getId());
    }

    private void reindexChangedEntities()
    {
        if (false == changedEntitiesMap.isEmpty())
        {
            for (Map.Entry<Class<? extends IEntityInformationWithPropertiesHolder>, List<Long>> entry : changedEntitiesMap.entrySet())
            {
                Class<? extends IEntityInformationWithPropertiesHolder> key = entry.getKey();
                // update modification timestamps of the entities
                EntityKind entityKind = getEntityKind(key.getClass());
                IEntityPropertyTypeDAO entityPropertyTypeDAO = getEntityPropertyTypeDAO(entityKind);
                entityPropertyTypeDAO.updateEntityModificationTimestamps(entry.getValue());

                getPersistencyResources().getDynamicPropertyEvaluationScheduler()
                        .scheduleUpdate(DynamicPropertyEvaluationOperation.evaluate(entry.getKey(), entry.getValue()));
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private EntityKind getEntityKind(Class clazz)
    {
        if (clazz.isInstance(ExperimentPE.class))
        {
            return EntityKind.EXPERIMENT;
        } else if (clazz.isInstance(SamplePE.class))
        {
            return EntityKind.SAMPLE;
        } else if (clazz.isInstance(DataPE.class))
        {
            return EntityKind.DATA_SET;
        } else if (clazz.isInstance(MaterialPE.class))
        {
            return EntityKind.DATA_SET;
        } else
        {
            throw new IllegalArgumentException("Unsupported entity class: " + clazz);
        }
    }

}