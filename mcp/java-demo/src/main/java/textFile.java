import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpServerTransportProvider;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;


OpenedFile file = null;

void main(String[] args) {
    final McpServerTransportProvider stdioTransportProvider = new StdioServerTransportProvider();
    McpSyncServer syncServer = McpServer.sync(stdioTransportProvider)
            .capabilities(McpSchema.ServerCapabilities.builder()
                    .tools(true)
                    .resources(false, true)
//                    .logging()
                    .build())
            .build();

    final String inputSchema = """
            {
              "type" : "object",
              "id" : "urn:jsonschema:Operation",
              "properties" : {
                "path" : {
                  "type" : "string"
                }
              }
            }
            """;
    syncServer.addTool(new McpServerFeatures.SyncToolSpecification(
            new McpSchema.Tool(
                    "open-file",
                    "It opens a file for it to be viewable as a resource. It returns absolute path of the opened file.",
                    inputSchema),
            ((exchange, arguments) -> {

                final String pathArgument = (String) arguments.get("path");
                final Path path = Path.of(pathArgument.trim()).toAbsolutePath();
                final String content;
                try {
                    content = Files.readString(path);
                } catch (IOException e) {
                    e.printStackTrace();
                    return new McpSchema.CallToolResult(e.toString(), true);
                }
                file = new OpenedFile(path, content);
                Thread.startVirtualThread(() -> addCapabilities(syncServer));
                return new McpSchema.CallToolResult(path.toString(), false);
            })
    ));
}

private void addCapabilities(McpSyncServer syncServer) {
    String inputSchema = """
            {
              "type" : "object",
              "id" : "urn:jsonschema:Operation",
              "properties" : {}
            }
            """;
    syncServer.addTool(new McpServerFeatures.SyncToolSpecification(
            new McpSchema.Tool(
                    "close-file",
                    "It closes the currently opened file.",
                    inputSchema
            ),
            (exchange, arguments) -> {
                final OpenedFile currentFile = file;
                file = null;
                removeCapabilities(syncServer, currentFile.uri());
                return new McpSchema.CallToolResult(currentFile.path.toString(), false);
            }
    ));

    syncServer.addResource(new McpServerFeatures.SyncResourceSpecification(
            new McpSchema.Resource(
                    file.uri(),
                    "opened-file",
                    "Content of the opened file.",
                    "text/plain",
                    null
            ),
            (exchange, readRequest) -> new McpSchema.ReadResourceResult(
                    List.of(new McpSchema.TextResourceContents(
                            file.uri(),
                            "text/plain",
                            file.content()
            )))
    ));
}

private void removeCapabilities(McpSyncServer syncServer, String resourceUri) {
    syncServer.removeTool("close-file");
    syncServer.removeResource(resourceUri);
}

record OpenedFile(Path path, String content) {
    public String uri() {
        return "file://" + path;
    }
}