package ru.practicum.shareit.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.user.dto.UserDTO;

import java.util.List;

@Service
public class UserClient extends BaseClient {

    private static final String API_PREFIX = "/users";

    @Autowired
    public UserClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ResponseEntity<Object> createUser(UserDTO userDTO) {
        return post("", userDTO);
    }

    public <T> ResponseEntity<Object> updateUser(long userId, UserDTO userDTO) {
        return patch("/" + userId, userDTO);
    }
    public ResponseEntity<Object> getUser(long userId) { return get("/" + userId); }

    public ResponseEntity<Object> getAllUsers() { return get(""); }

    public ResponseEntity<Object> deleteUser(long userId) {
        return delete("/" + userId);
    }
}
