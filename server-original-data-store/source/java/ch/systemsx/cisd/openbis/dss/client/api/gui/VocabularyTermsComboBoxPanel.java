/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.client.api.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import ch.systemsx.cisd.openbis.dss.client.api.gui.model.DataSetUploadClientModel;
import ch.systemsx.cisd.openbis.dss.client.api.gui.model.DataSetUploadClientModel.Observer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.ControlledVocabularyPropertyType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.VocabularyTerm;

/**
 * The class creates a ComboBox together with button which makes it possible to add new Vocabulary Term.
 * 
 * @author Pawel Glyzewski
 */
public class VocabularyTermsComboBoxPanel extends JPanel implements Observer
{

    /**
     * An adaptor to convert VocabularyTerms into something that can be put into combo boxes.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    protected static final class VocabularyTermAdaptor
    {
        private final VocabularyTerm term;

        private VocabularyTermAdaptor(VocabularyTerm term)
        {
            this.term = term;
        }

        @Override
        public String toString()
        {
            return term.getLabel();
        }

        public Long getOrdinal()
        {
            return term.getOrdinal();
        }
    }

    /**
     * A renderer which renders 'unofficial' vocabulary terms as grey and italic
     * 
     * @author Pawel Glyzewski
     */
    private static final class VocabularyTermsRenderer extends BasicComboBoxRenderer
    {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index,
                boolean isSelected, boolean cellHasFocus)
        {
            Component result =
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value != null && value instanceof VocabularyTermAdaptor)
            {
                VocabularyTermAdaptor termAdaptor = (VocabularyTermAdaptor) value;
                if (termAdaptor.term != null && termAdaptor.term.isOfficial() == false)
                {
                    result.setForeground(Color.GRAY);
                    Font font =
                            new Font(result.getFont().getName(), Font.ITALIC, result.getFont()
                                    .getSize());
                    result.setFont(font);
                }
            }

            return result;
        }
    }

    private static final long serialVersionUID = 1L;

    private final JComboBox comboBox;

    private final JButton button;

    private final Vocabulary vocabulary;

    public VocabularyTermsComboBoxPanel(final ControlledVocabularyPropertyType propertyType,
            final DataSetUploadClientModel clientModel)
    {
        super(new BorderLayout());

        this.button = new JButton("+");
        button.setMargin(new Insets(button.getMargin().top, 2, button.getMargin().bottom, 2));
        button.setFocusable(false);
        this.add(button, BorderLayout.EAST);

        this.comboBox = new JComboBox();
        this.add(comboBox, BorderLayout.CENTER);
        this.vocabulary = clientModel.getVocabulary(propertyType.getVocabulary().getCode());
        fillComboBoxWithTerms(vocabulary.getTerms(), null);
        comboBox.setRenderer(new VocabularyTermsRenderer());

        button.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    AddVocabularyTermDialog dialog =
                            new AddVocabularyTermDialog((JFrame) SwingUtilities
                                    .getRoot(VocabularyTermsComboBoxPanel.this), comboBox
                                    .getModel(), vocabulary, clientModel);

                    dialog.setVisible(true);
                }
            });
    }

    private void fillComboBoxWithTerms(List<VocabularyTerm> terms, String selectedCodeOrNull)
    {
        comboBox.removeAllItems();
        for (VocabularyTerm term : terms)
        {
            VocabularyTermAdaptor adaptor = new VocabularyTermAdaptor(term);
            comboBox.addItem(adaptor);
            if (adaptor.term.getCode().equals(selectedCodeOrNull))
            {
                comboBox.setSelectedItem(adaptor);
            }
        }
    }

    public int getItemCount()
    {
        return comboBox.getItemCount();
    }

    public VocabularyTerm getItemAt(int i)
    {
        return ((VocabularyTermAdaptor) comboBox.getItemAt(i)).term;
    }

    public void setSelectedIndex(int i)
    {
        comboBox.setSelectedIndex(i);
    }

    public void addItemListener(final ItemListener itemListener)
    {
        comboBox.addItemListener(new ItemListener()
            {
                @Override
                public void itemStateChanged(ItemEvent e)
                {
                    itemListener.itemStateChanged(new ItemEvent(e.getItemSelectable(), e.getID(),
                            ((VocabularyTermAdaptor) e.getItem()).term, e.getStateChange()));
                }
            });
    }

    @Override
    public void setToolTipText(String text)
    {
        comboBox.setToolTipText(text);
    }

    @Override
    public void update(@SuppressWarnings("hiding") Vocabulary vocabulary, String code)
    {
        String selectedCode =
                vocabulary.getId() != this.vocabulary.getId() ? ((VocabularyTermAdaptor) comboBox
                        .getSelectedItem()).term.getCode() : code;
        fillComboBoxWithTerms(vocabulary.getTerms(), selectedCode);
    }

    @Override
    public void setEnabled(boolean enabled)
    {
        comboBox.setEnabled(enabled);
        button.setEnabled(enabled);
    }
}
