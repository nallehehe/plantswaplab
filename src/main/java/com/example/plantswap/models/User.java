package com.example.plantswap.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import org.antlr.v4.runtime.misc.NotNull;

@Entity
@Table(name = "users")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotNull
    @Pattern(regexp = "^[A-Za-z\\s-]+$", message = "First name can only cotain letters and hyphens")
    private String firstName;

    @Pattern(regexp = "^[A-Za-z\\s-]+$", message = "Last name can only contain letters hyphens")
    @NotNull
    private String lastName;

    /*
    private List<Plant> ownedplants;*/

    public User() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /*public List<Plant> getOwnedplants() {
        return ownedplants;
    }

    public void setOwnedplants(List<Plant> ownedplants) {
        this.ownedplants = ownedplants;
    }*/
}
