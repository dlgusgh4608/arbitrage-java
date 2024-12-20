async function fetcher(method = 'GET', url = '', data = {}) {
    const headers = {
        'Content-Type': 'application/json',
    }

    try {
        const response = await fetch(
            url, {
            method,
            headers,
            body: JSON.stringify(data),
            credentials: 'include'
        })

        if (response.redirected) {
            window.location.href = response.url;
            return; // 함수 종료
        }

        try {
            const json = await response.json()

            if (Math.floor(response.status / 100) < 4) { // 100 ~ 300
                return { success: true, data: json }
            } else {
                return { success: false, data: json }
            }
        } catch (e) {
            if (Math.floor(response.status / 100) < 4) { // 100 ~ 300
                return { success: true, data: response }
            } else {
                return { success: false, data: response }
            }
        }

    } catch (e) {
        console.error(e)
    }
}