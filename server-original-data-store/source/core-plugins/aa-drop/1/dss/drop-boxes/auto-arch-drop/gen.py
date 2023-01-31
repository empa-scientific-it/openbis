import sys
from subprocess import call
import random

for x in range(0, 100):
    call(["dd", "if=/dev/random", "of=ab%d" % random.randint(0, 100000000), "count=%d" % random.randint(9, 597), "bs=793"])
    