#!/bin/bash

# PhonePe Payment Gateway Java Backend Setup Script
echo "🚀 Setting up PhonePe Payment Gateway Java Backend..."

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "❌ Java is not installed. Please install Java 17+ first."
    echo "Visit: https://adoptium.net/"
    exit 1
fi

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven is not installed. Please install Maven first."
    echo "Visit: https://maven.apache.org/install.html"
    exit 1
fi

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo "❌ Java 17+ is required. Current version: $JAVA_VERSION"
    exit 1
fi

echo "✅ Java version: $JAVA_VERSION"

# Create logs directory
mkdir -p logs

# Create database directory for H2 (if using local)
mkdir -p data

echo ""
echo "📋 PhonePe Configuration:"
echo "   Merchant ID: M23VNF8CE5TM4"
echo "   Merchant Name: SM9 MEDIA"
echo "   Environment: Sandbox (Development)"
echo "   Salt Key: Will be provided via environment variable"
echo ""

# Build the project
echo "🔨 Building the project..."
mvn clean compile

if [ $? -eq 0 ]; then
    echo "✅ Project built successfully!"
else
    echo "❌ Build failed. Please check the errors above."
    exit 1
fi

echo ""
echo "✅ Java Backend setup completed!"
echo ""
echo "📋 Next steps:"
echo "1. Set environment variable for salt key:"
echo "   export PHONEPE_SALT_KEY=your_actual_salt_key"
echo ""
echo "2. Start the backend server:"
echo "   mvn spring-boot:run"
echo ""
echo "3. Test the API:"
echo "   curl http://localhost:8080/api/health"
echo ""
echo "🔗 API Endpoints:"
echo "   - Health Check: http://localhost:8080/api/health"
echo "   - H2 Console: http://localhost:8080/api/h2-console"
echo "   - Subscription Setup: POST http://localhost:8080/api/phonepe/subscription/setup"
echo "   - Webhook: POST http://localhost:8080/api/webhook/phonepe"
echo ""
echo "📱 Android App Configuration:"
echo "   Update ApiService.kt BASE_URL to: http://your-domain.com/api/"
echo ""
echo "🌐 Production Deployment:"
echo "   - Deploy to AWS/GCP/Azure"
echo "   - Set up MySQL/PostgreSQL database"
echo "   - Configure HTTPS domain"
echo "   - Update environment variables"
echo ""
echo "🎉 Ready to process PhonePe payments with Java Spring Boot!"

