package ch.systemsx.cisd.openbis.generic.shared.dto;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;

/**
 * Persistent entity representing an invocation of the {@link ServiceForDataStoreServer#performEntityOperations(String, AtomicEntityOperationDetails)}
 * method. This table is used to check if the results of an invocation of this method made it into the database.
 * 
 * @author Chandrasekhar Ramakrishnan
 */

@Entity
@Table(name = TableNames.ENTITY_OPERATIONS_LOG_TABLE)
public class EntityOperationsLogEntryPE implements IIdHolder, Serializable
{
    private static final long serialVersionUID = IServer.VERSION;

    private Long id;

    private Long registrationId;

    @Override
    @SequenceGenerator(name = SequenceNames.ENTITY_OPERATIONS_LOG_SEQUENCE, sequenceName = SequenceNames.ENTITY_OPERATIONS_LOG_SEQUENCE, allocationSize = 1)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SequenceNames.ENTITY_OPERATIONS_LOG_SEQUENCE)
    public final Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    @NotNull(message = ValidationMessages.REGISTRATION_ID_NOT_NULL_MESSAGE)
    @Column(name = ColumnNames.REGISTRATION_ID)
    public Long getRegistrationId()
    {
        return registrationId;
    }

    public void setRegistrationId(Long registrationId)
    {
        this.registrationId = registrationId;
    }

}
