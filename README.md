# Red Hat Cases MCP Server

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![npm version](https://img.shields.io/npm/v/mcp-redhat-cases)](https://www.npmjs.com/package/mcp-redhat-cases)
[![Java](https://img.shields.io/badge/Java-21+-orange)](https://adoptium.net/)
[![GitHub release](https://img.shields.io/github/v/release/jeanlopezxyz/mcp-redhat-cases)](https://github.com/jeanlopezxyz/mcp-redhat-cases/releases/latest)

A [Model Context Protocol (MCP)](https://modelcontextprotocol.io/) server for Red Hat Support Cases and Knowledge Base integration.

Built with [Quarkus MCP Server](https://docs.quarkiverse.io/quarkus-mcp-server/dev/index.html).

## Transport Modes

This server supports two MCP transport modes:

| Mode | Description | Use Case |
|------|-------------|----------|
| **stdio** | Standard input/output communication | Default for Claude Desktop, Claude Code, Cursor, VS Code |
| **SSE** | Server-Sent Events over HTTP | Standalone server, web integrations, multiple clients |

## Table of Contents

- [Transport Modes](#transport-modes)
- [Requirements](#requirements)
- [Installation](#installation)
- [Configuration](#configuration)
- [Tools](#tools)
- [Prompts](#prompts)
- [Examples](#examples)
- [Development](#development)
- [Contributing](#contributing)

---

## Requirements

- **Java 21+** - [Download](https://adoptium.net/)
- **Red Hat API Token** - [Generate here](https://access.redhat.com/management/api)
- **Red Hat Subscription with Support** - Self-Support subscriptions cannot create cases

---

## Installation

### Claude Code

Add to `~/.claude/settings.json`:

```json
{
  "mcpServers": {
    "redhat-cases": {
      "command": "npx",
      "args": ["-y", "mcp-redhat-cases@latest"],
      "env": {
        "REDHAT_TOKEN": "your-token-here"
      }
    }
  }
}
```

### Claude Desktop

Add to `claude_desktop_config.json`:

```json
{
  "mcpServers": {
    "redhat-cases": {
      "command": "npx",
      "args": ["-y", "mcp-redhat-cases@latest"],
      "env": {
        "REDHAT_TOKEN": "your-token-here"
      }
    }
  }
}
```

### VS Code

```shell
code --add-mcp '{"name":"redhat-cases","command":"npx","args":["-y","mcp-redhat-cases@latest"],"env":{"REDHAT_TOKEN":"your-token-here"}}'
```

### Cursor

Add to `mcp.json`:

```json
{
  "mcpServers": {
    "redhat-cases": {
      "command": "npx",
      "args": ["-y", "mcp-redhat-cases@latest"],
      "env": {
        "REDHAT_TOKEN": "your-token-here"
      }
    }
  }
}
```

### Windsurf

Add to MCP configuration:

```json
{
  "mcpServers": {
    "redhat-cases": {
      "command": "npx",
      "args": ["-y", "mcp-redhat-cases@latest"],
      "env": {
        "REDHAT_TOKEN": "your-token-here"
      }
    }
  }
}
```

### Goose CLI

Add to `config.yaml`:

```yaml
extensions:
  redhat-cases:
    command: npx
    args:
      - -y
      - mcp-redhat-cases@latest
    env:
      REDHAT_TOKEN: your-token-here
```

### SSE Mode

Run as standalone server:

```bash
REDHAT_TOKEN="your-token" npx mcp-redhat-cases --port 9080
```

Endpoint: `http://localhost:9080/mcp/sse`

---

## Configuration

### Command Line Options

| Option | Description |
|--------|-------------|
| `--port <PORT>` | Start in SSE mode on specified port |
| `--help` | Show help message |
| `--version` | Show version |

### Environment Variables

| Variable | Description | Required |
|----------|-------------|----------|
| `REDHAT_TOKEN` | Red Hat API offline token | Yes |

---

## Tools

This server provides **12 tools** organized in 4 categories:

### Account

#### `getAccountInfo`
Verify Red Hat API connection and get account information.

**Parameters:** None

**Returns:** Account name, number, alias, status, region, and country.

---

#### `getEntitlements`
Get the user's Red Hat subscriptions (entitlements). Shows which products you have access to for creating support cases.

**Parameters:** None

**Returns:** List of entitlements separated by support level:
- Products WITH Support (can create cases)
- Self-Support (NO case creation)

> **Important:** Only products with PREMIUM, STANDARD, or similar support levels (not SELF-SUPPORTED) can be used to create support cases.

---

### Case Management

#### `createCase`
Create a new support case in Red Hat.

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `title` | string | Yes | Case title - brief description (max 255 chars) |
| `description` | string | Yes | Detailed description: what happened, error messages, steps to reproduce |
| `product` | string | Yes | Exact product name from `listProducts` |
| `version` | string | Yes | Product version from `listVersions` |
| `priority` | string | No | `LOW`, `NORMAL`, `HIGH`, `URGENT` (default: `NORMAL`) |
| `reporter` | string | No | Reporter email |

**Returns:** Created case details with assigned case number.

> **Tip:** Use `getEntitlements` first to check which products you can create cases for.

---

#### `getCase`
Get complete details of a support case by its number.

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `caseNumber` | string | Yes | 8-digit Red Hat case number (e.g.: `03881234`) |

**Returns:** Full case details including description, comments, timeline, status, priority, and product.

---

#### `searchCases`
Search and list support cases with optional filters.

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `query` | string | No | Text to search in title, description or case number |
| `status` | string | No | Filter: `Waiting on Red Hat`, `Waiting on Customer`, `Closed` |
| `priority` | string | No | Filter: `LOW`, `NORMAL`, `HIGH`, `URGENT` |
| `product` | string | No | Filter by product name |
| `includeClosed` | boolean | No | Include closed cases (default: `false`) |

**Returns:** List of matching cases.

**Examples:**
- List open cases: `searchCases`
- List ALL cases: `searchCases includeClosed=true`
- Find urgent cases: `searchCases priority='URGENT'`
- Find by product: `searchCases product='OpenShift'`

---

#### `updateCase`
Update a support case: change status or reassign contact.

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `caseNumber` | string | Yes | 8-digit Red Hat case number |
| `status` | string | No | New status: `Waiting on Red Hat`, `Waiting on Customer`, `Closed` |
| `contactSSOName` | string | No | SSO username of new primary contact |

> At least one of `status` or `contactSSOName` must be provided.

**Examples:**
- Reopen closed case: `updateCase caseNumber='03881234' status='Waiting on Red Hat'`
- Close case: `updateCase caseNumber='03881234' status='Closed'`
- Reassign: `updateCase caseNumber='03881234' contactSSOName='jsmith'`

---

#### `addComment`
Add a comment to an existing support case.

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `caseNumber` | string | Yes | 8-digit Red Hat case number |
| `comment` | string | Yes | Comment text: additional details, logs, or response |
| `author` | string | Yes | Your name or identifier |
| `isInternal` | boolean | No | Internal comment not visible to customer (default: `false`) |

**Returns:** Confirmation of added comment.

---

#### `getStatistics`
Get support case statistics and metrics.

**Parameters:** None

**Returns:** Statistics including:
- Total cases
- Open urgent cases
- Distribution by status, priority, and product

---

### Products

#### `listProducts`
List all Red Hat products available for creating support cases.

**Parameters:** None

**Returns:** List of products with name and code. Use the exact product NAME when creating a case.

---

#### `listVersions`
List available versions for a Red Hat product.

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `productCode` | string | Yes | Product code from `listProducts` |

**Returns:** List of available versions for the product.

---

### Knowledge Base

#### `searchKnowledgeBase`
Search Red Hat Knowledge Base for articles and solutions.

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `query` | string | Yes | Search keywords (use error messages for best results) |
| `maxResults` | integer | No | Maximum results to return (default: `10`) |
| `product` | string | No | Product filter (e.g.: `OpenShift`, `RHEL`) |
| `documentType` | string | No | Type: `Solution`, `Documentation`, `Article` |

**Returns:** List of matching articles with ID, title, URL, and summary.

---

#### `getSolution`
Get the full content of a solution or article from the Red Hat Knowledge Base.

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `solutionId` | string | Yes | Solution ID from search results (e.g.: `5049001`) |

**Returns:** Full article content including environment, issue, root cause, diagnostic steps, and resolution.

---

## Prompts

| Prompt | Description | Parameters |
|--------|-------------|------------|
| `createCaseGuide` | Step-by-step guide to create a support case | `product` (optional) |
| `troubleshootingGuide` | Guide to diagnose and manage existing cases | `caseNumber` (required) |
| `clusterDiagnosticGuide` | OpenShift cluster diagnostics with escalation workflow | `clusterName` (optional) |
| `executiveSummary` | Get an overview of all support cases with statistics | None |
| `knowledgeBaseSearchGuide` | Tips for effective KB searches | `problem` (optional) |
| `fullDiagnosticWorkflow` | Complete workflow: KB search -> diagnose -> escalate | `problemDescription`, `product` |

---

## Example Prompts

Use natural language to manage Red Hat support cases. Here are prompts organized by use case:

### Getting Started

```
"Show my Red Hat account info"
"Am I connected to Red Hat?"
"What products can I create cases for?"
"Show my entitlements"
"What subscriptions do I have?"
```

### Viewing Cases

```
"List my active support cases"
"Show all my cases"
"List all cases including closed ones"
"Show case 03881234"
"Get details for case 03881234"
"What's the status of my urgent cases?"
```

### Searching Cases

```
"Find cases related to authentication"
"Search for OpenShift cases"
"Show all urgent priority cases"
"Find cases waiting on Red Hat"
"Search for cases about networking"
```

### Creating Cases

```
"I need to create a support case for OpenShift"
"Create a case: my cluster nodes are NotReady"
"Open a new case for RHEL kernel panic"
"Help me create a support case for database connection issues"
"What products and versions are available for case creation?"
"List OpenShift versions"
```

### Managing Cases

```
"Add a comment to case 03881234: we found the root cause"
"Close case 03881234"
"Reopen case 03881234"
"Change case 03881234 to waiting on Red Hat"
"Reassign case 03881234 to user jsmith"
```

### Knowledge Base

```
"Search knowledge base for CrashLoopBackOff"
"Find solutions for etcd timeout errors"
"Search KB for oauth authentication error"
"Get the full solution for article 5049001"
"Find documentation about OpenShift networking"
"Search for RHEL storage issues"
```

### Statistics & Reports

```
"Show case statistics"
"How many urgent cases do I have open?"
"Give me an executive summary of support cases"
"What's the distribution of cases by priority?"
```

### Workflows

```
"I have an error: ImagePullBackOff - help me find a solution or create a case"
"My OpenShift cluster is down - what should I do?"
"Guide me through creating a support case"
"Help me troubleshoot case 03881234"
```

---

## Development

### Run in dev mode

```bash
export REDHAT_TOKEN="your-token"
./mvnw quarkus:dev
```

### Build

```bash
./mvnw package -DskipTests
```

### Test with script

```bash
export REDHAT_TOKEN="your-token"
./test_mcp.sh all          # Run basic tests
./test_mcp.sh entitlements # Check your subscriptions
./test_mcp.sh create       # Create a test case
./test_mcp.sh list         # List all cases
```

### Test with MCP Inspector

```bash
# stdio mode
REDHAT_TOKEN="your-token" npx @modelcontextprotocol/inspector npx mcp-redhat-cases

# SSE mode
REDHAT_TOKEN="your-token" npx mcp-redhat-cases --port 9080
# Then connect inspector to http://localhost:9080/mcp/sse
```

---

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines on:

- Branch protection rules
- Pull request workflow
- Development setup
- Code style and testing
- Release process

---

## License

MIT
