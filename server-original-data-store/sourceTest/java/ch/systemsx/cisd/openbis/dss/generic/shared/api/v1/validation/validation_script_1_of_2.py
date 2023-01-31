import os;

def find_match(directory):
    found_match = False;
    for filename in os.listdir(directory.getAbsolutePath()):
        if 'valid-file.txt' == filename:
            found_match = True
            break
    return found_match;
