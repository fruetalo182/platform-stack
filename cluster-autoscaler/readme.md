### Always use the `install.yaml` from this repository to install the cluster-autoscaler, it already has the recommended additions by AWS. https://docs.aws.amazon.com/eks/latest/userguide/autoscaling.html


### If you didn't use eksctl, you must manually tag your Auto Scaling groups with the following tags.
```bash
Key	                                    Value
k8s.io/cluster-autoscaler/my-cluster	owned
k8s.io/cluster-autoscaler/enabled	    true
```

## 1. Create policy

```bash
aws iam create-policy --policy-name AmazonEKSClusterAutoscalerPolicy-eks-prod --policy-document file://cluster-autoscaler/iam-policy.json
```

## 2. Create role

```bash
aws iam create-role --role-name AmazonEKSClusterAutoscalerRole-eks-prod --assume-role-policy-document file://cluster-autoscaler/iam-role.json
```

## 3. Assign role `AmazonEKSClusterAutoscalerRole-eks-prod` to the AmazonEKSClusterAutoscalerPolicy

## 4. Assign role to the `cluster-autoscaler` serviceaccount

```bash
kubectl annotate serviceaccount cluster-autoscaler -n kube-system eks.amazonaws.com/role-arn=arn:aws:iam::account-number:role/AmazonEKSClusterAutoscalerRole-cluster-name
```

### Autoscaler takes 10 minutes to confirm if a node has no usage before removing it.
### Autoscaler constantly calculates and changes the distribution of nodes based on workloads, that's why it can change the desired number of nodes in each nodeGroup depending on the load.