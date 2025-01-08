const prev = {}

const colors = {
    default: 'var(--text-color)',
    up: 'var(--up-color)',
    down: 'var(--down-color)',
}

const getColor = (prevValue, currentValue) => {
    if (prevValue === null) return colors.default

    if (prevValue < currentValue) return colors.up

    if (prevValue > currentValue) return colors.down

    return colors.default
}

export function handleMessage(message) {
    const json = JSON.parse(message.data)

    const { symbol, premium, upbit, binance } = json

    const premiumEle = $(`#${symbol.toLowerCase()}`).find('#premium')
    const upbitEle = $(`#${symbol.toLowerCase()}`).find('#upbit')
    const binanceEle = $(`#${symbol.toLowerCase()}`).find('#binance')

    premiumEle.text(premium.toLocaleString() + '%')
    upbitEle.text('₩' + upbit.toLocaleString())
    binanceEle.text('$' + binance.toLocaleString())

    if(!prev[symbol]) {
        prev[symbol] = {
            premium: premium,
            upbit: upbit,
            binance: binance,
        }
        
        return
    }
    
    const prevPremium = prev[symbol].premium
    const prevupbit = prev[symbol].upbit
    const prevBinance = prev[symbol].binance

    premiumEle.css('color', getColor(prevPremium, premium))
    upbitEle.css('color', getColor(prevupbit, upbit))
    binanceEle.css('color', getColor(prevBinance, binance))

    prev[symbol].premium = premium
    prev[symbol].upbit = upbit
    prev[symbol].binance = binance
}