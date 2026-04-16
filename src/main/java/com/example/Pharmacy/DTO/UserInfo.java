package com.example.Pharmacy.DTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserInfo {

    private Integer userId;
    private String userName;
    private String userRole;
}
