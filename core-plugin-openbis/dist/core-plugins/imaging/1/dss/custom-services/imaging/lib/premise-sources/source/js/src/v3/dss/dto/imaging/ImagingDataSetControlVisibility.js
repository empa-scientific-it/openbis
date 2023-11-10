define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var ImagingDataSetControlVisibility = function() {
	};
	stjs.extend(ImagingDataSetControlVisibility, null, [], function(constructor, prototype) {
		prototype['@type'] = 'dss.dto.imaging.ImagingDataSetControlVisibility';
		constructor.serialVersionUID = 1;
		prototype.label = null;
		prototype.values = null;
		prototype.range = null;
		prototype.unit = null;

		prototype.getLabel = function() {
			return this.label;
		};
		prototype.setLabel = function(label) {
			this.label = label;
		};
        prototype.getValues = function() {
            return this.values;
        };
        prototype.setValues = function(values) {
            this.values = values;
        };
        prototype.getUnit = function() {
            return this.unit;
        };
        prototype.setUnit = function(unit) {
            this.unit = unit;
        };
        prototype.getRange = function() {
            return this.range;
        };
        prototype.setRange = function(range) {
            this.range = range;
        };
		prototype.toString = function() {
            return "ImagingDataSetControlVisibility: " + this.label;
        };

	}, {
		values : {
            name : "List",
            arguments : [ "String"]
        },
        range : {
            name : "List",
            arguments : [ "String"]
        }
	});
	return ImagingDataSetControlVisibility;
})