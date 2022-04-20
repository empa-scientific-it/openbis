var EventUtil = new function() {
    this.DEFAULT_TIMEOUT = 15000;
    this.DEFAULT_TIMEOUT_STEP = 1000;

    this.getElement = function(elementId, ignoreError, resolve) {
        var element = $( "#" + elementId );
        if(!element) {
            if(ignoreError) {
                resolve();
            } else {
                throw "Element not found: #" + elementId;
            }
        }
        return element;
    };

    this.getElementThatStartWith = function(elementId, ignoreError, resolve) {
        var element = $('[id^="' + elementId +'"]')
        if(!element) {
            if(ignoreError) {
                resolve();
            } else {
                throw "Element not found: id start with" + elementId;
            }
        }
        return element;
    };

    this.getClass = function(className, ignoreError, resolve) {
        var element = $( "." + className );
        if(!element) {
            if(ignoreError) {
                resolve();
            } else {
                throw "Element not found: class: " + elementId;
            }
        }
        return element;
    };

    this.checkTimeout = function(elementId, timeout, ignoreError, resolve) {
        if (!timeout) {
            timeout = this.DEFAULT_TIMEOUT;
        }
        timeout -= this.DEFAULT_TIMEOUT_STEP;

        if (timeout <= 0) {
            if(ignoreError) {
                resolve();
            } else {
                throw "Element '" + elementId + "' does not exist.";
            }
        }
        return timeout;
    };

    this.sleep = function(ms) {
        return new Promise(resolve => setTimeout(resolve, ms));
    };
}

function EventExecutor(testId) {
    this.testId = testId;
    this.events = ["start test " + testId];

    this.click = function(elementId, ignoreError) {
        this.events.push("click " + elementId);
        return new Promise(function executor(resolve, reject) {
            try {
                var element = EventUtil.getElement(elementId, ignoreError, resolve);
                element.focus();
                element.trigger('click');
                resolve();
            } catch(error) {
                reject(error);
            }
        });
    };

    this.change = function(elementId, value, ignoreError) {
        this.events.push("change " + elementId + ": >" + value + "<");
        return new Promise(function executor(resolve, reject) {
            try {
                var element = EventUtil.getElement(elementId, ignoreError, resolve);
                element.focus();
                element.val(value).change();
                resolve();
            } catch(error) {
                reject(error);
            }
        });
    };

    this.changeStartsWith = function(elementId, value, ignoreError) {
        this.events.push("changeStartsWith " + elementId + ": >" + value + "<");
        return new Promise(function executor(resolve, reject) {
            try {
                var element = EventUtil.getElementThatStartWith(elementId, ignoreError, resolve);
                element.focus();
                element.val(value).change();
                resolve();
            } catch(error) {
                reject(error);
            }
        });
    };
    
    this.searchSelect2 = function(elementId, value, ignoreError) {
        this.events.push("searchSelect2 " + elementId + ": >" + value + "<");
         return new Promise(function executor(resolve, reject) {
             try {
                 var $el = EventUtil.getElementThatStartWith(elementId, ignoreError, resolve);
                 $el.select2('open');
                 var $search = $el.data('select2').dropdown.$search || $el.data('select2').selection.$search;
                 $search.val(value);
                 $search.trigger('input');
                 resolve();
             } catch(error) {
                 reject(error);
             }
         });
     };


	this.mouseUp = function(className, ignoreError) {
        this.events.push("mouseUp " + className);
        return new Promise(function executor(resolve, reject) {
            try {
                var element = EventUtil.getClass(className, ignoreError, resolve);
                element.trigger("mouseup");
                resolve();
            } catch(error) {
                reject(error);
            }
        });
    };

    this.changeSelect2 = function(elementId, value, ignoreError) {
        this.events.push("changeSelect2 " + elementId + ": >" + value + "<");
        return new Promise(function executor(resolve, reject) {
            try {
                var element = EventUtil.getElement(elementId, ignoreError, resolve);
                element.select2();
                element.focus();
                element.val(value);
                element.select2().trigger('change');
                resolve();
            } catch(error) {
                reject(error);
            }
        });
    };

    this.triggerSelectSelect2 = function(elementId, value, ignoreError) {
        this.events.push("triggerSelectSelect2 " + elementId + ": >" + value + "<");
        return new Promise(function executor(resolve, reject) {
            try {
                var element = EventUtil.getElement(elementId, ignoreError, resolve);
                element.focus();
                element.val(value);
                element.select2().trigger('select2:select');
                resolve();
            } catch(error) {
                reject(error);
            }
        });
    };

    this.checked = function(elementId, value, ignoreError) {
        this.events.push("checked " + elementId + ": >" + value + "<");
        return new Promise(function executor(resolve, reject) {
            try {
                var element = EventUtil.getElement(elementId, ignoreError, resolve);
                element.focus();
                element.prop('checked', value);
                resolve();
            } catch(error) {
                reject(error);
            }
        });
    };

    this.contains = function(elementId, values, ignoreError) {
        this.events.push("contains " + elementId + ": >" + values + "<");
        return new Promise(function executor(resolve, reject) {
            try {
                var element = EventUtil.getElement(elementId, ignoreError, resolve);
                if (values.indexOf(element.html()) >= 0 || values.indexOf(element.val()) >= 0) {
                    resolve();
                } else {
                    throw "Element #" + elementId + " should be in " + values;
                }
            } catch(error) {
                reject(error);
            }
        });
    };

    this.equalTo = function(elementId, value, isEqual, ignoreError) {
        this.events.push("equalTo " + isEqual + " " + elementId + ": >" + value + "<");
        return new Promise(function executor(resolve, reject) {
            try {
                var element = EventUtil.getElement(elementId, ignoreError, resolve);
                if (isEqual && (element.html() === value || element.val() === value) ||
                    !isEqual && (element.html() != value && element.val() != value)) {
                    resolve();
                } else {
                    throw "Element #" + elementId + " should" + (isEqual ? "" : " not") + " be equal " + value;
                }
            } catch(error) {
                reject(error);
            }
        });
    };

    this.write = function(elementId, text, ignoreError) {
        this.events.push("write " + elementId + ": >" + text + "<");
        return new Promise(function executor(resolve, reject) {
            try {
                var element = EventUtil.getElement(elementId, ignoreError, resolve);
                element.focus();
                $(element).val(text);
                for (var i = 0; i < text.length; i++) {
                    $(element).trigger('keydown', {which: text.charCodeAt(i)});
                    $(element).trigger('keyup', {which: text.charCodeAt(i)});
                }
                resolve();
            } catch(error) {
                reject(error);
            }
        });
    };

    this.keypress = function(elementId, key, ignoreError) {
        this.events.push("keypress " + elementId + ": >" + key + "<");
        return new Promise(function executor(resolve, reject) {
            try {
                var element = EventUtil.getElement(elementId, ignoreError, resolve);
                element.focus();
                var e = $.Event('keypress');
                e.which = key;
                $(element).trigger(e);
                resolve();
            } catch(error) {
                reject(error);
            }
        });
    };

    this.verifyExistence = function(elementId, isExist, ignoreError) {
        this.events.push("verifyExistence " + elementId + ": >" + isExist + "<");
        return new Promise(function executor(resolve, reject) {
            var elementExistence = $("#" + elementId).length > 0;
            if (elementExistence == isExist) {
                resolve();
            } else {
                if (ignoreError) {
                    resolve();
                } else {
                    throw "Element " + (isExist ? "not" : "") + " found: #" + elementId;
                }
            }
        });
    };

    this.waitForId = function(elementId, ignoreError, timeout) {
        return this.waitFor("#" + elementId, ignoreError, timeout);
    };

    this.waitForClass = function(className, ignoreError, timeout) {
        return this.waitFor("." + className, ignoreError, timeout);
    };

    this.waitFor = function(jquerySelectorString, ignoreError, timeout) {
        var method = "waitFor " + jquerySelectorString;
        if (jquerySelectorString.startsWith("#")) {
            method = "waitForId " + jquerySelectorString.substring(1);
        }
        if (jquerySelectorString.startsWith(".")) {
            method = "waitForClass " + jquerySelectorString.substring(1);
        }
        this.events.push(method + this.renderTimeout(timeout));
        return new Promise(function executor(resolve, reject) {
            try {
                timeout = EventUtil.checkTimeout(jquerySelectorString, timeout, ignoreError, resolve);
                
                if($(jquerySelectorString).length <= 0) {
                    setTimeout(executor.bind(null, resolve, reject), EventUtil.DEFAULT_TIMEOUT_STEP);
                } else {
                    resolve();
                }
            } catch(error) {
                reject(error);
            }
        });
    };
    
    this.waitForStyle = function(elementId, styleName, styleValue, ignoreError, timeout) {
        this.events.push("waitForStyle " + elementId + ":" + styleName + "=" + styleValue + this.renderTimeout(timeout));
        return new Promise(function executor(resolve, reject) {
            try {
                timeout = EventUtil.checkTimeout(elementId, timeout, ignoreError, resolve);

                var element = EventUtil.getElement(elementId, ignoreError, resolve);
                if (element[0].style[styleName] === styleValue) {
                    resolve();
                } else {
                    setTimeout(executor.bind(null, resolve, reject), EventUtil.DEFAULT_TIMEOUT_STEP);
                }
            } catch(error) {
                reject(error);
            }
        });
    };

    this.waitForFill = function(elementId, ignoreError, timeout) {
        this.events.push("waitForFill " + elementId + this.renderTimeout(timeout));
        return new Promise(function executor(resolve, reject) {
            try {
                timeout = EventUtil.checkTimeout(elementId, timeout, ignoreError, resolve);

                var element = EventUtil.getElement(elementId, ignoreError, resolve);
                if(element.html().length <= 0 && element.val().length <= 0) {
                    setTimeout(executor.bind(null, resolve, reject), EventUtil.DEFAULT_TIMEOUT_STEP);
                } else {
                    resolve();
                }
            } catch(error) {
                reject(error);
            }
        });
    };

    this.waitForCkeditor = function(elementId, data, timeout) {
        this.events.push("waitForCkeditor " + elementId + this.renderTimeout(timeout));
        return new Promise(function executor(resolve, reject) {
            try {
                timeout = EventUtil.checkTimeout(elementId, timeout, false, resolve);
                editor = CKEditorManager.getEditorById(elementId);

                if(editor === undefined) {
                    setTimeout(executor.bind(null, resolve, reject), EventUtil.DEFAULT_TIMEOUT_STEP);
                } else {
                    resolve();
                }
            } catch(error) {
                reject(error);
            }
        });
    };
    
    this.renderTimeout = function(timeout) {
        return timeout ? " (" + timeout + " msec)" : "";
    };

    this.sleep = function(ms) {
        return EventUtil.sleep(ms);
    };

    this.dragAndDrop = function(dragId, dropId, ignoreError) {
        this.events.push("dragAndDrop " + dragId + " " + dropId);
        return new Promise(function executor(resolve, reject) {
            try {
                var dragElement = EventUtil.getElement(dragId, ignoreError, resolve).draggable();
                var dropElement = EventUtil.getElement(dropId, ignoreError, resolve).droppable();

                var dt = new DataTransfer();

                var dragStartEvent = jQuery.Event("dragstart");
                dragStartEvent.originalEvent = jQuery.Event("mousedown");
                dragStartEvent.originalEvent.dataTransfer = dt;

                dropEvent = jQuery.Event("drop");
                dropEvent.originalEvent = jQuery.Event("DragEvent");
                dropEvent.originalEvent.dataTransfer = dt;

                dragElement.trigger(dragStartEvent);
                dropElement.trigger(dropEvent);
                resolve();
            } catch(error) {
                reject(error);
            }
        });
    };

    this.dropFile = function(fileName, url, dropId, ignoreError) {
        this.events.push("dropFile " + fileName + " " + url + " " + dropId);
        return new Promise(function executor(resolve, reject) {
            try {
                var dropElement = EventUtil.getElement(dropId, ignoreError, resolve).droppable();

                TestUtil.fetchBytes(url, function(file) {
                    file.name = fileName;

                    var dt = { files: [file] };

                    dropEvent = jQuery.Event("drop");
                    dropEvent.originalEvent = jQuery.Event("DragEvent");
                    dropEvent.originalEvent.dataTransfer = dt;
                    dropElement.trigger(dropEvent);
                    resolve();
                });
            } catch(error) {
                reject(error);
            }
        });
    };


    this.searchForObjectInSelect2 = function(e, id, addBtnId) {
        return new Promise(function executor(resolve, reject) {
            Promise.resolve().then(() => e.waitForId(addBtnId))
                             .then(() => e.searchSelect2("advanced-entity-search-dropdown-id", id))
                             .then(() => e.sleep(2000)) // wait when object will
                                                        // be found
                             .then(() => e.mouseUp("select2-results__option"))
                             .then(() => e.sleep(1000)) // wait when object will
                                                        // be selected
                             .then(() => e.click(addBtnId))
                             .then(() => resolve())
                             .catch(error => reject(error));
        });
    }


	this.checkGridRange = function(gridId, range, ignoreError) {
	    return new Promise(function executor(resolve, reject) {
	        try {
                var element = EventUtil.getElement(gridId, ignoreError, resolve);
                if (element.find('span').html().startsWith(range)) {
                    resolve();
                } else {
                    throw "Grid range #" + gridId + " should start with " + range;
                }
            } catch(error) {
                reject(error);
            }
	    });
	};
}