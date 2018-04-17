from flask import Flask, jsonify, make_response, request
import os
import uuid

from .utils.evaluate import evaluate

app = Flask(__name__)


@app.errorhandler(404)
def not_found(error):
    return make_response(jsonify({'error': 'Not found'}), 404)


@app.route('/api/evaluate/classes', methods=['POST'])
def evaluate_classes():
    file_upload = request.files['picture']
    if file_upload:
        temp_file_path = os.path.join('./Temp', str(uuid.uuid4()) + '.jpg')
        file_upload.save(temp_file_path)
        app.logger.debug('File is saved as %s', temp_file_path)

        classes = evaluate(temp_file_path)['classes']
        return jsonify(
            tags=[e[0].serialize() for e in classes],
            scores=[e[1].serialize() for e in classes]
        )