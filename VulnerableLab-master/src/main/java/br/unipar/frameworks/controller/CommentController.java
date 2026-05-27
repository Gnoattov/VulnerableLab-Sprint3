package br.unipar.frameworks.controller;

import br.unipar.frameworks.dto.CommentRequest;
import br.unipar.frameworks.model.Comment;
import br.unipar.frameworks.model.Product;
import br.unipar.frameworks.repository.CommentRepository;
import br.unipar.frameworks.repository.ProductRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ProductRepository productRepository;

    @GetMapping("/product/{productId}")
    public List<Comment> getCommentsByProduct(@PathVariable Long productId) {
        return commentRepository.findByProductId(productId);
    }

    @PostMapping
    public ResponseEntity<?> createComment(@Valid @RequestBody CommentRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        Comment comment = new Comment();
        comment.setText(request.getText());
        comment.setProduct(product);
        commentRepository.save(comment);

        return ResponseEntity.ok("Comentário adicionado com sucesso!");
    }
}