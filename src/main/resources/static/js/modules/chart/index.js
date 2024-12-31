import WebsocketClient from '../WebsocketClient.js'
import { createCandleOfMinutes, setOrderbook, updateCandleOfMinutes, updateOrderInfo, updatePosition } from './utils.js'

const URL = `ws://localhost:8000/ws/chart/${symbol}`
const client = new WebsocketClient(URL)


window.myModule = {
    websocketClient: client,
    createCandleOfMinutes,
    setOrderbook,
    updateCandleOfMinutes,
    updateOrderInfo,
    updatePosition,
}