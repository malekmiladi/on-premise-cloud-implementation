package com.onpremisecloudimplementation.vmmanager.image.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.onpremisecloudimplementation.vmmanager.instance.dao.Instance;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;



@Entity
@Table(name = "vm_image")
@Setter
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(value = "instances")
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    private UUID userId;

    private String name;

    private Integer sizeGb;
    
    private Integer minDiskGb;

    private Integer minRAMGb;

    private String downloadUrl;

    private String state;

    private Boolean isPublic;

    @OneToMany(mappedBy = "image")
    private List<Instance> instances = new ArrayList<>();

}
