package com.fiberhome.filemanager.log.bean;

public class ResourceInfo {

    /**
     * 业务类型
     */
    private String resourceType;

    /**
     * 业务ID
     */
    private String resourceId;

    public ResourceInfo() {}

    public ResourceInfo(String resourceType, String resourceId) {
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }
}
