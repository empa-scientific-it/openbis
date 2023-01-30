package ch.systemsx.cisd.openbis.generic.shared.dto;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;

/**
 * Persistence entity representing the DataSet in the PostRegistration Queue.
 * 
 * @author Jakub Straszewski
 */

@Entity
@Table(name = TableNames.POST_REGISTRATION_DATASET_QUEUE_TABLE)
public class PostRegistrationPE implements IIdHolder, Serializable
{
    private static final long serialVersionUID = IServer.VERSION;

    private Long id;

    @Override
    @SequenceGenerator(name = SequenceNames.POST_REGISTRATION_DATASET_QUEUE_SEQUENCE, sequenceName = SequenceNames.POST_REGISTRATION_DATASET_QUEUE_SEQUENCE, allocationSize = 1)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SequenceNames.POST_REGISTRATION_DATASET_QUEUE_SEQUENCE)
    public final Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    private DataPE dataSet;

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = DataPE.class)
    @JoinColumn(name = ColumnNames.DATA_SET_COLUMN, updatable = true)
    public DataPE getDataSet()
    {
        return dataSet;
    }

    public void setDataSet(DataPE dataSet)
    {
        this.dataSet = dataSet;
    }

}
