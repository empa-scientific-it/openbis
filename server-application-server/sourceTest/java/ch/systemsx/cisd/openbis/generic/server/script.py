def return_x_if_defined_else_0():
	try:
		x
	except NameError:
		return 0
	else:
		return x
		
def set_x(value):
	global x
	x = value