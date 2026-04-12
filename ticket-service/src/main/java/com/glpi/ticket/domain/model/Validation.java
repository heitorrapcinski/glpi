package com.glpi.ticket.domain.model;

/**
 * TicketValidation embedded in a ticket.
 * Requirements: 8.1
 */
public class Validation {

    private String id;
    private String validatorId;
    private String validatorKind;
    /** 1=WAITING, 2=ACCEPTED, 3=REFUSED, 4=NONE */
    private int status;
    private String comment;

    public Validation() {}

    public Validation(String id, String validatorId, String validatorKind, int status, String comment) {
        this.id = id;
        this.validatorId = validatorId;
        this.validatorKind = validatorKind;
        this.status = status;
        this.comment = comment;
    }

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
}
