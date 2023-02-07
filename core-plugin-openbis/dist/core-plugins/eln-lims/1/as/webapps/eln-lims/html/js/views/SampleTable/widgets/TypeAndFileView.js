/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
 *
 * Licensed under the Apache License, Version 2.0 (the 'License');
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

function TypeAndFileView(typeAndFileController, typeAndFileModel) {
	this._typeAndFileController = typeAndFileController;
	this._typeAndFileModel = typeAndFileModel;
	this.fileChooser = $('<input>', { 'type' : 'file', 'id' : 'fileToRegister' , 'required' : ''});
	this.linkContainer = $("<div>");

	this.repaint = function() {
		var _this = this;
		var $window = $('<form>', { 'action' : 'javascript:void(0);' });
		$window.submit(function() {
			Util.unblockUI();
			_this._typeAndFileModel.actionFunction(_this._typeAndFileModel.sampleType, _this._typeAndFileModel.file);
		});

		$window.append($('<legend>').append(this._typeAndFileModel.title));

		var $sampleTypeDropDown = FormUtil.getSampleTypeDropdown('choose-type-btn', true, this._typeAndFileModel.allowedSampleTypes, this._typeAndFileModel.allowedSampleTypes);
		$sampleTypeDropDown.change(function(event) {
			_this._typeAndFileModel.sampleTypeCode = $(this).val();
			_this.updateLink($(this).val());
		});
		var $sampleTypeDropDownBoxGroup = FormUtil.getFieldForComponentWithLabel($sampleTypeDropDown, ELNDictionary.Sample + ' Type');
		$window.append($sampleTypeDropDownBoxGroup);

		$window.append(this.linkContainer);

		this.fileChooser.change(function(event) {
			_this._typeAndFileModel.file = _this.fileChooser[0].files[0];
		});
		var $fileChooserBoxGroup = FormUtil.getFieldForComponentWithLabel(this.fileChooser, 'File');
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

	this.updateLink = function(sampleTypeCode) {
		this.linkContainer.empty();
		if(sampleTypeCode !== "") {
			var $component = $("<p>", {'class' : 'form-control-static', 'style' : 'border:none; box-shadow:none; background:transparent;'});
			var $link = $("<a>", {
				href : mainController.serverFacade.getTemplateLink("SAMPLE", sampleTypeCode,
					this._typeAndFileModel.linkType, 'tsv'),
				target : "_blank"
			}).text("Download");
			$component.append($link);
			var $linkGroup = FormUtil.getFieldForComponentWithLabel($component, 'Template');
			this.linkContainer.append($linkGroup);
		}
	}

}