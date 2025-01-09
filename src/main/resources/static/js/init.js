const darkSvg = `<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-moon-fill" viewBox="0 0 16 16"><path d="M6 .278a.77.77 0 0 1 .08.858 7.2 7.2 0 0 0-.878 3.46c0 4.021 3.278 7.277 7.318 7.277q.792-.001 1.533-.16a.79.79 0 0 1 .81.316.73.73 0 0 1-.031.893A8.35 8.35 0 0 1 8.344 16C3.734 16 0 12.286 0 7.71 0 4.266 2.114 1.312 5.124.06A.75.75 0 0 1 6 .278" /></svg>`
const lightSvg = `<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-sun-fill" viewBox="0 0 16 16"><path d="M8 12a4 4 0 1 0 0-8 4 4 0 0 0 0 8M8 0a.5.5 0 0 1 .5.5v2a.5.5 0 0 1-1 0v-2A.5.5 0 0 1 8 0m0 13a.5.5 0 0 1 .5.5v2a.5.5 0 0 1-1 0v-2A.5.5 0 0 1 8 13m8-5a.5.5 0 0 1-.5.5h-2a.5.5 0 0 1 0-1h2a.5.5 0 0 1 .5.5M3 8a.5.5 0 0 1-.5.5h-2a.5.5 0 0 1 0-1h2A.5.5 0 0 1 3 8m10.657-5.657a.5.5 0 0 1 0 .707l-1.414 1.415a.5.5 0 1 1-.707-.708l1.414-1.414a.5.5 0 0 1 .707 0m-9.193 9.193a.5.5 0 0 1 0 .707L3.05 13.657a.5.5 0 0 1-.707-.707l1.414-1.414a.5.5 0 0 1 .707 0m9.193 2.121a.5.5 0 0 1-.707 0l-1.414-1.414a.5.5 0 0 1 .707-.707l1.414 1.414a.5.5 0 0 1 0 .707M4.464 4.465a.5.5 0 0 1-.707 0L2.343 3.05a.5.5 0 1 1 .707-.707l1.414 1.414a.5.5 0 0 1 0 .708" /></svg>`

const getSvgHtml = (isDark = true) => {
    if (isDark) return darkSvg
    else return lightSvg
}

const light = isLight()

if (light) {
    $('#themeIcon').append(getSvgHtml(false))
    $('html').removeAttr('data-theme')
    $('html').removeAttr('data-bs-theme')
    $('#header-logo').attr('src', "/images/logo-black.png")
} else {
    // 기본값을 다크모드로 설정
    $('html').attr('data-theme', 'dark')
    $('html').attr('data-bs-theme', 'dark')
    localStorage.setItem('theme', 'dark')

    $('#themeIcon').append(getSvgHtml(true))
    $('#header-logo').attr('src', "/images/logo-white.png")
}

export function isLight() {
    const savedTheme = localStorage.getItem('theme')
    return savedTheme === 'light'
}

function toggleDarkMode() {
    const $html = $('html')
    const light = isLight()

    if (light) {
        $html.attr('data-theme', 'dark')
        $html.attr('data-bs-theme', 'dark')
        localStorage.setItem('theme', 'dark')

        $('#themeIcon').html(getSvgHtml(true))
        $('#header-logo').attr('src', "/images/logo-white.png")
    } else {
        $html.removeAttr('data-theme')
        $html.removeAttr('data-bs-theme')
        localStorage.setItem('theme', 'light')

        $('#themeIcon').html(getSvgHtml(false))
        $('#header-logo').attr('src', "/images/logo-black.png")
    }
}

async function fetcher(method = 'GET', url = '', data = {}) {
    const headers = {
        'Content-Type': 'application/json',
    }

    const payload = {
        method: method.toLocaleUpperCase(),
        headers,
        body: JSON.stringify(data),
        credentials: 'include'
    }

    if(method.toLocaleUpperCase() === 'GET') delete payload.body

    try {
        const response = await fetch(url, payload)

        if (response.redirected) {
            window.location.href = response.url;
            return; // 함수 종료
        }

        try {
            const json = await response.json()

            if (Math.floor(response.status / 100) < 4) { // 100 ~ 300
                return { success: true, data: json }
            } else {
                const message = json.message ? json.message : '알 수 없는 에러입니다. 다시 시도해주세요.'
                return { success: false, data: message }
            }
        } catch (e) {
            if (Math.floor(response.status / 100) < 4) { // 100 ~ 300
                return { success: true, data: response }
            } else {
                const message = response.message ? response.message : '알 수 없는 에러입니다. 다시 시도해주세요.'
                return { success: false, data: message }
            }
        }

    } catch (e) {
        console.error(e)
    }
}

window.myModule.fetch = fetcher
window.myModule.isLight = isLight
window.myModule.toggleDarkMode = toggleDarkMode