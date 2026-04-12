package com.glpi.change.domain.model;

/**
 * Validation step embedded in a change.
 * status: 1=WAITING, 2=ACCEPTED, 3=REFUSED
 * Requirements: 11.4, 11.5
 */
public class ValidationStep {

    private String id;
    private String validatorId;
    private String validatorKind;
    /** 1=WAITING, 2=ACCEPTED, 3=REFUSED */
    private int status;
    private String comment;
    private int order;

    public ValidationStep() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getValidatorId() { return validatorId; }
    public void setValidatorId(String validatorId) { this.validatorId = validatorId; }

    public String getValidatorKind() { return validatorKind; }
    public void setValidatorKind(String validatorKind) { this.validatorKind = validatorKind; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }
}
