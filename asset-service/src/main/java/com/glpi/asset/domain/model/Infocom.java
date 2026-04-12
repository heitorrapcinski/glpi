package com.glpi.asset.domain.model;

import java.time.Instant;

/**
 * Financial information value object embedded in an asset.
 * Requirements: 12.11
 */
public class Infocom {

    private Instant purchaseDate;
    private double purchasePrice;
    private Instant warrantyExpiry;
    private String orderNumber;
    private Instant deliveryDate;
    private double depreciationRate;

    public Infocom() {}

    public Instant getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(Instant purchaseDate) { this.purchaseDate = purchaseDate; }

    public double getPurchasePrice() { return purchasePrice; }
    public void setPurchasePrice(double purchasePrice) { this.purchasePrice = purchasePrice; }

    public Instant getWarrantyExpiry() { return warrantyExpiry; }
    public void setWarrantyExpiry(Instant warrantyExpiry) { this.warrantyExpiry = warrantyExpiry; }

    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

    public Instant getDeliveryDate() { return deliveryDate; }
    public void setDeliveryDate(Instant deliveryDate) { this.deliveryDate = deliveryDate; }

    public double getDepreciationRate() { return depreciationRate; }
    public void setDepreciationRate(double depreciationRate) { this.depreciationRate = depreciationRate; }
}
