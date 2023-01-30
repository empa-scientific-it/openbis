package ch.ethz.sis.openbis.generic.server.asapi.v3.translator;

import ch.ethz.sis.openbis.generic.server.asapi.v3.context.Context;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

public class TranslationContext extends Context
{
    private final TranslationCache translationCache;

    public TranslationContext(Session session)
    {
        super(session);
        this.translationCache = new TranslationCache();
    }

    public TranslationCache getTranslationCache()
    {

        return translationCache;
    }

}
