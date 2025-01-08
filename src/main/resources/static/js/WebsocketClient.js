class WebsocketClient {
    #url = ''
    #socket = null
    #messageHandler = (msg) => { }

    constructor(url) {
        this.#url = url
    }

    connect() {
        const socket = new WebSocket(this.#url)
        this.#socket = socket

        socket.onopen = () => console.log('connected websocket')
        socket.onmessage = (msg) => this.#messageHandler(msg)
        socket.onerror = (error) => console.error("WebSocket 오류 발생:", error)
        socket.onclose = () => console.log("disconnected websocket")
    }


    setMessageHandler(handler = () => { }) {
        this.#messageHandler = handler
    }

    sendMessage(message) {
        if (this.#socket && this.#socket.readyState === this.#socket.OPEN) {
            this.#socket.send(message)
        }
    }

    disconnect() {
        if (this.#socket) {
            this.#socket.close()
        }
    }

}

export default WebsocketClient