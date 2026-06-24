package com.insurance.api.model;

public class ClaimRequest {
    private String policyId;
    private String customerId;
    private double claimAmount;
    private String userId;
    private String userRole;

    // --- GETTERS ---
    public String getPolicyId() {return policyId; }
    public String getCustomerId() {return customerId; }
    public double getClaimAmount() {return claimAmount; }
    public String getUserId() {return userId; }
    public String getUserRole() {return userRole; }

    // --- SETTERS ---
    public void setPolicyId(String policyId) {this.policyId = policyId; }
    public void setCustomerId(String customerId) {this.customerId = customerId; }
    public void setClaimAmount(double claimAmount) {this.claimAmount = claimAmount; }
    public void setUserId(String userId) {this.userId = userId; }
    public void setUserRole(String userRole) {this.userRole = userRole; }
}
