from .file_handling import FileHandler
import dotdict
import openbis_utils


def merge_dicts(product, dict_to_merge):
    for dict_key, dict_val in dict_to_merge.items():
        product[dict_key] = dict_val
    return product
