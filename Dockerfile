FROM openjdk:21

WORKDIR /usrapp/bin

ENV PORT=35000

COPY /target/classes /usrapp/bin/classes
COPY /target/dependency /usrapp/bin/dependency

# Archivos estaticos
COPY src/main/resources/static /usrapp/bin/resources/static
COPY /target/classes/static /usrapp/bin/target/classes/static

CMD ["java","-cp","./classes:./dependency/*","co.edu.eci.WebApplication"]