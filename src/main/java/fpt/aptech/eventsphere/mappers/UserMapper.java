package fpt.aptech.eventsphere.mappers;

import fpt.aptech.eventsphere.dto.UserDTO;
import fpt.aptech.eventsphere.dto.UserDetailDTO;
import fpt.aptech.eventsphere.models.UserDetails;
import fpt.aptech.eventsphere.models.Users;

public class UserMapper implements BaseMapper<Users, UserDTO>{

    @Override
    public UserDTO toDTO(Users entity) {
        if(entity == null) return null;
        UserDTO dto = new UserDTO();
        dto.setUserId(entity.getUserId());
        dto.setEmail(entity.getEmail());
        if(entity.getUserDetails() != null){
            UserDetails userDetails = entity.getUserDetails();
            UserDetailDTO userDetailDTO = new UserDetailDTO();
            userDetailDTO.setFullName(userDetails.getFullName());
            userDetailDTO.setPhone(userDetails.getPhone());
            dto.setUserDetail(userDetailDTO);
        }
        return dto;
    }

    @Override
    public Users toEntity(UserDTO dto) {
        if(dto == null) return null;
        Users entity = new Users();
        entity.setUserId(dto.getUserId());
        //only give id, then findById in service, temporary only
        return  entity;
    }
}
