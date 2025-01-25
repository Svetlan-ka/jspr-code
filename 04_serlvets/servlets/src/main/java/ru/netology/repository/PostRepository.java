package ru.netology.repository;

import ru.netology.model.Post;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

// Stub
@Repository
public class PostRepository {
    private final AtomicLong idPost = new AtomicLong();
    private final Map<Long, Post> storagePost = new ConcurrentHashMap<>();

    public List<Post> all() {
        return new ArrayList<>(storagePost.values());
    }

    public Optional<Post> getById(long id) {
        return Optional.ofNullable(storagePost.get(id));
    }

    public Post save(Post post) {
        var postId = post.getId();
        if (postId == 0)
            storagePost.put(idPost.incrementAndGet(), post);

        if (postId != 0) {
            if (storagePost.containsKey(postId))
                storagePost.put(postId, post);
        } else {
            throw new NotFoundException();
        }

        return post;
    }

    public void removeById(long id) {
        if (storagePost.containsKey(id)) {
            storagePost.remove(id);
        } else {
            throw new NotFoundException();
        }
    }

}


