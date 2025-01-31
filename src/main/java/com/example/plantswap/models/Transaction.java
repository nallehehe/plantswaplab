package com.example.plantswap.models;


import com.example.plantswap.enumHolder.Status;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;

@Entity
@Table(name = "transactions")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plant_id")
    private Plant plant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plant_trade_id")
    private Plant plantTrade;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status;

    @Enumerated(EnumType.STRING)
    @Column(name = "owner_exchange")
    private Status buyerStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "seller_exchange")
    private Status sellerStatus;

    @Max(1000)
    private Integer totalcost;

    public Transaction() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Plant getPlant() {
        return plant;
    }

    public void setPlant(Plant plant) {
        this.plant = plant;
    }

    public Integer getTotalcost() {
        return totalcost;
    }

    public void setTotalcost(Integer totalcost) {
        this.totalcost = totalcost;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Status getBuyerStatus() {
        return buyerStatus;
    }

    public void setBuyerStatus(Status buyerStatus) {
        this.buyerStatus = buyerStatus;
    }

    public Status getSellerStatus() {
        return sellerStatus;
    }

    public void setSellerStatus(Status sellerStatus) {
        this.sellerStatus = sellerStatus;
    }
}
