package com.example.easypc.data.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BuildSaveRequest {

    private String buildName;

    public BuildSaveRequest(String buildName) {
        this.buildName = buildName;
    }

    public BuildSaveRequest() {
    }
}
