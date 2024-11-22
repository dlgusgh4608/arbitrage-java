async function fetcher(method = 'GET', url = '', data = {}) {
  const accessToken = getCookie('accessToken')
  
  const authorization = accessToken ? { 'Authorization': `Bearer ${accessToken}` } : {}
  const headers = {
    'Content-Type': 'application/json',
    ...authorization
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
      
    const json = await response.json()
    
    if(Math.floor(response.status / 100) < 4) { // 100 ~ 300
      return { success: true, data: json }
    }else {
      return { success: false, data: json }
    }
  }catch(e) {
    console.error(e)
  }
}