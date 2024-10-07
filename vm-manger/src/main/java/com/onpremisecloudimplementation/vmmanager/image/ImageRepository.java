package com.onpremisecloudimplementation.vmmanager.image;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.onpremisecloudimplementation.vmmanager.image.dao.Image;



public interface ImageRepository extends JpaRepository<Image, UUID> {
}
