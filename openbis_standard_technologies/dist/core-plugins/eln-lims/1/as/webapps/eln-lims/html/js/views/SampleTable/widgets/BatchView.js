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
            Util.unblockUI();
            _this._model.actionFunction(_this._model.file);
        });
        $window.append($('<legend>').append(this._model.title));

        $window.append("Allowed " + ELNDictionary.sample + " types: " + this._model.allowedSampleTypes.join(", "));
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