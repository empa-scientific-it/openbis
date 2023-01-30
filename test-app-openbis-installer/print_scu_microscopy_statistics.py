#!/usr/bin/python
import sys

class Statistics(object):
    def __init__(self, test_name):
        self.test_name = test_name
        self.expected_upper_limit_for_thumbnail_creation_time = None

all_statistics = []
with open(sys.argv[1], 'r') as f:
    for line in f:
        if "\/\ dropTestExample" in line:
            test_name = line.split()
            statistics = Statistics(test_name[1])
        elif "Registration took" in line:
            splitted_line = line.split()
            statistics.registration_time = float(splitted_line[2])
            statistics.thumbnail_creation_time = float(splitted_line[4][1:])
            statistics.number_of_data_sets = int(splitted_line[-3])
        elif "expected upper limit <" in line:
            number = float(line.split()[-1][1:-1])
            if statistics.expected_upper_limit_for_thumbnail_creation_time is None:
                statistics.expected_upper_limit_for_thumbnail_creation_time = number
            else:
                statistics.expected_upper_limit_for_registration_time = number
                all_statistics.append(statistics)

BAR_LENGTH = 20
print("Test\tNumber of Data Sets\tThumbnail Time\tExpected Thumbnail Time\tRegistration Time\tExpected Registration Time\tRatio")
for stat in all_statistics:
    ratio = int (BAR_LENGTH * stat.registration_time / stat.expected_upper_limit_for_registration_time);
    bar = '*' * ratio
    if ratio < BAR_LENGTH:
        bar += '-' * (BAR_LENGTH - ratio)
    print("%s\t%s\t%s\t%s\t%s\t%s\t%s" % (stat.test_name, stat.number_of_data_sets, 
                                          stat.thumbnail_creation_time, stat.expected_upper_limit_for_thumbnail_creation_time,
                                          stat.registration_time, stat.expected_upper_limit_for_registration_time, bar))

