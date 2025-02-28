import { fetcher } from '../../../init.js';
import { isFetching, isNotFetching } from '../index.js';

let currentTime = 1; // 1, 3, 5, 15
let chart;

let updating = false
let done = false

const getUpdateFlag = (min, visibleMin) => {
    const standardTime = 1000 * 60 * currentTime * 50
    
    return visibleMin - min <= standardTime
}

const setCurrentTime = (timeOfMinutes = 1) => {
    if(!Number(timeOfMinutes)) return // 숫자 아님
    if(timeOfMinutes === currentTime) return // 같은 시간값
    if(![1,3,5,15].some(v => v === timeOfMinutes)) return // 허용된 시간값 아님

    currentTime = timeOfMinutes

    isFetching()
    updating = false
    done = false

    getChartData().then(async data => {
        chart.reset()
        
        const chartData = data
        const { min, max } = getInitialScales(chartData)
        
        chart.data.datasets[0].data = chartData
        chart.scales.x.min = min
        chart.scales.x.max = max
        chart.config.options.plugins.zoom.limits.x.min = chartData[0].x
        
        chart.zoomScale('x', { min, max })
        
        isNotFetching()
    })

}

async function getChartData(lastTime = new Date().getTime()) {
    const { data } = await fetcher('GET', `/api/ohlc?symbol=${symbol}&unit=${currentTime}&lastTime=${lastTime}`)

    if(data.length < 300) {
        done = true
    }
    
    return data.reverse().map(v => ({ ...v, x: v.x * 1000 }))
}

const getInitialScales = (data) => {
    const min = data.at(-155)?.x ? data.at(-155)?.x : data.at(0)?.x
    const max = data.at(-1).x + data.at(2).x - data.at(0).x
    
    return { min, max }
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
                            `Time: ${new Date(point.x).toLocaleTimeString()}`,
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
                    onPan: async ({ chart }) => {
                        const chartData = chart.data.datasets[0].data

                        const { min, max } = chart.scales.x
                        prevZoomScale = { min, max }

                        if(updating) return
                        if(done) return
                        
                        const lastChartAt = chartData.at(0).x
                        if(getUpdateFlag(lastChartAt, chart.getZoomedScaleBounds().x.min)) {
                            updating = true
                            
                            const data = await getChartData(lastChartAt)
                            chartData.unshift(...data)
                            chart.config.options.plugins.zoom.limits.x.min = data[0].x

                            chart.update('none')

                            updating = false
                        }
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
                    onZoom: async ({ chart }) => {
                        const chartData = chart.data.datasets[0].data
                        const zoomLevel = chart.getZoomLevel();
                        const MAXIMUM_ZOOM_LEVEL = 3.5
                        const MINIMUM_ZOOM_LEVEL = 0.3

                        if(zoomLevel * currentTime <= MINIMUM_ZOOM_LEVEL || zoomLevel * currentTime >= MAXIMUM_ZOOM_LEVEL)
                            return chart.zoomScale('x', prevZoomScale, 'none')
                        
                        const { min, max } = chart.scales.x
                        prevZoomScale = { min, max }

                        if(updating) return
                        if(done) return
                        
                        const lastChartAt = chartData.at(0).x
                        if(getUpdateFlag(lastChartAt, chart.getZoomedScaleBounds().x.min)) {
                            updating = true
                            
                            const data = await getChartData(lastChartAt)
                            chartData.unshift(...data)
                            chart.config.options.plugins.zoom.limits.x.min = data[0].x

                            chart.update('none')

                            updating = false
                        }
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

const drawChart = async (chartJquery) => {
    const chartData = await getChartData();

    chart = new Chart(chartJquery[0].getContext('2d'), getChartProperties(chartData))

    return {
        chart,
        chartData,
    }
}

export default {
    updateOfMinute,
    drawChart,
    setCurrentTime,
}