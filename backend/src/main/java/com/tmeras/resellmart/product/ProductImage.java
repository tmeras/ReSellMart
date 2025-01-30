package com.tmeras.resellmart.product;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductImage {

    @Id
    @GeneratedValue
    private Integer id;

    private String filePath;
}
