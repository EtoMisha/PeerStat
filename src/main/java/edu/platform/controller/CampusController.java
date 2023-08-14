package edu.platform.controller;

import edu.platform.models.Campus;
import edu.platform.models.Cluster;
import edu.platform.models.User;
import edu.platform.service.CampusService;
import edu.platform.service.ClusterService;
import edu.platform.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/campus")
public class CampusController {

    private final CampusService campusService;
    private final UserService userService;
    private final ClusterService clusterService;

    @GetMapping("/")
    public List<Campus> getCampuses() {
        return campusService.getAll();
    }

    @GetMapping("/{id}/users")
    public List<User> getCampusUsers(@PathVariable Long id, @ParameterObject Pageable pageable) {
        return userService.getCampusUsers(id, pageable);
    }

    @GetMapping("/{id}/clusters")
    public List<Cluster> getCampusClusters(@PathVariable Long id) {
        return clusterService.getCampusClusters(id);
    }

}
