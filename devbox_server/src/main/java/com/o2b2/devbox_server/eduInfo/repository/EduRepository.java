package com.o2b2.devbox_server.eduInfo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.o2b2.devbox_server.eduInfo.model.EduEntity;
import java.util.Optional;


public interface EduRepository extends JpaRepository <EduEntity, Long>{
    Optional<EduEntity> findById(Long id);
}
