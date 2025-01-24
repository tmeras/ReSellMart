package com.tmeras.resellmart.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CategoryRequest {

    private Integer id;

    @NotBlank(message = "Name must not be empty")
    private String name;

    private Integer parentId;
}
