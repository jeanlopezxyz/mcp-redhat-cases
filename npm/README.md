# mcp-redhat-cases

MCP Server for Red Hat Support Cases and Knowledge Base. Create, search, and manage support cases through AI assistants.

## Quick Start

```bash
REDHAT_TOKEN="your-token" npx mcp-redhat-cases
```

## Configuration

Add to `~/.claude/settings.json` (Claude Code) or your MCP client config:

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

## Requirements

- Java 21+ ([Download](https://adoptium.net/))
- Red Hat API Token ([Generate here](https://access.redhat.com/management/api))
- Red Hat Subscription with Support (not Self-Support)

## Tools (12)

| Tool | Description |
|------|-------------|
| `getAccountInfo` | Verify API connection and get account info |
| `getEntitlements` | View subscriptions and which products allow cases |
| `createCase` | Open a new support case |
| `getCase` | Get full case details by number |
| `searchCases` | List and filter cases |
| `updateCase` | Change status or reassign contact |
| `addComment` | Add information to an existing case |
| `getStatistics` | Get case metrics and distribution |
| `listProducts` | List products for case creation |
| `listVersions` | List versions for a product |
| `searchKnowledgeBase` | Find solutions and articles |
| `getSolution` | Get full KB article content |

## Example Prompts

```
"Show my Red Hat account info"
"What products can I create cases for?"
"List my active support cases"
"Show case 03881234"
"Create a support case for OpenShift networking issue"
"Search knowledge base for CrashLoopBackOff"
"Close case 03881234"
"Add comment to case: we found the root cause"
```

## Documentation

Full docs: https://github.com/jeanlopezxyz/mcp-redhat-cases

## License

MIT
