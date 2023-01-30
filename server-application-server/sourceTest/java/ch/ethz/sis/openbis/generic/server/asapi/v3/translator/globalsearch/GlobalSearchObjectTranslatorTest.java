package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.globalsearch;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.GlobalSearchObject;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.fetchoptions.GlobalSearchObjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.search.GlobalSearchObjectKind;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MatchingEntity;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

public class GlobalSearchObjectTranslatorTest
{
    @Test
    public void testTranslateWithDifferentObjectsWithTheSameIds()
    {
        GlobalSearchObjectTranslator translator = new GlobalSearchObjectTranslator();

        RoleAssignmentPE roleAssignment = new RoleAssignmentPE();
        roleAssignment.setRole(RoleCode.ADMIN);

        PersonPE person = new PersonPE();
        person.setRoleAssignments(Collections.singleton(roleAssignment));

        Session session = new Session();
        session.setPerson(person);

        TranslationContext context = new TranslationContext(session);
        GlobalSearchObjectFetchOptions fo = new GlobalSearchObjectFetchOptions();

        MatchingEntity sample = new MatchingEntity();
        sample.setId(1L);
        sample.setEntityKind(EntityKind.SAMPLE);
        sample.setPermId("SAMPLE_PERM_ID");
        sample.setIdentifier("SAMPLE_IDENTIFIER");

        MatchingEntity experiment = new MatchingEntity();
        experiment.setId(1L);
        experiment.setEntityKind(EntityKind.EXPERIMENT);
        experiment.setPermId("EXPERIMENT_PERM_ID");
        experiment.setIdentifier("EXPERIMENT_IDENTIFIER");

        Collection<MatchingEntity> entities = Arrays.asList(sample, experiment);

        Map<MatchingEntity, GlobalSearchObject> result = translator.translate(context, entities, fo);

        assertEquals(result.size(), 2);

        GlobalSearchObject translatedSample = result.get(sample);
        assertEquals(translatedSample.getObjectKind(), GlobalSearchObjectKind.SAMPLE);
        assertEquals(translatedSample.getObjectPermId().toString(), sample.getPermId());
        assertEquals(translatedSample.getObjectIdentifier().toString(), sample.getIdentifier());

        GlobalSearchObject translatedExperiment = result.get(experiment);
        assertEquals(translatedExperiment.getObjectKind(), GlobalSearchObjectKind.EXPERIMENT);
        assertEquals(translatedExperiment.getObjectPermId().toString(), experiment.getPermId());
        assertEquals(translatedExperiment.getObjectIdentifier().toString(), experiment.getIdentifier());
    }
}
