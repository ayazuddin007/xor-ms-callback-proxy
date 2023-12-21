FROM ${xorDockerRegistry}/${msDockerBaseImage}

USER root

ADD maven /

RUN addgroup --gid ${xorUserGid} ${xorUserGroupName}
RUN adduser -D -h /home/${xorUserName} -g "" -u ${xorUserUid} -S -G ${xorUserGroupName} ${xorUserName}

RUN find /ms -exec chgrp -h ${xorUserGroupName} {} +
RUN find /ms -exec chown -h ${xorUserName} {} +

RUN [ -d ${spring-logback.filepath}/ ] || mkdir -p ${spring-logback.filepath}/
RUN find ${spring-logback.filepath} -exec chgrp -h ${xorUserGroupName} {} +
RUN find ${spring-logback.filepath} -exec chown -h ${xorUserName} {} +

WORKDIR /ms
USER ${xorUserName}

CMD umask 0002 ; java -jar /ms/${project.artifactId}.jar