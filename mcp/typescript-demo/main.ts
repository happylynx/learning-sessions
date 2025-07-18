import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js"
import { StdioServerTransport } from "@modelcontextprotocol/sdk/server/stdio.js"
import { z } from "zod"

type HistoryItem = {
    time: Date,
    value: number
 }
const diceHistory: Array<HistoryItem> = []

const server = new McpServer({
    name: "dice-server",
    version: "0.1.0"
})

server.registerTool(
    'roll-dice', 
    {
        title: 'Roll a dice',
        description: 'Roll a dice to get a random number in interval [1, 6]',
        inputSchema: undefined,
    },
    async ({}) => {
        const value = Math.ceil(Math.random() * 6)
        diceHistory.push({ time: new Date(), value })
        return {
            content: [{ type: 'text', text: '' + value }]
        }
    }
)

server.registerTool(
    'roll-polyhedron',
    {
        title: 'Roll a polyhedron',
        description: 'Rolls a virtual regular polyhedron. Generates a random integer in range [1, max].',
        inputSchema: z.object({ max: z.number().int().min(1).describe('number of sides') }).shape,

    },
    async ({ max }) => ({
        content: [{ type: 'text', text: '' + Math.ceil(Math.random() * max) }]
    })
)

server.registerResource(
    'dice-history',
    'history://dice',
    {
        title: "Dice roll history",
        description: "List of dice rolls in JSON format. Each roll is described by its time and dice value",
        mimeType: 'application/json'
    },
    async (uri) => ({
        contents: [{
            uri: uri.href,
            text: JSON.stringify(diceHistory)
        }]
    })
)

const transport = new StdioServerTransport();
await server.connect(transport)
