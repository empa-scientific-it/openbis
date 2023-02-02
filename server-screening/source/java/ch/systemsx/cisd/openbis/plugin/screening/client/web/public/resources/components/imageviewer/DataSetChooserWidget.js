define([ "jquery", "components/common/ChangeEvent", "components/imageviewer/AbstractWidget", "components/imageviewer/DataSetChooserView" ], function(
		$, ChangeEvent, AbstractWidget, DataSetChooserView) {

	//
	// DATA SET CHOOSER WIDGET
	//

	function DataSetChooserWidget(dataSetCodes) {
		this.init(dataSetCodes);
	}

	$.extend(DataSetChooserWidget.prototype, AbstractWidget.prototype, {
		init : function(dataSetCodes) {
			AbstractWidget.prototype.init.call(this, new DataSetChooserView(this));
			this.setDataSetCodes(dataSetCodes)
		},

		getDataSetCodes : function() {
			if (this.dataSetCodes) {
				return this.dataSetCodes;
			} else {
				return [];
			}
		},

		setDataSetCodes : function(dataSetCodes) {
			if (!dataSetCodes) {
				dataSetCodes = [];
			}
			if (this.getDataSetCodes().toString() != dataSetCodes.toString()) {
				this.dataSetCodes = dataSetCodes;
				if (dataSetCodes.length > 0) {
					this.setSelectedDataSetCode(dataSetCodes[0]);
				}
				this.refresh();
			}
		},

		getSelectedDataSetCode : function() {
			if (this.selectedDataSetCode != null) {
				return this.selectedDataSetCode;
			} else {
				return null;
			}
		},

		setSelectedDataSetCode : function(dataSetCode) {
			if (this.getSelectedDataSetCode() != dataSetCode) {
				var event = new ChangeEvent(this.getSelectedDataSetCode(), dataSetCode);
				this.selectedDataSetCode = dataSetCode;
				this.refresh();
				this.notifyChangeListeners(event);
			}
		}

	});

	return DataSetChooserWidget;

});