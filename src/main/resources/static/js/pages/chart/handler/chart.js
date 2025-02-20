const createOfMinute = (data) => {
    const aMinuteMs = 60 * 1000;
    const result = []
    let currentCandle = null

    data.forEach(d => {
        const timestamp = new Date(d.createdAt).getTime()
        const roundedTimestamp = Math.floor(timestamp / aMinuteMs) * aMinuteMs

        if (!currentCandle || currentCandle.x !== roundedTimestamp) {
            // 새로운 캔들 시작
            if (currentCandle) {
                result.unshift(currentCandle);
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
        result.unshift(currentCandle)
    }

    return result
}

const updateOfMinute = (originArr, data) => {
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

const getChartProperties = (initialData) => ({
    type: 'candlestick',
    data: {
        datasets: [{
            label: 'OHLC',
            data: initialData,
            borderColors: {
                up: '#2ebd85',
                down: '#f6465d',
                unchanged: '#999999'
            },
            backgroundColors: {
                up: '#2ebd85',
                down: '#f6465d',
                unchanged: '#999999'
            },
            borderWidth: 1
        }]
    },
    options: {
        responsive: true,
        maintainAspectRatio: false,
        scales: {
            x: {
                type: 'time',
                min: initialData.at(-155)?.x ? initialData.at(-155)?.x : initialData.at(0)?.x,
                max: initialData.at(-1).x + initialData.at(2).x - initialData.at(0).x,
                time: {
                    unit: 'minute',
                    stepSize: 1,
                },
            },
            y: {
                position: 'right',
                // y축도 데이터를 모두 포함하도록 설정
                beginAtZero: false,
                grace: '5%'  // 위아래 5% 여백
            }
        },
        plugins: {
            legend: {
                display: false
            },
            tooltip: {
                enabled: true,
                mode: 'index',
                intersect: false,
                callbacks: {
                    label: function (context) {
                        const point = context.raw;
                        return [
                            `Time: ${point.x}`,
                            `Open: ${point.o}`,
                            `High: ${point.h}`,
                            `Low: ${point.l}`,
                            `Close: ${point.c}`
                        ];
                    }
                }
            },
            zoom: {
                limits: {
                    x: {
                        min: initialData[0].x,
                        max: initialData.at(-1).x + initialData.at(2).x - initialData.at(0).x,
                    }
                },
                pan: {
                    enabled: true,
                    mode: 'x',
                    modifierKey: null,
                    scaleMode: 'x',
                    threshold: 10,
                },
                zoom: {
                    wheel: {
                        enabled: true,
                        speed: 0.1
                    },
                    drag: {
                        enabled: false
                    },
                    pinch: {
                        enabled: true
                    },
                    mode: 'x'
                }
            },
            crosshair: {
                line: {
                    color: '#666',
                    width: 1
                },
                sync: {
                    enabled: false,
                },
                zoom: {
                    enabled: false,
                },
                snap: {
                    enabled: true
                }
            }
        }
    }
})

const drawChart = (chartJquery, initialData) => {
    const chartData = createOfMinute(initialData)


    const chart = new Chart(chartJquery[0].getContext('2d'), getChartProperties(chartData))

    return {
        chart,
        chartData,
    }
}

export default {
    createOfMinute,
    updateOfMinute,
    drawChart
}