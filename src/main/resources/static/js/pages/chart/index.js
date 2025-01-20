import WebsocketClient from '../../WebsocketClient.js'
import {
    chart,
    order,
    orderbook,
    position
} from './handler/index.js'

const URL = `ws://localhost:8000/ws/chart/${symbol}`

const client = new WebsocketClient(URL)

const setSocketInitialItems = (requiredItems) => (message) => {
    const {
        exchangeRateJquery,
        orderbookJquery,
        orderJquery,
        leverageModalJquery,
        orderHistoryJquery,
    } = requiredItems
    const { premium, orderbookPair } = JSON.parse(message.data)
    const { upbit, binance } = orderbookPair
    const { premium: kimp, usdToKrw } = premium

    // orderbook update
    orderbook.update(
        orderbookJquery.asks,
        { upbit: upbit.asks.reverse(), binance: binance.asks.reverse() },
        premium)
    orderbook.update(
        orderbookJquery.bids,
        { upbit: upbit.bids, binance: binance.bids },
        premium)
    orderbookJquery.premium.text(`${kimp}%`)

    // exchange rate update
    exchangeRateJquery.usdToKrw.text(usdToKrw)

    chart.updateOfMinute(requiredItems.chartData, premium)

    requiredItems.chart.update()

    order.update(premium, usdToKrw, leverageModalJquery.modalBtn, orderJquery)

    position.update(premium, orderHistoryJquery)
}

window.myModule.websocketClient = client
window.myModule.setSocketInitialItems = setSocketInitialItems;
window.myModule.drawChart = chart.drawChart;