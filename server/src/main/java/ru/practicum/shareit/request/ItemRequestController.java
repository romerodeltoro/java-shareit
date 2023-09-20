package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/requests")
public class ItemRequestController {

    private final ItemRequestService itemRequestService;

    @PostMapping
    public ResponseEntity<ItemRequestDto> createItem(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestBody ItemRequestDto itemRequestDto) {
        return ResponseEntity.ok().body(itemRequestService.createItemRequest(userId, itemRequestDto));

    }

    @GetMapping
    public ResponseEntity<List<ItemRequestDto>> getAllUserItems(
            @RequestHeader("X-Sharer-User-Id") Long userId) {
        return ResponseEntity.ok().body(itemRequestService.getAllUserItemsRequests(userId));
    }

    @GetMapping("/all")
    public ResponseEntity<List<ItemRequestDto>> getAllItems(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestParam(value = "from", defaultValue = "0", required = false) Integer from,
            @RequestParam(value = "size", defaultValue = "10", required = false) Integer size) {
        return ResponseEntity.ok().body(itemRequestService.getAllItems(userId, from, size));
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<ItemRequestDto> getItemRequest(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable Long requestId) {
        return ResponseEntity.ok().body(itemRequestService.getItemRequest(userId, requestId));
    }


}
