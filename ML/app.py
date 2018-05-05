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
    '~', 'oxdnn', 'ML', 'trained_snapshots', 'resnet50_csv_02.h5'
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
            '~', 'oxdnn', 'temp'
        ))

        logger.info('Received evaluation request for {}'.format(image_path+filename))

        evaluation = evaluate(
            os.path.join(image_path, filename),
            model,
            return_image=annotate_image
        )

        outpath = os.path.join(image_path, 'annotated_'+filename),
        plt.imsave(
            outpath,
            evaluation['image']
        )

        logger.info('Saved annotated image at {}'.format(outpath))

        return {
            'outimage': 'annotated_'+filename,
            'classes': evaluation['classes']
        }


server = zerorpc.Server(EvaluationRPC())
server.bind('tcp://*:44224')
server.run()
