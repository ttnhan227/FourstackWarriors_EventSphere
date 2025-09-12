package fpt.aptech.eventsphere.mappers;

import java.util.List;
import java.util.stream.Collectors;

public interface BaseMapper<E, D> {

    D toDTO(E entity);

    E toEntity(D dto);

    default List<D> toDTOList(List<E> entities) {
        if (entities == null) return null;
        return entities.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    default List<E> toEntityList(List<D> dtos) {
        if (dtos == null) return null;
        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}
