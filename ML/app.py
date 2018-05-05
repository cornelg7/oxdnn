import os
import zerorpc
import keras
import logging
import matplotlib
matplotlib.use('TkAgg')
import matplotlib.pyplot as plt

from keras_retinanet.models.resnet import custom_objects

from utils.evaluate import evaluate

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

model_path = os.path.join(
    '.', 'trained_snapshots', 'resnet50_csv_02.h5'
)
model = keras.models.load_model(
    model_path,
    custom_objects=custom_objects
)

logger.info('Loaded model successfully')        

class EvaluationRPC(object):
    def __init__(self):
        logger.info('Started Python evaluation server')

    def evaluate(self, filename):
        image_path = os.path.join(
            '..', 'temp', filename
        )
        logger.info('Received evaluation request for {}'.format(image_path))

        evaluation = evaluate(
            image_path,
            model,
            return_image=annotate_image
        )

        outpath = os.path.join('..', 'temp', 'annotated_'+filename),
        plt.imsave(
            outpath,
            evaluation['image']
        )

        logger.info('Saved annotated image at {}'.format(outpath))

        # return {
        #     'impath': outpath,
        #     'classes': evaluation['classes']
        # }

        return 'annotated_'+filename

server = zerorpc.Server(EvaluationRPC())
server.bind('tcp://*:44224')
server.run()
