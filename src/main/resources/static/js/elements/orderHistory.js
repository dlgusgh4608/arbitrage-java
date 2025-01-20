const generateOrder = (order) => {
    /**
     * 해당 element의 부모는 id가 orders이어야합니다.
     * 해당 element 생성기를 사용하려면 orderHistory.css를 로드하고 사용해야합니다.
     * 
     */
    const element = `
        <ul class="history-container" id="${'order-' + order.id}">
            <li class="history-wrap">
                <div class="history-icon-wrap">
                    <div class="history-icon text-up-color">BUY</div>
        ${order.sellOrders.length > 0
            ? `
                    <div
                        class="history-icon close"
                        onclick="(function() {
                            const ele = $(this)
                            const nextAll = ele.parent().parent().nextAll()
                            if (ele.hasClass('close')) {
                                ele.removeClass('close').addClass('open');
                                nextAll.each(function () { $(this).removeClass('d-none') })
                            } else {
                                ele.removeClass('open').addClass('close');
                                nextAll.each(function () { $(this).addClass('d-none') })
                            }
                        }).call(this)">
                        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor"
                            class="bi bi-chevron-down" viewBox="0 0 16 16">
                            <path fill-rule="evenodd"
                                d="M1.646 4.646a.5.5 0 0 1 .708 0L8 10.293l5.646-5.647a.5.5 0 0 1 .708.708l-6 6a.5.5 0 0 1-.708 0l-6-6a.5.5 0 0 1 0-.708" />
                        </svg>
                    </div>`
            : `
                    <div class="history-icon"></div>`
        }
                </div>
                <div class="history-item-wrap">
                    <div class="history-item-row">
                        <div class="history-item">
                            <div>심볼</div>
                            <div>${order.symbol}</div>
                        </div>
                        <div class="history-item">
                            <div>바이낸스 거래시각</div>
                            <div>${order.binanceEventTime}</div>
                        </div>
                        <div class="history-item">
                            <div>바이낸스 평단가</div>
                            <div>${order.binanceAvgPrice}</div>
                        </div>
                        <div class="history-item">
                            <div>바이낸스 수량</div>
                            <div>${order.binanceQty}</div>
                        </div>
                        <div class="history-item">
                            <div>바이낸스 수수료</div>
                            <div>${order.binanceCommission}</div>
                        </div>
                        <div class="history-item">
                            <div>거래상태</div>
                            <div class="text-point-color">${order.close ? 'CLOSE' : 'OPEN'}</div>
                        </div>
                    </div>
                    <div class="history-item-row">
                        <div class="history-item">
                            <div>기준 환율</div>
                            <div>${order.usdToKrw}</div>
                        </div>
                        <div class="history-item">
                            <div>업비트 거래시각</div>
                            <div>${order.upbitEventTime}</div>
                        </div>
                        <div class="history-item">
                            <div>업비트 평단가</div>
                            <div>${order.upbitAvgPrice}</div>
                        </div>
                        <div class="history-item">
                            <div>업비트 수량</div>
                            <div>${order.upbitQty}</div>
                        </div>
                        <div class="history-item">
                            <div>업비트 수수료</div>
                            <div>${order.upbitCommission}</div>
                        </div>
                        <div class="history-item">
                            <div>프리미엄</div>
                            <div>${order.premium}</div>
                        </div>
                    </div>
                </div>
            </li>
            ${order.sellOrders.map(sellOrder => (
            `
            <li class="history-wrap d-none">
                <div class="history-icon-wrap">
                    <div class="history-icon text-down-color">SELL</div>
                    <div class="history-icon"></div>
                </div>
                <div class="history-item-wrap">
                    <div class="history-item-row">
                        <div class="history-item">
                            <div>심볼</div>
                            <div>${order.symbol}</div>
                        </div>
                        <div class="history-item">
                            <div>바이낸스 거래시각</div>
                            <div>${sellOrder.binanceEventTime}</div>
                        </div>
                        <div class="history-item">
                            <div>바이낸스 평단가</div>
                            <div>${sellOrder.binanceAvgPrice}</div>
                        </div>
                        <div class="history-item">
                            <div>바이낸스 수량</div>
                            <div>${sellOrder.binanceQty}</div>
                        </div>
                        <div class="history-item">
                            <div>바이낸스 수수료</div>
                            <div>${sellOrder.binanceCommission}</div>
                        </div>
                        <div class="history-item">
                            <div>수익률</div>
                            <div class="${sellOrder.profitRate <= 0 ? 'text-down-color' : 'text-up-color'}">
                                ${String(sellOrder.profitRate).concat('%')}
                            </div >
                        </div >
                    </div >
                    <div class="history-item-row">
                        <div class="history-item">
                            <div>기준 환율</div>
                            <div>${sellOrder.usdToKrw}</div>
                        </div>
                        <div class="history-item">
                            <div>업비트 거래시각</div>
                            <div>${sellOrder.upbitEventTime}</div>
                        </div>
                        <div class="history-item">
                            <div>업비트 평단가</div>
                            <div>${sellOrder.upbitAvgPrice}</div>
                        </div>
                        <div class="history-item">
                            <div>업비트 수량</div>
                            <div>${sellOrder.upbitQty}</div>
                        </div>
                        <div class="history-item">
                            <div>업비트 수수료</div>
                            <div>${sellOrder.upbitCommission}</div>
                        </div>
                        <div class="history-item">
                            <div>프리미엄</div>
                            <div>${sellOrder.premium}</div>
                        </div>
                    </div>
                </div>
            </li>
        `)).join('')
        }
        </ul>
    `.trim()


    return {
        id: order.id,
        element: element,
    }
}

window.myModule.generateOrder = generateOrder