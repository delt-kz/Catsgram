package ru.yandex.practicum.catsgram.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.catsgram.exception.ConditionsNotMetException;
import ru.yandex.practicum.catsgram.exception.NotFoundException;
import ru.yandex.practicum.catsgram.model.Post;

import java.time.Instant;
import java.util.*;

@Service
public class PostService {
    private final Map<Long, Post> posts = new HashMap<>();
    private final UserService userService;

    public PostService(UserService userService) {
        this.userService = userService;
    }

    public Collection<Post> findAll(Optional<Integer> fromOpt,
                                    Optional<Integer> sizeOpt,
                                    Optional<String> sortOpt) {
        List<Post> subPosts;
        int from = fromOpt.orElse(0);
        int size = sizeOpt.orElse(10);
        if (size < 0) {
            size = 10;
        }
        if (sortOpt.isPresent() && sortOpt.get().equals("desc")) {
            subPosts = posts.values().stream()
                    .sorted(Comparator.comparing(Post::getPostDate).reversed())
                    .skip(from)
                    .limit(size)
                    .toList();
        } else {
            subPosts = posts.values().stream()
                    .sorted(Comparator.comparing(Post::getPostDate))
                    .skip(from)
                    .limit(size)
                    .toList();
        }
        return subPosts;
    }

    public Post create(Post post) {
        if (post.getDescription() == null || post.getDescription().isBlank()) {
            throw new ConditionsNotMetException("Описание не может быть пустым");
        }
        userService.findById(post.getAuthorId()).orElseThrow(() ->
                new ConditionsNotMetException("«Автор с id = " + post.getAuthorId() + " не найден»"));
        post.setId(getNextId());
        post.setPostDate(Instant.now());
        posts.put(post.getId(), post);
        return post;
    }

    public Post update(Post newPost) {
        if (newPost.getId() == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        if (posts.containsKey(newPost.getId())) {
            Post oldPost = posts.get(newPost.getId());
            if (newPost.getDescription() == null || newPost.getDescription().isBlank()) {
                throw new ConditionsNotMetException("Описание не может быть пустым");
            }
            oldPost.setDescription(newPost.getDescription());
            return oldPost;
        }
        throw new NotFoundException("Пост с id = " + newPost.getId() + " не найден");
    }

    private long getNextId() {
        long currentMaxId = posts.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    public Optional<Post> findById(long id) {
        return Optional.ofNullable(posts.get(id));
    }
}