#!/bin/sh
set -eu

NACOS_ADDR="${NACOS_ADDR:-http://nacos:8848}"
NACOS_USERNAME="${NACOS_USERNAME:-nacos}"
NACOS_PASSWORD="${NACOS_PASSWORD:-nacos}"
NACOS_GROUP="${NACOS_GROUP:-DEFAULT_GROUP}"
NACOS_NAMESPACE="${NACOS_NAMESPACE:-}"

echo "[nacos-init] waiting for nacos: ${NACOS_ADDR}"
for i in $(seq 1 60); do
  if curl -fsS "${NACOS_ADDR}/nacos/actuator/health" >/dev/null 2>&1; then
    echo "[nacos-init] nacos is healthy"
    break
  fi
  if [ "${i}" -eq 60 ]; then
    echo "[nacos-init] nacos is not ready after 60 attempts"
    exit 1
  fi
  sleep 2
done

publish_config() {
  data_id="$1"
  config_file="$2"
  echo "[nacos-init] publishing ${data_id}"
  curl -fsS -X POST "${NACOS_ADDR}/nacos/v1/cs/configs" \
    --data-urlencode "dataId=${data_id}" \
    --data-urlencode "group=${NACOS_GROUP}" \
    --data-urlencode "tenant=${NACOS_NAMESPACE}" \
    --data-urlencode "type=yaml" \
    --data-urlencode "username=${NACOS_USERNAME}" \
    --data-urlencode "password=${NACOS_PASSWORD}" \
    --data-urlencode "content@${config_file}" >/dev/null
}

publish_config "edu-admin" "/configs/edu-admin.yaml"
publish_config "edu-business" "/configs/edu-business.yaml"

echo "[nacos-init] done"
