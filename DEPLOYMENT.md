# Deployment Guide

This guide covers various deployment strategies for the User Management Service.

## Table of Contents

1. [Docker Deployment](#docker-deployment)
2. [Kubernetes Deployment](#kubernetes-deployment)
3. [Cloud Deployment (AWS)](#cloud-deployment-aws)
4. [Production Checklist](#production-checklist)

## Docker Deployment

### Building the Image

```bash
# Build the Docker image
docker build -t user-management-service:1.0.0 .

# Tag for registry
docker tag user-management-service:1.0.0 your-registry/user-management-service:1.0.0

# Push to registry
docker push your-registry/user-management-service:1.0.0
```

### Running with Docker Compose

**Production docker-compose.yml:**

```yaml
version: '3.8'

services:
  user-management-service:
    image: user-management-service:1.0.0
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: prod
      DB_HOST: postgres
      DB_PASSWORD: ${DB_PASSWORD}
      JWT_SECRET: ${JWT_SECRET}
      REDIS_HOST: redis
      KAFKA_BOOTSTRAP_SERVERS: kafka:29092
    depends_on:
      - postgres
      - redis
      - kafka
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "wget", "--spider", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
```

### Deploy:

```bash
# Create .env file with secrets
cat > .env << EOF
DB_PASSWORD=your-secure-password
JWT_SECRET=your-256-bit-secret-key
EOF

# Start services
docker-compose -f docker-compose.prod.yml up -d

# View logs
docker-compose logs -f user-management-service

# Scale service
docker-compose up -d --scale user-management-service=3
```

## Kubernetes Deployment

### Prerequisites

- Kubernetes cluster (1.20+)
- kubectl configured
- Helm 3 (optional)

### 1. Create Namespace

```bash
kubectl create namespace user-management
```

### 2. Create Secrets

```bash
# Database credentials
kubectl create secret generic db-credentials \
  --from-literal=username=postgres \
  --from-literal=password=your-secure-password \
  -n user-management

# JWT secret
kubectl create secret generic jwt-secret \
  --from-literal=secret=your-256-bit-secret-key \
  -n user-management
```

### 3. ConfigMap

**configmap.yaml:**

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: app-config
  namespace: user-management
data:
  SPRING_PROFILES_ACTIVE: "prod"
  DB_HOST: "postgres-service"
  DB_PORT: "5432"
  DB_NAME: "user_management"
  REDIS_HOST: "redis-service"
  REDIS_PORT: "6379"
  KAFKA_BOOTSTRAP_SERVERS: "kafka-service:9092"
```

### 4. Deployment

**deployment.yaml:**

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: user-management-service
  namespace: user-management
spec:
  replicas: 3
  selector:
    matchLabels:
      app: user-management-service
  template:
    metadata:
      labels:
        app: user-management-service
    spec:
      containers:
      - name: user-management-service
        image: your-registry/user-management-service:1.0.0
        ports:
        - containerPort: 8080
        env:
        - name: DB_USERNAME
          valueFrom:
            secretKeyRef:
              name: db-credentials
              key: username
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: db-credentials
              key: password
        - name: JWT_SECRET
          valueFrom:
            secretKeyRef:
              name: jwt-secret
              key: secret
        envFrom:
        - configMapRef:
            name: app-config
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 5
```

### 5. Service

**service.yaml:**

```yaml
apiVersion: v1
kind: Service
metadata:
  name: user-management-service
  namespace: user-management
spec:
  selector:
    app: user-management-service
  ports:
  - protocol: TCP
    port: 80
    targetPort: 8080
  type: LoadBalancer
```

### 6. Ingress (Optional)

**ingress.yaml:**

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: user-management-ingress
  namespace: user-management
  annotations:
    cert-manager.io/cluster-issuer: "letsencrypt-prod"
    nginx.ingress.kubernetes.io/ssl-redirect: "true"
spec:
  ingressClassName: nginx
  tls:
  - hosts:
    - api.yourdomain.com
    secretName: user-management-tls
  rules:
  - host: api.yourdomain.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: user-management-service
            port:
              number: 80
```

### Deploy to Kubernetes

```bash
# Apply all configurations
kubectl apply -f configmap.yaml
kubectl apply -f deployment.yaml
kubectl apply -f service.yaml
kubectl apply -f ingress.yaml

# Check deployment status
kubectl get pods -n user-management
kubectl get services -n user-management

# View logs
kubectl logs -f deployment/user-management-service -n user-management

# Scale deployment
kubectl scale deployment user-management-service --replicas=5 -n user-management
```

## Cloud Deployment (AWS)

### AWS ECS Deployment

**1. Create ECR Repository:**

```bash
aws ecr create-repository --repository-name user-management-service
```

**2. Push Image:**

```bash
# Login to ECR
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin YOUR_ACCOUNT.dkr.ecr.us-east-1.amazonaws.com

# Build and push
docker build -t user-management-service:1.0.0 .
docker tag user-management-service:1.0.0 YOUR_ACCOUNT.dkr.ecr.us-east-1.amazonaws.com/user-management-service:1.0.0
docker push YOUR_ACCOUNT.dkr.ecr.us-east-1.amazonaws.com/user-management-service:1.0.0
```

**3. Create Task Definition:**

```json
{
  "family": "user-management-service",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "1024",
  "memory": "2048",
  "containerDefinitions": [
    {
      "name": "user-management-service",
      "image": "YOUR_ACCOUNT.dkr.ecr.us-east-1.amazonaws.com/user-management-service:1.0.0",
      "portMappings": [
        {
          "containerPort": 8080,
          "protocol": "tcp"
        }
      ],
      "environment": [
        {
          "name": "SPRING_PROFILES_ACTIVE",
          "value": "prod"
        }
      ],
      "secrets": [
        {
          "name": "DB_PASSWORD",
          "valueFrom": "arn:aws:secretsmanager:region:account:secret:db-password"
        },
        {
          "name": "JWT_SECRET",
          "valueFrom": "arn:aws:secretsmanager:region:account:secret:jwt-secret"
        }
      ],
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "/ecs/user-management-service",
          "awslogs-region": "us-east-1",
          "awslogs-stream-prefix": "ecs"
        }
      }
    }
  ]
}
```

**4. Create ECS Service:**

```bash
aws ecs create-service \
  --cluster your-cluster \
  --service-name user-management-service \
  --task-definition user-management-service:1 \
  --desired-count 3 \
  --launch-type FARGATE \
  --network-configuration "awsvpcConfiguration={subnets=[subnet-12345,subnet-67890],securityGroups=[sg-12345],assignPublicIp=ENABLED}" \
  --load-balancers "targetGroupArn=arn:aws:elasticloadbalancing:region:account:targetgroup/user-management/abc123,containerName=user-management-service,containerPort=8080"
```

### AWS RDS (PostgreSQL)

```bash
# Create RDS instance
aws rds create-db-instance \
  --db-instance-identifier user-management-db \
  --db-instance-class db.t3.medium \
  --engine postgres \
  --engine-version 16.1 \
  --master-username admin \
  --master-user-password YOUR_PASSWORD \
  --allocated-storage 100 \
  --vpc-security-group-ids sg-12345 \
  --db-subnet-group-name your-subnet-group \
  --backup-retention-period 7 \
  --multi-az
```

### AWS ElastiCache (Redis)

```bash
# Create Redis cluster
aws elasticache create-cache-cluster \
  --cache-cluster-id user-management-redis \
  --cache-node-type cache.t3.medium \
  --engine redis \
  --engine-version 7.0 \
  --num-cache-nodes 1 \
  --cache-subnet-group-name your-subnet-group \
  --security-group-ids sg-12345
```

## Production Checklist

### Security

- [ ] Change default JWT secret to a strong, random value
- [ ] Enable HTTPS/TLS with valid certificates
- [ ] Configure firewall rules to restrict access
- [ ] Use AWS Secrets Manager or similar for sensitive data
- [ ] Enable database encryption at rest
- [ ] Configure SSL/TLS for database connections
- [ ] Implement API rate limiting
- [ ] Set up WAF (Web Application Firewall)
- [ ] Enable audit logging
- [ ] Configure CORS appropriately

### Database

- [ ] Set up automated backups
- [ ] Configure replication for high availability
- [ ] Optimize connection pool settings
- [ ] Set up database monitoring
- [ ] Plan for database migrations
- [ ] Test disaster recovery procedures

### Monitoring

- [ ] Configure application metrics export
- [ ] Set up Prometheus and Grafana
- [ ] Configure alerting (PagerDuty, Slack, etc.)
- [ ] Enable distributed tracing (Jaeger, Zipkin)
- [ ] Set up log aggregation (ELK Stack, CloudWatch)
- [ ] Configure health checks
- [ ] Monitor resource usage (CPU, memory, disk)

### Performance

- [ ] Configure appropriate JVM settings
- [ ] Enable Redis caching
- [ ] Optimize database queries
- [ ] Set up CDN for static assets
- [ ] Configure connection pooling
- [ ] Enable HTTP compression
- [ ] Implement request/response caching

### High Availability

- [ ] Deploy multiple instances (minimum 3)
- [ ] Configure load balancer
- [ ] Set up auto-scaling policies
- [ ] Configure health checks
- [ ] Plan for zero-downtime deployments
- [ ] Set up circuit breakers

### Compliance

- [ ] GDPR compliance (if applicable)
- [ ] Data retention policies
- [ ] Privacy policy implementation
- [ ] Terms of service
- [ ] Cookie consent

### Documentation

- [ ] API documentation up to date
- [ ] Runbooks for common operations
- [ ] Disaster recovery procedures
- [ ] Incident response plan
- [ ] Architecture diagrams

## Rolling Updates

### Docker Compose

```bash
# Build new version
docker build -t user-management-service:1.0.1 .

# Update docker-compose.yml with new version

# Rolling update
docker-compose up -d --no-deps --build user-management-service
```

### Kubernetes

```bash
# Update image
kubectl set image deployment/user-management-service \
  user-management-service=your-registry/user-management-service:1.0.1 \
  -n user-management

# Check rollout status
kubectl rollout status deployment/user-management-service -n user-management

# Rollback if needed
kubectl rollout undo deployment/user-management-service -n user-management
```

## Troubleshooting

### Check Logs

```bash
# Docker
docker logs user-management-service

# Kubernetes
kubectl logs -f deployment/user-management-service -n user-management

# AWS ECS
aws logs tail /ecs/user-management-service --follow
```

### Common Issues

**Service won't start:**
- Check database connectivity
- Verify environment variables
- Review application logs

**High memory usage:**
- Adjust JVM heap settings
- Check for memory leaks
- Review connection pool sizes

**Slow response times:**
- Check database query performance
- Verify Redis cache is working
- Review application metrics
