/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Based on http://stackoverflow.com/questions/4770025/how-to-disable-scrolling-temporarily

// left: 37, up: 38, right: 39, down: 40,
// spacebar: 32, pageup: 33, pagedown: 34, end: 35, home: 36
var BlockScrollUtil = new function() {
		this.keys = [37, 38, 39, 40];
		
		this.preventDefault = function(e) {
			e = e || window.event;
			if (e.preventDefault)
				e.preventDefault();
			e.returnValue = false;
		}
		
		this.keydown = function(e) {
			for (var i = BlockScrollUtil.keys.length; i--;) {
				if (e.keyCode === BlockScrollUtil.keys[i]) {
					BlockScrollUtil.preventDefault(e);
					return;
				}
			}
		}
		
		this.wheel = function(e) {
			BlockScrollUtil.preventDefault(e);
		}
		
		this.disable_scroll = function() {
			if (window.addEventListener) {
				window.addEventListener('DOMMouseScroll', BlockScrollUtil.wheel, false);
			}
			window.onmousewheel = document.onmousewheel = BlockScrollUtil.wheel;
			
			document.onkeydown = function(e) {
				BlockScrollUtil.keydown(e);
			};
		}
		
		this.enable_scroll = function() {
			if (window.removeEventListener) {
				window.removeEventListener('DOMMouseScroll', BlockScrollUtil.wheel, false);
			}
			window.onmousewheel = document.onmousewheel = document.onkeydown = null;
		}
	}
