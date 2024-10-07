package com.onpremisecloudimplementation.vmmanager.project;

import org.springframework.web.bind.annotation.RestController;

import com.onpremisecloudimplementation.vmmanager.project.dao.Project;
import com.onpremisecloudimplementation.vmmanager.project.dao.ProjectTag;
import com.onpremisecloudimplementation.vmmanager.project.dto.ProjectCreateRequest;
import com.onpremisecloudimplementation.vmmanager.project.dto.ProjectData;
import com.onpremisecloudimplementation.vmmanager.project.dto.ProjectTagCreateRequest;
import com.onpremisecloudimplementation.vmmanager.project.dto.ProjectTagData;
import com.onpremisecloudimplementation.vmmanager.project.dto.ProjectUpdateRequest;
import com.onpremisecloudimplementation.vmmanager.project.dto.UserProjectsRequest;
import com.onpremisecloudimplementation.vmmanager.utils.GenericModelMapper;
import com.onpremisecloudimplementation.vmmanager.project.dto.UserProjectsData;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

import org.modelmapper.TypeToken;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;



@RestController
@RequiredArgsConstructor
@RequestMapping("/projects")
public class ProjectController {
    
    private final ProjectService projectService;
    private final GenericModelMapper GenericModelMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectData createProject(@RequestBody ProjectCreateRequest projectData) {
        Project project = projectService.createProject(projectData);
        return GenericModelMapper.map(project, ProjectData.class);
    }

    @PutMapping("/{projectId}")
    @ResponseStatus(HttpStatus.OK)
    public ProjectData updateProject(@PathVariable UUID projectId, @RequestBody ProjectUpdateRequest project) {
        Project updatedProject = projectService.updateProject(projectId, project);
        return GenericModelMapper.map(updatedProject, ProjectData.class);
    }

    @DeleteMapping("/{projectId}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteProject(@PathVariable UUID projectId) {
        projectService.deleteProject(projectId);
    }

    @GetMapping
    public List<UserProjectsData> getUserProjects(@RequestBody UserProjectsRequest userProjectsRequest) {
        List<Project> userProjects = projectService.getUserProjects(userProjectsRequest.getId());
        TypeToken<List<UserProjectsData>> returnType = new TypeToken<>() {};
        return GenericModelMapper.map(userProjects, returnType.getType());
    }
    


    @PostMapping("/{projectId}/tags")
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectTagData addTagsToProject(@PathVariable UUID projectId, @RequestBody ProjectTagCreateRequest newTag) {
        ProjectTag tag = projectService.addTagToProject(projectId, newTag);

        ProjectTagData tagCreateResponse = new ProjectTagData();
        tagCreateResponse.setId(tag.getId());
        tagCreateResponse.setValue(newTag.getValue());

        return tagCreateResponse;
    }
    
    @PutMapping("/{projectId}/tags/{tagId}")
    @ResponseStatus(HttpStatus.OK)
    public ProjectTagData updateProjectTag(@PathVariable UUID projectId, @PathVariable UUID tagId, @RequestBody ProjectTag newTag) {
        ProjectTag updatedTag = projectService.updateProjectTag(projectId, tagId, newTag);

        ProjectTagData updatedTagResponse = new ProjectTagData();
        updatedTagResponse.setId(updatedTag.getId());
        updatedTagResponse.setValue(updatedTag.getValue());

        return updatedTagResponse;
    }

    @DeleteMapping("/{projectId}/tags/{tagId}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteProjectTag(@PathVariable UUID projectId, @PathVariable UUID tagId) {
        projectService.deleteProjectTag(projectId, tagId);
    }

}
