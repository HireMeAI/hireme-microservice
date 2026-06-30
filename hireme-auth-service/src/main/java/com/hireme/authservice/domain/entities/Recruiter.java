package com.hireme.authservice.domain.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "recruiters")
@PrimaryKeyJoinColumn(name = "user_id") // <--- La clé qui fait le lien avec la table Users
@SuperBuilder // <--- Indispensable pour construire l'objet complet
@EqualsAndHashCode(callSuper = true)
public class Recruiter extends User {
    @Column(name = "actual_position")
    private String actualPosition;

    @Column(name = "phone_number")
    private int phoneNumber;


}
