define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var ImagingDataSetControl = function() {
	};
	stjs.extend(ImagingDataSetControl, null, [], function(constructor, prototype) {
		prototype['@type'] = 'dss.dto.imaging.ImagingDataSetControl';
		constructor.serialVersionUID = 1;
		prototype.label = null;
		prototype.type = null;
		prototype.values = null;
		prototype.range = null;
		prototype.multiselect = null;
		prototype.playable = null;
		prototype.speeds = null;
		prototype.metaData = null;

		prototype.getLabel = function() {
			return this.label;
		};
		prototype.setLabel = function(label) {
			this.label = label;
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
		prototype.getMetaData = function() {
			return this.metaData;
		};
		prototype.setMetaData = function(metaData) {
			this.metaData = metaData;
		};

	}, {
		values : {
            name : "List",
            arguments : [ "String"]
        },
        range : {
            name : "List",
            arguments : [ "Integer"]
        },
        speeds : {
            name : "List",
            arguments : [ "Integer"]
        },
        metaData : {
            name : "Map",
            arguments : [ "String", "String" ]
        }
	});
	return ImagingDataSetControl;
})