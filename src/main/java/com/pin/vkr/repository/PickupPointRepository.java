package com.pin.vkr.repository;

import com.pin.vkr.model.PickupPoint;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class PickupPointRepository {
    private final JdbcTemplate jdbcTemplate;

    public PickupPointRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<PickupPoint> findAll() {
        String sql = "SELECT * FROM pickup_points ORDER BY name";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            PickupPoint point = new PickupPoint();
            point.setId(rs.getLong("id"));
            point.setName(rs.getString("name"));
            point.setAddress(rs.getString("address"));
            point.setPhone(rs.getString("phone"));
            point.setWorkingHours(rs.getString("working_hours"));
            return point;
        });
    }

    public Optional<PickupPoint> findById(Long id) {
        String sql = "SELECT * FROM pickup_points WHERE id = ?";
        try {
            return Optional.of(jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                PickupPoint point = new PickupPoint();
                point.setId(rs.getLong("id"));
                point.setName(rs.getString("name"));
                point.setAddress(rs.getString("address"));
                point.setPhone(rs.getString("phone"));
                point.setWorkingHours(rs.getString("working_hours"));
                return point;
            }, id));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public PickupPoint save(PickupPoint point) {
        String sql = "INSERT INTO pickup_points (name, address, phone, working_hours) VALUES (?, ?, ?, ?) RETURNING id";
        Long id = jdbcTemplate.queryForObject(sql, Long.class,
                point.getName(), point.getAddress(), point.getPhone(), point.getWorkingHours());
        point.setId(id);
        return point;
    }

    public PickupPoint update(PickupPoint point) {
        String sql = "UPDATE pickup_points SET name=?, address=?, phone=?, working_hours=? WHERE id=?";
        jdbcTemplate.update(sql, point.getName(), point.getAddress(), point.getPhone(), point.getWorkingHours(), point.getId());
        return point;
    }

    public void deleteById(Long id) {
        jdbcTemplate.update("DELETE FROM pickup_points WHERE id = ?", id);
    }
}
