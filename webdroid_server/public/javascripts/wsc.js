function webSocketClient (server, sessionID) {
    this.server = new WebSocket(server);
    this.server.sessionID = sessionID;

    this.server.onopen = function () {
        this.send('{"type":"newSession","data":"1"}');
    };

    this.server.onerror = function (error) {
        console.error('WebSocket Error: ' + error);
    };

    return this.server;
}