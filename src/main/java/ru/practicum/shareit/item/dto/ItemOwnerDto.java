package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.dto.LastBookingDto;
import ru.practicum.shareit.booking.dto.NextBookingDto;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemOwnerDto extends ItemDto{
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private LastBookingDto lastBooking;
    private NextBookingDto nextBooking;

}
