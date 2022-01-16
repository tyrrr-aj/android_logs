FROM tensorflow/tensorflow:latest-gpu

RUN pip3 install Flask
RUN pip3 install numpy pandas
ENV FLASK_APP=/app/android_log_clf_server.py

RUN mkdir /app
COPY ./android_log_clf_server.py /app/
COPY ./AndroidLogsClf.h5 /app/
COPY ./tokenizer.pickle /app/

WORKDIR /app/

CMD ["flask", "run", "--host=0.0.0.0"]
