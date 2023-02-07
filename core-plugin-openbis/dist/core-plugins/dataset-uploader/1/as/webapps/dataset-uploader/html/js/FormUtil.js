var FormUtil = new function() {
	this.profile = null;

	//
	// Form css classes
	//
	this.shortformColumClass = 'col-md-9'
	this.formColumClass = 'col-md-12'
	this.labelColumnClass = 'col-md-2';
	this.shortControlColumnClass = 'col-md-5';
	this.controlColumnClass = 'col-md-6';
	this.controlColumnClassBig = 'col-md-10';

	//
	// Standard Form Fields
	//

	this.getDataSetsDropDown = function(code, dataSetTypes) {
		var $component = $("<select>", { class : 'form-control ' });
		$component.attr('id', code);

		$component.attr('required', '');

		$component.append($("<option>").attr('value', '').attr('selected', '').text(''));

		for(var i = 0; i < dataSetTypes.length; i++) {
			var displayName = dataSetTypes[i].code;
			if(dataSetTypes[i].description) {
				displayName += " (" + dataSetTypes[i].description + ")";
			}
			$component.append($("<option>").attr('value',dataSetTypes[i].code).text(displayName));
		}

		return $component;
	}

	//
	// Get Field with container to obtain a correct layout
	//
	this.getFieldForComponentWithLabel = function($component, label, postComponent) {
		var $fieldset = $('<div>');

		var $controlGroup = $('<div>', {class : 'form-group'});
		var requiredText = '';
		if($component.attr('required')) {
			requiredText = "&nbsp;(*)"
		}

		var labelText = "";
		if(label) {
			labelText = label + requiredText + ":";
		}

		var $controlLabel = $('<label>', { class : 'control-label ' + this.labelColumnClass }).html(labelText);
		var $controls = $('<div>', { class : 'controls ' + this.controlColumnClass });

		$controlGroup.append($controlLabel);
		$controlGroup.append($controls);
		if(postComponent) {
			$controlGroup.append(postComponent);
		}
		$fieldset.append($controlGroup);

		$controls.append($component);


		return $fieldset;
	}

	this.getFieldForTextWithLabel = function(text, label) {
		var $fieldset = $('<div>');

		var $controlGroup = $('<div>', {class : 'form-group'});

		var $controlLabel = $('<label>', {class : 'control-label ' + this.labelColumnClass});
		if(label) {
			$controlLabel.text(label + ":");
		}

		var $controls = $('<div>', {class : 'controls ' + this.controlColumnClass });

		$controlGroup.append($controlLabel);
		$controlGroup.append($controls);
		$fieldset.append($controlGroup);

		var $component = $("<p>", {'class' : 'form-control-static', 'style' : 'border:none; box-shadow:none; background:transparent; word-wrap: break-word; white-space: pre-line;'});
		$component.text(text);
		$controls.append($component);

		return $fieldset;
	}

	//
	// Get Field from property
	//

	this.getFieldForPropertyType = function(propertyType) {
		var $component = null;
		if (propertyType.dataType === "BOOLEAN") {
			$component = getBooleanField(propertyType.code, propertyType.description);
		} else if (propertyType.dataType === "CONTROLLEDVOCABULARY") {
			$component = getDropDownFieldForVocabulary(propertyType.code, propertyType.vocabulary.terms, propertyType.description, propertyType.mandatory);
		} else if (propertyType.dataType === "HYPERLINK") {
			$component = getInputField("url", propertyType.code, propertyType.description, null, propertyType.mandatory);
		} else if (propertyType.dataType === "INTEGER") {
			$component = getInputField("number", propertyType.code, propertyType.description, '1', propertyType.mandatory);
		} else if (propertyType.dataType === "MATERIAL") {
			$component = getInputField("text", propertyType.code, propertyType.description, null, propertyType.mandatory);
		} else if (propertyType.dataType === "MULTILINE_VARCHAR") {
			$component = getTextBox(propertyType.code, propertyType.description, propertyType.mandatory);
		} else if (propertyType.dataType === "REAL") {
			$component = getInputField("number", propertyType.code, propertyType.description, 'any', propertyType.mandatory);
		} else if (propertyType.dataType === "TIMESTAMP") {
			$component = getDatePickerField(propertyType.code, propertyType.description, propertyType.mandatory);
		} else if (propertyType.dataType === "VARCHAR") {
			$component = getInputField("text", propertyType.code, propertyType.description, null, propertyType.mandatory);
		} else if (propertyType.dataType === "XML") {
			$component = getTextBox(propertyType.code, propertyType.description, propertyType.mandatory);
		}

		return $component;
	}

	//
	// Form Fields
	//
	var getBooleanField = function(id, alt) {
		return $('<div>', {'class' : 'checkbox'}).append($('<input>', {'type' : 'checkbox', 'id' : id, 'alt' : alt, 'placeholder' : alt }));
	}

	var getDropDownFieldForVocabulary = function(code, terms, alt, isRequired) {
		var $component = $("<select>", {'placeholder' : alt, 'class' : 'form-control'});
		$component.attr('id', code);

		if (isRequired) {
			$component.attr('required', '');
		}

		$component.append($("<option>").attr('value', '').attr('selected', '').text(''));
		for(var i = 0; i < terms.length; i++) {
			$component.append($("<option>").attr('value',terms[i].code).text(terms[i].label));
		}

		return $component;
	}

	var getInputField = function(type, id, alt, step, isRequired) {
		var $component = $('<input>', {'type' : type, 'id' : id, 'alt' : alt, 'placeholder' : alt, 'class' : 'form-control'});
		if (isRequired) {
			$component.attr('required', '');
		}
		if (step) {
			$component.attr('step', step);
		}
		return $component;
	}

	var getTextBox = function(id, alt, isRequired) {
		var $component = $('<textarea>', {'id' : id, 'alt' : alt, 'style' : 'height: 80px; width: 450px;', 'placeholder' : alt, 'class' : 'form-control'});
		if (isRequired) {
			$component.attr('required', '');
		}
		return $component;
	}

	var getDatePickerField = function(id, alt, isRequired) {
		var $component = $('<div>', {'class' : 'form-group', 'style' : 'margin-left: 0px;', 'placeholder' : alt });
		var $subComponent = $('<div>', {'class' : 'input-group date', 'id' : 'datetimepicker_' + id });
		var $input = $('<input>', {'class' : 'form-control', 'type' : 'text', 'id' : id, 'data-format' : 'yyyy-MM-dd HH:mm:ss'});
		if (isRequired) {
			$input.attr('required', '');
		}
		var $spanAddOn = $('<span>', {'class' : 'input-group-addon'})
							.append($('<span>', {'class' : 'glyphicon glyphicon-calendar' }));

		$subComponent.append($input);
		$subComponent.append($spanAddOn);
		$subComponent.datetimepicker({ language: 'en' });
		$component.append($subComponent);

		return $component;
	}
}