# Platform Stack
Platform stack utilized in different projects.

### This repository will provide useful kubernetes tools consumed by ArgoCD (*not all of them*) in desired state.
___

#### ArgoCD apps file structure

```bash
/app-name/*
```
Here there will be files like readme, json policies, argocd-app.yaml, etc.
___
```bash
/app-name/argocd-app.yaml
```
This file will be applied only one time in the cluster to create the app with the manifest resources.
___
```bash
/app-name/argocd-files/*.yaml
```
Here will be the desired kubernetes manifest files which ArgoCD will apply inside the cluster.
