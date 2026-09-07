#!/usr/bin/env bash
# Deploys the service. Idempotent — safe to re-run. Run from the repo root: ./deploy/deploy.sh
set -euo pipefail

NS=swapi
HOST=swapi.ec1.mateu.io
cd "$(dirname "$0")/.."

kubectl apply -f deploy/00-namespace.yaml

# The write key is generated once and kept out of git. Re-running never rotates it: a rotated key
# would silently break every client that already has it.
if kubectl get secret swapi-write-key -n "$NS" >/dev/null 2>&1; then
  echo "Write key already present; leaving it alone."
else
  KEY="${WRITE_KEY:-$(head -c 24 /dev/urandom | base64 | tr -d '/+=' | head -c 32)}"
  kubectl create secret generic swapi-write-key -n "$NS" --from-literal=WRITE_KEY="$KEY"
  mkdir -p deploy/.secrets
  printf 'WRITE_KEY=%s\n' "$KEY" > deploy/.secrets/credentials.env
  echo "Generated a write key and saved it to deploy/.secrets/credentials.env"
fi

kubectl apply -f deploy/10-swapi.yaml
kubectl apply -f deploy/20-ingress.yaml
kubectl rollout status deployment/swapi -n "$NS" --timeout=5m

echo
echo "  https://$HOST/swagger-ui.html"
echo "  https://$HOST/api/people?search=sky"
echo
echo "The certificate is issued on the first request and can take a minute the very first time."
