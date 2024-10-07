package com.onpremisecloudimplementation.vmmanager.instance.dao;

import java.sql.Date;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.lang.NonNull;

import com.onpremisecloudimplementation.vmmanager.image.dao.Image;
import com.onpremisecloudimplementation.vmmanager.project.dao.Project;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;



@Entity
@Table(name = "vm_instance")
@Setter
@Getter
@NoArgsConstructor
public class Instance {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID userId;

    @CreatedDate
    private Date createdAt;

    private String region;
    private Integer memorySizeGb;
    private Integer diskSizeGb;
    private Integer cpusCount;
    private String name;
    private String description;
    private String internalIp;
    private String externalIp;
    private String state;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne
    @JoinColumn(name = "image_id")
    private Image image;

    @OneToOne(mappedBy = "instance")
    @NonNull
    private InstanceProcessingStatus status;

}
