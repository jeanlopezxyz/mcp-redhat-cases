#!/bin/bash
# =============================================================================
# MCP Server Test Script
# Tests all consolidated tools for Red Hat Support Cases
# =============================================================================

cd /home/jeanlopez/Documents/personal/projects/workshop-ai-case-redhat/mcp/mcp-redhat-cases

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if REDHAT_TOKEN is set
if [ -z "$REDHAT_TOKEN" ]; then
    echo -e "${RED}ERROR: REDHAT_TOKEN environment variable is not set${NC}"
    echo "Please set it with: export REDHAT_TOKEN='your-offline-token'"
    echo "Get your token at: https://access.redhat.com/management/api"
    exit 1
fi

echo -e "${GREEN}REDHAT_TOKEN is configured (length: ${#REDHAT_TOKEN} chars)${NC}"

run_mcp_test() {
    local request="$1"
    local timeout_sec="${2:-10}"

    (
        echo '{"jsonrpc":"2.0","id":0,"method":"initialize","params":{"protocolVersion":"2024-11-05","capabilities":{},"clientInfo":{"name":"test","version":"1.0"}}}'
        sleep 1
        echo '{"jsonrpc":"2.0","method":"notifications/initialized"}'
        sleep 1
        echo "$request"
        sleep $timeout_sec
    ) | java -Dquarkus.mcp.server.stdio.enabled=true \
           -Dquarkus.log.console.enable=false \
           -Dquarkus.banner.enabled=false \
           -jar target/quarkus-app/quarkus-run.jar 2>/dev/null | grep -v '"id":0'
}

case "$1" in
    "account")
        echo -e "\n${YELLOW}=== Testing getAccountInfo ===${NC}"
        echo "This verifies API connectivity"
        run_mcp_test '{"jsonrpc":"2.0","id":1,"method":"tools/call","params":{"name":"getAccountInfo","arguments":{}}}'
        ;;

    "list")
        echo -e "\n${YELLOW}=== Testing searchCases (all cases including closed) ===${NC}"
        run_mcp_test '{"jsonrpc":"2.0","id":1,"method":"tools/call","params":{"name":"searchCases","arguments":{"includeClosed":true}}}' 20
        ;;

    "list-open")
        echo -e "\n${YELLOW}=== Testing searchCases (only open cases) ===${NC}"
        run_mcp_test '{"jsonrpc":"2.0","id":1,"method":"tools/call","params":{"name":"searchCases","arguments":{}}}' 20
        ;;

    "get")
        if [ -z "$2" ]; then
            echo -e "${RED}ERROR: Case number required${NC}"
            echo "Usage: $0 get CASE_NUMBER"
            exit 1
        fi
        echo -e "\n${YELLOW}=== Testing getCase for $2 ===${NC}"
        run_mcp_test "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"tools/call\",\"params\":{\"name\":\"getCase\",\"arguments\":{\"caseNumber\":\"$2\"}}}" 15
        ;;

    "create")
        echo -e "\n${YELLOW}=== Testing createCase ===${NC}"
        echo "Creating a LOW priority test case..."
        run_mcp_test '{"jsonrpc":"2.0","id":1,"method":"tools/call","params":{"name":"createCase","arguments":{"title":"[TEST] MCP Server Test Case - Please Close","description":"This is an automated test case created by the MCP server to verify functionality.\n\nTest Details:\n- Created by: test_mcp.sh script\n- Purpose: Verify createCase tool functionality\n- Action needed: Please close this case after verification\n\nThis case can be safely closed.","product":"OpenShift Container Platform","version":"4.14","priority":"LOW"}}}' 25
        ;;

    "close")
        if [ -z "$2" ]; then
            echo -e "${RED}ERROR: Case number required${NC}"
            echo "Usage: $0 close CASE_NUMBER"
            exit 1
        fi
        echo -e "\n${YELLOW}=== Testing updateCase - Closing case $2 ===${NC}"
        run_mcp_test "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"tools/call\",\"params\":{\"name\":\"updateCase\",\"arguments\":{\"caseNumber\":\"$2\",\"status\":\"Closed\"}}}" 15
        ;;

    "reopen")
        if [ -z "$2" ]; then
            echo -e "${RED}ERROR: Case number required${NC}"
            echo "Usage: $0 reopen CASE_NUMBER"
            exit 1
        fi
        echo -e "\n${YELLOW}=== Testing updateCase - Reopening case $2 ===${NC}"
        run_mcp_test "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"tools/call\",\"params\":{\"name\":\"updateCase\",\"arguments\":{\"caseNumber\":\"$2\",\"status\":\"Waiting on Red Hat\"}}}" 15
        ;;

    "products")
        echo -e "\n${YELLOW}=== Testing listProducts ===${NC}"
        run_mcp_test '{"jsonrpc":"2.0","id":1,"method":"tools/call","params":{"name":"listProducts","arguments":{}}}' 15
        ;;

    "stats")
        echo -e "\n${YELLOW}=== Testing getStatistics ===${NC}"
        run_mcp_test '{"jsonrpc":"2.0","id":1,"method":"tools/call","params":{"name":"getStatistics","arguments":{}}}' 15
        ;;

    "entitlements")
        echo -e "\n${YELLOW}=== Testing getEntitlements ===${NC}"
        echo "Shows which products you can create cases for..."
        run_mcp_test '{"jsonrpc":"2.0","id":1,"method":"tools/call","params":{"name":"getEntitlements","arguments":{}}}' 15
        ;;

    "comment")
        if [ -z "$2" ] || [ -z "$3" ]; then
            echo -e "${RED}ERROR: Case number and comment required${NC}"
            echo "Usage: $0 comment CASE_NUMBER \"Your comment here\""
            exit 1
        fi
        echo -e "\n${YELLOW}=== Testing addComment on case $2 ===${NC}"
        run_mcp_test "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"tools/call\",\"params\":{\"name\":\"addComment\",\"arguments\":{\"caseNumber\":\"$2\",\"comment\":\"$3\",\"author\":\"MCP Test Script\"}}}" 15
        ;;

    "all")
        echo -e "${GREEN}========================================${NC}"
        echo -e "${GREEN}Running All Tests${NC}"
        echo -e "${GREEN}========================================${NC}"

        echo -e "\n${YELLOW}[1/4] Testing getAccountInfo...${NC}"
        $0 account

        echo -e "\n${YELLOW}[2/4] Testing searchCases (all cases)...${NC}"
        $0 list

        echo -e "\n${YELLOW}[3/4] Testing listProducts...${NC}"
        $0 products

        echo -e "\n${YELLOW}[4/4] Testing getStatistics...${NC}"
        $0 stats

        echo -e "\n${GREEN}========================================${NC}"
        echo -e "${GREEN}Basic Tests Complete!${NC}"
        echo -e "${GREEN}========================================${NC}"
        echo ""
        echo "To test case operations, run:"
        echo "  $0 create                    # Create a test case"
        echo "  $0 get CASE_NUMBER           # Get case details"
        echo "  $0 close CASE_NUMBER         # Close a case"
        echo "  $0 reopen CASE_NUMBER        # Reopen a closed case"
        ;;

    *)
        echo -e "${GREEN}========================================${NC}"
        echo -e "${GREEN}MCP Server Test Script${NC}"
        echo -e "${GREEN}========================================${NC}"
        echo ""
        echo "Usage: $0 COMMAND [args]"
        echo ""
        echo "Commands:"
        echo "  account              Test getAccountInfo (verify API connection)"
        echo "  list                 Test searchCases (all cases including closed)"
        echo "  list-open            Test searchCases (only open cases)"
        echo "  get CASE#            Test getCase for a specific case"
        echo "  create               Test createCase (creates LOW priority test case)"
        echo "  close CASE#          Test updateCase - close a case"
        echo "  reopen CASE#         Test updateCase - reopen a closed case"
        echo "  comment CASE# \"msg\"  Test addComment"
        echo "  products             Test listProducts"
        echo "  stats                Test getStatistics"
        echo "  all                  Run all read-only tests"
        echo ""
        echo "Examples:"
        echo "  $0 all                       # Run basic tests"
        echo "  $0 create                    # Create a test case"
        echo "  $0 get 03881234              # Get case details"
        echo "  $0 close 03881234            # Close case 03881234"
        echo "  $0 reopen 03881234           # Reopen case 03881234"
        echo "  $0 comment 03881234 \"Test\"   # Add comment to case"
        ;;
esac
