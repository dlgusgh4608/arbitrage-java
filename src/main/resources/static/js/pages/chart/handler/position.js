const calculateLongProfitRate = (entryPrice, markPrice) => {
    return ((markPrice - entryPrice) / entryPrice) * 100;
}

const calculateShortProfitRate = (entryPrice, markPrice) => {
    return calculateLongProfitRate(entryPrice, markPrice) * -1
}

const update = (premium, orderHistoryJquery) => {
    const { binanceProfit, upbitProfit, upbitAvgPrice, binanceAvgPrice, positionSymbol } = orderHistoryJquery
    
    if(!positionSymbol.text()) return

    const { binance, upbit } = premium
    const upbitEntryPrice = Number(upbitAvgPrice.text().replaceAll(',', ''))
    const binanceEntryPrice = Number(binanceAvgPrice.text().replaceAll(',', ''))

    const binanceProfitRate = calculateShortProfitRate(binanceEntryPrice, binance)
    const upbitProfitRate = calculateLongProfitRate(upbitEntryPrice, upbit)

    if (binanceProfitRate > 0) {
        binanceProfit.removeClass('text-down-color').addClass('text-up-color')
    } else {
        binanceProfit.removeClass('text-up-color').addClass('text-down-color')
    }
    if (upbitProfitRate > 0) {
        upbitProfit.removeClass('text-down-color').addClass('text-up-color')
    } else {
        upbitProfit.removeClass('text-up-color').addClass('text-down-color')
    }

    binanceProfit.text(binanceProfitRate.toFixed(4).concat('%'))
    upbitProfit.text(upbitProfitRate.toFixed(4).concat('%'))
}

export default {
    update
}