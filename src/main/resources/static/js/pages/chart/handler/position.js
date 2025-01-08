const calculateLongProfitRate = (entryPrice, markPrice) => {
    return ((markPrice - entryPrice) / entryPrice) * 100;
}

const calculateShortProfitRate = (entryPrice, markPrice) => {
    return calculateLongProfitRate(entryPrice, markPrice) * -1
}

const update = (premium, orderHistoryJquery) => {
    if (!hasPosition) return

    const { binanceProfit, upbitProfit } = orderHistoryJquery

    const { binance, upbit } = premium
    const { avg_buy_price } = upbitPosition
    const { entryPrice } = binancePosition

    const binanceProfitRate = calculateShortProfitRate(entryPrice, binance)
    const upbitProfitRate = calculateLongProfitRate(avg_buy_price, upbit)

    if (binanceProfitRate > 0) {
        binanceProfit.addClass('text-up-color')
    } else {
        binanceProfit.addClass('text-down-color')
    }
    if (upbitProfitRate > 0) {
        upbitProfit.addClass('text-up-color')
    } else {
        upbitProfit.addClass('text-down-color')
    }

    binanceProfit.text(binanceProfitRate.toFixed(2).concat('%'))
    upbitProfit.text(upbitProfitRate.toFixed(2).concat('%'))
}

export default {
    update
}