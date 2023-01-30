#!/usr/bin/env python

"""
Generate a CSV file with feature vectors.

Usage: generate-test-plate-analysis.py <plate code>[.<analysis procedure>] <feature 1> <feature 2> ...

It creates a folder named <plate code>[.<analysis procedure>].csv with random data for each specified feature.
If a feature is of the form

<feature name>:<term 1>:<term 2>:...

Then a non-numerical feature with a specified list of possible values is created.

The features 'row number' and 'column number' are created automatically

"""

import sys
import string
import random

fileName = sys.argv[1]
features = sys.argv[2:]
file = open(fileName + ".csv", "w")
file.write("barcode,Well,row number,column number")
for feature in features:
    file.write(",")
    file.write(feature.split(':')[0])
file.write("\n")
plateName = fileName.split('.')[0]
for row in range(0, 8):
    for column in range(0, 12):
        file.write(plateName)
        file.write(",")
        file.write(string.letters[26 + row])    
        file.write(str(column + 1))
        file.write(",")
        file.write(str(row + 1))
        file.write(",")
        file.write(str(column + 1))
        for feature in features:
            file.write(",")
            splittedFeature = feature.split(':')
            if len(splittedFeature) > 1:
                terms = splittedFeature[1:]
                file.write(terms[random.randint(0, len(terms) - 1)])
            else:
                file.write(str(random.gauss(0.1 * row, 0.01 * (column + 1))))
        file.write("\n")
file.close()
  