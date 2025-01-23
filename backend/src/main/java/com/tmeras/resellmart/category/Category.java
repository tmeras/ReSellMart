package com.tmeras.resellmart.category;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
@NoArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue
    private Integer id;

    private String name;

    // TODO: Add self-refelential relationship

}
