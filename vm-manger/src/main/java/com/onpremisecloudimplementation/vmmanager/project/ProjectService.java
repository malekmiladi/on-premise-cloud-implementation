package com.onpremisecloudimplementation.vmmanager.project;

import com.onpremisecloudimplementation.vmmanager.project.dao.Project;
import com.onpremisecloudimplementation.vmmanager.project.dao.ProjectTag;
import com.onpremisecloudimplementation.vmmanager.project.dto.ProjectCreateRequest;
import com.onpremisecloudimplementation.vmmanager.project.dto.ProjectTagCreateRequest;
import com.onpremisecloudimplementation.vmmanager.project.dto.ProjectUpdateRequest;
import com.onpremisecloudimplementation.vmmanager.utils.GenericModelMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectTagRepository projectTagRepository;
    private final GenericModelMapper genericModelMapper;

    public Project createProject(ProjectCreateRequest newProject) {
        Project project = genericModelMapper.map(newProject, Project.class);
        List<ProjectTag> tags = project.getTags();
        project = projectRepository.save(project);

        for (ProjectTag tag : tags) {
            tag.setProject(project);
            projectTagRepository.save(tag);
        }
        
        return project;
    }

    public ProjectTag addTagToProject(UUID projectId, ProjectTagCreateRequest newTag) {
        Optional<Project> optionalProject = projectRepository.findById(projectId);
        Project project = null;
        if (optionalProject.isPresent()) {
            project = optionalProject.get();
        }
        ProjectTag tag = genericModelMapper.map(newTag, ProjectTag.class);
        tag.setProject(project);
        return projectTagRepository.save(tag);
    }

    public ProjectTag updateProjectTag(UUID projectId, UUID tagId, ProjectTag newTag) {
        Optional<ProjectTag> optionalTag = projectTagRepository.findById(tagId);
        if (optionalTag.isPresent()) {
            ProjectTag tag = optionalTag.get();
            tag.setValue(newTag.getValue());
            return projectTagRepository.save(tag);
        }
        return null;
    }

    public Project updateProject(UUID projectId, ProjectUpdateRequest newProject) {
        Optional<Project> optionalProject = projectRepository.findById(projectId);
        if (optionalProject.isPresent()) {
            Project project = optionalProject.get();
            project.setDescription(newProject.getDescription());
            project.setName(newProject.getName());
            return projectRepository.save(project);
        }
        return null;
    }

    public void deleteProject(UUID projectId) {
        Optional<Project> optionalProject = projectRepository.findById(projectId);
        if (optionalProject.isPresent()) {
            Project project = optionalProject.get();
            for (ProjectTag tag : project.getTags()) {
                projectTagRepository.delete(tag);
            }
            projectRepository.delete(project);
        }
    }

    public void deleteProjectTag(UUID projectId, UUID tagId) {
        projectTagRepository.deleteById(tagId);
    }

    public List<Project> getUserProjects(UUID userId) {
        return projectRepository.findAllByUserId(userId);
    }

}
