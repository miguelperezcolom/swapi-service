# Proxy protocol on the cluster's ingress — prepared, NOT applied

The rate limits in `deploy/20-ingress.yaml` are keyed on the client address, and today nginx sees
**one address for everybody**: `10.0.0.3`, the Hetzner load balancer, which proxies TCP and so
replaces the source. `externalTrafficPolicy: Local` does not help — the LB is a proxy, not a NAT.
The only way the real address reaches nginx is proxy protocol, on both ends at once.

This is a change to the **whole cluster**: every ingress behind `ingress-nginx` goes through it.

## The change

1. The load balancer starts sending the PROXY header — a CFKE annotation on the controller's Service.
2. nginx starts expecting it — `use-proxy-protocol` in the controller's ConfigMap.

Both through the Helm release, or the next `helm upgrade` reverts them:

```bash
helm -n ingress-nginx upgrade ingress-nginx ingress-nginx/ingress-nginx --version 4.15.1 \
  --reuse-values -f deploy/cluster/ingress-nginx-values.yaml
```

Between 1 and 2 taking effect, **all** traffic fails: nginx rejects a PROXY header it does not
expect, and expects one the LB is not sending yet. One `helm upgrade` applies both, but the LB is
reconfigured by CFKE asynchronously, so expect a short outage of every host on the cluster.

## The blocker: in-cluster traffic to the public hostnames

The controller Service's status carries the LB address with `ipMode: VIP`. With that, kube-proxy
short-circuits any connection **from inside the cluster** to `135.181.84.98` straight to nginx,
without going through the LB — so without a PROXY header — and once nginx requires one, those
connections fail.

Workloads in the cluster do call the public hostnames today (found in Deployments/ConfigMaps):
`https://auth.ec1.mateu.io/realms/ec-demo1` (an OIDC issuer: token validation fetches its JWKS),
`console.`, `front.`, `rw-console.` and `grafana.ec1.mateu.io`. cert-manager's HTTP-01 self-check
does the same on every issuance and renewal.

So turning this on as it stands breaks the ec-demo1 platform's login and certificate renewals. One
of these has to happen first:

- **The LB status reports `ipMode: Proxy`** (Kubernetes ≥ 1.30), so kube-proxy sends in-cluster
  traffic out through the LB too. That status is written by CFKE, not by us: ask Cloudfleet whether
  their Hetzner integration can set it (the upstream Hetzner CCM has
  `load-balancer.hetzner.cloud/ipmode-proxy` for exactly this; CFKE documents no equivalent).
- **Or every in-cluster caller uses the internal Service address** instead of the public host. For
  the OIDC issuer that is not just a URL swap: the `iss` in the tokens is the public URL, so the
  services need the issuer and the JWKS endpoint configured separately.

Until one of those is true, this file is a plan, not a step.
