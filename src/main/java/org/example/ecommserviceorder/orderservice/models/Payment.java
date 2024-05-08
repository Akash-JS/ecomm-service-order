package org.example.ecommserviceorder.orderservice.models;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;
import java.time.Instant;

public class Payment {

    @SerializedName("id")
    private String id;

    @SerializedName("orderId")
    private String orderId;

    @SerializedName("paymentMethod")
    private String paymentMethod;

    @SerializedName("amount")
    private BigDecimal amount;

    @SerializedName("paymentDate")
    private Instant paymentDate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Instant getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(Instant paymentDate) {
        this.paymentDate = paymentDate;
    }

}