var UiComponents = new function() {

    this.addLabelInInput = function($input, label)
    {
        var $inputGroup = $("<div>", { class : "input-group" });
        $inputGroup.append($("<span>", { class : "input-group-addon" }).text(label).css({ 'min-width' : '150px' }));
        $input.append($inputGroup);
    }

    this.getButton = function($html, action, size, icon) {
        var buttonSize = size ? size : 'md';
        var $button = $('<button>', {
            type : 'button',
            class : 'btn btn-default btn-' + buttonSize,
            'aria-label' : 'Left Align'
        });
        if(icon) {
            var $icon = $('<span>', { class : 'glyphicon ' + icon });
            $icon.css("margin-right", "0.5rem");
            $button.append($icon);
        }
        if($html) {
            $button.append($("<span>").text($html));
        }
        if(action) {
            $button.click(action);
        }
        return $button;
    }

    this.getFieldset = function(legendText) {
        var $fieldset = $('<fieldset>');
        if (legendText) {
            var $legend = $('<legend>', { class : 'section-legend' }).append(legendText)
            $fieldset.append($legend);
        }
        return $fieldset;
    }

    this.getLoader = function() {
        return $('<div>').attr('class', 'loader col-centered');
    }

    // blocks given $component or whole screen of none given
    this.startLoading = function($component) {
        var params = {
            message : this.getLoader(),
            css: {
                border: 'none',
                padding: '15px',
                backgroundColor: 'transparent',
            },
            overlayCSS: {
                opacity : 0.1,
            }
        };
        if ($component) {
            $component.block(params);
        } else {
            $.blockUI(params);
        }
    }

    // unblocks given $component or whole screen of none given
    this.stopLoading = function($component) {
        if ($component) {
            $component.unblock();
        } else {
            $.unblockUI();
        }
    }
}