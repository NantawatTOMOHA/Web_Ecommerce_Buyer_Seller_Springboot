package com.shop.backend.entity;
import jakarta.persistence.*;
import lombok.*;

import java.net.ProtocolFamily;

@Entity
@Table(name= "role")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 50)
    private String name;

}
