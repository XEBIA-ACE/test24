#!/bin/bash

# User Management Service - Project Verification Script
# This script verifies that all required files are present

echo "======================================"
echo "User Management Service"
echo "Project Verification"
echo "======================================"
echo ""

# Color codes
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m' # No Color

check_file() {
    if [ -f "$1" ]; then
        echo -e "${GREEN}✓${NC} $1"
        return 0
    else
        echo -e "${RED}✗${NC} $1 (missing)"
        return 1
    fi
}

check_dir() {
    if [ -d "$1" ]; then
        echo -e "${GREEN}✓${NC} $1/"
        return 0
    else
        echo -e "${RED}✗${NC} $1/ (missing)"
        return 1
    fi
}

missing=0

echo "Checking root files..."
check_file "pom.xml" || ((missing++))
check_file "Dockerfile" || ((missing++))
check_file "docker-compose.yml" || ((missing++))
check_file "Makefile" || ((missing++))
check_file ".gitignore" || ((missing++))
check_file ".env.example" || ((missing++))
echo ""

echo "Checking documentation..."
check_file "README.md" || ((missing++))
check_file "API_EXAMPLES.md" || ((missing++))
check_file "DEPLOYMENT.md" || ((missing++))
check_file "ARCHITECTURE.md" || ((missing++))
check_file "PROJECT_SUMMARY.md" || ((missing++))
echo ""

echo "Checking source structure..."
check_dir "src/main/java/com/usermanagement" || ((missing++))
check_dir "src/main/java/com/usermanagement/api/controller" || ((missing++))
check_dir "src/main/java/com/usermanagement/api/dto" || ((missing++))
check_dir "src/main/java/com/usermanagement/domain/entity" || ((missing++))
check_dir "src/main/java/com/usermanagement/domain/repository" || ((missing++))
check_dir "src/main/java/com/usermanagement/domain/service" || ((missing++))
check_dir "src/main/java/com/usermanagement/config" || ((missing++))
check_dir "src/main/java/com/usermanagement/security" || ((missing++))
check_dir "src/main/java/com/usermanagement/exception" || ((missing++))
echo ""

echo "Checking configuration files..."
check_file "src/main/resources/application.yml" || ((missing++))
check_file "src/main/resources/application-dev.yml" || ((missing++))
check_file "src/main/resources/application-prod.yml" || ((missing++))
echo ""

echo "Checking database migrations..."
check_dir "src/main/resources/db/migration" || ((missing++))
check_file "src/main/resources/db/migration/V1__create_users_table.sql" || ((missing++))
check_file "src/main/resources/db/migration/V2__insert_default_roles_and_permissions.sql" || ((missing++))
echo ""

echo "Checking test structure..."
check_dir "src/test/java/com/usermanagement" || ((missing++))
check_file "src/test/resources/application-test.yml" || ((missing++))
echo ""

echo "======================================"
if [ $missing -eq 0 ]; then
    echo -e "${GREEN}✓ All required files are present!${NC}"
    echo ""
    echo "Project Statistics:"
    echo "  - Java source files: $(find src/main/java -name "*.java" 2>/dev/null | wc -l)"
    echo "  - Test files: $(find src/test/java -name "*.java" 2>/dev/null | wc -l)"
    echo "  - Total source files: $(find src -type f 2>/dev/null | wc -l)"
    echo ""
    echo "Next steps:"
    echo "  1. Review .env.example and create .env file"
    echo "  2. Run 'make dev-setup' to start dependencies"
    echo "  3. Run 'make build' to compile the project"
    echo "  4. Run 'make test' to verify tests pass"
    echo "  5. Run 'make run' to start the application"
    echo "  6. Visit http://localhost:8080/swagger-ui.html"
    exit 0
else
    echo -e "${RED}✗ $missing file(s) missing!${NC}"
    exit 1
fi
