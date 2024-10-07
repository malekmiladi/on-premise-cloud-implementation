package com.onpremisecloudimplementation.vmorchestrator.instance.dao;

import java.sql.Date;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;

import com.onpremisecloudimplementation.vmorchestrator.computenode.dao.ComputeNode;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
    private UUID instanceId;
    
    @ManyToOne
    @JoinColumn(name = "compute_node_id")
    private ComputeNode computeNode;

    private UUID name;

    private String region;

    @CreatedDate
    private Date createdAt;

}
