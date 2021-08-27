/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

function UserProfileView(userProfileController, userProfileModel) {
	this._userProfileController = userProfileController;
	this._userProfileModel = userProfileModel;

    this._$firstNameInput = null;
    this._$lastNameInput = null;
    this._$emailInput = null;
    this._$zenodoToken = null;

	this.repaint = function(views) {

        // header
        var $header = views.header;
        var typeTitle = "User Profile";
        var $formTitle = $("<h2>").append(typeTitle);
        $header.append($formTitle);

        var toolbarModel = [];		
        if(this._userProfileModel.mode === FormMode.VIEW) {
            //Edit
            var $editButton = FormUtil.getButtonWithIcon("glyphicon-edit", function () {
                mainController.changeView("showEditUserProfilePage");
            }, "Edit");
            toolbarModel.push({ component : $editButton });
        } else { //Create and Edit
            //Save
            var $saveBtn = FormUtil.getButtonWithIcon("glyphicon-floppy-disk", (function() {
                this._userProfileController.save(this._getUserInformation());
            }).bind(this), "Save");
            $saveBtn.removeClass("btn-default");
            $saveBtn.addClass("btn-primary");
            toolbarModel.push({ component : $saveBtn });
        }
		// ToolBox
        var $options = this._getOptionsMenu();
        toolbarModel.push({ component : $options, tooltip: null });
        $header.append(FormUtil.getToolbar(toolbarModel));

        // formColumn for content
		var $container = views.content;
        var $form = $("<div>");
        var $formColumn = $("<div>");
        $form.append($formColumn);
        $container.append($form);

        this._userProfileController.getUserInformation((function(getUserInformation) {
            // first name
            this._$firstNameInput = $("<input>", { type : "text", class : "form-control" });
            this._$firstNameInput.val(getUserInformation.firstName);
            $formColumn.append(this._getFormGroup(this._$firstNameInput, "First Name:"));
            // last name
            this._$lastNameInput = $("<input>", { type : "text", class : "form-control" });
            this._$lastNameInput.val(getUserInformation.lastName);
            $formColumn.append(this._getFormGroup(this._$lastNameInput, "Last Name:"));
            // user email
            this._$emailInput = $("<input>", { type : "text", class : "form-control" });
            this._$emailInput.val(getUserInformation.email);
            $formColumn.append(this._getFormGroup(this._$emailInput, "Email:"));
            // session token
            this._$sessionTokenInput = $("<p>", {'class' : 'form-control-static', 'style' : 'border:none; box-shadow:none; background:transparent; word-wrap: break-word;'});
            this._$sessionTokenInput.text(mainController.serverFacade.openbisServer.getSession());
            $formColumn.append(this._getFormGroup(this._$sessionTokenInput, "openBIS session token:"));
            // personal Zenodo API token
            this._$zenodoToken = $("<input>", { type : "text", class : "form-control" });

            this._userProfileController.getSettingValue(this._userProfileController._zenodoApiTokenKey, (function (settingsValue) {
                if (settingsValue) {
                    this._$zenodoToken.val(settingsValue.trim());
                }
            }).bind(this));
            $formColumn.append(this._getFormGroup(this._$zenodoToken, "Zenodo API Token:"));

            // disable in view mode
            if (this._userProfileModel.mode === FormMode.VIEW ||
                    !this._userProfileController.isFileAuthentication()) {
                this._$firstNameInput.prop("disabled", true);
                this._$lastNameInput.prop("disabled", true);
                this._$emailInput.prop("disabled", true);
            }

            if (this._userProfileModel.mode === FormMode.VIEW) {
                this._$zenodoToken.prop("disabled", true);
            }
        }).bind(this));
    }

	this._getOptionsMenu = function() {
        var items = [];
        if(this._userProfileController.isFileAuthentication()) {
            items.push({
                label : "Change Password",
                event : this._userProfileController.resetPassword.bind(this._userProfileController),
            })
        }

        return FormUtil.getOperationsMenu(items);
	}

    this._getUserInformation = function() {
        return {
            firstName : this._$firstNameInput.val(),
            lastName : this._$lastNameInput.val(),
            email : this._$emailInput.val(),
            zenodoToken : this._$zenodoToken.val()
        };
    }

	this._getFormGroup = function($input, labelText) {
		var $formGroup = $("<div>", { class : "form-group" });
		$formGroup.append($("<label>", { class : "control-label" }).text(labelText));
		var $controls = $("<div>", { class : "controls" });
		$formGroup.append($controls);
		$controls.append($input);
		return $formGroup;
	}
}