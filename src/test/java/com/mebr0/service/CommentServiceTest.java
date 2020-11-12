package com.mebr0.service;

import com.mebr0.entity.Blog;
import com.mebr0.entity.Comment;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestMethodOrder(OrderAnnotation.class)
public class CommentServiceTest {

    @Inject
    CommentService service;

    @Inject
    BlogService blogService;

    private static Long id;
    private static Long blogId = null;

    @BeforeEach
    public void checkOrCreateBlog() {
        if (blogId == null) {
            List<Blog> blogs = blogService.list();

            if (blogs.size() > 0) {
                blogId = blogs.get(0).getId();
            }
            else {
                blogId = blogService.create(Blog.of("qwe", "qwe")).getId();
            }
        }
    }

    @Order(1)
    @Test
    public void testList() {
        var comments = service.list(blogId);

        assertNotNull(comments);

        comments.parallelStream().forEach(comment -> {
            assertNotNull(comment);

            assertNotNull(comment.getId());
            assertNotNull(comment.getText());
            assertNotNull(comment.getBlog());
        });
    }

    @Order(1)
    @Test
    public void testList_notFound() {
        assertThrows(NotFoundException.class, () -> service.list(-1L));
    }

    @Order(2)
    @Test
    public void testCreate() {
        var comment = service.create(blogId, Comment.of("qwe"));

        assertNotNull(comment);
        assertNotNull(comment.getId());
        assertEquals("qwe", comment.getText());
        assertEquals(blogId, comment.getBlog().getId());

        id = comment.getId();
    }

    @Order(2)
    @Test
    public void testCreate_notFound() {
        assertThrows(NotFoundException.class, () -> service.create(-1L, Comment.of("qwe")));
    }

    @Order(3)
    @Test
    public void testGet() {
        var comment = service.get(blogId, id);

        assertNotNull(comment);
        assertNotNull(comment.getId());
        assertNotNull(comment.getText());
        assertEquals(blogId, comment.getBlog().getId());
    }

    @Order(3)
    @Test
    public void testGet_notFoundBlog() {
        assertThrows(NotFoundException.class, () -> service.get(-1L, -1L));
    }

    @Order(3)
    @Test
    public void testGet_notFoundComment() {
        assertThrows(NotFoundException.class, () -> service.get(blogId, -1L));
    }

    @Order(4)
    @Test
    public void testUpdate() {
        Comment comment = service.get(blogId, id);

        assertNotNull(comment);
        assertNotNull(comment.getId());
        assertNotNull(comment.getText());
        assertNotNull(comment.getBlog());

        comment.setText(comment.getText() + "q");

        Comment saved = service.update(blogId, id, comment);

        assertNotNull(saved);
        assertEquals(comment.getId(), saved.getId());
        assertEquals(comment.getText(), saved.getText());
        assertEquals(comment.getBlog().getId(), saved.getBlog().getId());
    }

    @Order(5)
    @Test
    public void testDelete() {
        assertDoesNotThrow(() -> service.delete(blogId, id));
    }

    @Order(5)
    @Test
    public void testDelete_notFoundBlog() {
        assertThrows(NotFoundException.class, () -> service.delete(-1L, -1L));
    }

    @Order(5)
    @Test
    public void testDelete_notFoundComment() {
        assertThrows(NotFoundException.class, () -> service.delete(blogId, -1L));
    }
}
