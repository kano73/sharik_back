package com.mary.sharik.model.dto.responce;

import com.mary.sharik.model.entity.MyUser;
import com.mary.sharik.model.enums.RoleEnum;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MyUserPublicInfoDTO {
    private String id;
    private String firstName;
    private String lastName;
    private String address;
    private String email;
    private RoleEnum role;

    public static MyUserPublicInfoDTO fromUser(MyUser myUser) {
        if(myUser == null) return null;
        MyUserPublicInfoDTO myUserPublicInfoDTO = new MyUserPublicInfoDTO();
        myUserPublicInfoDTO.setId(myUser.getId());
        myUserPublicInfoDTO.setFirstName(myUser.getFirstName());
        myUserPublicInfoDTO.setLastName(myUser.getLastName());
        myUserPublicInfoDTO.setAddress(myUser.getAddress());
        myUserPublicInfoDTO.setEmail(myUser.getEmail());
        myUserPublicInfoDTO.setRole(myUser.getRole());
        return myUserPublicInfoDTO;
    }

    @Override
    public String toString() {
        return "MyUserPublicInfoDTO{" +
                "id='" + id + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", address='" + address + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                '}';
    }
}
