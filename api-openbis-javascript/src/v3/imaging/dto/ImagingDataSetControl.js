define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var ImagingDataSetControl = function() {
	};
	stjs.extend(ImagingDataSetControl, null, [], function(constructor, prototype) {
		prototype['@type'] = 'imaging.dto.ImagingDataSetControl';
		constructor.serialVersionUID = 1;
		prototype.label = null;
		prototype.section = null;
		prototype.type = null;
		prototype.values = null;
		prototype.unit = null;
		prototype.range = null;
		prototype.multiselect = null;
		prototype.playable = null;
		prototype.speeds = null;
		prototype.visibility = null;
		prototype.metadata = null;

		prototype.getLabel = function() {
			return this.label;
		};
		prototype.setLabel = function(label) {
			this.label = label;
		};
		prototype.getSection = function() {
            return this.section;
        };
        prototype.setSection = function(section) {
            this.section = section;
        };
		prototype.getType = function() {
            return this.type;
        };
        prototype.setType = function(type) {
            this.type = type;
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
        prototype.getMultiselect = function() {
            return this.multiselect;
        };
        prototype.setMultiselect = function(multiselect) {
            this.multiselect = multiselect;
        };
        prototype.getPlayable = function() {
            return this.playable;
        };
        prototype.setPlayable = function(playable) {
            this.playable = playable;
        };
        prototype.getSpeeds = function() {
            return this.speeds;
        };
        prototype.setSpeeds = function(speeds) {
            this.speeds = speeds;
        };
        prototype.getVisibility = function() {
            return this.visibility;
        };
        prototype.setVisibility = function(visibility) {
            this.visibility = visibility;
        };
		prototype.getMetadata = function() {
			return this.metadata;
		};
		prototype.setMetadata = function(metadata) {
			this.metadata = metadata;
		};
		prototype.toString = function() {
            return "ImagingDataSetControl: " + this.label;
        };

	}, {
		values : {
            name : "List",
            arguments : [ "String"]
        },
        visibility: {
            name : "List",
            arguments : [ "ImagingDataSetControlVisibility"]
        },
        range : {
            name : "List",
            arguments : [ "String"]
        },
        speeds : {
            name : "List",
            arguments : [ "Integer"]
        },
        metadata : {
            name : "Map",
            arguments : [ "String", "String" ]
        }
	});
	return ImagingDataSetControl;
})