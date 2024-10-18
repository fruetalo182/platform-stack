## Source: https://kubernetes-sigs.github.io/aws-load-balancer-controller/v2.5/deploy/installation/

## 1. Download policy

```bash
curl -o iam-policy.json https://raw.githubusercontent.com/kubernetes-sigs/aws-load-balancer-controller/v2.5.1/docs/install/iam_policy.json
```

## 2. Create policy

```bash
aws iam create-policy \
    --policy-name AWSLoadBalancerControllerIAMPolicy \
    --policy-document file://iam-policy.json \
    --profile <profile>
```

## 3. Create SA in EKS attached to the policy

```bash
eksctl create iamserviceaccount \
--cluster=cluster-name \
--namespace=kube-system \
--name=aws-load-balancer-controller \
--attach-policy-arn=arn:aws:iam::account-number:policy/AWSLoadBalancerControllerIAMPolicy \
--override-existing-serviceaccounts \
--region us-region-1 \
--approve \
--profile <profile>
```

## 4. Install cert-manager | Do not install using ArgoCD

```bash
kubectl apply --validate=false -f https://github.com/jetstack/cert-manager/releases/download/v1.5.4/cert-manager.yaml
```

## 5. Create Argo CD App

```bash
kubectl apply -f lb-controller-argoapp.yaml
```

## 6. Install ingress-class | Do not install using ArgoCD

```bash
kubectl apply --validate=false -f https://github.com/kubernetes-sigs/aws-load-balancer-controller/releases/download/v2.5.1/v2_5_1_ingclass.yaml
```

## 7. Configure the following tags in the subnets used by the cluster (eks only shows the private ones, you must search for the public ones in VPC/Subnets manually)

```bash
kubernetes.io/cluster/<cluster-name>	shared
```

### Private subnets

```bash
kubernetes.io/role/internal-elb     1
```

### Public subnets

```bash
kubernetes.io/role/elb      1
```

## 8. Set the following tag to the VPC cluster

```bash
kubernetes.io/cluster/<cluster-name>	shared
```

### Tip: The `Service` of each app must be `NodePort`
