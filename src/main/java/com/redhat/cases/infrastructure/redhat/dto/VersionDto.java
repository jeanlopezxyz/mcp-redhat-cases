package com.redhat.cases.infrastructure.redhat.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * DTO para una versión de producto de Red Hat desde la API Hydra.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class VersionDto {

    private String id;
    private String name;
    private String description;
    private Boolean isDefault;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Boolean getIsDefault() { return isDefault; }
    public void setIsDefault(Boolean isDefault) { this.isDefault = isDefault; }

    @Override
    public String toString() {
        return name + (Boolean.TRUE.equals(isDefault) ? " (default)" : "");
    }
}
