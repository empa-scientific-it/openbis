var BarcodeUtil = new function() {
    var MIN_BARCODE_LENGTH = 15;
    var barcodeTimeout = false;
    var barcodeReader = "";
    var barcodePattern = /^[-0-9]+$/;

    var readSample = function(action) {
        // Trigger search if needed
        // permID Format 23 char, 1 hyphen: 20170912112249208-38888
        // UUID Format 36 char, 4 hyphens: 123e4567-e89b-12d3-a456-426655440000

        if(barcodeReader.length >= MIN_BARCODE_LENGTH && barcodePattern.test(barcodeReader)) {
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

    this.preGenerateBarcodes = function(views) {
        views.header.append($("<h2>").append("Barcode Generator"));
	    var $generateBtn = FormUtil.getButtonWithText("Generate Barcodes", function() {}, "btn-primary");
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
        $lineOne.append($("<span>", { style:"width:15%; margin-left: 10px; display:inline-block;"}).append($barcodeTypesDropdown))
                .append($("<span>", { style:"width:10%; margin-left: 10px; display:inline-block;"}).append($numberDropdown))
                .append($("<span>", { style:"width:15%; margin-left: 10px; display:inline-block;"}).append($layout))
                .append($("<span>", { style:"width:15%; margin-left: 10px; display:inline-block;"}).append($width))
                .append($("<span>", { style:"width:15%; margin-left: 10px; display:inline-block;"}).append($height))
                .append($("<span>", { style:"margin-left: 10px; display:inline-block;"}).append($generateBtn))
                .append($("<span>", { style:"margin-left: 10px; display:inline-block;"}).append($printButton));
        $toolbar.append($lineOne);



        views.header.append($toolbar);

        var _this = this;
        $generateBtn.click(function() {
            var width  = $width.val();
            width = parseInt(width);
            var height = $height.val();
            height = parseInt(height);
            var layout = $layout.val();

            views.content.empty();
            $layoutForPrinter = $('<div>', { 'id' : 'layout-for-printer' });
            views.content.append($layoutForPrinter);

            var value = parseInt($numberDropdown.val());
            mainController.serverFacade.createPermIdStrings(value, function(newPermIds) {
                var format = null;
                if(width && height) {
                    format = {
                        orientation: ((layout === 'split')?'l':'p'),
                        unit: 'mm',
                        format: [width, height * ((layout === 'split')?1:value) + ((layout === 'split')?0:2*value)],
                        putOnlyUsedFonts:true
                    };
                    console.log(format);
                    pdf = new jsPDF(format);
                }

                for(var idx = 0; idx < value; idx++) {
                    // HTML
                    _this.addBarcode($layoutForPrinter, idx, $barcodeTypesDropdown.val(), newPermIds[idx], idx === 0, width, height, layout);

                    // PDF
                    var imgData = _this.generateBarcode($barcodeTypesDropdown.val(), newPermIds[idx], newPermIds[idx], null, width, height);
                    if(pdf !== null) {
                        if(layout === 'split') {
                            if(idx > 0) {
                                pdf.addPage(format.format, 'l');
                            }
                            pdf.addImage(imgData, 'png', 0, 0, width, height);
                        } else {
                            console.log("height: " + (height * idx + idx));
                            pdf.addImage(imgData, 'png', 0, (height * idx + 2*idx), width, height);
                        }
                    }
                }
            });
        });

        this.preloadLibrary();
    }

    this.preloadLibrary = function() {
        this.generateBarcode("code128", "Barcode", "Text", function() {
        });
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

        var $btnAccept = $('<input>', { 'type': 'submit', 'class' : 'btn btn-primary', 'value' : actionLabel });
        $btnAccept.click(function(event) {
            // Swap event listeners
            document.removeEventListener('keyup', barcodeReaderLocalEventListener);
            _this.enableAutomaticBarcodeReading();
            Util.unblockUI();
            action(objects);
        });

        var $btnCancel = $('<input>', { 'type': 'submit', 'class' : 'btn', 'value' : 'Close' });
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

    this.readBarcode = function(entity) {
        var $window = $('<form>', {
            'action' : 'javascript:void(0);'
        });

        var $btnAccept = $('<input>', { 'type': 'submit', 'class' : 'btn btn-primary', 'value' : 'Save Barcode' });
        $btnAccept.prop("disabled",false);

        var $barcodeReader = $('<input>', { 'type': 'text', 'placeholder': 'barcode', 'style' : 'min-width: 50%;' });
        $barcodeReader.keyup(function() {
            if($barcodeReader.val().length >= MIN_BARCODE_LENGTH ||
               $barcodeReader.val().length === 0) {
                $btnAccept.prop("disabled", false);
            } else {
                $btnAccept.prop("disabled", true);
            }
        });

        $btnAccept.click(function(event) {
            Util.blockUINoMessage();

            var updateBarcode = function() {
                require([ "as/dto/sample/update/SampleUpdate", "as/dto/sample/id/SamplePermId" ],
                    function(SampleUpdate, SamplePermId) {
                        var sample = new SampleUpdate();
                        sample.setSampleId(new SamplePermId(entity.permId));
                        sample.setProperty("$BARCODE", $barcodeReader.val());
                        mainController.openbisV3.updateSamples([ sample ]).done(function(result) {
                            Util.unblockUI();
                            Util.showInfo("Barcode Updated", function() {
                                mainController.changeView('showViewSamplePageFromPermId', entity.permId);
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
                        Util.showError("Barcode already in use by " +  results.objects[0].identifier.identifier + " : It will not be assigned.");
                    }
                });
            }
        });

        var $btnCancel = $('<input>', { 'type': 'submit', 'class' : 'btn', 'value' : 'Close' });
        $btnCancel.click(function(event) {
            Util.unblockUI();
        });

        $window.append($('<legend>').append("Update Barcode"));
        $window.append($('<br>'));
        $window.append(FormUtil.getInfoText("A valid barcode need to have " + MIN_BARCODE_LENGTH + " or more characters."));
        $window.append(FormUtil.getWarningText("An empty barcode will delete the current barcode."));
        $window.append($('<br>'));
        $window.append($('<center>').append($barcodeReader));
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