from flask import Flask
from flask import jsonify
from flask import request
from flask import Response
from tensorflow import keras
import tensorflow as tf
import pandas as pd
import numpy as np
import pickle


app = Flask(__name__)


# data preprocessing

severity_levels = ['V', 'D', 'I', 'W', 'E', 'F', 'S']


def load_tokenizer():
    with open('tokenizer.pickle', 'rb') as handle:
        return pickle.load(handle)

tokenizer = load_tokenizer()

def to_df(new_lines):
    return pd.DataFrame(new_lines, columns=['log'])

def filter_out_irrelevant_lines(data):
    mask = data['log'].map(lambda line: len(line.strip()) > 0 and not line.startswith('-'))
    return data[mask]

def extract_severity(data):
    data[['severity', 'log']] = data['log'].str.split('/', expand=True, n=1)

def one_hot_encode_severity(data):
    new_columns = pd.get_dummies(data['severity'].astype(pd.CategoricalDtype(categories=severity_levels)), prefix='sev_', dummy_na=True)
    return pd.concat([data.drop('severity', axis=1), new_columns], axis=1)

def tokenize_log(data):
    bag_of_words = tokenizer.texts_to_matrix(data['log'])
    log_df = pd.DataFrame(bag_of_words, index=data.index, columns=[f'word{i}' for i in range(bag_of_words.shape[1])])
    data_without_log = data.drop(['log'], axis=1)
    return pd.concat([data_without_log, log_df], axis=1, join='inner')

def to_tensor(data):
    data_np = data.to_numpy().astype(np.float32)
    return tf.ragged.constant([data_np])


def preprocess(new_lines):
    new_lines = to_df(new_lines)
    new_lines = filter_out_irrelevant_lines(new_lines)
    extract_severity(new_lines)
    new_lines = one_hot_encode_severity(new_lines)
    new_lines = tokenize_log(new_lines)
    new_lines = to_tensor(new_lines)

    return new_lines


# model usage

def load_model():
    return keras.models.load_model('AndroidLogsClf.h5')

model = load_model()
lines = {}
lines_len = {}

def predict(pid, new_lines):
    global lines

    preprocessed = preprocess(new_lines)
    lines[pid] = tf.concat([lines[pid], preprocessed], axis=1) if pid in lines else preprocessed
    lines_len[pid] = (lines_len[pid] + len(new_lines)) if pid in lines_len else len(new_lines)
    return float(model(lines[pid], training=False).numpy()[0,0])



# HTTP handling

@app.route('/single', methods=['POST'])
def predict_single():
    global lines_len

    req = request.get_json(force=True, silent=True)

    if req is None or 'pid' not in req or 'line' not in req:
        return Response("Bad request - passed data was incompliant with API specification", 400)

    pid = req['pid']
    new_lines = [req['line']]

    score = predict(pid, new_lines)

    response = {'idx': lines_len[pid], 'score': score}
    return jsonify(response)


@app.route('/pack', methods=['POST'])
def predict_pack():
    global lines_len

    req = request.get_json(force=True, silent=True)

    if req is None or 'pid' not in req or 'lines' not in req:
        return Response("Bad request - passed data was incompliant with API specification", 400)

    pid = req['pid']
    new_lines = req['lines']

    score = predict(pid, new_lines)

    response = {'idx': lines_len[pid], 'score': score}
    return jsonify(response)
