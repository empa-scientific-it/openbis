class FileHandler(object):

    def __init__(self, scripts):
        self.scripts = scripts

    def get_script(self, script_path):
        return self.scripts[script_path]
