package com.crossover.report.service;

import org.springframework.stereotype.Component;

@Component
public class ActivityGroups extends BaseService {

    @Override
    public String getUrl() {
        return "tracker/activity/groups";
    }
}
