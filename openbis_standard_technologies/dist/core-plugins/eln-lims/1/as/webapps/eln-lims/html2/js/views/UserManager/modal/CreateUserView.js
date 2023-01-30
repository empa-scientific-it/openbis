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

function CreateUserView(createUserController, createUserModel) {
	this._createUserController = createUserController;
	this._createUserModel = createUserModel;
	this._warning = null;
	this._passField = null;
	this._passwordGroup = null;
	this._passFieldRepeat = null;
	this._passwordGroupRepeat = null;
	this._$btnAccept = $('<input>', { 'type': 'submit', 'class' : 'btn btn-primary', 'id' : 'createUserBtn', 'value' : 'Accept' });
	
	this.disableAccept = function() {
		this._$btnAccept.attr("disabled", "");
	}
	
	this.enableAccept = function() {
		this._$btnAccept.removeAttr("disabled");
	}
	
	this.showPasswordField = function() {
		this._passField.removeAttr('disabled');
		this._passwordGroup.show();
		this._passFieldRepeat.removeAttr('disabled');
		this._passwordGroupRepeat.show();
		this._warning.show();
	}

    this.hidePasswordField = function() {
        this._passField.attr('disabled','disabled');
        this._passwordGroup.hide();
        this._passFieldRepeat.attr('disabled','disabled');
        this._passwordGroupRepeat.hide();
    	this._warning.hide();
    }
	
	this.repaint = function() {
		var _this = this;
		var $window = $('<form>', {
			'action' : 'javascript:void(0);'
		});
		
		$window.submit(function() {
			_this._createUserController.createUser();
		});
		
		$window.append($('<legend>').append("Create User"));
		
		//
		// Warning
		//
		this._warning = $("<p>")
							.append($("<span>", { class: "glyphicon glyphicon-info-sign" }))
							.append(" File authentication requires a password to create a user, the user can change the password later at the profile.");
		this._warning.hide();
		$window.append(this._warning);

		//
		// Authentication Service
		//
		var authenticationServicesValues = [{ value : 'Default Authentication Service', label : 'Default Authentication Service', selected : true }];
		if(this._createUserModel.authenticationService.indexOf("file") !== -1) {
		    authenticationServicesValues.push({ value : 'File Authentication Service', label : 'File Authentication Service' });
		}
        var authServicesDropdown = FormUtil.getDropdown(authenticationServicesValues, 'Authentication Service');
		var $authenticationServices = FormUtil.getFieldForComponentWithLabel(authServicesDropdown, 'Authentication Service', null);
		$window.append($authenticationServices);

        $authenticationServices.change(function() {
            if(authServicesDropdown.val() === "File Authentication Service") {
                _this.showPasswordField();
                _this._createUserModel.isPasswordRequired = true;
            } else {
                _this.hidePasswordField();
                _this._createUserModel.isPasswordRequired = false;
            }
        });

		//
		// User ID
		//
		var $userIdField = FormUtil._getInputField('text', 'userId', 'User ID', null, true);
		$userIdField.change(function(event) {
		    $(this).val($(this).val().toLowerCase());
			_this._createUserModel.userId = $(this).val();
		});
		var $userIdGroup = FormUtil.getFieldForComponentWithLabel($userIdField, "User ID", null);
		$window.append($userIdGroup);
		
		//
		// Password
		//
		var $passField = FormUtil._getInputField('password', 'passwordId', 'Password', null, true);
		$passField.change(function(event) {
			_this._createUserModel.password = $(this).val();
		});
		$passField.prop('disabled', true);
		var $passwordGroup = FormUtil.getFieldForComponentWithLabel($passField, "Password", null);
		$passwordGroup.hide();
		$window.append($passwordGroup);
		this._passField = $passField;
		this._passwordGroup = $passwordGroup;
		
		//
		// Password Repeat
		//
		var $passFieldRepeat = FormUtil._getInputField('password', 'passwordRepeatId', 'Repeat the same password', null, true);
		$passFieldRepeat.change(function(event) {
			_this._createUserModel.passwordRepeat = $(this).val();
		});
		$passFieldRepeat.prop('disabled', true);
		var $passwordGroupRepeat = FormUtil.getFieldForComponentWithLabel($passFieldRepeat, "Password Repeat", null);
		$passwordGroupRepeat.hide();
		$window.append($passwordGroupRepeat);
		this._passFieldRepeat = $passFieldRepeat;
		this._passwordGroupRepeat = $passwordGroupRepeat;
		
		//
		// Buttons
		//
		var $btnCancel = $('<a>', { 'class' : 'btn btn-default', 'id' : 'cancelBtn' }).append('Cancel');
		$btnCancel.click(function() {
			Util.unblockUI();
		});
		
		$window.append(this._$btnAccept).append('&nbsp;').append($btnCancel);
		
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