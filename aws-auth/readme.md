### To configure authentication on the cluster, edit the `aws-auth` ConfigMap as follows:

```bash
kubectl edit cm aws-auth -n kube-system
```

### Then, you will need to add the Federated Users role, and if needed, any service accounts as well. It should look something like this:

```bash
apiVersion: v1
data:
    mapRoles:
      - groups:
        - system:bootstrappers
        - system:nodes
        rolearn: arn:aws:iam::<account﹥:role/eks-node-group-general
        username: system:node:{{EC2PrivateDNSName}}
      - groups:
        - system:masters
        rolearn: arn:aws:iam::<account﹥:role/FederatedAdministratorRole
        username: FederatedAdministratorRole
    mapUsers: | 
      - groups:
        - system:masters
        userarn: arn:aws:iam::<account﹥:user/ci-system-user
kind: ConfigMap
...
```