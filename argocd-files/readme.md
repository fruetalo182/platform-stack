# Read this document before to apply the install.yaml ###

### If you are installing everything from scratch, you should download the latest install.yaml manifest to deploy ArgoCD.

### You can do it by running the following: 

```bash
cd argocd-files
curl -LO https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml
```

### Before to run the install.yaml, create the argocd namespace

```bash
kubectl create namespace argocd
kubectl apply -f install.yaml -n argocd
```

### To get the first admin password generated run:

```bash
kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 -d
```

## Customizations + Oauth

1. Inside Deployment section of argocd-server, under "- command: \n - argocd-server" definition it is a "- --insecure" flag added to avoid TLS DNS redirection problems. (In our case, the DNS provider takes care of that)

```bash
    ...
                    matchLabels:
                  app.kubernetes.io/part-of: argocd
              topologyKey: kubernetes.io/hostname
            weight: 5
      containers:
      - command:
        - argocd-server
        - --insecure
        env:
    ...
```

2. Inside ConfigMap section of argocd-cm configure the oauth app to allow Github SSO login.

```bash    
    ...
    apiVersion: v1
    data:
        dex.config: |
            connectors:
            - type: github
                id: github
                name: GitHub
                config:
                clientID: xxxxxxxxxxxxx
                clientSecret: yyyyyyyyyyyyyyyyyyy
                orgs:
                - name: fruetalo182
      url: https://argo.domain.app
    kind: ConfigMap
    metadata:
      annotations:
      ...
```      

3. Once dex.config is configured you should login as admin and in settings.projects.roles create a new role allowing access to all the wished team <org:team> of Github