# 📊 ELK Stack Integration

I have successfully added the **ELK Stack (Elasticsearch, Logstash, Kibana)** to your project for centralized logging.

## 🚀 How to Access

| Service | URL | Credentials |
| :--- | :--- | :--- |
| **Kibana Dashboard** | http://localhost:5601 | None (Open) |
| **Elasticsearch** | http://localhost:9200 | None |
| **Logstash** | `localhost:5000` (TCP) | N/A |

## ⚙️ Configuration Details

### 1. Docker Services
Added 3 new containers to `docker-compose.yml`:
*   `elasticsearch`: Stores the logs.
*   `logstash`: Receives logs from microservices and sends them to Elasticsearch.
*   `kibana`: Visualizes the logs.

### 2. Order Service Integration
*   Added `logstash-logback-encoder` dependency.
*   Configured `logback-spring.xml` to ship logs via TCP to `logstash:5000`.

## 🔍 How to View Logs in Kibana

I have **automatically configured** Kibana for you!

1.  **Simply click here:** 👉 **[http://localhost:5601/app/discover](http://localhost:5601/app/discover)**
2.  You should see your `microservices-logs-*` already selected and logs appearing.
3.  If you don't see logs immediately, ensure the time filter (top right) is set to **"Last 15 minutes"** or **"Today"**.

## ⚠️ Resource Warning
The ELK stack is resource-intensive. If your computer runs slow or you run out of disk space, you can stop these specific services:

```bash
docker stop elasticsearch logstash kibana
```
