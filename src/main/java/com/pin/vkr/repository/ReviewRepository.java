package com.pin.vkr.repository;

import com.pin.vkr.model.Reply;
import com.pin.vkr.model.Review;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class ReviewRepository {
    private final JdbcTemplate jdbcTemplate;

    public ReviewRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Review> findByProductId(Long productId) {
        String sql = """
            SELECT r.id, r.product_id, r.user_id, u.username, r.rating, 
                   r.comment, r.created_at
            FROM reviews r
            LEFT JOIN users u ON r.user_id = u.id
            WHERE r.product_id = ?
            ORDER BY r.created_at DESC
            """;
        return jdbcTemplate.query(sql, new ReviewRowMapper(), productId);
    }

    public List<Reply> findByReviewId(Long reviewId) {
        String sql = """
            SELECT rep.id, rep.review_id, rep.user_id, u.username, 
                   rep.text, rep.created_at
            FROM review_replies rep
            LEFT JOIN users u ON rep.user_id = u.id
            WHERE rep.review_id = ?
            ORDER BY rep.created_at ASC
            """;
        return jdbcTemplate.query(sql, new ReplyRowMapper(), reviewId);
    }

    public void save(Review review) {
        String sql = "INSERT INTO reviews (product_id, user_id, rating, comment) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, review.getProductId(), review.getUserId(),
                review.getRating(), review.getComment());
    }

    public void saveReply(Reply reply) {
        String sql = "INSERT INTO review_replies (review_id, user_id, text) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, reply.getReviewId(), reply.getUserId(), reply.getText());
    }

    private static class ReviewRowMapper implements RowMapper<Review> {
        @Override
        public Review mapRow(ResultSet rs, int rowNum) throws SQLException {
            Review review = new Review();
            review.setId(rs.getLong("id"));
            review.setProductId(rs.getLong("product_id"));
            review.setUserId(rs.getLong("user_id"));
            review.setUsername(rs.getString("username"));
            review.setRating(rs.getInt("rating"));
            review.setComment(rs.getString("comment"));
            review.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            return review;
        }
    }

    private static class ReplyRowMapper implements RowMapper<Reply> {
        @Override
        public Reply mapRow(ResultSet rs, int rowNum) throws SQLException {
            Reply reply = new Reply();
            reply.setId(rs.getLong("id"));
            reply.setReviewId(rs.getLong("review_id"));
            reply.setUserId(rs.getLong("user_id"));
            reply.setUsername(rs.getString("username"));
            reply.setText(rs.getString("text"));
            reply.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            return reply;
        }
    }
}
