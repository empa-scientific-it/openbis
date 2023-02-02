"""
@author: Aaron Ponti
"""

import os
import logging
import re

from Processor import Processor


def process(transaction):
    """Dropbox entry point.

    @param transaction, the transaction object
    """

    # Create a Processor
    processor = Processor(transaction)

    # Run
    processor.run()
