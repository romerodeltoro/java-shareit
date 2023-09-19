package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDTO;

import javax.validation.Valid;

@Validated
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserGatewayController {

    private final UserClient userClient;

    @PostMapping
    public ResponseEntity<Object> createUser(@Valid @RequestBody UserDTO userDto) {
        return ResponseEntity.ok().body(userClient.createUser(userDto).getBody());
    }

/*    @PatchMapping("/{userId}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long userId, @RequestBody UserDTO userDto) {
        return ResponseEntity.ok().body(userClient.updateUser(userId, userDto));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDTO> getUser(@PathVariable long userId) {
        return ResponseEntity.ok().body(userClient.getUser(userId));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable long userId) {
        userClient.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok().body(userClient.getAllUsers());
    }*/
}
