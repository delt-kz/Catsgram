package ru.yandex.practicum.catsgram.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.catsgram.exception.ConditionsNotMetException;
import ru.yandex.practicum.catsgram.exception.DuplicatedDataException;
import ru.yandex.practicum.catsgram.exception.NotFoundException;
import ru.yandex.practicum.catsgram.model.User;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {
    private final Map<Long, User> users = new HashMap<>();

    public Collection<User> getAll() {
        return users.values();
    }

    public User addUser(User user) {
        if (user.getEmail() == null) {
            throw new ConditionsNotMetException("Имейл должен быть указан");
        }
        validateEmail(user.getEmail());
        user.setId(getNextId());
        user.setRegistrationDate(Instant.now());
        users.put(user.getId(), user);
        return user;
    }

    public User updateUser(User newUser) {
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

    public Optional<User> findById(long id) {
        return Optional.ofNullable(users.get(id));
    }
}
