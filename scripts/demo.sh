# scripts/demo.sh dosyası oluştur
cat > demo.sh << 'EOF'
#!/bin/bash
echo "🎯 Inspection API Demo"
echo "====================="

BASE_URL="http://localhost:8080"

echo "1. Health Check:"
curl -s $BASE_URL/actuator/health | jq '.' || curl -s $BASE_URL/actuator/health
echo ""

echo "2. Metrics:"
curl -s $BASE_URL/actuator/metrics/http.server.requests | head -10
echo ""

echo "3. API Test:"
curl -s $BASE_URL/api/inspections/read/ABC123
echo ""
EOF

chmod +x demo.sh