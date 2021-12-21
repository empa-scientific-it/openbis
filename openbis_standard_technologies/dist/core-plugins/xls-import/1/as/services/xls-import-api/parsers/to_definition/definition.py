from utils.dotdict import dotdict


class Definition(object):
    '''
        Used to hold values for object(Vocabulary, SampleType etc.) creation.
    '''

    def __init__(self):
        self.row_number = 0
        self.type = None
        self.attributes = dotdict()
        self.properties = []

    def __str__(self):
        return "\n".join([
            "Row number:",
            str(self.row_number),
            "Definition type:",
            str(self.type),
            "Attributes:",
            str(self.attributes),
            "Properties:",
            str(self.properties),
            "==================" * 3, ''])

    __repr__ = __str__
