import groovy.json.JsonSlurper
import groovy.json.JsonOutput

metadata {
    definition (name: "OpenObserve", namespace: "hubitatuser349", author: "Hubitat User 349") {
        capability "Initialize"
    }
    command "disconnect"

    preferences {
        input("org", "text", title: "OpenObserve Org", description: "OpeObserve Organization", required: true)
        input("hostname", "text", title: "OpenObserve Hostname", description: "hostname OpenObserve API", required: true, defaultValue: "api.openobserve.ai")
        input("authToken", "text", title: "Authorization Token for OpenObserve", description: "Authorization token for OpenObserve API access", required: true)
        input("logEnable", "bool", title: "Enable debug logging", description: "", defaultValue: false)
    }
}

void logsOff() {
    log.warn "debug logging disabled..."
    device.updateSetting("logEnable", [value: "false", type: "bool"])
}

void installed() {
    if (logEnable) log.debug "installed()"
    updated()
}

void updated() {
    if (logEnable) log.debug "updated()"
    initialize()
    if (logEnable) runIn(1800, logsOff)
}

String parseLevel(String level) {
    int prival = 1 * 8
    switch (level) {
        case "info": prival += 6; break
        case "debug":
        case "trace": prival += 7; break
        case "warn": prival += 4; break
        case "error": prival += 3; break
        default: prival += 6
    }
    return prival.toString()
}

void parse(String description) {
    hostname = hostname?.trim()
    def descData = new JsonSlurper().parseText(description)
    if ("${descData.id}" != "${device.id}") {
        if (hostname != null) {
            if (logEnable) log.debug "sending: ${description}"
            sendToOpenObserve(description)
        } else {
            log.warn "No log server set"
        }
    }
}

void sendToOpenObserve(String logString) {
    def uri = "https://${hostname}"
    def path = "/api/${org}/stream1/_json"
    def headers = [
        "Authorization": "Basic ${authToken}",
        "Content-Type": "application/json",
        host: hostname
    ]
    def postData = JsonOutput.toJson(["data": logString])
    httpPost(uri, path, postData, headers)
}

void httpPost(String uri, String path, String postData, Map headers) {
    def parameters = [
        uri: uri,
        path: path,
        requestContentType: "application/json",
        contentType: "application/json",
        body: postData,
        headers: headers
    ]

    try {
        asynchttpPost('postCallback', parameters)
    } catch (Exception e) {
        log.error "HTTP POST request failed: ${e}"
    }
}

void postCallback(resp, data) {
    if (resp.status == 200) {
        log.debug "Successfully posted data to server"
    } else {
        log.error "Failed to post data: HTTP status ${resp.status}"
    }
}

void disconnect() {
    interfaces.webSocket.close()
}

void initialize() {
    if (logEnable) log.debug "initialize()"
    log.info "Starting log export to syslog"
    runIn(10, "connect")
}

void connect() {
    if (logEnable) log.debug "attempting connection"
    try {
        interfaces.webSocket.connect("http://localhost:8080/logsocket")
        pauseExecution(1000)
    } catch (Exception e) {
        log.error "initialize error: ${e.message}"
        runIn(60, connect)
    }
}

void uninstalled() {
    disconnect()
}

void webSocketStatus(String message) {
    if (logEnable) log.debug "Got status ${message}"
    if (message.startsWith("failure")) {
        runIn(5, connect)
    }
}