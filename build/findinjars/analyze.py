#!/usr/bin/python

import sys, glob

def renderKey(dir, key):
	return key.replace(dir + "/", "\n").replace("--", "/")

mode = sys.argv[1]
dir = sys.argv[2]
if mode != 'duplicates' and mode != 'loners':
	print "argument must be either duplicates or loners"
	sys.exit(1)

filenames =  glob.glob(dir + "/*.jar")

map = dict()
for filename in filenames:
	with open(filename) as file:
    		lines = file.readlines()
		for l in lines:
			line = l.replace("\n", "")
			if not map.has_key(line):
				map[line] = []
			map[line].append(filename)


map2 = dict()

for clazz, jars in map.iteritems():
	jarlist = sorted(jars)
	key = ""
	for jar in jarlist:
		key = key + jar + " "

	if not map2.has_key(key):
		map2[key] = []
	map2[key].append(clazz)
	

for key, value in map2.iteritems():
	keylength = key.count(' ')
	
	if mode == 'duplicates':
		if keylength > 1:
			print "In all these: "+renderKey(dir, key)
			print "-----------------------------"
			for clazz in sorted(value):
				print clazz
			print "\n" 
	else:
		if keylength == 1:
			print "Only in "+renderKey(dir, key)
			print "-----------------------------"
			for clazz in sorted(value):
				print clazz
			print "\n"
