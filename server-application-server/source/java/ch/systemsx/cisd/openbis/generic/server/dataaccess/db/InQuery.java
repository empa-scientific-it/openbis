package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hibernate.SQLQuery;
import org.hibernate.Session;

public class InQuery<I, O>
{
    public List<O> withBatch(Session session, String inQuery, String inParameter, List<I> inArguments, Map<String, Object> fixParams)
    {
        List<O> result = new ArrayList<O>(inArguments.size());
        int fixParamsSize = (fixParams == null) ? 0 : fixParams.size();

        InQueryScroller<I> scroller = new InQueryScroller<>(inArguments, fixParamsSize);
        List<I> partialInArguments = null;

        while ((partialInArguments = scroller.next()) != null)
        {
            SQLQuery query = session.createSQLQuery(inQuery);
            query.setParameterList(inParameter, partialInArguments);

            if (fixParams != null)
            {
                for (String paramName : fixParams.keySet())
                {
                    query.setParameter(paramName, fixParams.get(paramName));
                }
            }
            List<O> partialResult = query.list();
            result.addAll(partialResult);
        }

        return result;
    }
}