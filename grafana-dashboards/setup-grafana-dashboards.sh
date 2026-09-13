#!/bin/bash

# Comprehensive script to set up Grafana dashboards for FTC Alignment PD Tuning
# This script will:
# 1. Wait for Grafana to be ready
# 2. Configure InfluxDB datasource
# 3. Import both basic and advanced dashboards
# 4. Set up proper permissions and configurations

GRAFANA_URL="http://localhost:3000"
GRAFANA_USER="admin"
GRAFANA_PASSWORD="admin"

# Determine InfluxDB URL based on environment
# When running in Docker containers, use service name for internal communication
# When running from host, use localhost for external access
if docker-compose ps | grep -q "ftc-grafana.*Up" && docker-compose ps | grep -q "ftc-influxdb.*Up"; then
    # Running in Docker - use service name for internal communication
    INFLUXDB_URL="http://influxdb:8086"
    echo -e "${BLUE}🐳 Detected Docker environment - using internal service URL${NC}"
else
    # Running from host - use localhost for external access
    INFLUXDB_URL="http://localhost:8086"
    echo -e "${BLUE}🖥️  Detected host environment - using localhost URL${NC}"
fi

INFLUXDB_DATABASE="ftc_robot_data"
INFLUXDB_USER="admin"
INFLUXDB_PASSWORD="admin123"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🚀 Setting up FTC Alignment PD Tuning Dashboards${NC}"
echo "=================================================="

# Function to check if Grafana is ready
wait_for_grafana() {
    echo -e "${YELLOW}⏳ Waiting for Grafana to be ready...${NC}"
    local max_attempts=30
    local attempt=1
    
    while [ $attempt -le $max_attempts ]; do
        if curl -s "$GRAFANA_URL/api/health" > /dev/null 2>&1; then
            echo -e "${GREEN}✅ Grafana is ready!${NC}"
            return 0
        fi
        
        echo -e "${YELLOW}   Attempt $attempt/$max_attempts - waiting 5 seconds...${NC}"
        sleep 5
        ((attempt++))
    done
    
    echo -e "${RED}❌ Grafana failed to start within expected time${NC}"
    return 1
}

# Function to check if InfluxDB is ready
wait_for_influxdb() {
    echo -e "${YELLOW}⏳ Waiting for InfluxDB to be ready...${NC}"
    local max_attempts=20
    local attempt=1
    
    # For Docker environments, we need to check from the host perspective
    # Use localhost for health checks since we're running the script from the host
    local health_check_url="http://localhost:8086"
    
    while [ $attempt -le $max_attempts ]; do
        if curl -s "$health_check_url/ping" > /dev/null 2>&1; then
            echo -e "${GREEN}✅ InfluxDB is ready!${NC}"
            return 0
        fi
        
        echo -e "${YELLOW}   Attempt $attempt/$max_attempts - waiting 3 seconds...${NC}"
        sleep 3
        ((attempt++))
    done
    
    echo -e "${RED}❌ InfluxDB failed to start within expected time${NC}"
    return 1
}

# Function to delete existing InfluxDB datasource
delete_influxdb_datasource() {
    echo -e "${YELLOW}🗑️  Checking for existing InfluxDB datasource...${NC}"
    
    local datasource_id=$(curl -s -X GET \
        -H "Authorization: Basic $(echo -n $GRAFANA_USER:$GRAFANA_PASSWORD | base64)" \
        "$GRAFANA_URL/api/datasources" | jq -r '.[] | select(.name=="InfluxDB") | .id')
    
    if [ "$datasource_id" != "null" ] && [ -n "$datasource_id" ]; then
        echo -e "${YELLOW}⚠️  Found existing InfluxDB datasource (ID: $datasource_id), deleting...${NC}"
        
        local delete_response=$(curl -s -X DELETE \
            -H "Authorization: Basic $(echo -n $GRAFANA_USER:$GRAFANA_PASSWORD | base64)" \
            "$GRAFANA_URL/api/datasources/$datasource_id")
        
        if echo "$delete_response" | grep -q '"message":"Data source deleted"'; then
            echo -e "${GREEN}✅ Existing datasource deleted successfully${NC}"
        else
            echo -e "${YELLOW}⚠️  Could not delete existing datasource, continuing anyway...${NC}"
        fi
    fi
}

# Function to create InfluxDB datasource
create_influxdb_datasource() {
    echo -e "${YELLOW}📊 Creating InfluxDB datasource...${NC}"
    
    # Delete existing datasource first to avoid conflicts
    delete_influxdb_datasource
    
    local datasource_config='{
        "name": "InfluxDB",
        "type": "influxdb",
        "url": "'$INFLUXDB_URL'",
        "database": "'$INFLUXDB_DATABASE'",
        "access": "proxy",
        "isDefault": true,
        "basicAuth": true,
        "basicAuthUser": "'$INFLUXDB_USER'",
        "secureJsonData": {
            "basicAuthPassword": "'$INFLUXDB_PASSWORD'"
        }
    }'
    
    local response=$(curl -s -X POST \
        -H "Content-Type: application/json" \
        -H "Authorization: Basic $(echo -n $GRAFANA_USER:$GRAFANA_PASSWORD | base64)" \
        -d "$datasource_config" \
        "$GRAFANA_URL/api/datasources")
    
    if echo "$response" | grep -q '"message":"Datasource added"'; then
        echo -e "${GREEN}✅ InfluxDB datasource created successfully${NC}"
        return 0
    elif echo "$response" | grep -q '"message":"data source with the same name already exists"'; then
        echo -e "${YELLOW}⚠️  InfluxDB datasource already exists${NC}"
        return 0
    elif echo "$response" | grep -q '"message":"Datasource with the same name already exists"'; then
        echo -e "${YELLOW}⚠️  InfluxDB datasource already exists${NC}"
        return 0
    else
        echo -e "${RED}❌ Failed to create InfluxDB datasource${NC}"
        echo -e "${YELLOW}Full response: $response${NC}"
        return 1
    fi
}

# Function to check if dashboard exists
check_dashboard_exists() {
    local dashboard_title="$1"
    
    # Debug output (can be removed in production)
    echo -e "${BLUE}🔍 Checking for dashboard: '$dashboard_title'${NC}" >&2
    
    local response=$(curl -s -X GET \
        -H "Authorization: Basic $(echo -n $GRAFANA_USER:$GRAFANA_PASSWORD | base64)" \
        "$GRAFANA_URL/api/search?type=dash-db")
    
    # Check if curl command succeeded
    if [ $? -ne 0 ]; then
        echo -e "${RED}❌ Failed to connect to Grafana API${NC}" >&2
        return 1
    fi
    
    # Check if the response is valid JSON
    if ! echo "$response" | jq empty 2>/dev/null; then
        echo -e "${RED}❌ Invalid JSON response from Grafana API${NC}" >&2
        echo -e "${YELLOW}Response: $response${NC}" >&2
        return 1
    fi
    
    # Check if response is an empty array
    local dashboard_count=$(echo "$response" | jq '. | length' 2>/dev/null)
    if [ "$dashboard_count" -eq 0 ]; then
        echo -e "${YELLOW}⚠️  No dashboards found in Grafana${NC}" >&2
        return 1
    fi
    
    # Check if any dashboard title contains the search term (case-insensitive)
    if echo "$response" | jq -r '.[].title' 2>/dev/null | grep -qi "$dashboard_title"; then
        echo -e "${GREEN}✅ Found matching dashboard${NC}" >&2
        return 0  # Dashboard exists
    else
        echo -e "${YELLOW}⚠️  No dashboard found matching '$dashboard_title'${NC}" >&2
        return 1  # Dashboard doesn't exist
    fi
}

# Function to ask user for confirmation
ask_user_confirmation() {
    local message="$1"
    while true; do
        echo -e "${YELLOW}$message (y/n): ${NC}"
        read -r response
        case $response in
            [Yy]* ) return 0;;
            [Nn]* ) return 1;;
            * ) echo -e "${YELLOW}Please answer yes (y) or no (n).${NC}";;
        esac
    done
}

# Function to import dashboard
import_dashboard() {
    local dashboard_file="$1"
    local dashboard_name="$2"
    
    if [ ! -f "$dashboard_file" ]; then
        echo -e "${RED}❌ Dashboard file $dashboard_file not found${NC}"
        return 1
    fi
    
    # Extract dashboard title from JSON file
    local dashboard_title=$(grep -o '"title":"[^"]*"' "$dashboard_file" | head -1 | cut -d'"' -f4)
    
    # Check if dashboard already exists
    if check_dashboard_exists "$dashboard_title"; then
        echo -e "${YELLOW}⚠️  Dashboard '$dashboard_title' already exists${NC}"
        if ask_user_confirmation "Do you want to replace it?"; then
            echo -e "${YELLOW}📥 Replacing $dashboard_name...${NC}"
        else
            echo -e "${YELLOW}⏭️  Skipping $dashboard_name${NC}"
            return 0
        fi
    else
        echo -e "${YELLOW}📥 Importing $dashboard_name...${NC}"
    fi
    
    # Create a temporary file with the properly wrapped dashboard JSON
    local temp_file=$(mktemp)
    echo '{"dashboard":' > "$temp_file"
    cat "$dashboard_file" >> "$temp_file"
    echo ',"overwrite":true}' >> "$temp_file"
    
    local response=$(curl -s -X POST \
        -H "Content-Type: application/json" \
        -H "Authorization: Basic $(echo -n $GRAFANA_USER:$GRAFANA_PASSWORD | base64)" \
        -d @"$temp_file" \
        "$GRAFANA_URL/api/dashboards/db")
    
    # Clean up temporary file
    rm -f "$temp_file"
    
    if echo "$response" | grep -q '"status":"success"'; then
        echo -e "${GREEN}✅ $dashboard_name imported successfully${NC}"
        return 0
    else
        echo -e "${RED}❌ Failed to import $dashboard_name${NC}"
        echo -e "${YELLOW}Response: $response${NC}"
        return 1
    fi
}

# Function to create database if it doesn't exist
create_database() {
    echo -e "${YELLOW}🗄️  Creating InfluxDB database if needed...${NC}"
    
    # Use localhost for database creation since we're running from the host
    local db_url="http://localhost:8086"
    
    local create_db_query="CREATE DATABASE $INFLUXDB_DATABASE"
    local response=$(curl -s -X POST \
        -H "Content-Type: application/x-www-form-urlencoded" \
        -u "$INFLUXDB_USER:$INFLUXDB_PASSWORD" \
        -d "q=$create_db_query" \
        "$db_url/query")
    
    if echo "$response" | grep -q '"results"'; then
        echo -e "${GREEN}✅ Database $INFLUXDB_DATABASE is ready${NC}"
        return 0
    else
        echo -e "${YELLOW}⚠️  Database creation response: $response${NC}"
        return 0  # Continue anyway, database might already exist
    fi
}

# Main execution
main() {
    # Check if docker-compose is running
    if ! docker-compose ps | grep -q "Up"; then
        echo -e "${RED}❌ Docker Compose services are not running${NC}"
        echo "Please start services with: docker-compose up -d"
        exit 1
    fi
    
    # Wait for services to be ready
    if ! wait_for_grafana; then
        exit 1
    fi
    
    if ! wait_for_influxdb; then
        exit 1
    fi
    
    # Create database
    create_database
    
    # Create datasource
    if ! create_influxdb_datasource; then
        exit 1
    fi
    
    # Import dashboards
    echo -e "${BLUE}📊 Importing dashboards...${NC}"
    
    if import_dashboard "grafana-dashboard-alignment-tuning.json" "Basic PD Tuning Dashboard"; then
        echo -e "${GREEN}✅ Basic dashboard imported${NC}"
    fi
    
    if import_dashboard "grafana-dashboard-advanced-pd-tuning.json" "Advanced PD Tuning Dashboard"; then
        echo -e "${GREEN}✅ Advanced dashboard imported${NC}"
    fi
    
    # Success message
    echo ""
    echo -e "${GREEN}🎉 Setup Complete!${NC}"
    echo "=================================================="
    echo -e "${BLUE}📊 Access your dashboards at:${NC}"
    echo "   • Basic Dashboard: $GRAFANA_URL/d/ftc-alignment-tuning/ftc-alignment-pd-tuning-dashboard"
    echo "   • Advanced Dashboard: $GRAFANA_URL/d/ftc-advanced-alignment-tuning/ftc-advanced-alignment-pd-tuning-dashboard"
    echo ""
    echo -e "${BLUE}🔧 Next steps:${NC}"
    echo "1. Start your FTC robot and run the AlignmentLoggingExample"
    echo "2. View the dashboards in Grafana to analyze PD controller behavior"
    echo "3. Use the data to tune Kp and Kd parameters for optimal performance"
    echo ""
    echo -e "${BLUE}📈 Dashboard Features:${NC}"
    echo "   • Real-time error vs correction plotting"
    echo "   • P and D term analysis"
    echo "   • Derivative filtering visualization"
    echo "   • Performance metrics monitoring"
    echo "   • Stability analysis"
    echo "   • Parameter tracking over time"
    echo ""
    echo -e "${YELLOW}💡 Pro Tip: Use the advanced dashboard for detailed PD tuning analysis${NC}"
}

# Run main function
main "$@"
