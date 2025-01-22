function countDecimals(value) {
    if (Math.floor(value) === value) return 0
    return value.toString().split(".")[1]?.length || 0
}

function floorTo(value, to) {
    return Math.floor(value * Math.pow(10, to)) / Math.pow(10, to)
}
function calculateQty(
    premium,
    krw,
    usdt,
    currentLeverage = 0,
) {
    if (!krw) return { min: 0, max: 0 }
    if (!usdt) return { min: 0, max: 0 }
    if (currentLeverage < 0) return { min: 0, max: 0 }
    if (!premium?.usdToKrw) return { min: 0, max: 0 }

    const { upbit, binance } = premium

    const decimalPlaces = countDecimals(stepSize)

    const upbitQty = floorTo(krw / upbit, decimalPlaces)
    const binanceQty = floorTo((usdt * currentLeverage) / binance, decimalPlaces)
    const minimumQtyFromExchange = floorTo(minUsdt / binance, decimalPlaces)

    const maxQtyOfExchange = Math.min(upbitQty, binanceQty)

    const min = Math.max(minQty, minimumQtyFromExchange)

    const max = Math.min(maxQtyOfExchange, maxQty)

    return { max, min }
}

const prev = {
    minQty: 0,
    maxQty: 0,
}

const update = (
    premium,
    usdToKrw,
    leverageModalBtn,
    orderJquery
) => {
    if (!canTrade) return

    const {
        buyQtyRange,
        sellQtyRange,
        maxBuyQty,
        upbitKrw,
        upbitUsd,
        binanceUsd,
        binanceKrw,
    } = orderJquery

    const krw = Number(upbitKrw.text().replaceAll(',', ''))
    const usdt = Number(binanceUsd.text().replaceAll(',', ''))

    upbitUsd.text((krw / usdToKrw).toLocaleString())
    binanceKrw.text(Math.floor(usdt * usdToKrw).toLocaleString())

    const leverage = Number(leverageModalBtn.text().replaceAll('x', ''))

    const { max, min } = calculateQty(premium, krw, usdt, leverage)
    const { maxQty, minQty } = prev

    if (max === 0 && maxQty === 0) {
        buyQtyRange.attr('max', max)
        maxBuyQty.text(max)
    }

    if (max !== maxQty) {
        prev.maxQty = max
        buyQtyRange.attr('max', max)
        maxBuyQty.text(max)
    }

    if (min !== minQty) {
        buyQtyRange.attr('min', min)
        sellQtyRange.attr('min', min)
        prev.minQty = min
        if (max === min) buyQtyRange.val(max).trigger('input')
        if (Number(sellQtyRange.attr('max')) === min) sellQtyRange.val(max).trigger('input')
    }
}

export default {
    update
}