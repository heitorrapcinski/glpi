package com.glpi.ticket.domain.model;

import java.time.Instant;

/**
 * Embedded SLA/OLA deadline context on a ticket.
 * Requirements: 9.1, 9.2, 9.3, 9.4, 9.5, 9.6, 9.8
 */
public class SlaContext {

    private String slaId;
    private Instant ttoDeadline;
    private Instant ttrDeadline;
    /** Total seconds spent in WAITING status */
    private long waitingDuration;
    private Instant waitingStart;
    private String olaId;
    private Instant internalTtoDeadline;
    private Instant internalTtrDeadline;

    public SlaContext() {}

    public SlaContext(String slaId, Instant ttoDeadline, Instant ttrDeadline,
                      long waitingDuration, Instant waitingStart,
                      String olaId, Instant internalTtoDeadline, Instant internalTtrDeadline) {
        this.slaId = slaId;
        this.ttoDeadline = ttoDeadline;
        this.ttrDeadline = ttrDeadline;
        this.waitingDuration = waitingDuration;
        this.waitingStart = waitingStart;
        this.olaId = olaId;
        this.internalTtoDeadline = internalTtoDeadline;
        this.internalTtrDeadline = internalTtrDeadline;
    }

    public String getSlaId() { return slaId; }
    public void setSlaId(String slaId) { this.slaId = slaId; }

    public Instant getTtoDeadline() { return ttoDeadline; }
    public void setTtoDeadline(Instant ttoDeadline) { this.ttoDeadline = ttoDeadline; }

    public Instant getTtrDeadline() { return ttrDeadline; }
    public void setTtrDeadline(Instant ttrDeadline) { this.ttrDeadline = ttrDeadline; }

    public long getWaitingDuration() { return waitingDuration; }
    public void setWaitingDuration(long waitingDuration) { this.waitingDuration = waitingDuration; }

    public Instant getWaitingStart() { return waitingStart; }
    public void setWaitingStart(Instant waitingStart) { this.waitingStart = waitingStart; }

    public String getOlaId() { return olaId; }
    public void setOlaId(String olaId) { this.olaId = olaId; }

    public Instant getInternalTtoDeadline() { return internalTtoDeadline; }
    public void setInternalTtoDeadline(Instant internalTtoDeadline) { this.internalTtoDeadline = internalTtoDeadline; }

    public Instant getInternalTtrDeadline() { return internalTtrDeadline; }
    public void setInternalTtrDeadline(Instant internalTtrDeadline) { this.internalTtrDeadline = internalTtrDeadline; }
}
