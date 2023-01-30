def calculate():
	temp = value
	runner.evaluate(action);
	if temp != value:
		raise Exception(str(temp)+" vs "+str(value))