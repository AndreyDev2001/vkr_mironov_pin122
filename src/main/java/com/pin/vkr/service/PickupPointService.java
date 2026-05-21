package com.pin.vkr.service;

import com.pin.vkr.model.PickupPoint;
import com.pin.vkr.repository.PickupPointRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class PickupPointService {
    private final PickupPointRepository repository;

    public PickupPointService(PickupPointRepository repository) {
        this.repository = repository;
    }

    public List<PickupPoint> findAll() { return repository.findAll(); }
    public Optional<PickupPoint> findById(Long id) { return repository.findById(id); }

    @Transactional
    public PickupPoint save(PickupPoint point) {
        if (point.getName() == null || point.getAddress() == null)
            throw new RuntimeException("Название и адрес обязательны");
        return repository.save(point);
    }

    @Transactional
    public PickupPoint update(PickupPoint point) { return repository.update(point); }

    @Transactional
    public void delete(Long id) { repository.deleteById(id); }
}
