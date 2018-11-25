package com.crossover.report.service;

import org.springframework.stereotype.Component;

@Component
public class TeamAssignementService extends BaseService implements IService {

    @Override
    public String getUrl() {
        return "v2/teams/assignments";
    }

}
