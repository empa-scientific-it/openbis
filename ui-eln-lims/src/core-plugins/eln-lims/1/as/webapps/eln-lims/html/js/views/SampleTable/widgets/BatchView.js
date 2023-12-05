/*
 * Copyright 2021 ETH Zuerich, Scientific IT Services
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
function BatchView(controller, model) {
    this._controller = controller;
    this._model = model;

    this._fileChooser = $('<input>', { 'type' : 'file', 'id' : 'fileToRegister' , 'required' : ''});

    this.repaint = function() {
        var _this = this;
        var $window = $('<form>', { 'action' : 'javascript:void(0);' });
        $window.submit(function() {
            var allowedSampleTypes = getAllowedSampleTypes();
            if (allowedSampleTypes.length == 0) {
                alert("Select at least one " + ELNDictionary.Sample + " type.");
            } else {
                Util.unblockUI();
                _this._model.actionFunction(_this._model.file, allowedSampleTypes);
            }
        });
        $window.append($('<legend>').append(this._model.title));
        if (this._model.allowSampleTypeSelection) {
            var $sampleTypeField = $('<div>', { class : 'form-inline'});
            $window.append($sampleTypeField);
            var $sampleTypeDropDownLabel = $('<label>', { class : 'control-label', 'style' : 'margin-right: 5px' }).html(ELNDictionary.Sample + ' Type(s) to be imported (*): ');
            $sampleTypeField.append($sampleTypeDropDownLabel);
            var $sampleTypeDropDown = $('<select>', { 'id' : 'sampleTypesSelector' , class : 'multiselect' , 'multiple' : 'multiple'});
            this._model.allowedSampleTypes.forEach(function(type) {
                $sampleTypeDropDown.append($('<option>', { 'value' : type}).text(Util.getDisplayNameFromCode(type)));
            });
            $sampleTypeField.append($sampleTypeDropDown);
            $sampleTypeDropDown.multiselect();
        } else {
            $window.append("Allowed " + ELNDictionary.sample + " types: " + this._model.allowedSampleTypes.join(", "));
        }
        getAllowedSampleTypes = function() {
            if (_this._model.allowSampleTypeSelection) {
                var selected = $sampleTypeDropDown.val();
                return selected ? selected : [];
            }
            return _this._model.allowedSampleTypes;
        }

        var $component = $("<p>", {'class' : 'form-control-static', 'style' : 'border:none; box-shadow:none; background:transparent;'});
        var $templateLink = $("<a>").text("Download");
        $templateLink.on("click", function() {
            var allowedSampleTypes = getAllowedSampleTypes();
            var importMode = _this._model.linkType;
            var templateType = _this._model.allowSampleTypeSelection ? "GENERAL" : "COLLECTION";
            mainController.serverFacade.getSamplesImportTemplate(allowedSampleTypes, templateType, importMode, function(result) {
                var mimeType = "application/application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                var filename = "SAMPLE-" + templateType + "-" + importMode + "-template.xlsx";
                Util.download(result, mimeType, true, filename);
            });
        });
        $component.append($templateLink);
        var $linkGroup = FormUtil.getFieldForComponentWithLabel($component, 'Template');
        $window.append($linkGroup);
        
        this._fileChooser.change(function(event) {
            _this._model.file = _this._fileChooser[0].files[0];
        });
        var $fileChooserBoxGroup = FormUtil.getFieldForComponentWithLabel(this._fileChooser, 'File');
        $window.append($fileChooserBoxGroup);

        var $btnAccept = $('<input>', { 'type': 'submit', 'class' : 'btn btn-primary', 'value' : 'Accept', 'id' : 'accept-type-file' });
        var $btnCancel = $('<a>', { 'class' : 'btn btn-default' }).append('Cancel');
        $btnCancel.click(function() {
            Util.unblockUI();
        });
        $window.append($btnAccept).append('&nbsp;').append($btnCancel);

        var css = {
                'text-align' : 'left',
                'top' : '15%',
                'width' : '70%',
                'left' : '15%',
                'right' : '20%',
                'overflow' : 'hidden'
        };

        Util.blockUI($window, css);
    }
    
}