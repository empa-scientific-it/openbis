package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework;

import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;

/**
 * Describes column's metadata and UI.
 * 
 * @author Tomasz Pylak
 */
public interface IColumnDefinitionUI<T> extends IColumnDefinition<T>
{
    /** width of the column */
    int getWidth();

    /** Returns <code>true</code> if this column should initially be hidden. */
    boolean isHidden();

    /**
     * Returns <code>true</code> if the values of the column are numerically.
     */
    boolean isNumeric();

    /**
     * Returns <code>true</code> if the column cell is editable.
     */
    boolean isEditable();

    /**
     * Returns <code>true</code> if the column cell is the controlled vocabulary
     */
    boolean isVocabulary();

    /**
     * Returns the vocabulary if the column cell is a controlled vocabulary
     */
    Vocabulary tryGetVocabulary();

    String tryGetLink(T entity);

    /**
     * Returns <code>true</code> if the column cell is a dynamic property
     */
    boolean isDynamicProperty();
}
