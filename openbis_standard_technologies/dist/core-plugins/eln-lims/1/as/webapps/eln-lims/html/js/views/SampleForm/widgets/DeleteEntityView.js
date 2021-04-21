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

function DeleteEntityView(deleteEntityController, deleteEntityModel, $plugin) {
	this._deleteEntityController = deleteEntityController;
	this._deleteEntityModel = deleteEntityModel;
	
	this.repaint = function() {
		var _this = this;
		var $window = $('<form>', { 'action' : 'javascript:void(0);' });
		$window.submit(function() {
			_this._deleteEntityModel.deleteFunction(_this._deleteEntityModel.reason);
			Util.unblockUI();
		});
		
		$window.append($('<legend>').append('Confirm Delete'));
        if ($plugin) {
            $window.append($plugin);
        } else if (this._deleteEntityModel.warningText) {
			var $warning = FormUtil.getFieldForLabelWithText(null, this._deleteEntityModel.warningText);
			$warning.css('color', FormUtil.warningColor);
			$window.append($warning);
		}
		
		if(this._deleteEntityModel.includeReason) {
			var $reasonTextBox = FormUtil._getTextBox("reason-to-delete-id", 'Reason for the delete', true);
			$reasonTextBox.keyup(function(event) {
				_this._deleteEntityModel.reason = $(this).val();
			});
			var $reasonTextBoxGroup = FormUtil.getFieldForComponentWithLabel($reasonTextBox, 'Reason');
			$window.append($reasonTextBoxGroup);
		}
		
		
		var $btnAccept = $('<input>', { 'type': 'submit', 'class' : 'btn btn-primary', 'value' : 'Accept' , 'id' : 'accept-btn'});
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