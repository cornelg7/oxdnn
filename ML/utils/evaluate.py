"""
Adapted from https://github.com/fizyr/keras-retinanet/blob/master/examples/ResNet50RetinaNet.ipynb
"""
import numpy as np
import cv2

from keras_retinanet.utils.image import read_image_bgr, preprocess_image, \
    resize_image
from keras_retinanet.utils.visualization import draw_box, draw_caption

from .display import label_colour

labels_to_classes = {
    0: 'Disabled Parking Sign',
    1: 'Disabled Parking Space',
    2: 'Ramp without Railing',
    3: 'Chair Lift',
    4: 'Ramp with Railing'
}

THRESHOLD = 0.5


def evaluate(file_path, model, return_image=False, debug=False):
    """
    Keras-RetinaNet evaluation adapted from github.com/fizyr/keras-retinanet
    :param model: loaded pretrained model. We ideally load this only once
    :param file_path: path to temp file
    :param return_image: Whether to return annotated image
    :param debug: Whether to return classification confidence scores
    :return:
    """
    image = read_image_bgr(file_path)

    if return_image:
        draw = image.copy()
        draw = cv2.cvtColor(draw, cv2.COLOR_BGR2RGB)

    image = preprocess_image(image)
    image, scale = resize_image(image)

    _, _, boxes, nms_class = model.predict_on_batch(
        np.expand_dims(image, axis=0)
    )

    predicted_labels = np.argmax(nms_class[0, :, :], axis=1)

    scores = nms_class[
        0,
        np.arange(nms_class.shape[1]),
        predicted_labels
    ]

    boxes /= scale

    classes_found = []
    for i in np.where(scores > THRESHOLD)[0]:
        label = labels_to_classes[predicted_labels[i]]
        score = scores[i]

        classes_found.append(label)

        # TODO: write own version with better presentation
        if return_image:
            colour = label_colour(predicted_labels[i])
            b = boxes[0, i, :].astype(int)

            if debug:
                caption = "{} {:.3f}".format(label, score)
            else:
                caption = "{}".format(label)

            draw_box(draw, b, color=colour)
            draw_caption(draw, b, caption)

    return {
        'classes': classes_found,
        'image': draw if return_image else None
    }



