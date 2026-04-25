import { Server } from "@modelcontextprotocol/sdk/server/index.js";
import { StdioServerTransport } from "@modelcontextprotocol/sdk/server/stdio.js";
import { CallToolRequestSchema, ListToolsRequestSchema } from "@modelcontextprotocol/sdk/types.js";

import { getAthenzToken } from "./athenz-client.js";

// Create MCP server
const server = new Server(
  { name: "k8s-docs-server", version: "1.0.0" },
  { capabilities: { tools: {} } }
);

// Let AI know what tools it can use
server.setRequestHandler(ListToolsRequestSchema, async () => ({
  tools: [
    {
      name: "get_k8s_docs",
      description: "Get the list of documents from the API running on local Kubernetes.",
      inputSchema: { type: "object", properties: {} } // no input parameters
    },
    {
      name: "post_k8s_doc",
      description: "Post a new document to the API running on local Kubernetes.",
      inputSchema: {
        type: "object",
        properties: {
          name: { type: "string", description: "title of the document" },
          content: { type: "string", description: "content of the document" }
        },
        required: ["name", "content"]
      }
    },
  ]
}));

// When AI uses the tool
server.setRequestHandler(CallToolRequestSchema, async (request) => {
  const athenzAccessToken = await getAthenzToken();
  const headers = {
    "Authorization": `Bearer ${athenzAccessToken}`,
    "Content-Type": "application/json",
  }

  if (request.params.name === "get_k8s_docs") {
    try {
      // Call the API running on local Kubernetes
      const response = await fetch("http://localhost:14443/api/docs", { headers });
      const data = await response.text();
      
      // Return the result to AI
      return { content: [{ type: "text", text: data }] };
    } catch (error) {
      return { content: [{ type: "text", text: `Error: ${error.message}` }], isError: true };
    }
  }
  if (request.params.name === "post_k8s_doc") {
    try {
      // Call the API running on local Kubernetes
      const response = await fetch("http://localhost:14443/api/docs", {
        method: "POST",
        headers: headers,
        body: JSON.stringify(request.params)
      });
      const data = await response.text();
      // Return the result to AI
      return { content: [{ type: "text", text: data }] };
    } catch (error) {
      return { content: [{ type: "text", text: `Error: ${error.message}` }], isError: true };
    }
  }
  throw new Error("Unknown tool");
});

// Run the server (communicate with AI via standard input/output)
const transport = new StdioServerTransport();
await server.connect(transport);
console.error("MCP server is ready"); // MCP uses stdio communication, so console.log will break the communication. Use error instead.
