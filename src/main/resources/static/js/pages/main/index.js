import WebsocketClient from '../../WebsocketClient.js'
import { handleMessage } from './handler.js'

const URL = `ws/premium`
const client = new WebsocketClient(URL)

client.setMessageHandler(handleMessage)

window.myModule.websocketClient = client