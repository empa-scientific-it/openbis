var ReactTestUtils = new function() {

    this.choosePopValue = function(selector, value) {
        return new Promise(function executor(resolve, reject) {
            try {
                var elements = document.querySelectorAll(selector); // '[data-value]'

                for (i = 0; i < elements.length; i++) {
                    if (elements[i].innerText === value) {
                        let event = new MouseEvent("click", { view: window, bubbles: true, cancelable: true, buttons: 1});
                        elements[i].dispatchEvent(event);
                    }
                }

                resolve();
            } catch(error) {
                reject(error);
            }
        });
    };

    this.mousedown = function(elementId) {
        return new Promise(function executor(resolve, reject) {
            try {
                var element = $( "#" + elementId );
                let event = new MouseEvent("mousedown", { view: window, bubbles: true, cancelable: true, buttons: 1});
                element[0].dispatchEvent(event);

                resolve();
            } catch(error) {
                reject(error);
            }
        });
    };

    this.click = function(elementId) {
        return new Promise(function executor(resolve, reject) {
            try {
                var element = $( "#" + elementId );
                let event = new MouseEvent("click", { view: window, bubbles: true, cancelable: true, buttons: 1});
                element[0].dispatchEvent(event);

                resolve();
            } catch(error) {
                reject(error);
            }
        });
    };

    this.setValue = function(elementId, value) {
        return new Promise(function executor(resolve, reject) {
            try {
                var element =  $( "#" + elementId )[0];

                let lastValue = element.value;
                element.value = value;
                let event = new Event("input", { target: element, bubbles: true });
                let tracker = element._valueTracker;
                if (tracker) {
                    tracker.setValue(lastValue);
                }
                element.dispatchEvent(event);

                resolve();
            } catch(error) {
                reject(error);
            }
        });
    };
}