import os
import zerorpc
import keras
import logging
import matplotlib
# For Mac OSX compatibility when testing
matplotlib.use('TkAgg')
import matplotlib.pyplot as plt

from keras_retinanet.models.resnet import custom_objects

from utils.evaluate import evaluate

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

model_path = os.path.expanduser(os.path.join(
    '~', 'oxdnn', 'ML', 'trained_snapshots', 'resnet50_csv_10.h5'
))
model = keras.models.load_model(
    model_path,
    custom_objects=custom_objects
)

logger.info('Loaded model successfully')        

class EvaluationRPC(object):
    def __init__(self):
        logger.info('Started Python evaluation server')

    def evaluate(self, filename):
        image_path = os.path.expanduser(os.path.join(
            '~', 'oxdnn', 'temp', filename
        ))

        logger.info('Received evaluation request for {}'.format(image_path))

        evaluation = evaluate(
            image_path,
            model,
            return_image=True
        )

        plt.imsave(
            # overwrite original
            image_path,
            evaluation['image']
        )

        logger.info('Saved annotated image at {}'.format(image_path))

        return {
            'outimage': image_path,
            'classes': evaluation['classes']
        }


server = zerorpc.Server(EvaluationRPC())
server.bind('tcp://*:44224')
server.run()
