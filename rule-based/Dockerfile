FROM tomcat:9.0.40-jdk11

COPY ./target/rule-based-refactorer.war /usr/local/tomcat/webapps/rule-based-refactorer.war
EXPOSE 8080
ENV prometheus  http://prometheus:9090
ENV xopera  http://xopera-nginx:5000
ENV refacopdiscoverer  http://refactoring_option_discoverer:8080
ENV perfpredictor  http://fo_perf_predictor_api:5000
ENV forecaster  http://forecast-api:5000
ENV reasoner  http://reasoner-api:5000/reasoner-api/v0.6/
ENV authapi  http://authapi:5000
ENV iacbuilder  http://iacbuilder-api:5000
ENV username  indika
ENV client_id  client_id
ENV client_secret  client_secret
ENV apikey  test

# Add docker-compose-wait tool -------------------
ENV WAIT_VERSION 2.7.3
ADD https://github.com/ufoscout/docker-compose-wait/releases/download/2.7.3/wait /wait
RUN chmod +x /wait

CMD ["catalina.sh","run"]
