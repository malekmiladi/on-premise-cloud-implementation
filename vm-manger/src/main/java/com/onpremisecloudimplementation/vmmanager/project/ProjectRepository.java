package com.onpremisecloudimplementation.vmmanager.project;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.onpremisecloudimplementation.vmmanager.project.dao.Project;



public interface ProjectRepository extends JpaRepository<Project, UUID> {
    List<Project> findAllByUserId(UUID userId);
}
