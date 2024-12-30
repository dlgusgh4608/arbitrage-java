export const createCandleOfMinutes = (data) => {
    const aMinuteMs = 60 * 1000;
    const result = []
    let currentCandle = null

    data.forEach(d => {
        const timestamp = new Date(d.createdAt).getTime()
        const roundedTimestamp = Math.floor(timestamp / aMinuteMs) * aMinuteMs

        if (!currentCandle || currentCandle.x !== roundedTimestamp) {
            // 새로운 캔들 시작
            if (currentCandle) {
                result.push(currentCandle);
            }

            // initial
            currentCandle = {
                x: roundedTimestamp,
                o: d.premium,
                h: d.premium,
                l: d.premium,
                c: d.premium
            }
        } else {
            // 기존 캔들 업데이트
            currentCandle.h = Math.max(currentCandle.h, d.premium)
            currentCandle.l = Math.min(currentCandle.l, d.premium)
            currentCandle.c = d.premium
        }
    })

    if (currentCandle) {
        result.push(currentCandle)
    }

    return result
}

export const updateCandleOfMinutes = (originArr, data) => {
    // chart js 원본배열 참조함.
    const aMinuteMs = 60 * 1000;
    const timestamp = Date.now()
    const roundedTimestamp = Math.floor(timestamp / aMinuteMs) * aMinuteMs

    if (!originArr.at(-1)?.x) return

    if (originArr.at(-1).x !== roundedTimestamp) {
        originArr.push({
            x: roundedTimestamp,
            o: data.premium,
            h: data.premium,
            l: data.premium,
            c: data.premium
        })
    } else {
        originArr.at(-1).h = Math.max(originArr.at(-1).h, data.premium)
        originArr.at(-1).l = Math.min(originArr.at(-1).l, data.premium)
        originArr.at(-1).c = data.premium
    }
}

export const setOrderbook = (cls = '', { upbit, binance }, premium) => {
    $(`.${cls}`).children().each((index, wrapper) => {
        const orderPages = $(wrapper)

        const upbitPage = orderPages.find('#upbit')
        const binancePage = orderPages.find('#binance')

        const upbitSizeEle = $(upbitPage).find('#size')
        const upbitPriceEle = $(upbitPage).find('#price')
        const binanceSizeEle = $(binancePage).find('#size')
        const binancePriceEle = $(binancePage).find('#price')

        const { size: upbitSizeValue, price: upbitPriceValue } = upbit[index]
        const { size: binanceSizeValue, price: binancePriceValue } = binance[index]

        upbitSizeEle.text(upbitSizeValue.toFixed(3))
        upbitPriceEle.text(upbitPriceValue.toLocaleString())
        binancePriceEle.text(binancePriceValue.toFixed(3))
        binanceSizeEle.text(binanceSizeValue.toLocaleString())

        const { upbit: upbitPrice, binance: binancePrice } = premium

        if (upbitPrice === upbitPriceValue) {
            $(upbitPage).css('border', '1px solid var(--gray9)')
        } else {
            $(upbitPage).css('border', '1px solid transparent')
        }

        if (binancePrice === binancePriceValue) {
            $(binancePage).css('border', '1px solid var(--gray9)')
        } else {
            $(binancePage).css('border', '1px solid transparent')
        }
    })
}

function countDecimals(value) {
    if (Math.floor(value) === value) return 0
    return value.toString().split(".")[1]?.length || 0
}

function floorTo(value, to) {
    return Math.floor(value * Math.pow(10, to)) / Math.pow(10, to)
}

export const calculateQty = (
    premium,
    krw,
    usdt,
    currentLeverage = 0,
    defaultSymbolInfo = { maxQty: 0, minQty: 0, stepSize: 0, minUsdt: 0 }
) => {
    if (!krw) return { min: 0, max: 0 }
    if (!usdt) return { min: 0, max: 0 }
    if (currentLeverage < 0) return { min: 0, max: 0 }
    if (!premium?.usdToKrw) return { min: 0, max: 0 }

    const { upbit, binance } = premium
    const { maxQty, minQty, minUsdt, stepSize } = defaultSymbolInfo

    const decimalPlaces = countDecimals(stepSize)

    const upbitQty = floorTo(krw / upbit, decimalPlaces)
    const binanceQty = floorTo((usdt * currentLeverage) / binance, decimalPlaces)
    const minimumQtyFromExchange = floorTo(minUsdt / binance, decimalPlaces)

    const min = Math.min(upbitQty, binanceQty, minQty, minimumQtyFromExchange)
    const maxQtyOfExchange = Math.min(upbitQty, binanceQty)

    const max = Math.min(maxQtyOfExchange, maxQty)

    return { max, min }
}