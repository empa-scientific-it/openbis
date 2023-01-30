#!/usr/bin/env python

import re, sys

lines = [line.strip() for line in open(sys.argv[1])]
map = dict()

regex = ur"(\d+)\s?ms\)\s([^\s]+)"
for line in lines:
	match = re.findall(regex, line)
	if len(match) == 1:
		time, operation = match[0]
		if map.has_key(operation):
			map[operation].append(time)
		else:
			map[operation] = [time]

averages = []
for key, value in map.iteritems():
	sum = reduce(lambda x, y: int(x) + int(y), value)
	avg = int(sum) / float(len(value))
	averages.append((key, avg, value))

for operation, average, values in sorted(averages, key=lambda x: x[1], reverse=True):
	print operation+" average: "+str(average)+" ms ("+str(values)+")"
