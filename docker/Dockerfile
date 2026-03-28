FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /workspace

RUN mkdir -p /root/.m2 && \
    printf '%s\n' \
      '<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"' \
      '          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"' \
      '          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 https://maven.apache.org/xsd/settings-1.0.0.xsd">' \
      '  <mirrors>' \
      '    <mirror>' \
      '      <id>aliyunmaven</id>' \
      '      <mirrorOf>*</mirrorOf>' \
      '      <name>Aliyun Maven</name>' \
      '      <url>https://maven.aliyun.com/repository/public</url>' \
      '    </mirror>' \
      '  </mirrors>' \
      '</settings>' \
    > /root/.m2/settings.xml

COPY back/pom.xml back/pom.xml
RUN mvn -f back/pom.xml -DskipTests dependency:go-offline

COPY back back
RUN mvn -f back/pom.xml -DskipTests package


FROM rockylinux:9 AS runtime

WORKDIR /app

RUN sed -e 's|^mirrorlist=|#mirrorlist=|g' \
        -e 's|^#baseurl=http://dl.rockylinux.org/$contentdir|baseurl=https://mirrors.aliyun.com/rockylinux|g' \
        -i.bak \
        /etc/yum.repos.d/rocky-*.repo && \
    dnf makecache

RUN dnf install -y java-21-openjdk-headless python3 python3-pip mesa-libGL && \
    dnf clean all

ENV PIP_DISABLE_PIP_VERSION_CHECK=1
ENV PIP_NO_CACHE_DIR=1
ENV PIP_DEFAULT_TIMEOUT=120
RUN python3 -m pip config set global.index-url https://mirrors.aliyun.com/pypi/simple/

RUN python3 -m pip install --no-cache-dir --retries 10 --prefer-binary \
    --index-url https://download.pytorch.org/whl/cpu \
    --extra-index-url https://mirrors.aliyun.com/pypi/simple/ \
    torch torchvision

COPY --from=build /workspace/back/src/main/resources/scripts/requirements.txt /app/requirements.txt
RUN python3 - <<'PY'
from pathlib import Path

src = Path("/app/requirements.txt").read_text(encoding="utf-8").splitlines()
filtered = []
for line in src:
    s = line.strip()
    if not s or s.startswith("#"):
        continue
    if s.startswith("torch") or s.startswith("torchvision"):
        continue
    filtered.append(s)

Path("/app/requirements-runtime.txt").write_text("\n".join(filtered) + "\n", encoding="utf-8")
PY
RUN python3 -m pip install --no-cache-dir --retries 10 -r /app/requirements-runtime.txt

COPY --from=build /workspace/back/target/*.jar /app/app.jar

COPY --from=build /workspace/back/models /app/models

RUN mkdir -p /app/target/classes/scripts
COPY --from=build /workspace/back/src/main/resources/scripts/detect.py /app/target/classes/scripts/detect.py
COPY --from=build /workspace/back/src/main/resources/scripts/fusion_predict.py /app/target/classes/scripts/fusion_predict.py

RUN mkdir -p /app/uploads

ENV PYTHONIOENCODING=utf-8
ENV PYTHONUTF8=1

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
