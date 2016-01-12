package ch.systemsx.cisd.openbis.generic.server.dataaccess.db.deletion;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.SQLQuery;
import org.hibernate.SharedSessionContract;
import org.hibernate.transform.ResultTransformer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

public class EntityHistoryCreator
{

    private boolean enabled = false;

    public void setEnabled(String s)
    {
        if (s != null && s.length() > 0 && s.equalsIgnoreCase("true"))
        {
            enabled = true;
        } else
        {
            enabled = false;
        }
    }

    public String apply(SharedSessionContract session,
            List<Long> entityIdsToDelete,
            String propertyHistoryQuery,
            String relationshipHistoryQuery)
    {
        if (!enabled)
        {
            return "";
        }
        Map<String, List<? extends EntityModification>> histories = new HashMap<String, List<? extends EntityModification>>();

        List<PropertyHistoryEntry> propertyHistory =
                selectHistoryPropertyEntries(session.createSQLQuery(propertyHistoryQuery), entityIdsToDelete);

        for (PropertyHistoryEntry entry : propertyHistory)
        {
            addToHistories(entry.permId, histories, entry);
        }

        List<RelationshipHistoryEntry> relationshipHistory =
                selectRelationshipHistoryEntries(session.createSQLQuery(relationshipHistoryQuery),
                        entityIdsToDelete);
        for (RelationshipHistoryEntry entry : relationshipHistory)
        {
            addToHistories(entry.permId, histories, entry);
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        ObjectWriter ow = new ObjectMapper().setDateFormat(dateFormat).writer().withDefaultPrettyPrinter();
        String content;
        try
        {
            content = ow.writeValueAsString(histories);
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
        return content;
    }

    private void addToHistories(String permId, Map<String, List<? extends EntityModification>> histories, EntityModification modification)
    {
        List<? extends EntityModification> current = histories.get(permId);
        if (current == null)
        {
            current = new ArrayList<>();
        }
        histories.put(permId, addModification(current, modification));
    }

    private List<EntityModification> addModification(List<? extends EntityModification> current, EntityModification modification)
    {
        List<EntityModification> list = new ArrayList<>(current);
        list.add(modification);
        Collections.sort(list, new Comparator<EntityModification>()
            {
                @Override
                public int compare(EntityModification o1, EntityModification o2)
                {
                    return o1.validFrom.compareTo(o2.validFrom);
                }
            });
        return Collections.unmodifiableList(list);
    }

    private List<PropertyHistoryEntry> selectHistoryPropertyEntries(
            final SQLQuery selectPropertyHistory, final List<Long> entityIds)
    {
        selectPropertyHistory.setParameterList("entityIds", entityIds);
        selectPropertyHistory.setResultTransformer(new ResultTransformer()
            {
                private static final long serialVersionUID = 1L;

                @Override
                public Object transformTuple(Object[] values, String[] aliases)
                {
                    PropertyHistoryEntry entry = new PropertyHistoryEntry();
                    int i = 0;
                    entry.permId = (String) values[i++];
                    entry.propertyCode = (String) values[i++];
                    entry.value = (String) values[i++];
                    entry.userId = (String) values[i++];
                    entry.validFrom = (Date) values[i++];
                    entry.validUntil = (Date) values[i++];
                    return entry;
                }

                @SuppressWarnings("rawtypes")
                @Override
                public List transformList(List list)
                {
                    return list;
                }
            });
        return cast(selectPropertyHistory.list());
    }

    private List<RelationshipHistoryEntry> selectRelationshipHistoryEntries(final SQLQuery selectRelationshipHistory,
            final List<Long> entityIds)
    {
        selectRelationshipHistory.setParameterList("entityIds", entityIds);
        selectRelationshipHistory.setResultTransformer(new ResultTransformer()
            {
                private static final long serialVersionUID = 1L;

                @Override
                public Object transformTuple(Object[] values, String[] aliases)
                {
                    RelationshipHistoryEntry entry = new RelationshipHistoryEntry();
                    int i = 0;
                    entry.permId = (String) values[i++];
                    entry.relationType = (String) values[i++];
                    entry.relatedEntity = (String) values[i++];
                    entry.entityType = (String) values[i++];
                    entry.userId = (String) values[i++];
                    entry.validFrom = (Date) values[i++];
                    entry.validUntil = (Date) values[i++];
                    return entry;
                }

                @SuppressWarnings("rawtypes")
                @Override
                public List transformList(List list)
                {
                    return list;
                }
            });
        return cast(selectRelationshipHistory.list());
    }

    @SuppressWarnings("unchecked")
    protected final <T> List<T> cast(final List<?> list)
    {
        return (List<T>) list;
    }

}
