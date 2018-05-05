import os
import zerorpc
import keras
import logging
import matplotlib.pyplot as plt

from keras_retinanet import models

from .utils.evaluate import evaluate

logger = logging.getLogger(__name__)

class EvaluationRPC(object):
    def __init__(self):
        model_path = os.path.join(
            '..', 'trained_snapshots', 'resnet50_csv_02.h5'
        )
        self.model = models.load_model(
            model_path,
            backbone_name='resnet50'
        )
        logger.info('Loaded model successfully')

    def evaluate(self, filename):
        image_path = os.path.join(
            '..', 'temp', filename
        )
        logger.info('Received evaluation request for {}'.format(image_path))

        evaluation = evaluate(
            image_path,
            self.model,
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
