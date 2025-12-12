package com.redhat.cases.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * DTO para un producto de Red Hat desde la API Hydra.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductDto {

    private String code;
    private String name;
    private String line;
    private Boolean active;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLine() { return line; }
    public void setLine(String line) { this.line = line; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    @Override
    public String toString() {
        return name + " (" + code + ")";
    }
}
