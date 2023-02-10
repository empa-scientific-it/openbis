/**
 * Copyright (c) Mozilla Foundation http://www.mozilla.org/
 * This code is available under the terms of the MIT License
 */

if (!Array.prototype.filter)
{
  Array.prototype.filter = function(fun /*, thisp*/)
  {
    var len = this.length;
    if (typeof fun != "function")
      throw new TypeError();

    var res = new Array();
    var thisp = arguments[1];
    for (var i = 0; i < len; i++)
    {
      if (i in this)
      {
        var val = this[i]; // in case fun mutates this
        if (fun.call(thisp, val, i, this))
          res.push(val);
      }
    }

    return res;
  };
}

if (!Array.prototype.search)
{
  Array.prototype.search = function(fun /*, thisp*/)
  {
    var len2 = this.length;
    if (typeof fun != "function")
      throw new TypeError();

    var res2 = new Array();
    var thisp = arguments[1];
    for (var i = 0; i < len2; i++)
    {
      if (i in this)
      {
        var val2 = this[i]; // in case fun mutates this
        if (fun.call(thisp, val2, i, this))
          res2.push(val2);
      }
    }

    return res2;
  };
}

if (!Function.prototype.curry) 
{ 
	(function () 
	{
		var slice = Array.prototype.slice;
		Function.prototype.curry = function () 
		{
			var target = this; 
			var args = slice.call(arguments);
			return function () 
			{
				var allArgs = args;
				if (arguments.length > 0) 
				{ 
					allArgs = args.concat(slice.call(arguments));
				}
				return target.apply(this, allArgs); 
			};
		}; 
	}())
};

