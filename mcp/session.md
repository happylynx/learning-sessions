# Model Context Protocol

* https://modelcontextprotocol.io
* a way to provide context (often a way to call tools) for LLMs
* "it's like REST where LLMs are the clients"
* LLM independent, developed by Anthropic
  * supported by apps: Claude, Gemini, ChatGPT, Cline
* changing fast, versions identified by dates
* well documented
* ![MCP server diagram](./images/MCP-server.drawio.svg)

## Introduction

* Client-sever protocol
  * Clients - usually end user LLM chatbot apps 
  * Servers - services that want to be exposed through LLM
* https://modelcontextprotocol.io/introduction
* transport layer
  * JSON-RPC 2.0 over
    * stdio
    * Streamable HTTP
      * HTTP POST & GET + SSE
* Development often using SDK

## Demo of the Idea plugin

cline_mcp_settings.json
```json
{
  "mcpServers": {
    "idea": {
      "type": "stdio",
      "env": {
        "IJ_MCP_SERVER_PORT": "64342"
      },
      "command": "/Applications/IntelliJ IDEA.app/Contents/jbr/Contents/Home/bin/java",
      "args": [
        "-classpath",
        "/Applications/IntelliJ IDEA.app/Contents/plugins/mcpserver/lib/mcpserver-frontend.jar:/Applications/IntelliJ IDEA.app/Contents/lib/util-8.jar",
        "com.intellij.mcpserver.stdio.McpStdioRunnerKt"
      ]
    }
  }
}
```

## Custom server demo

### Inspector

```bash
npx -y @modelcontextprotocol/inspector <command>
```

https://modelcontextprotocol.io/docs/tools/inspector

## Servers repository

https://github.com/modelcontextprotocol/servers
