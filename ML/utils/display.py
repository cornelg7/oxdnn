import cv2
import numpy as np

def label_colour(label):
    return colour_list[label]

"""
Colours generated from rotating around HSV spectrum with S and V at 90%
"""
colour_list = [
    [188, 229,  22],
    [229, 146,  22],
    [22, 229, 105],
    [64,  22, 229],
    [22, 105, 229],
    [22, 229, 229],
    [64, 229,  22],
    [188,  22, 229],
    [229,  22, 146],
    [229,  22,  22]
]

def draw_caption(image, box, caption):
    """
    Adapted from keras_retinanet.utils.visualization, with text moved
    """
    box = np.array(box).astype(int)
    cv2.putText(image, caption, (box[0], box[1] + 20), cv2.FONT_HERSHEY_PLAIN, 1, (0, 0, 0), 2)
    cv2.putText(image, caption, (box[0], box[1] + 20), cv2.FONT_HERSHEY_PLAIN, 1, (255, 255, 255), 1)

