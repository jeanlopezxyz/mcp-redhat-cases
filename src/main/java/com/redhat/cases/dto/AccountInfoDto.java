package com.redhat.cases.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * DTO para la informacion de cuenta de Red Hat desde la API.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountInfoDto {

    private String accountId;
    private String accountNumber;
    private String accountAlias;
    private String name;
    private String description;
    private String accountStatus;
    private String superRegion;
    private String identifyingAddressCountry;
    private Boolean isPartner;
    private Boolean isActive;
    private Boolean hasChat;
    private Boolean hasEnhancedSLA;
    private Boolean secureSupport;
    private Boolean strategic;

    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getAccountAlias() { return accountAlias; }
    public void setAccountAlias(String accountAlias) { this.accountAlias = accountAlias; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAccountStatus() { return accountStatus; }
    public void setAccountStatus(String accountStatus) { this.accountStatus = accountStatus; }

    public String getSuperRegion() { return superRegion; }
    public void setSuperRegion(String superRegion) { this.superRegion = superRegion; }

    public String getIdentifyingAddressCountry() { return identifyingAddressCountry; }
    public void setIdentifyingAddressCountry(String identifyingAddressCountry) { this.identifyingAddressCountry = identifyingAddressCountry; }

    public Boolean getIsPartner() { return isPartner; }
    public void setIsPartner(Boolean isPartner) { this.isPartner = isPartner; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Boolean getHasChat() { return hasChat; }
    public void setHasChat(Boolean hasChat) { this.hasChat = hasChat; }

    public Boolean getHasEnhancedSLA() { return hasEnhancedSLA; }
    public void setHasEnhancedSLA(Boolean hasEnhancedSLA) { this.hasEnhancedSLA = hasEnhancedSLA; }

    public Boolean getSecureSupport() { return secureSupport; }
    public void setSecureSupport(Boolean secureSupport) { this.secureSupport = secureSupport; }

    public Boolean getStrategic() { return strategic; }
    public void setStrategic(Boolean strategic) { this.strategic = strategic; }
}
