# Using python:3.7-slim-buster image
FROM python:3.7-slim-buster
# Defining working directory and copy the requirements file
WORKDIR /usr/src/myapp
RUN apt-get update
RUN apt-get -y install gcc
RUN apt-get -y install g++
RUN pip install numpy==1.18.3
RUN pip install pandas
RUN pip install convertdate
RUN pip install lunarcalendar
RUN pip install holidays
RUN pip install tqdm
RUN pip install pystan==2.18
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt
COPY . .
EXPOSE 5000
CMD ["python3","app.py"]