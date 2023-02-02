/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.client.api.v1;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.text.JTextComponent;

import org.apache.log4j.PropertyConfigurator;

import ch.systemsx.cisd.common.ssl.SslCertificateHelper;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IDataSetDss;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.filter.TypeBasedDataSetFilter;
import ch.systemsx.cisd.openbis.plugin.screening.client.api.v1.ScreeningOpenbisServiceFacade.IImageOutputStreamProvider;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVector;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDataset;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IDatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageSize;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Plate;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateImageReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.WellIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.WellPosition;

/**
 * A test class which shows how to use API.
 * 
 * @author Tomasz Pylak
 */
public class ScreeningClientApiTester
{
    private static final class Form extends JPanel
    {
        private static final long serialVersionUID = 1L;

        private final JPanel panel;

        private final Component parent;

        private final String title;

        Form(Component parent, String title)
        {
            super(new BorderLayout());
            this.parent = parent;
            this.title = title;
            panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            add(panel, BorderLayout.CENTER);
        }

        JTextComponent createTextField(String fieldName, int width, boolean passwordField)
        {
            JTextComponent result = passwordField ? new JPasswordField(width) : new JTextField(width);
            addField(fieldName, result);
            return result;
        }

        void addField(String fieldName, JComponent field)
        {
            JPanel fieldPanel = new JPanel(new BorderLayout());
            JLabel label = new JLabel(fieldName + ":");
            Dimension preferredSize = label.getPreferredSize();
            label.setPreferredSize(new Dimension(100, preferredSize.height));
            fieldPanel.add(label, BorderLayout.WEST);
            fieldPanel.add(field, BorderLayout.CENTER);
            panel.add(fieldPanel);
        }

        void showForm()
        {
            JOptionPane.showMessageDialog(parent, this, title, JOptionPane.QUESTION_MESSAGE);
        }

    }

    private static final class TesterFrame extends JFrame
    {
        private static final long serialVersionUID = 1L;

        private IScreeningOpenbisServiceFacade facade;

        private JPanel content;

        private JMenu callApiMenu;

        TesterFrame()
        {
            setTitle("Screening API Tester");
            setSize(800, 600);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            Container contentPane = getContentPane();

            content = new JPanel();
            content.setLayout(new BorderLayout());
            JScrollPane scrollPane = new JScrollPane(content);
            scrollPane.getVerticalScrollBar().setBlockIncrement(40);
            contentPane.add(scrollPane);

            JMenuBar menuBar = new JMenuBar();
            setJMenuBar(menuBar);
            callApiMenu = new JMenu("Call API");
            callApiMenu.setEnabled(false);
            menuBar.add(callApiMenu);
            JMenuItem loadPlatesMenuItem = new JMenuItem("List Plates");
            loadPlatesMenuItem.addActionListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        loadPlates();
                    }
                });
            callApiMenu.add(loadPlatesMenuItem);
            JMenuItem loadImagesByDataSetMenu = new JMenuItem("Load Images by Data Set Code...");
            callApiMenu.add(loadImagesByDataSetMenu);
            loadImagesByDataSetMenu.addActionListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        loadImagesByDataSetCode();
                    }
                });
            JMenuItem overlayItem = new JMenuItem("List Overlay Data Sets");
            callApiMenu.add(overlayItem);
            overlayItem.addActionListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        loadOverlays();
                    }
                });
            JMenuItem listAnalysisProceduresMenuItem = new JMenuItem("List Analysis Procedures...");
            callApiMenu.add(listAnalysisProceduresMenuItem);
            listAnalysisProceduresMenuItem.addActionListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        listAnalysisProcedures();
                    }
                });
            JMenuItem listPlatesMenuItem = new JMenuItem("List Plates...");
            callApiMenu.add(listPlatesMenuItem);
            listPlatesMenuItem.addActionListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        listPlates();
                    }
                });
            JMenuItem listFeatureVectorsMenuItem = new JMenuItem("Counts Feature Vectors");
            callApiMenu.add(listFeatureVectorsMenuItem);
            listFeatureVectorsMenuItem.addActionListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        loadFeatureVectors();
                    }
                });
        }

        void setUp(String[] args)
        {
            setVisible(true);
            if (args.length == 3)
            {
                String serverUrl = args[2];
                SslCertificateHelper.trustAnyCertificate(serverUrl);
                facade =
                        ScreeningOpenbisServiceFacadeFactory.INSTANCE.tryToCreate(args[0], args[1],
                                serverUrl);
            } else
            {
                Form form = new Form(this, "Connection to openBIS");
                JTextField url = new JTextField(20);
                JTextField user = new JTextField(20);
                JTextField password = new JPasswordField(20);
                form.addField("Base URL", url);
                form.addField("User ID", user);
                form.addField("Password", password);
                form.showForm();
                facade =
                        ScreeningOpenbisServiceFacadeFactory.INSTANCE.tryToCreate(user.getText(),
                                user.getText(), url.getText());
                if (facade == null)
                {
                    throw new RuntimeException("Couldn't connect openBIS.");
                }
                JOptionPane.showMessageDialog(this, "Successfully connected to openBIS.");
            }
            if (facade != null)
            {
                callApiMenu.setEnabled(true);
            }
        }

        private void loadOverlays()
        {
            content.removeAll();
            final JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            content.add(panel, BorderLayout.NORTH);
            JTextArea textArea = new JTextArea();
            textArea.setEditable(false);
            content.add(textArea, BorderLayout.CENTER);
            List<Plate> plates = facade.listPlates();
            panel.add(new JLabel(plates.size() + " plates", SwingConstants.LEFT));
            validate(panel);
            List<ImageDatasetReference> rawImageDatasets = facade.listRawImageDatasets(plates);
            panel.add(new JLabel(rawImageDatasets.size() + " raw image data sets", SwingConstants.LEFT));
            validate(panel);
            List<ImageDatasetReference> overlays = facade.listSegmentationImageDatasets(plates, null);
            panel.add(new JLabel(overlays.size() + " overlay data sets:", SwingConstants.LEFT));
            StringBuilder builder = new StringBuilder();
            for (ImageDatasetReference overlay : overlays)
            {
                builder.append(overlay).append('\n');
                ImageDatasetReference parent = overlay.getParentImageDatasetReference();
                if (parent != null)
                {
                    builder.append("      overlay data set of ").append(parent).append('\n');
                }
            }
            textArea.setText(builder.toString());
        }

        private void loadPlates()
        {
            content.removeAll();
            final JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            content.add(panel, BorderLayout.CENTER);
            List<Plate> plates = facade.listPlates();
            for (int i = 0, n = Math.min(10, plates.size()); i < n; i++)
            {
                final Plate plate = plates.get(i);
                final List<WellIdentifier> listPlateWells = facade.listPlateWells(plate);
                JButton button = new JButton(plate.toString() + " " + listPlateWells.size() + " wells");
                button.addActionListener(new ActionListener()
                    {
                        @Override
                        public void actionPerformed(ActionEvent e)
                        {
                            Map<String, String> plateProperties = facade.getPlateProperties(plate);
                            StringBuilder builder = new StringBuilder();
                            for (Entry<String, String> property : plateProperties.entrySet())
                            {
                                builder.append('\n').append(property.getKey()).append(" = ").append(property.getValue());
                            }
                            JOptionPane.showMessageDialog(TesterFrame.this, "Plate "
                                    + plate.getAugmentedCode() + " has these properties:" + builder);
                            if (listPlateWells.isEmpty())
                            {
                                return;
                            }
                            for (WellIdentifier wellIdentifier : listPlateWells)
                            {
                                List<IDataSetDss> dataSets =
                                        facade.getDataSets(wellIdentifier,
                                                new TypeBasedDataSetFilter(".*"));
                                if (dataSets.isEmpty() == false)
                                {
                                    JOptionPane.showMessageDialog(TesterFrame.this, "Well "
                                            + wellIdentifier + " has " + dataSets.size() + " data sets");
                                    break;
                                }
                            }
                        }
                    });
                panel.add(button);
            }
            validate(panel);
        }

        private void listAnalysisProcedures()
        {
            Form form = new Form(this, "Analysis Procedures for an Experiment");
            List<ExperimentIdentifier> experiments = facade.listExperiments();
            JComboBox experimentComboBox =
                    new JComboBox(experiments.toArray(new ExperimentIdentifier[0]));
            form.addField("Experiment", experimentComboBox);
            form.showForm();
            List<String> analysisProcedures =
                    facade.listAnalysisProcedures((ExperimentIdentifier) experimentComboBox
                            .getSelectedItem());
            JOptionPane.showMessageDialog(this, "Analysis Procedures: " + analysisProcedures);
        }

        private void listPlates()
        {
            Form form = new Form(this, "Plates for an Experiment");
            List<ExperimentIdentifier> experiments = facade.listExperiments();
            JComboBox experimentComboBox =
                    new JComboBox(experiments.toArray(new ExperimentIdentifier[0]));
            form.addField("Experiment", experimentComboBox);
            JTextField analysisProcedureField = new JTextField();
            form.addField("Analysis Procedure", analysisProcedureField);
            form.showForm();
            List<Plate> plates = facade.listPlates((ExperimentIdentifier) experimentComboBox
                    .getSelectedItem(), analysisProcedureField.getText());
            JOptionPane.showMessageDialog(this, "Plates: " + plates);
        }

        private void loadFeatureVectors()
        {
            List<Plate> plates = facade.listPlates();
            List<FeatureVectorDatasetReference> featureDatasets = facade.listFeatureVectorDatasets(plates);
            long t0 = System.currentTimeMillis();
            List<FeatureVectorDataset> features = facade.loadFeatures(featureDatasets, null);
            long duration = System.currentTimeMillis() - t0;
            int featureVectors = 0;
            int featureColumns = 0;
            for (FeatureVectorDataset featureVectorDataSet : features)
            {
                List<FeatureVector> featureVector = featureVectorDataSet.getFeatureVectors();
                featureColumns += featureVectorDataSet.getFeatureCodes().size();
                featureVectors += featureVector.size();
            }
            JOptionPane.showMessageDialog(this, plates.size() + " Plates\n" + features.size()
                    + " Feature Data Sets\n" + featureVectors + " Feature Vectors\n"
                    + featureColumns + " Features\ntook " + duration + " msec");
        }

        private void loadImagesByDataSetCode()
        {
            Form form = new Form(this, "Parameters for Loading Images by Data Set");
            List<ImageDatasetReference> dataSets = facade.listRawImageDatasets(facade.listPlates());
            List<String> dataSetCodes = new ArrayList<String>();
            for (ImageDatasetReference imageDataset : dataSets)
            {
                dataSetCodes.add(imageDataset.getDatasetCode());
            }
            JComboBox dataSetCodesComboBox = new JComboBox(dataSetCodes.toArray(new String[0]));
            form.addField("Data Set", dataSetCodesComboBox);
            JTextComponent wellsField = form.createTextField("Wells", 20, false);
            JTextComponent channelField = form.createTextField("Channel", 20, false);
            JTextComponent sizeField = form.createTextField("Size", 20, false);
            sizeField.setText("200x160");
            form.showForm();
            String dataSetCode = dataSetCodesComboBox.getSelectedItem().toString();
            final List<IDatasetIdentifier> datasetIdentifiers = facade.getDatasetIdentifiers(Arrays.asList(dataSetCode));
            if (datasetIdentifiers.isEmpty())
            {
                JOptionPane.showMessageDialog(this, "Unkown data set: " + dataSetCode);
            }
            try
            {
                final List<WellPosition> wellPositions = WellPosition.parseWellPositions(wellsField.getText());
                content.removeAll();
                final JPanel imagePanel = new JPanel();
                imagePanel.setLayout(new BoxLayout(imagePanel, BoxLayout.Y_AXIS));
                content.add(imagePanel, BorderLayout.CENTER);
                final String channel = channelField.getText().toUpperCase();
                final ImageSize imageSize = getImageSize(sizeField);
                new Thread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            final long t0 = System.currentTimeMillis();
                            try
                            {
                                facade.loadImages(datasetIdentifiers.get(0), wellPositions,
                                        channel, imageSize, createPlateImageHandler(imagePanel, t0));
                            } catch (final Throwable ex)
                            {
                                ex.printStackTrace();
                                EventQueue.invokeLater(new Runnable()
                                    {

                                        @Override
                                        public void run()
                                        {
                                            JOptionPane.showMessageDialog(TesterFrame.this,
                                                    ex.toString());
                                        }
                                    });
                            }
                        }
                    }).start();
            } catch (Exception ex)
            {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, ex.toString());
            }
        }

        private void showFullImage(final PlateImageReference plateImageReference)
        {
            new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        try
                        {
                            facade.loadImages(Collections.singletonList(plateImageReference),
                                    new IImageOutputStreamProvider()
                                        {
                                            @Override
                                            public OutputStream getOutputStream(
                                                    PlateImageReference imageReference)
                                                    throws IOException
                                            {
                                                return outputStream;
                                            }
                                        }, true);
                            EventQueue.invokeLater(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        ImageIcon image = new ImageIcon(outputStream.toByteArray());
                                        JScrollPane scrollPane = new JScrollPane(new JLabel(image));
                                        scrollPane.getHorizontalScrollBar().setBlockIncrement(40);
                                        scrollPane.getVerticalScrollBar().setBlockIncrement(40);
                                        scrollPane.setPreferredSize(new Dimension(800, 600));
                                        JFrame frame = new JFrame(plateImageReference.toString());
                                        frame.getContentPane().add(scrollPane);
                                        frame.setSize(800, 600);
                                        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                                        frame.setVisible(true);
                                    }
                                });
                        } catch (Exception ex)
                        {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(TesterFrame.this, ex.toString());
                        }
                    }
                }).start();
        }

        private ImageSize getImageSize(JTextComponent sizeField)
        {
            String text = sizeField.getText();
            if (text == null || text.length() == 0)
            {
                return null;
            }
            int indexOfX = text.indexOf('x');

            int width = Integer.parseInt(text.substring(0, indexOfX));
            int height = Integer.parseInt(text.substring(indexOfX + 1));
            return new ImageSize(width, height);
        }

        private void validate(JComponent component)
        {
            component.invalidate();
            getContentPane().validate();
        }

        private IPlateImageHandler createPlateImageHandler(final JPanel imagePanel, final long t0)
        {
            return new IPlateImageHandler()
                {
                    @Override
                    public void handlePlateImage(final PlateImageReference plateImageReference,
                            final byte[] imageFileBytes)
                    {
                        System.out.println(plateImageReference + " loaded after "
                                + (System.currentTimeMillis() - t0) + " msec");
                        EventQueue.invokeLater(new Runnable()
                            {

                                @Override
                                public void run()
                                {
                                    JButton image =
                                            new JButton(plateImageReference.toString(),
                                                    new ImageIcon(imageFileBytes));
                                    image.addActionListener(new ActionListener()
                                        {

                                            @Override
                                            public void actionPerformed(ActionEvent e)
                                            {
                                                showFullImage(plateImageReference);
                                            }
                                        });
                                    imagePanel.add(image);
                                    validate(imagePanel);
                                }
                            });
                    }
                };
        }
    }

    public static void main(String[] args) throws IOException
    {
        configureLogging();
        TesterFrame testerFrame = new TesterFrame();
        try
        {
            testerFrame.setUp(args);
        } catch (Throwable ex)
        {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(testerFrame, ex.toString());
        }
    }

    private static void configureLogging()
    {
        Properties props = new Properties();
        props.put("log4j.appender.STDOUT", "org.apache.log4j.ConsoleAppender");
        props.put("log4j.appender.STDOUT.layout", "org.apache.log4j.PatternLayout");
        props.put("log4j.appender.STDOUT.layout.ConversionPattern", "%d %-5p [%t] %c - %m%n");
        props.put("log4j.rootLogger", "INFO, STDOUT");
        PropertyConfigurator.configure(props);
    }
}
