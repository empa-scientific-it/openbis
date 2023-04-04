var BarcodeUtil = new function() {
    var barcodeTimeout = false;
    var barcodeReader = "";
    var _this = this;

    var readSample = function(action) {
        // Trigger search if needed
        // permID Format 23 char, 1 hyphen: 20170912112249208-38888

        if (_this.isValidBarcode(barcodeReader)) {
            var rules = {};
            rules["UUIDv4-1"] = { type: "Property/Attribute", 	name: "PROP.$BARCODE", operator : "thatEqualsString", value: barcodeReader };
            rules["UUIDv4-2"] = { type: "Property/Attribute", 	name: "ATTR.PERM_ID",  operator : "thatEqualsString", value: barcodeReader };

            var criteria = {};
            criteria.entityKind = "SAMPLE";
            criteria.logicalOperator = "OR";
            criteria.rules = rules;

            mainController.serverFacade.searchForSamplesAdvanced(criteria, { only : true, withProperties: true },
            function(results) {
                if(results.totalCount === 1) {
                    if(action) {
                        action(results.objects[0]);
                    } else {
                        mainController.changeView('showViewSamplePageFromPermId', results.objects[0].permId.permId);
                    }
                }
            });
        }
    }

    var codeReader = null;

    this.readBarcodeFromCamera = function() {
        if(codeReader != null) {
            codeReader.reset();
            codeReader = null;
        }

        // Steals the main controller to show the video feed and a cancel button
        mainController.changeView("showBlancPage");
        var content = mainController.currentView.content;
        content.empty();
        content.append(FormUtil.getButtonWithIcon("glyphicon-minus-sign", function() {
            if(codeReader != null) {
                codeReader.reset();
                codeReader = null;
            }
            // On cancel go back to previews view
            mainController.backButtonLogic();
            mainController.backButtonLogic();
        }));
        var $videoCameraSelection = $("<select>", { id: "videoCameraSelect", style : "margin-left: 4px;"});
        content.append($videoCameraSelection);
        content.append($("<legend>").text("Read Barcode:"));
        var $video = $("<video>", { id : "video", width : "100%", height : "100%" });
        content.append($video);

        // Starts the camera reading code
        codeReader = new ZXing.BrowserMultiFormatReader();
                codeReader.listVideoInputDevices().then((videoInputDevices) => {
                    const sourceSelect = document.getElementById('sourceSelect');
                    selectedDeviceId = videoInputDevices[0].deviceId;

                    var decodeFromVideoDeviceCallback = (result, err) => {
                        if(result && result.text) {
                            codeReader.reset();
                            BarcodeUtil.readSample(result.text);
                        }
                        if (err && !(err instanceof ZXing.NotFoundException)) {
                            Util.showError("Failed to read barcode");
                            // On cancel go back to previews view
                            mainController.backButtonLogic();
                            mainController.backButtonLogic();
                        }
                    };

                    if(videoInputDevices.length > 1) {
                        videoInputDevices.forEach((element) => {
                            var option = $("<option>", { value: element.deviceId }).text(element.label);
                            $videoCameraSelection.append(option);
                        });

                        $videoCameraSelection.change(function(event) {
                            //Stop
                            codeReader.reset();
                            //Start
                            selectedDeviceId = $(this).val();
                            codeReader.decodeFromVideoDevice(selectedDeviceId, 'video', decodeFromVideoDeviceCallback);
                        });
                    }

                    codeReader.decodeFromVideoDevice(selectedDeviceId, 'video', decodeFromVideoDeviceCallback);
        });
    }

    this.readBarcodeFromFile = function() {
                var $input = $("<input>", { type : "file", accept : "image/*" });
                $input.click();
                $input.change(function(event) {
//                            const hints = new Map();
//                            const formats = [ZXing.BarcodeFormat.QR_CODE /*, ...*/];
//                            hints.set(ZXing.DecodeHintType.POSSIBLE_FORMATS, formats);
//                            const codeReader = new ZXing.BrowserMultiFormatReader(hints);
                            const codeReader = new ZXing.BrowserMultiFormatReader();
                            const fileReader = new FileReader();
                            fileReader.readAsArrayBuffer(event.target.files[0]);
                            fileReader.onloadend = (evt) => {
                                if (evt.target.readyState === FileReader.DONE) {
                                    var img = null;
                                        img = Images.decodeArrayBuffer(evt.target.result, function(event) {
                                            img.videoWidth = 0; // Bugfix so ZXing decodes the image instead throwing an exception
                                            var result = codeReader.decode(img);
                                            if(result && result.text) {
                                                BarcodeUtil.readSample(result.text);
                                            } else {
                                                Util.showError("Failed to read barcode");
                                            }
                                        });
                                }
                            }
                })
    };

    this.readSample = function(barcodeReaderInput) {
        barcodeReader = barcodeReaderInput;
        readSample();
    }

    var barcodeReaderEventListener = function(action) {
        return function(event) {
            if(!barcodeTimeout) {
                  barcodeTimeout = true;
                  var timeoutFunc = function() {
                      readSample(action);
                      // reset
                      barcodeTimeout = false;
                      barcodeReader = "";
                  }
                  setTimeout(timeoutFunc, 1000);
            }
            if(event.key === "Clear") {
                barcodeReader = "";
            } else if(event.key === "Enter") {
                // Ignore the enter character
            } else {
                barcodeReader += event.key;
            }
        };
    }

    var barcodeReaderGlobalEventListener = barcodeReaderEventListener();

    this.enableAutomaticBarcodeReading = function() {
        if(profile.mainMenu.showBarcodes) {
            document.addEventListener('keyup', barcodeReaderGlobalEventListener);
        }
    }

    this.disableAutomaticBarcodeReading = function() {
        if(profile.mainMenu.showBarcodes) {
            document.removeEventListener('keyup', barcodeReaderGlobalEventListener);
        }
    }

    this.getMinBarcodeLength = function() {
        return profile.minBarcodeLength ? profile.minBarcodeLength : 15;
    }

    this.getBarcodePattern = function() {
        return profile.barcodePattern ? profile.barcodePattern : /^[-0-9]+$/;
    }

    this.isValidBarcode = function(barcode) {
        return barcode.length >= this.getMinBarcodeLength() && this.getBarcodePattern().test(barcode)
    }

    this.preGenerateBarcodes = function(views, selectedBarcodes) {
        views.header.append($("<h2>").append("Barcode Generator"));

        var generateBarcodeText = null;
        if(selectedBarcodes === undefined) {
            generateBarcodeText = "Generate Custom Barcodes";
        } else {
            generateBarcodeText = "Update Custom Barcodes";
        }

	    var $generateBtn = FormUtil.getButtonWithText(generateBarcodeText, function() {}, "btn-primary");
        $generateBtn.css("margin-bottom", "14px");

        var $toolbar = $("<span>");

        var $barcodeTypesDropdown = FormUtil.getDropdown(this.supportedBarcodes());

        var numberDropdownModel = [];
        for(var nIdx = 1; nIdx <= 100; nIdx++) {
            numberDropdownModel.push({ label: '' + nIdx, value: nIdx });
            if(nIdx === 10) {
                numberDropdownModel[nIdx-1].selected = true;
            }
        }
        var $numberDropdown = FormUtil.getDropdown(numberDropdownModel);

        var $width = FormUtil.getDropdown([ { label: '10 mm', value: 10 },
                                            { label: '15 mm', value: 15 },
                                            { label: '20 mm', value: 20 },
                                            { label: '25 mm', value: 25 },
                                            { label: '30 mm', value: 30 },
                                            { label: '35 mm', value: 35 },
                                            { label: '40 mm', value: 40 },
                                            { label: '45 mm', value: 45 },
                                            { label: '50 mm', value: 50, selected: true }
        ]);

        var $height = FormUtil.getDropdown([ { label: ' 5 mm', value:  5 },
                                             { label: '10 mm', value: 10 },
                                             { label: '15 mm', value: 15, selected: true },
                                             { label: '20 mm', value: 20 },
                                             { label: '25 mm', value: 25 },
                                             { label: '30 mm', value: 30 },
                                             { label: '35 mm', value: 35 },
                                             { label: '40 mm', value: 40 },
                                             { label: '45 mm', value: 45 },
                                             { label: '50 mm', value: 50 }
        ]);

        var $layout = FormUtil.getDropdown([
                    { label: 'Split Layout',        value: 'split',         selected: true},
                    { label: 'Continuous Layout',   value: 'continuous' }
                ]);

        var $layoutForPrinter = null;

        var pdf = null;
		var $printButton = $("<a>", { 'class' : 'btn btn-default', style : 'margin-bottom:13px;' } ).append($('<span>', { 'class' : 'glyphicon glyphicon-print' }));
        $printButton.click(function() {
            if(pdf !== null) {
                pdf.save("barcodes.pdf");
            }
        });

        var $lineHeaders = $("<div>");
        $lineHeaders.append($("<span>", { style:"width:15%; margin-left: 10px; display:inline-block;"}).append($("<label>", { class : "control-label"}).append("Type:")))
                .append($("<span>", { style:"width:10%; margin-left: 10px; display:inline-block;"}).append($("<label>", { class : "control-label"}).append("Count:")))
                .append($("<span>", { style:"width:15%; margin-left: 10px; display:inline-block;"}).append($("<label>", { class : "control-label"}).append("Layout:")))
                .append($("<span>", { style:"width:15%; margin-left: 10px; display:inline-block;"}).append($("<label>", { class : "control-label"}).append("Width:")))
                .append($("<span>", { style:"width:15%; margin-left: 10px; display:inline-block;"}).append($("<label>", { class : "control-label"}).append("Height:")));
        $toolbar.append($lineHeaders);

        var $lineOne = $("<div>");
        $lineOne.append($("<span>", { style:"width:15%; margin-left: 10px; display:inline-block;"}).append($barcodeTypesDropdown));
        if(selectedBarcodes === undefined) {
            $lineOne.append($("<span>", { style:"width:10%; margin-left: 10px; display:inline-block;"}).append($numberDropdown));
        }
        $lineOne.append($("<span>", { style:"width:15%; margin-left: 10px; display:inline-block;"}).append($layout));
        $lineOne.append($("<span>", { style:"width:15%; margin-left: 10px; display:inline-block;"}).append($width));
        $lineOne.append($("<span>", { style:"width:15%; margin-left: 10px; display:inline-block;"}).append($height));
        $lineOne.append($("<span>", { style:"margin-left: 10px; display:inline-block;"}).append($generateBtn));
        $lineOne.append($("<span>", { style:"margin-left: 10px; display:inline-block;"}).append($printButton));
        $toolbar.append($lineOne);

        views.header.append($toolbar);

        var _this = this;
        var addBarcodes = function(barcodes) {
            var format = null;
            var width  = $width.val();
            width = parseInt(width);
            var height = $height.val();
            height = parseInt(height);
            var layout = $layout.val();

            views.content.empty();
            $layoutForPrinter = $('<div>', { 'id' : 'layout-for-printer' });
            views.content.append($layoutForPrinter);

            if(width && height) {
                format = {
                    orientation: ((layout === 'split')?'l':'p'),
                    unit: 'mm',
                    format: [width, height * ((layout === 'split')?1:barcodes.length) + ((layout === 'split')?0:2*barcodes.length)],
                    putOnlyUsedFonts:true
                };

                pdf = new jsPDF(format);
            }

            for(var idx = 0; idx < barcodes.length; idx++) {
                // HTML
                _this.addBarcode($layoutForPrinter, idx, $barcodeTypesDropdown.val(), barcodes[idx], idx === 0, width, height, layout);

                // PDF
                var imgData = _this.generateBarcode($barcodeTypesDropdown.val(), barcodes[idx], barcodes[idx], null, width, height);
                if(pdf !== null) {
                    if(layout === 'split') {
                        if(idx > 0) {
                            pdf.addPage(format.format, 'l');
                        }
                        pdf.addImage(imgData, 'png', 0, 0, width, height);
                     } else {
                        pdf.addImage(imgData, 'png', 0, (height * idx + 2*idx), width, height);
                    }
                }
            }
        }

        if(selectedBarcodes === undefined) {
            $generateBtn.click(function() {
                var value = parseInt($numberDropdown.val());
                mainController.serverFacade.createPermIdStrings(value, function(newPermIds) {
                    addBarcodes(newPermIds);
                });
            });
            this.preloadLibrary();
        } else {
            $generateBtn.click(function() {
                addBarcodes(selectedBarcodes);
            });
            this.preloadLibrary(function() {
                $generateBtn.click();
            });
        }
    }

    this.preloadLibrary = function(doAfter) {
        if(doAfter === undefined) {
            doAfter = function() {};
        }
        this.generateBarcode("code128", "Barcode", "Text", doAfter);
    }

    this.addBarcode = function(content, idx, type, text, isFirst, width, height, layout) {
        if(!isFirst) {
            var $br = null;
            if(layout && layout === 'split') {
                $br = $('<hr>', { style : 'page-break-after: always;'});
            } else {
                $br = $('<br>');
            }
            content.append($br);
        }
        var imageSRC = this.generateBarcode(type, text, text, null, width, height);
        var imagePNG = $('<img>', { src : imageSRC });
        content.append(imagePNG);
        if(width && height) {
            imagePNG.css('width', width + 'mm');
            imagePNG.css('height', height + 'mm');
        }
    }

    this.preventFormSubmit = function(e) {
        var keyCode = e.keyCode || e.which;
        if (keyCode === 13) {
            e.preventDefault();
            return false;
        }
    }

    this.readBarcodeMulti = function(actionLabel, action) {
        var _this = this;
        var $readed = $('<div>');

        // Remove global event
        this.disableAutomaticBarcodeReading();
        // Add local event
        var objects = [];
        var gatherReaded = function(object) {
            objects.push(object);
            var displayName = "";
            var $container = $('<div>');
            var $identifier = $('<span>').append(object.identifier.identifier);
            var $removeBtn = FormUtil.getButtonWithIcon("glyphicon-remove", function() {
                $container.remove();
                for(var oIdx = 0; oIdx < objects.length; oIdx++) {
                    if(objects[oIdx].identifier.identifier === object.identifier.identifier) {
                        objects.splice(oIdx, 1);
                    }
                }
            });
            $readed.append($container.append($identifier).append($removeBtn));
        }

        var barcodeReaderLocalEventListener = barcodeReaderEventListener(gatherReaded);
        document.addEventListener('keyup', barcodeReaderLocalEventListener);

        var $window = $('<form>', {
            'action' : 'javascript:void(0);'
        });
        $window.on('keyup keypress', this.preventFormSubmit);

        var $btnAccept = $('<input>', { 'type': 'submit', 'class' : 'btn btn-primary', 'value' : actionLabel });
        $btnAccept.on('keyup keypress', this.preventFormSubmit);
        $btnAccept.click(function(event) {
            // Swap event listeners
            document.removeEventListener('keyup', barcodeReaderLocalEventListener);
            _this.enableAutomaticBarcodeReading();
            Util.unblockUI();
            action(objects);
        });

        var $btnCancel = $('<input>', { 'type': 'submit', 'class' : 'btn', 'value' : 'Close' });
        $btnCancel.on('keyup keypress', this.preventFormSubmit);
        $btnCancel.click(function(event) {
            // Swap event listeners
            document.removeEventListener('keyup', barcodeReaderLocalEventListener);
            _this.enableAutomaticBarcodeReading();
            Util.unblockUI();
        });

        $window.append($('<legend>').append("Barcode Reader"));
        $window.append($('<br>'));
        $window.append($btnAccept).append('&nbsp;').append($btnCancel);
        $window.append($('<legend>').append('Read'));
        $window.append($('<br>'));
        $window.append($('<div>').append($readed));

        var css = {
            'text-align' : 'left',
            'top' : '15%',
            'width' : '70%',
            'height' : '400px',
            'left' : '15%',
            'right' : '20%',
            'overflow' : 'auto'
        };

        Util.blockUI($window, css);
    }

    this.readBarcode = function(entities) {
        var _this = this;
        var $window = $('<form>', {
            'action' : 'javascript:void(0);'
        });
        $window.on('keyup keypress', this.preventFormSubmit);

        var $btnAccept = $('<input>', { 'type': 'submit', 'class' : 'btn btn-primary', 'value' : 'Save Barcode' });
        $btnAccept.on('keyup keypress', this.preventFormSubmit);
        $btnAccept.prop("disabled",false);

        var $barcodeReaders = [];
        for(var eIdx = 0; eIdx < entities.length; eIdx++) {
            var $barcodeReader = $('<input>', { 'type': 'text', 'placeholder': 'barcode', 'style' : 'min-width: 50%;' });
            $barcodeReaders.push($barcodeReader);
            if (entities[eIdx].properties && entities[eIdx].properties["$BARCODE"]) {
                $barcodeReader.val(entities[eIdx].properties["$BARCODE"]);
                $barcodeReader.select();
            }
        }

        $btnAccept.click(function(event) {
            var errors = [];
            for(var eIdx = 0; eIdx < entities.length; eIdx++) {
                var barcode = $barcodeReaders[eIdx].val();
                if (barcode.length === 0 || _this.isValidBarcode(barcode)) {
                   // OK
                } else {
                    errors.push(entities[eIdx]);
                }
            }
            if(errors.length > 0) {
                Util.showUserError("Invalid Barcode found", function() {}, true);
                return;
            }

            Util.blockUINoMessage();

            var updateBarcode = function() {
                require([ "as/dto/sample/update/SampleUpdate", "as/dto/sample/id/SamplePermId" ],
                    function(SampleUpdate, SamplePermId) {

                        var sampleUpdates = [];
                        for(var eIdx = 0; eIdx < entities.length; eIdx++) {
                            var sampleUpdate = new SampleUpdate();
                            sampleUpdate.setSampleId(new SamplePermId(entities[eIdx].permId));
                            sampleUpdate.setProperty("$BARCODE", $barcodeReaders[eIdx].val());
                            sampleUpdates.push(sampleUpdate);
                        }

                        mainController.openbisV3.updateSamples(sampleUpdates).done(function(result) {
                            Util.unblockUI();
                            var message = null;
                            if(sampleUpdates.length === 1) {
                                message = "Custom Barcode Updated";
                            } else {
                                message = sampleUpdates.length + " Custom Barcodes Updated";
                            }

                            Util.showInfo(message, function() {
                                mainController.refreshView();
                            }, true);
                        }).fail(function(result) {
                            Util.showFailedServerCallError(result);
                        });
                });
            }

            if($barcodeReader.val().length === 0) {
                updateBarcode();
            } else {
                var criteria = {
			        entityKind : "SAMPLE",
				    logicalOperator : "OR",
				    rules : {
				        "UUIDv4-1": { type: "Property/Attribute", 	name: "PROP.$BARCODE", operator : "thatEqualsString", value: $barcodeReader.val() }
				    }
			    };
                mainController.serverFacade.searchForSamplesAdvanced(criteria, {
                only : true,
                withProperties : true
                }, function(results) {
                    if(results.objects.length === 0) {
                        updateBarcode();
                    } else {
                        Util.showError("Custom Barcode already in use by " +  results.objects[0].identifier.identifier + " : It will not be assigned.");
                    }
                });
            }
        });

        var $btnCancel = $('<input>', { 'type': 'submit', 'class' : 'btn', 'value' : 'Close' });
        $btnCancel.on('keyup keypress', this.preventFormSubmit);
        $btnCancel.click(function(event) {
            Util.unblockUI();
        });

        $window.append($('<legend>').append("Update Custom Barcode"));
        $window.append($('<br>'));
        $window.append(FormUtil.getInfoText("A valid barcode need to have " + this.getMinBarcodeLength() + " or more characters. Only characters in the pattern " + this.getBarcodePattern() + " are allowed."));
        $window.append(FormUtil.getInfoText("If a custom barcode is not given the permId is always used as default barcode."));
        $window.append(FormUtil.getWarningText("Empty the custom barcode to delete the current custom barcode."));

        $window.append($('<br>'));
        for(var eIdx = 0; eIdx < entities.length; eIdx++) {
            var $barcodeBlock = $("<div>");
            $barcodeBlock.append($('<label>', { class : 'control-label' }).text(Util.getDisplayNameForEntity(entities[eIdx]) + ":"));
            $barcodeBlock.append($('<br>'));
            $barcodeBlock.append($barcodeReaders[eIdx]);
            $barcodeBlock.append($('<br>'));
            $window.append($barcodeBlock);
        }
        $window.append($('<br>'));
        if(entities.length > 0) {
            $window.append($btnAccept).append('&nbsp;');
        }
        $window.append($btnCancel);

        var css = {
            'text-align' : 'left',
            'top' : '15%',
            'width' : '70%',
            'height' : '400px',
            'left' : '15%',
            'right' : '20%',
            'overflow' : 'auto'
        };

        Util.blockUI($window, css);
    }

    this.showBarcode = function(entity) {
        var _this = this;
        var barcode = null;
        if(entity.properties && entity.properties["$BARCODE"]) {
            barcode = entity.properties["$BARCODE"];
        } else {
            barcode = entity.permId;
        }

        var $window = $('<form>', {
            'action' : 'javascript:void(0);'
        });

        var $canvas = $('<img>');

        var $barcodeTypesDropdown = FormUtil.getDropdown(this.supportedBarcodes());

        var $width = FormUtil.getDropdown([ { label: '10 mm', value: 10 },
                                            { label: '15 mm', value: 15 },
                                            { label: '20 mm', value: 20 },
                                            { label: '25 mm', value: 25 },
                                            { label: '30 mm', value: 30 },
                                            { label: '35 mm', value: 35 },
                                            { label: '40 mm', value: 40 },
                                            { label: '45 mm', value: 45 },
                                            { label: '50 mm', value: 50, selected: true }
        ]);

        var $height = FormUtil.getDropdown([{ label: ' 5 mm', value:  5 },
                                            { label: '10 mm', value: 10 },
                                            { label: '15 mm', value: 15, selected: true },
                                            { label: '20 mm', value: 20 },
                                            { label: '25 mm', value: 25 },
                                            { label: '30 mm', value: 30 },
                                            { label: '35 mm', value: 35 },
                                            { label: '40 mm', value: 40 },
                                            { label: '45 mm', value: 45 },
                                            { label: '50 mm', value: 50 }
        ]);

        // The interaction with the library to generate barcodes is buggy so a double call is needed, this should probably be wrapped on the generateBarcode method instead of here
        var updateBarcode = function() {
            _this.generateBarcode($barcodeTypesDropdown.val(), barcode, barcode, function() {
                var imageData = _this.generateBarcode($barcodeTypesDropdown.val(), barcode, barcode,  null, parseInt($width.val()), parseInt($height.val()));
                $canvas.attr('src', imageData);
            }, parseInt($width.val()), parseInt($height.val()));
        };

        var $btnAccept = $('<input>', { 'type': 'submit', 'class' : 'btn btn-primary', 'value' : 'Download' });
        $btnAccept.click(function(event) {
            var pdf = new jsPDF({
                orientation: 'l',
                unit: 'mm',
                format: [parseInt($width.val()), parseInt($height.val())],
                putOnlyUsedFonts:true
            });
            var imageData = _this.generateBarcode($barcodeTypesDropdown.val(), barcode, barcode,  null, parseInt($width.val()), parseInt($height.val()));
            pdf.addImage(imageData, 'png', 0, 0, parseInt($width.val()), parseInt($height.val()));
            pdf.save("barcodes.pdf");
        });

        var $btnCancel = $('<input>', { 'type': 'submit', 'class' : 'btn', 'value' : 'Close' });
		$btnCancel.click(function(event) {
			Util.unblockUI();
		});

        $barcodeTypesDropdown.change(updateBarcode);
        $width.change(updateBarcode);
        $height.change(updateBarcode);

		$window.append($('<legend>').append("Print Barcode"));
	    $window.append($('<br>'));
	    $window.append($('<center>').append($barcodeTypesDropdown));
	    $window.append($('<br>'));
	    $window.append($('<center>').append($width));
	    $window.append($('<br>'));
	    $window.append($('<center>').append($height));
	    $window.append($('<br>'));
	    $window.append($('<center>').append($canvas));
	    $window.append($('<br>'));
	    $window.append($btnAccept).append('&nbsp;').append($btnCancel);

        var css = {
            'text-align' : 'left',
            'top' : '15%',
            'width' : '70%',
            'height' : '400px',
            'left' : '15%',
            'right' : '20%',
            'overflow' : 'auto'
        };

        Util.blockUI($window, css);

        // The first call is to load the library and show the barcode
        updateBarcode();
    }

    this.supportedBarcodes = function() {
        return [
                {
                    value : "code128",
                    label : "Code 128",
                    selected : true
                },
                {
                    value : "qrcode",
                    label : "QR Code"
                },
                {
                    value : "microqrcode",
                    label : "Micro QR Code"
                }
            ];
    }

    this.generateBarcode = function(barcodeType, text, altx, action, width, height) {
        var elt  = symdesc[barcodeType];
        var opts = {};
        var rot  = "N";
        var monochrome = true;
        var scaleX = 1;
        var scaleY = 1;

        var bw = new BWIPJS(bwipjs_fonts, monochrome);

        var canvas = document.createElement('canvas');
        canvas.height = 1;
        canvas.width  = 1;
        canvas.style.visibility = 'hidden';

        // Add the alternate text
        if (altx) {
            opts.alttext = altx;
            opts.includetext = true;
        }
        // We use mm rather than inches for height - except pharmacode2 height
        // which is expected to be in mm
        if (+opts.height && elt.sym != 'pharmacode2') {
            opts.height = opts.height / 25.4 || 0.5;
        }
        if(height) {
            opts.height = height / 25.4;
        }
        // Likewise, width.
        if (+opts.width) {
            opts.width = opts.width / 25.4 || 0;
        }
        if(width) {
            opts.width = width / 25.4;
        }
        // BWIPP does not extend the background color into the
        // human readable text.  Fix that in the bitmap interface.
        if (opts.backgroundcolor) {
            bw.bitmap(new Bitmap(canvas, rot, opts.backgroundcolor));
            delete opts.backgroundcolor;
        } else {
            bw.bitmap(new Bitmap(canvas, rot));
        }

        // Set the scaling factors
        bw.scale(scaleX, scaleY);

        // Add optional padding to the image
        bw.bitmap().pad(+opts.paddingwidth*scaleX || 0,
                        +opts.paddingheight*scaleY || 0);

        try {
            // Call into the BWIPP cross-compiled code.
            BWIPP()(bw, elt.sym, text, opts);

            // Allow the font manager to demand-load any required fonts
            // before calling render().
            bwipjs_fonts.loadfonts(function(e) {
                if (e) {
                    Util.manageError(e);
                } else {
                    // Draw the barcode to the canvas
                    bw.render();
                    canvas.style.visibility = 'visible';
                    if(action) {
                        action();
                    }
                }
            });
        } catch (e) {
            // Watch for BWIPP generated raiseerror's.
            var msg = ''+e;
            if (msg.indexOf("bwipp.") >= 0) {
                Util.manageError(msg);
            } else if (e.stack) {
                Util.manageError(e.stack);
            } else {
                Util.manageError(e);
            }
        }
        return canvas.toDataURL('image/png');
    }
}