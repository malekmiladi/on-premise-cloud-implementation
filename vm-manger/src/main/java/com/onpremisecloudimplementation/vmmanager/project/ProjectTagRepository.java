package com.onpremisecloudimplementation.vmmanager.project;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.onpremisecloudimplementation.vmmanager.project.dao.ProjectTag;



public interface ProjectTagRepository extends JpaRepository<ProjectTag, UUID> {
}
