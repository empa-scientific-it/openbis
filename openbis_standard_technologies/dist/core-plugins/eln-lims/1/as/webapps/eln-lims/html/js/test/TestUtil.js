var TestUtil = new function() {
    this.realSaveAs = null;
    this.savedFiles = new Map();

	this.login = function(username, password) {
        return new Promise(function executor(resolve, reject) {
            var e = new EventExecutor();
            testChain = Promise.resolve();
            testChain.then(() => e.waitForId("login-service-selector"))
                     .then(() => e.changeSelect2("login-service-selector", "Default Login Service"))
                     .then(() => e.waitForId("username"))
                     .then(() => e.write("username", username))
                     .then(() => e.write("password", password))
                     .then(() => e.click("login-button"))
                     .then(() => resolve())
                     .catch((error) => reject(error));
        });
    }

    this.getElnFrame = function() {
        var iframe = $("iframe");
        return iframe ? $("#eln-frame", iframe.contents()) : $("#eln-frame");
    }

    this.log = function(msg, attributes) {
        console.log(msg, attributes);
        var testLog = $("#test-log", TestUtil.getElnFrame().contents());
        console.log(testLog);
        if (testLog) {
            console.log("test log element:"+testLog.text());
            testLog.text(msg);
        }
    }

    this.reportToJenkins = function(id, msg) {
        if ($.cookie("report-to-jenkins") == "true") {
            var testName = "test" + id + "event";
            var event = jQuery.Event(testName);
            event.msg = msg;
            TestUtil.getElnFrame().trigger(event);
        }
    }

    this.sendEventToJenkins = function(e, i, msg) {
        var id = e.testId;
        var event = jQuery.Event("test" + i + "event");
        if (i == id) {
            event.msg = msg;
        } else {
            event.msg = "Test " + id + " failed:";
            e.events.forEach(function(ev) {
                event.msg += "\n{" + ev + "}, ";
            });
            TestUtil.log(event.msg);
        }
        TestUtil.log("Test " + i + " failed.");
        TestUtil.getElnFrame().trigger(event);
    }

    this.reportErrorToJenkins = function(e, msg) {
        if ($.cookie("report-to-jenkins") == "true") {
            // If one test is broken, then all tests must be failed.
            // If you need to add a new test, make sure that it will fail.

            chain = Promise.resolve();
            for(let i = e.testId; i <= TestProtocol.getTestCount(); i++) {
                chain = chain.then(() => TestUtil.sendEventToJenkins(e, i, error))
                             .then(() => EventUtil.sleep(1000));
            }
            chain.catch(error => { TestUtil.log(error) });
        }
    }

    this.reportError = function(e, error, reject) {
        TestUtil.reportErrorToJenkins(e, error);
        reject(error);
    }

    this.testPassed = function(e) {
        var id = e.testId;
        return new Promise(function executor(resolve, reject) {
            var msg = "Test " + id + " passed";
            TestUtil.reportToJenkins(id, msg);
            TestUtil.log("%c" + msg, "color: green");
            resolve();
        });
    }

    this.testNotExist = function(id) {
        return new Promise(function executor(resolve, reject) {
            var msg = "Test " + id +" is not exist";
            TestUtil.reportToJenkins(id, msg);
            TestUtil.log("%c" + msg, "color: grey");
            resolve();
        });
    }

    this.testLocally = function(id) {
        return new Promise(function executor(resolve, reject) {
            var msg = "Test " + id + " should be tested locally";
            TestUtil.reportToJenkins(id, msg);
            TestUtil.log("%c" + msg, "color: blue");
            resolve();
        });
    }

    this.allTestsPassed = function() {
        return new Promise(function executor(resolve, reject) {
            if ($.cookie("report-to-jenkins") != "true") {
                alert("Tests passed!");
            }
            resolve();
        });
    }

    this.setCookies = function(key, value) {
        return new Promise(function executor(resolve, reject) {
            $.cookie(key, value);
            resolve();
        });
    }

    this.deleteCookies = function(key) {
        return new Promise(function executor(resolve, reject) {
            $.removeCookie(key);
            resolve();
        });
    }

    this.verifyInventory  = function(e, ids) {
        return new Promise(function executor(resolve, reject) {
            chain = Promise.resolve();
            for (let i = 0; i < ids.length; i++) {
                chain = chain.then(() => e.waitForId(ids[i]));
            }
            chain.then(() => resolve()).catch(error => TestUtil.reportError(e, error, reject));
        });
    }

    this.setFile = function(name, url, mimeType) {
        var _this = this;
        return new Promise(function executor(resolve, reject) {
                _this.fetchBytes(url, function(file) {
                file.name = name;
                mainController.currentView.typeAndFileController.setFile(file);
                resolve();
            });
        });
    }

    this.checkFileEquality = function(name, url, replacer) {
        var _this = this;
        return new Promise(function executor(resolve, reject) {
            _this.fetchBytes(url, function(file) {
                _this.blob2Text(file, function(textFile) {
                    downloadedFile = _this.savedFiles[name];
                    downloadedFile = replacer(downloadedFile);

                    _this.savedFiles.delete(name);

                    if (textFile === downloadedFile) {
                        resolve();
                    } else {
                        throw "File " + name + " is not correct!";
                    }
                });
            });
        });
    }

    this.blob2Text = function(blob, action) {
        // get text from blob
        let fileReader = new FileReader();

        fileReader.readAsText(blob);

        fileReader.onload = function(event) {
            action(fileReader.result);
        };
    }

    this.idReplacer = function(text) {
        return text.replace(new RegExp('/openbis/openbis/file-service/eln-lims/[A-Za-z0-9/-]+', 'g'),
                                       '/openbis/openbis/file-service/eln-lims/identifier');
    }

    this.dateReplacer = function(text) {
        return text.replace(new RegExp('Date: [0-9-]+ [0-9:]+', 'g'), 'Date: YYYY-MM-DD HH:MM:SS');
    }

    this.fetchBytes = function(url, action) {
	    var xhr = new XMLHttpRequest();
        xhr.open('GET', url, true);
        xhr.responseType = 'blob';

        xhr.onload = function(e) {
            if (this.status == 200) {
                // get binary data as a response
                action(this.response);
            }
        };

        xhr.send();
    }

    this.ckeditorSetData = function(id, data) {
        return new Promise(function executor(resolve, reject) {
            editor = CKEditorManager.getEditorById(id);
            editor.setData(data);
            resolve();
        });
    }

    this.ckeditorTestData = function(id, data) {
        return new Promise(function executor(resolve, reject) {
            try {
                editor = CKEditorManager.getEditorById(id);
                var realData = editor.getData();

                if (realData === data) {
                    resolve();
                } else {
                    throw "CKEditor #" + elementId + " should be equal " + data;
                }
            } catch (error) {
                reject(error);
            }
        });
    }

    this.ckeditorDropFile = function(id, fileName, url) {
        return new Promise(function executor(resolve, reject) {
            editor = CKEditorManager.getEditorById(id);
            TestUtil.fetchBytes(url, function(file) {
                editor = CKEditorManager.getEditorById(id);

                file.name = fileName;

                editor.model.enqueueChange( 'default', () => {
                    editor.execute( 'imageUpload', { file: [file] } );
                } );
                resolve();
            });
        });
    }

    this.overloadSaveAs = function() {
        var _this = this;
        return new Promise(function executor(resolve, reject) {
            if (_this.realSaveAs === null) {
                _this.realSaveAs = saveAs;
            }

            saveAs = function(blob, name, no_auto_bom) {
                _this.blob2Text(blob, function(textFile) {
                    _this.savedFiles[name] = textFile;
                });
            }
            resolve();
        });
    }

    this.returnRealSaveAs = function() {
        var _this = this;
        return new Promise(function executor(resolve, reject) {
            saveAs = _this.realSaveAs;
            resolve();
        });
    }
}