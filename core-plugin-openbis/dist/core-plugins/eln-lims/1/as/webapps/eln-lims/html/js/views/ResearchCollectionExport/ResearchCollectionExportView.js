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

function ResearchCollectionExportView(researchCollectionExportController, researchCollectionExportModel) {
    this.repaint = function(views) {
        var $header = views.header;
        var $container = views.content;

        var $form = $("<div>");
        var $formColumn = $("<form>", {
            'name': 'rcExportForm',
            'role': 'form',
            'action': 'javascript:void(0);',
            'onsubmit': 'mainController.currentView.exportSelected();'
        });
        $form.append($formColumn);

        var $infoBox = FormUtil.getInfoBox('You can select any parts of the accessible openBIS structure to export:', [
            'If you select a tree node and do not expand it, everything below this node will be exported by default.',
            'To export selectively only parts of a tree, open the nodes and select what to export.'
        ]);
        $infoBox.css('border', 'none');
        $container.append($infoBox);

        var $tree = $('<div>', { 'id' : 'exportsTree' });
        $formColumn.append($('<br>'));
        $formColumn.append(FormUtil.getBox().append($tree));

        $container.append($form);

        researchCollectionExportModel.tree = TreeUtil.getCompleteTree($tree);
        researchCollectionExportModel.tableModel = ExportUtil.getTableModel();

        this.paintSubmissionTypeDropdown($container);
        this.paintRetentionPeriodDropdown($container);

        if (researchCollectionExportModel.tableModel.getValues().length > 0) {
            ExportUtil.paintGroupCheckboxes($container, "rc-groups");
        }

        var $formTitle = $('<h2>').append('Research Collection Export Builder');
        $header.append($formTitle);

        var $exportButton = $('<input>', {'type': 'submit', 'class': 'btn btn-primary', 'value': 'Export Selected',
                'onClick': '$("form[name=\'rcExportForm\']").submit()'});
        $header.append($exportButton);
    };

    this.paintSubmissionTypeDropdown = function($container) {
        this.$submissionTypeDropdown = this.getSubmissionTypeDropdown();
        var submissionTypeDropdownFormGroup = FormUtil.getFieldForComponentWithLabel(this.$submissionTypeDropdown, 'Submission Type', null, true);
        submissionTypeDropdownFormGroup.css('width', '50%');
        $container.append(submissionTypeDropdownFormGroup);
    };

    this.paintRetentionPeriodDropdown = function($container) {
        this.$retentionPeriodDropdown = this.getRetentionPeriodDropdown();
        var retentionPeriodDropdownFormGroup = FormUtil.getFieldForComponentWithLabel(this.$retentionPeriodDropdown, 'Retention Period', null, true);
        retentionPeriodDropdownFormGroup.css('width', '50%');
        $container.append(retentionPeriodDropdownFormGroup);
    };

    this.getSubmissionTypeDropdown = function() {
        return FormUtil.getDropdown(researchCollectionExportModel.submissionTypes, 'Select a submission type');
    };

    this.getRetentionPeriodDropdown = function() {
        var values = [
            {
                value: '10 years',
                label: '10 years'
            },
            {
                value: '15 years',
                label: '15 years'
            },
            {
                value: 'indefinite',
                label: 'indefinite'
            }
        ];
        return FormUtil.getDropdown(values, 'Select a retention period');
    };

}