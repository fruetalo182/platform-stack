### Source: https://ptuladhar3.medium.com/getting-started-with-external-secrets-operator-on-kubernetes-using-aws-secrets-manager-6dc403d9630c

## 1. External-Secrets installation

```bash
helm repo add external-secrets https://charts.external-secrets.io
helm repo update
kubectl create namespace external-secrets
helm upgrade --namespace external-secrets --install --wait external-secrets external-secrets/external-secrets
```

## 2. New IAM user, keys and default secret creation

```bash
aws iam create-user --user-name external-secrets
aws iam attach-user-policy --user-name external-secrets --policy-arn arn:aws:iam::aws:policy/SecretsManagerReadWrite
aws iam create-access-key --user-name external-secrets
echo -n "GENERATED_ACCESS_KEY_ID" > access-key
echo -n "GENERATED_SECRET_ACCESS_KEY" > secret-access-key
kubectl -n kube-system create secret generic awssm-secret --from-file=./access-key --from-file=./secret-access-key
```

## 3. Cluster-scoped secret store creation

```bash
cat > cluster-secret-store.yaml <<EOF
apiVersion: external-secrets.io/v1beta1
kind: ClusterSecretStore
metadata:
  name: global-secret-store
spec:
  provider:
    aws:
      service: SecretsManager
      region: us-region-1
      auth:
        secretRef:
          accessKeyIDSecretRef:
            name: awssm-secret
            key: access-key
            namespace: kube-system
          secretAccessKeySecretRef:
            name: awssm-secret
            key: secret-access-key
            namespace: kube-system
EOF
```

## 4. Apply the resource

```bash
kubectl apply -f cluster-secret-store.yaml
```

## 5. New test secret creation

```bash
aws secretsmanager create-secret --name app-secret --secret-string '{"username":"bob","password":"abc123xyz456"}' --region us-region-1
```

## 6. New test app namespace creation

```bash
kubectl create namespace test-app
```

## 7. New test external secret creation

```bash
cat > app-secret.yaml <<EOF
apiVersion: external-secrets.io/v1beta1
kind: ExternalSecret
metadata:
  name: app-secret
spec:
  refreshInterval: 1m
  secretStoreRef:
    name: global-secret-store
    kind: ClusterSecretStore
  target:
    name: app-secret
    creationPolicy: Owner
  dataFrom:
  - extract:
      key: app-secret
EOF
```

## 8. Apply external secret

```bash
kubectl -n test-app apply -f app-secret.yaml
```

## 9. New test pod creation, executin "env" commando wich will have the secrets defined

```bash
cat > app-pod.yaml <<EOF
apiVersion: v1
kind: Pod
metadata:
  name: app-pod
spec:
  containers:
    - name: app
      image: k8s.gcr.io/busybox
      command: [ "/bin/sh", "-c", "env" ]
      envFrom:
      - secretRef:
          name: app-secret
EOF
```

## 10. Apply pod

```bash
kubectl -n test-app apply -f app-pod.yaml
```

## 11. Verify pod logs

```bash
kubectl -n test-app logs app-pod | egrep 'username|password'
```