package com.o2b2.devbox_server.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.o2b2.devbox_server.entity.Comment;
import com.o2b2.devbox_server.service.CommentService;
import com.o2b2.devbox_server.exception.ResourceNotFoundException;

@RestController
@RequestMapping("/api/comments")
public class CommentController {
    @Autowired
    private CommentService commentService;

    @GetMapping("/post/{postId}")
    public ResponseEntity<List<Comment>> getCommentsByPostId(@PathVariable Long postId) {
        List<Comment> comments = commentService.getCommentsByPostId(postId);
        return ResponseEntity.ok(comments);
    }

    @PostMapping("/post/{postId}")
    public ResponseEntity<Comment> createComment(@PathVariable Long postId, @RequestBody Comment comment) {
        Comment createdComment = commentService.createComment(postId, comment);
        return ResponseEntity.ok(createdComment);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteComment(@PathVariable Long id) {
        try {
            commentService.deleteComment(id);
            return ResponseEntity.ok().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}