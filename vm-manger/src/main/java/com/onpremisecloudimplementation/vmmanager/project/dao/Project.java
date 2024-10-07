package com.onpremisecloudimplementation.vmmanager.project.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.lang.NonNull;

import com.onpremisecloudimplementation.vmmanager.instance.dao.Instance;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
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
@Table(name = "project")
@Setter
@Getter
@NoArgsConstructor
public class Project {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @OneToMany(mappedBy = "project", cascade = CascadeType.MERGE)
    @NonNull
    private List<ProjectTag> tags = new ArrayList<ProjectTag>();

    @OneToMany(mappedBy = "project")
    private List<Instance> instances = new ArrayList<Instance>();

}
