const update = (jqueryElement, { upbit, binance }, premium) => {
    jqueryElement.children().each((index, wrapper) => {
        const orderPages = $(wrapper)

        const upbitPage = orderPages.find('.upbit')
        const binancePage = orderPages.find('.binance')

        const upbitSizeEle = $(upbitPage).find('.size')
        const upbitPriceEle = $(upbitPage).find('.price')
        const binanceSizeEle = $(binancePage).find('.size')
        const binancePriceEle = $(binancePage).find('.price')

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

export default {
    update
}