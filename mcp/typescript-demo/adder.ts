import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js"
import { StdioServerTransport } from "@modelcontextprotocol/sdk/server/stdio.js"
import { z } from "zod"

type HistoryItem = {
    time: Date,
    addend1: number
    addend2: number
 }
const additionHistory: Array<HistoryItem> = []

const server = new McpServer({
    name: "adder",
    version: "0.1.0"
})


function registerAdder() {
    return server.registerTool(
        'add',
        {
            title: 'sum two number',
            description: 'It sums its two arguments',
            inputSchema: z.object({
                addend1: z.number().describe("addend 1"),
                addend2: z.number().describe("addend 2") }).shape,

        },
        async ({ addend1, addend2 }) => {
            additionHistory.push({ time: new Date(), addend1, addend2 })
            return {
                content: [{ type: 'text', text: '' + (addend1 + addend2) }]
            }
        }
    )
}

let adderRegistration = registerAdder()

server.registerTool(
    'addRemoveAdder',
    {
        title: "adds or removes the adder tool",
        description: "adds or removes the adder tool based on the `present` argument",
        inputSchema: z.object({
            present: z.boolean().describe("true if the adder tool should be present")
        }).shape
    },
    ({ present }) => {
        if (present) {
            if (!adderRegistration) {
                adderRegistration = registerAdder()
            }
            return {
                content: [{ type: 'text', text: 'present' }]
            }
        }
        if (adderRegistration) {
            adderRegistration.remove()
            adderRegistration = null
        }
        return {
            content: [{ type: 'text', text: 'not present' }]
        }
    }
)

server.registerResource(
    'addition-history',
    'history://adding',
    {
        title: "History of adding",
        description: "List of adding in form of time addition was executed and both addends",
        mimeType: 'application/json'
    },
    async (uri) => ({
        contents: [{
            uri: uri.href,
            text: JSON.stringify(additionHistory)
        }]
    })
)

const transport = new StdioServerTransport();
await server.connect(transport)
