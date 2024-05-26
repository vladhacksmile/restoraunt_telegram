package com.vladhacksmile.crm.jdbc;

import lombok.*;

import javax.persistence.*;

@Entity(name = "Restaurant")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "address", nullable = false)
    private String address;

    /**
     * Владелец ресторана
     */
    @Column(name = "userId", nullable = false)
    private Long userId;

    // время работы и т п? закрыт открыт и т п
}
