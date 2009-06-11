package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property_type;

import java.util.List;

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.widget.Component;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.vocabulary.VocabularyRegistrationFieldSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.vocabulary.VocabularySelectionWidget;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;

/**
 * A small {@link VocabularySelectionWidget} extension suitable for <i>Property Type</i>
 * registration.
 * 
 * @author Christian Ribeaud
 */
final class VocabularySelectionWidgetForPropertyTypeRegistration extends VocabularySelectionWidget
{
    private final Component vocabularyRegistrationFieldSet;

    VocabularySelectionWidgetForPropertyTypeRegistration(
            final IViewContext<ICommonClientServiceAsync> viewContext,
            final VocabularyRegistrationFieldSet vocabularyRegistrationFieldSet)
    {
        super(viewContext);
        assert vocabularyRegistrationFieldSet != null : "Unspecified VocabularyRegistrationFieldSet";
        this.vocabularyRegistrationFieldSet = vocabularyRegistrationFieldSet;
        addSelectionChangedListener(new SelectionChangedListener<BaseModelData>()
            {

                //
                // SelectionChangedListener
                //

                @Override
                public final void selectionChanged(final SelectionChangedEvent<BaseModelData> se)
                {
                    // NOTE: Somehow this SelectionChangedEvent is fired when
                    // Property Registration Form and Data Type is not yet selected or even visible.
                    // It happens only if there are no Controlled Vocabularies registered
                    // and the only ComboBox item is '(New Vocabulary)'.
                    if (VocabularySelectionWidgetForPropertyTypeRegistration.this.isVisible())
                    {
                        final BaseModelData selectedItem = se.getSelectedItem();
                        final boolean visible;
                        if (selectedItem != null)
                        {
                            visible =
                                    selectedItem.get(ModelDataPropertyNames.CODE).equals(
                                            NEW_VOCABULARY_CODE);
                        } else
                        {
                            visible = false;
                        }
                        vocabularyRegistrationFieldSet.setVisible(visible);
                    }
                }
            });

    }

    //
    // VocabularySelectionWidget
    //

    @Override
    public final void setVisible(final boolean visible)
    {
        super.setVisible(visible);
        if (visible == false && isRendered())
        {
            vocabularyRegistrationFieldSet.setVisible(visible);
        }
    }

    @Override
    protected void loadData(final AbstractAsyncCallback<List<Vocabulary>> callback)
    {
        super.loadData(new AbstractAsyncCallback<List<Vocabulary>>(null)
            {
                @Override
                protected void process(List<Vocabulary> result)
                {
                    result.add(0, createNewVocabulary());
                    callback.onSuccess(result);
                }
            });
    }
}