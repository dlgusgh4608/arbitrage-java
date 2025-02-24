let currentTime = 1; // 1, 3, 5, 15

const setCurrentTime = (timeOfMinutes = 1) => {
    if(!Number(timeOfMinutes)) return // 숫자 아님
    if(timeOfMinutes === currentTime) return // 같은 시간값
    if(![1,3,5,15].some(v => v === timeOfMinutes)) return // 허용된 시간값 아님

    currentTime = timeOfMinutes
}



const genChartData = (date, p, minutes) => {
    const loadCount = 300 * minutes * 12
    const interval = 5000; // 5초 (ms)
    let premium = p
    const result = []

    for (let i = 0; i < loadCount; i++) {
        const currentTime = new Date(date.getTime() - i * interval);
        
        // 가격에 약간의 변동 추가 (랜덤 워크 예시)
        premium += (Math.random() - 0.5) * 0.5;
        
        result.push({
            symbol: "ETH",
            premium: parseFloat(premium.toFixed(4)),
            createdAt: currentTime.toISOString()
        });
    }
    return result
}


const createOfMinute = (data) => {
    const aMinuteMs = 60 * 1000 * currentTime;
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
    const aMinuteMs = 60 * 1000  * currentTime;
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

function getVisibleData({ min, max }, chart) {
    const chartData = chart.data.datasets[0].data
    const visibleData = chartData.filter(({ x }) => x >= min && x <= max)
    return {
        first: visibleData.at(0),
        last: visibleData.at(-1),
        count: visibleData.length
    }
  }

let prevZoomScale = {
    min: null,
    max: null
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
                    // modifierKey: null,
                    // scaleMode: 'x',
                    // threshold: 10,
                    onPan: ({ chart }) => {
                        const chartData = chart.data.datasets[0].data
                        // const { first, last, count } = getVisibleData(chart.scales.x, chart);
                        // const firstChart = chartData.at(0)
                        // const lastChart = chartData.at(-1)

                        const lastChartData = chartData.at(0)

                        const a = genChartData(new Date(lastChartData.x), lastChartData.o, 3)
                        const b = createOfMinute(a)
                        chartData.unshift(...b)

                        const { min, max } = chart.scales.x
                        console.log({ min, max, init: lastChartData.x })
                        prevZoomScale = { min, max }

                        // chart.update('none')
                        console.log('updated')
                    }
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
                    mode: 'x',
                    onZoom: ({ chart }) => {
                        const chartData = chart.data.datasets[0].data
                        const zoomLevel = chart.getZoomLevel();
                        const MAXIMUM_ZOOM_LEVEL = 3.5
                        const MINIMUM_ZOOM_LEVEL = 0.3

                        console.log(chartData.length)

                        // console.log({ min: chart.scales.x.min, max: chart.scales.x.max })
                        console.log({ min: chartData.at(0).x, max: chartData.at(-1).x })
                        
                        if(zoomLevel <= MINIMUM_ZOOM_LEVEL || zoomLevel >= MAXIMUM_ZOOM_LEVEL)
                            return chart.zoomScale('x', prevZoomScale, 'none')
                        
                        const { min, max } = chart.scales.x
                        prevZoomScale = { min, max }
                    },
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
    drawChart,
    setCurrentTime
}