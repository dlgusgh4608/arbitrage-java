import WebsocketClient from '../../WebsocketClient.js'
import {
    chart,
    order,
    orderbook,
    position
} from './handler/index.js'

const URL = `ws/chart/${symbol}`

const client = new WebsocketClient(URL)

const setSocketInitialItems = (requiredItems) => (message) => {
    const {
        exchangeRateJquery,
        orderbookJquery,
        orderJquery,
        leverageModalJquery,
        orderHistoryJquery,
        chart: chartJs,
        chartData
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

    chart.updateOfMinute(chartData, premium)

    const zoomLimit = chartData.at(-1).x + chartData.at(6).x - chartData.at(0).x
    
    chartJs.config.options.plugins.zoom.limits.x.max = zoomLimit;

    chartJs.update('none')

    order.update(premium, usdToKrw, leverageModalJquery.modalBtn, orderJquery)

    position.update(premium, orderHistoryJquery)
}

window.myModule.websocketClient = client
window.myModule.setSocketInitialItems = setSocketInitialItems;
window.myModule.drawChart = chart.drawChart;