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

function ResetPasswordView(resetPasswordController, resetPasswordModel) {
	this._resetPasswordController = resetPasswordController;
	this._resetPasswordModel = resetPasswordModel;
	
	this.repaint = function() {
		var _this = this;
		var $window = $('<form>', {
			'action' : 'javascript:void(0);'
		});
		
		$window.submit(function() {
			_this._resetPasswordController.resetPassword();
		});
		
		$window.append($('<legend>').append("Change Password"));
		
		//
		// Warning
		//
		$window.append($("<p>")
				.append($("<span>", { class: "glyphicon glyphicon-info-sign" }))
				.append(" Password change only works with file authentication service."));
		//
		// User ID
		//
		var $userIdField = FormUtil._getInputField('text', null, 'User ID', null, true);
		$userIdField.val(this._resetPasswordModel.userId);
		$userIdField.prop('disabled', true);
		var $userIdGroup = FormUtil.getFieldForComponentWithLabel($userIdField, "User ID", null);
		$window.append($userIdGroup);
		
		//
		// Password
		//
		var $passField = FormUtil._getInputField('password', null, 'Password', null, true);
		$passField.change(function(event) {
			_this._resetPasswordModel.password = $(this).val();
		});
		var $passwordGroup = FormUtil.getFieldForComponentWithLabel($passField, "Password", null);
		$window.append($passwordGroup);
		
		var $passFieldRepeat = FormUtil._getInputField('password', null, 'Repeat the same password', null, true);
		$passFieldRepeat.change(function(event) {
			_this._resetPasswordModel.passwordRepeat = $(this).val();
		});
		var $passwordGroupRepeat = FormUtil.getFieldForComponentWithLabel($passFieldRepeat, "Password Repeat", null);
		$window.append($passwordGroupRepeat);
		
		//
		// Buttons
		//
		var $btnAccept = $('<input>', { 'type': 'submit', 'class' : 'btn btn-primary', 'value' : 'Accept' });

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