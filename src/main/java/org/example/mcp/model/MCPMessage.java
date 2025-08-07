package org.example.mcp.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)

public class MCPMessage {
    @JsonProperty("jsonrpc")
    private String jsonrpc = "2.0";

    @JsonProperty("id")
    private Object id;

    @JsonProperty("method")
    private String method;

    @JsonProperty("params")
    private Object params;

    @JsonProperty("result")
    private Object result;

    @JsonProperty("error")
    private MCPError error;

    // Constructores
    public MCPMessage() {}

    public MCPMessage(Object id, String method, Object params) {
        this.id = id;
        this.method = method;
        this.params = params;
    }

    // Getters y Setters
    public String getJsonrpc() { return jsonrpc; }
    public void setJsonrpc(String jsonrpc) { this.jsonrpc = jsonrpc; }

    public Object getId() { return id; }
    public void setId(Object id) { this.id = id; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public Object getParams() { return params; }
    public void setParams(Object params) { this.params = params; }

    public Object getResult() { return result; }
    public void setResult(Object result) { this.result = result; }

    public MCPError getError() { return error; }
    public void setError(MCPError error) { this.error = error; }
}