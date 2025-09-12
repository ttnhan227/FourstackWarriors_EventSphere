package fpt.aptech.eventsphere.mappers;

import fpt.aptech.eventsphere.dto.RegistrationDTO;
import fpt.aptech.eventsphere.models.Registrations;

public class RegistrationMapper implements BaseMapper<Registrations, RegistrationDTO> {

    UserMapper userMapper = new UserMapper();

    @Override
    public RegistrationDTO toDTO(Registrations entity) {
        if(entity == null) return null;
        RegistrationDTO dto = new RegistrationDTO();
        dto.setRegistrationId(entity.getRegistrationId());
        dto.setUser(userMapper.toDTO(entity.getStudent()));
        dto.setEventId(entity.getEvent().getEventId());
        dto.setStatus(entity.getStatus().name());
        dto.setRegisteredOn(entity.getRegisteredOn());
        return dto;
    }

    @Override
    public Registrations toEntity(RegistrationDTO dto) {
        //do nothing for now
        return null;
    }
}
