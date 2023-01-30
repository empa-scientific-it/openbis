package ch.systemsx.cisd.openbis.generic.server.business.bo;

import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermPE;

public class InternalVocabularyAuthorization
{

    public void canCreateVocabulary(Session session, VocabularyPE vocabulary)
    {
        checkVocabulary(session, vocabulary);
    }

    public void canUpdateVocabulary(Session session, VocabularyPE vocabulary)
    {
        checkVocabulary(session, vocabulary);
    }

    public void canDeleteVocabulary(Session session, VocabularyPE vocabulary)
    {
        checkVocabulary(session, vocabulary);
    }

    public void canCreateTerm(Session session, VocabularyPE vocabulary, VocabularyTermPE term)
    {
        // do not check anything - allow new terms to be created even in internally managed vocabularies
    }

    public void canUpdateTerm(Session session, VocabularyPE vocabulary, VocabularyTermPE term)
    {
        checkTerm(session, vocabulary, term);
    }

    public void canDeleteTerm(Session session, VocabularyPE vocabulary, VocabularyTermPE term)
    {
        checkTerm(session, vocabulary, term);
    }

    private void checkVocabulary(Session session, VocabularyPE vocabulary)
    {
        if (vocabulary.isManagedInternally() && isSystemUser(session) == false)
        {
            throw new AuthorizationFailureException("Internal vocabularies can be managed only by the system user.");
        }
    }

    private void checkTerm(Session session, VocabularyPE vocabulary, VocabularyTermPE term)
    {
        if (vocabulary.isManagedInternally() && isSystemTerm(term) && isSystemUser(session) == false)
        {
            throw new AuthorizationFailureException(
                    "Terms created by the system user that belong to internal vocabularies can be managed only by the system user.");
        }
    }

    private boolean isSystemTerm(VocabularyTermPE term)
    {
        PersonPE registrator = term.getRegistrator();

        if (registrator == null)
        {
            throw new AuthorizationFailureException("Could not check access because the vocabulary term does not have any registrator assigned.");
        } else
        {
            return registrator.isSystemUser();
        }
    }

    private boolean isSystemUser(Session session)
    {
        PersonPE user = session.tryGetPerson();

        if (user == null)
        {
            throw new AuthorizationFailureException("Could not check access because the current session does not have any user assigned.");
        } else
        {
            return user.isSystemUser();
        }
    }

}
