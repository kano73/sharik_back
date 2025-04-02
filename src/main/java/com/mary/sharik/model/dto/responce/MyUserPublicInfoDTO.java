package com.mary.sharik.model.dto.responce;

import com.mary.sharik.model.entity.MyUser;
import com.mary.sharik.model.enumClass.RoleEnum;

public record MyUserPublicInfoDTO(
        String id,
        String firstName,
        String lastName,
        String address,
        String email,
        RoleEnum role
) {
    public static MyUserPublicInfoDTO fromUser(MyUser myUser) {
        return new MyUserPublicInfoDTO(
                myUser.getId(),
                myUser.getFirstName(),
                myUser.getLastName(),
                myUser.getAddress(),
                myUser.getEmail(),
                myUser.getRole()
        );
    }
}
