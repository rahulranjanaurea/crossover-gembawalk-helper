package com.crossover.report.service;

import org.springframework.stereotype.Component;

@Component
public class WorkDiariesService extends BaseService {

    @Override
    public String getUrl() {
        return "timetracking/workdiaries";
    }

}
