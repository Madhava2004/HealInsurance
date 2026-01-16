package com.genc.healthins.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "claim")
public class Claim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "claim_id")
    private Long id;

    @Column(name = "claim_amount")
    private BigDecimal claimAmount;

    @Column(name = "claim_date")
    private LocalDateTime claimDate;

    @Column(name = "claim_status")
    private String claimStatus;

   

    @ManyToOne
    @JoinColumn(name = "policy_id") 
    private UserPolicy policy;
    
    @Column(name = "requested_amount")
    private BigDecimal requestedAmount;
    
    
    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "incident_date")
    private LocalDate incidentDate;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public BigDecimal getClaimAmount() {
		return claimAmount;
	}

	public void setClaimAmount(BigDecimal claimAmount) {
		this.claimAmount = claimAmount;
	}

	public LocalDateTime getClaimDate() {
		return claimDate;
	}

	public void setClaimDate(LocalDateTime claimDate) {
		this.claimDate = claimDate;
	}

	public String getClaimStatus() {
		return claimStatus;
	}

	public void setClaimStatus(String claimStatus) {
		this.claimStatus = claimStatus;
	}



	public UserPolicy getPolicy() {
		return policy;
	}

	public void setPolicy(UserPolicy policy) {
		this.policy = policy;
	}

	public BigDecimal getRequestedAmount() {
		return requestedAmount;
	}

	public void setRequestedAmount(BigDecimal requestedAmount) {
		this.requestedAmount = requestedAmount;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public LocalDate getIncidentDate() {
		return incidentDate;
	}

	public void setIncidentDate(LocalDate incidentDate) {
		this.incidentDate = incidentDate;
	}
   
    
}