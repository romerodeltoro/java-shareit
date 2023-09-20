package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/requests")
public class RequestGatewayController {

    private final RequestClient requestClient;

    @PostMapping
    public ResponseEntity<Object> createItem(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @Valid @RequestBody ItemRequestDto itemRequestDto) {
        return requestClient.createItemRequest(userId, itemRequestDto);

    }

    @GetMapping
    public ResponseEntity<Object> getAllUserItems(
            @RequestHeader("X-Sharer-User-Id") long userId) {
        return requestClient.getAllUserItemsRequests(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllItems(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @RequestParam(value = "from", defaultValue = "0", required = false) @Min(0) Integer from,
            @RequestParam(value = "size", defaultValue = "10", required = false) @Min(1) @Max(100) Integer size) {
        return requestClient.getAllItems(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getItemRequest(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @PathVariable long requestId) {
        return requestClient.getItemRequest(userId, requestId);
    }
}
