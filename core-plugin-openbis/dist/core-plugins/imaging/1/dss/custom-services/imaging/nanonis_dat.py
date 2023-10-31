import numpy
from PIL import Image
import io
import base64
import sys
import json
import os
import sys
from spmpy_terry import spm   # <--- class spm defines objects of type spm with their attributes and class functions
import spmpy_terry as spmpy   # <--- spmpy has other methods

import matplotlib.pyplot as plt
# %matplotlib inline


def load_image(path):
    return spm(path)


def get_lock_in(img):
    param_name = 'lock-in>lock-in status'
    param = img.get_param(param_name)
    return param


def get_channel(img, channel_name = 'z'):
    # channel_name = 'z'
    channel = img.get_channel(channel_name)
    return channel


file = sys.argv[1]
params = json.loads(sys.argv[2])
meta_data = json.loads(sys.argv[3])


folder_dir = os.path.join(file, 'original')
file_path = os.path.join(folder_dir, os.listdir(folder_dir)[0])
# print(file_path)


def generate_random_image(height, width):
    imarray = numpy.random.rand(height,width,3) * 255
    im = Image.fromarray(imarray.astype('uint8')).convert('RGBA')
    img_byte_arr = io.BytesIO()
    im.save(img_byte_arr, format='PNG')
    img_byte_arr = img_byte_arr.getvalue()
    encoded = base64.b64encode(img_byte_arr)
    return encoded


def get_sxm_image():
    img = load_image(file_path)
    channel = get_channel(img, 'z')
    img_byte_arr = io.BytesIO()
    plt.imshow(channel[0])
    plt.savefig(img_byte_arr, format="png")
    # print(img.header)
    img_byte_arr = img_byte_arr.getvalue()
    encoded = base64.b64encode(img_byte_arr)
    return encoded


def get_sxm_image2():
    img = load_image(file_path)
    channel = get_channel(img, 'z')[0]

    min_val = numpy.min(channel)
    max_val = numpy.max(channel)
    scaled_data = (channel - min_val) / (max_val - min_val)
    img = Image.fromarray(numpy.uint8(scaled_data * 255), 'L')

    img_byte_arr = io.BytesIO()
    img.save(img_byte_arr, format='PNG')
    img_byte_arr = img_byte_arr.getvalue()
    encoded = base64.b64encode(img_byte_arr)
    return encoded

print(params)
if params['mode'] == '1':
    print(get_sxm_image())
elif params['mode'] == '2':
    print(get_sxm_image2())
elif params['mode'] == '3':
    print(generate_random_image(640, 640))