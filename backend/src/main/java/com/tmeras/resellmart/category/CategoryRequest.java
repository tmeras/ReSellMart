package com.tmeras.resellmart.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryRequest {

    private Integer id;

    @NotBlank(message = "Name must not be empty")
    private String name;

    @NotNull(message = "Parent category ID must not be empty")
    private Integer parentId;
}
