import WebsocketClient from '../WebsocketClient.js'
import { calculateQty, createCandleOfMinutes, setOrderbook, updateCandleOfMinutes } from './utils.js'

const URL = `ws://localhost:8000/ws/chart/${symbol}`
const client = new WebsocketClient(URL)


window.myModule = {
    websocketClient: client,
    createCandleOfMinutes,
    setOrderbook,
    updateCandleOfMinutes,
    calculateQty,
}