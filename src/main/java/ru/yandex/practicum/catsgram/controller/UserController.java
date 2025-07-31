package ru.yandex.practicum.catsgram.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.catsgram.exception.ConditionsNotMetException;
import ru.yandex.practicum.catsgram.exception.DuplicatedDataException;
import ru.yandex.practicum.catsgram.exception.NotFoundException;
import ru.yandex.practicum.catsgram.model.User;

import java.time.Instant;
import java.util.*;


@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> getAll() {
        return users.values();
    }

    @PostMapping
    public User addUser(@RequestBody User user) {
        if (user.getEmail() == null) {
            throw new ConditionsNotMetException("Имейл должен быть указан");
        }
        validateEmail(user.getEmail());
        user.setId(getNextId());
        user.setRegistrationDate(Instant.now());
        users.put(user.getId(), user);
        return user;
    }

    @PutMapping
    public User updateUser(@RequestBody User newUser) {
        if (newUser.getId() == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        User user = users.get(newUser.getId());
        if (user == null) {
            throw new NotFoundException("Пользователя с таким id не существует");
        }
        String username = newUser.getUsername();
        String password = newUser.getPassword();
        String email = newUser.getEmail();

        if (username != null) {
            user.setUsername(username);
        }
        if (password != null) {
            user.setPassword(password);
        }
        if (email != null) {
            validateEmail(newUser.getEmail());
            user.setEmail(email);
        }
        return user;
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    private void validateEmail(String email) {
        users.values().stream()
                .map(User::getEmail)
                .filter(exEmail -> exEmail.equals(email))
                .findAny()
                .ifPresent((i) -> {
                    throw new DuplicatedDataException("Этот имейл уже используется");
                });
    }
}
