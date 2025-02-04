package com.example.plantswap.models;

import com.example.plantswap.enums.Growth;
import com.example.plantswap.enums.ItemStatus;
import com.example.plantswap.enums.Level;
import com.example.plantswap.enums.Status;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import org.antlr.v4.runtime.misc.NotNull;

@Entity
@Table(name = "plant")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Plant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotNull
    private String name;

    @NotNull
    private Integer age;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "growth")
    private Growth growth;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "light")
    private Level light;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "water")
    private Level water;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "care_difficulty")
    private Level careDifficulty;

    @NotNull
    private String plantUrl;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "trade_or_sale")
    private ItemStatus itemStatus;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status;

    @Positive
    @Max(1000)
    private Integer price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id")
    @NotNull
    private User user;

    public Plant() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Level getLight() {
        return light;
    }

    public void setLight(Level light) {
        this.light = light;
    }

    public Level getWater() {
        return water;
    }

    public void setWater(Level water) {
        this.water = water;
    }

    public Level getCareDifficulty() {
        return careDifficulty;
    }

    public void setCareDifficulty(Level careDifficulty) {
        this.careDifficulty = careDifficulty;
    }

    public String getPlantUrl() {
        return plantUrl;
    }

    public void setPlantUrl(String plantUrl) {
        this.plantUrl = plantUrl;
    }

    public ItemStatus getItemStatus() {
        return itemStatus;
    }

    public void setItemStatus(ItemStatus itemStatus) {
        this.itemStatus = itemStatus;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }


    public Growth getGrowth() {
        return growth;
    }

    public void setGrowth(Growth growth) {
        this.growth = growth;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }
}
