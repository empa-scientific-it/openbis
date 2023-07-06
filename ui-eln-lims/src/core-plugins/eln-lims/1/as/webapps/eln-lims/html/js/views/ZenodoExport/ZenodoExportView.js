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

function ZenodoExportView(exportController, exportModel) {
    this.repaint = function(views) {
        var $header = views.header;
        var $container = views.content;

        var $form = $("<div>");
        var $formColumn = $("<form>", {
            'name': 'zenodoExportForm',
            'role': 'form',
            'action': 'javascript:void(0);',
            'onsubmit': 'mainController.currentView.exportSelected();'
        });
        $form.append($formColumn);

        var $infoBox1 = FormUtil.getInfoBox('You can select any parts of the accessible openBIS structure to export:', [
            'If you select a tree node and do not expand it, everything below this node will be exported by default.',
            'To export selectively only parts of a tree, open the nodes and select what to export.'
        ]);
        $infoBox1.css('border', 'none');
        $container.append($infoBox1);

        var $infoBox2 = FormUtil.getInfoBox('Publication time constraint', [
            'After the resource has been exported it should be published in Zenodo UI within 2 hours.',
            'Otherwise, the publication metadata will not be registered in openBIS.'
        ]);
        $infoBox2.css('border', 'none');
        $container.append($infoBox2);

        var $tree = $('<div>', { 'id' : 'exportsTree' });
        $formColumn.append($('<br>'));
        $formColumn.append(FormUtil.getBox().append($tree));

        $container.append($form);

        exportModel.tree = TreeUtil.getCompleteTree($tree);
        exportModel.tableModel = ExportUtil.getTableModel();

        this.paintTitleTextBox($container);
        if (exportModel.tableModel.getValues().length > 0) {
            ExportUtil.paintGroupCheckboxes($container, "zenodo-groups");
        }

        var $formTitle = $('<h2>').append('Zenodo Export Builder');
        $header.append($formTitle);

        var $exportButton = $('<input>', { 'type': 'submit', 'class': 'btn btn-primary', 'value': 'Export Selected',
            'onClick': '$("form[name=\'zenodoExportForm\']").submit()'});
        $header.append($exportButton);
    };

    this.paintTitleTextBox = function ($container) {
        this.$titleTextBox = FormUtil.getTextInputField('zenodo-submission-title', 'Submission title', true);
        var titleTextBoxFormGroup = FormUtil.getFieldForComponentWithLabel(this.$titleTextBox, 'Submission Title', null, true);
        titleTextBoxFormGroup.css('width', '50%');
        $container.append(titleTextBoxFormGroup);
    };



}