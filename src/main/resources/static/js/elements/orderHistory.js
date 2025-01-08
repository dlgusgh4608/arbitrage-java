export const generateOrder = (order) => {    
    const element = `
    <div id="${'order-' + order.id}">
        <div class="d-flex align-items-center py-2" style="border-bottom: 1px solid var(--gray3);">
            ${
                order.sellOrders.length > 0
                ?
                `<div
                    class="order-history-item-chevron-wrap close"
                    onclick="(function() {
                        const ele = $(this);
                        const nextAll = ele.parent().nextAll();
                        if (ele.hasClass('close')) {
                            ele.removeClass('close').addClass('open');
                            nextAll.each(function () { $(this).removeClass('d-none').addClass('d-flex') });
                        } else {
                            ele.removeClass('open').addClass('close');
                            nextAll.each(function () { $(this).removeClass('d-flex').addClass('d-none') });
                        }
                    }).call(this)"
                >
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor"
                        class="bi bi-chevron-down" viewBox="0 0 16 16">
                        <path fill-rule="evenodd"
                            d="M1.646 4.646a.5.5 0 0 1 .708 0L8 10.293l5.646-5.647a.5.5 0 0 1 .708.708l-6 6a.5.5 0 0 1-.708 0l-6-6a.5.5 0 0 1 0-.708" />
                    </svg>
                </div>`
                :
                `<div class="order-history-item-empty"></div>`
            }
            <div class="history-item">${order.symbol}</div>
            <div class="history-item text-up-color">BUY</div>
            <div class="history-item">${order.createdAt}</div>
            <div class="history-item">${order.binanceAvgPrice.toLocaleString()}</div>
            <div class="history-item">${order.binanceQty}</div>
            <div class="history-item">${order.binanceCommission}</div>
            <div class="history-item">${order.upbitAvgPrice.toLocaleString()}</div>
            <div class="history-item">${order.upbitQty}</div>
            <div class="history-item">${order.upbitCommission}</div>
            <div class="history-item">${order.usdToKrw.toLocaleString()}</div>
            <div class="history-item">${order.isMaker ? 'MAKER' : 'TAKER'}</div>
            <div class="history-item">${order.premium}</div>
            <div class="history-item order-close ${order.close || 'text-up-color'}">${order.close ? 'CLOSE' : 'RUN'}</div>
        </div>
        ${
            order.sellOrders.map(sellOrder => (
                `
                <div class="d-none align-items-center py-2 order">
                    <div class="order-history-item-empty"></div>
                    <div class="history-item">
                        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor"
                            class="bi bi-arrow-return-right" viewBox="0 0 16 16">
                            <path fill-rule="evenodd"
                                d="M1.5 1.5A.5.5 0 0 0 1 2v4.8a2.5 2.5 0 0 0 2.5 2.5h9.793l-3.347 3.346a.5.5 0 0 0 .708.708l4.2-4.2a.5.5 0 0 0 0-.708l-4-4a.5.5 0 0 0-.708.708L13.293 8.3H3.5A1.5 1.5 0 0 1 2 6.8V2a.5.5 0 0 0-.5-.5" />
                        </svg>
                    </div>
                    <div class="history-item text-down-color">SELL</div>
                    <div class="history-item">${sellOrder.createdAt}</div>
                    <div class="history-item">${sellOrder.binanceAvgPrice.toLocaleString()}</div>
                    <div class="history-item">${sellOrder.binanceQty}</div>
                    <div class="history-item">${sellOrder.binanceCommission}</div>
                    <div class="history-item">${sellOrder.upbitAvgPrice.toLocaleString()}</div>
                    <div class="history-item">${sellOrder.upbitQty}</div>
                    <div class="history-item">${sellOrder.upbitCommission}</div>
                    <div class="history-item">${sellOrder.usdToKrw.toLocaleString()}</div>
                    <div class="history-item">${sellOrder.isMaker ? 'MAKER' : 'TAKER'}</div>
                    <div class="history-item">${sellOrder.premium}</div>
                    <div class="history-item ${sellOrder.profitRate <= 0 ? 'text-down-color' : 'text-up-color'}">${String(sellOrder.profitRate).concat('%')}</div>
                </div>
                `
            ))
        }
    </div>
    `.trim()

    
    return {
        id: order.id,
        element: element,
    }
}