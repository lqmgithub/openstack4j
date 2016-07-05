package org.openstack4j.model.common;

import org.openstack4j.model.ModelEntity;

public interface NameEntity extends ModelEntity {
    /**
     * @return the name for this resource
     */
    String getName();
    
    /**
     * Sets the name for this resource.  
     * assign one on the create call
     * 
     * @param name
     */
    void setName(String name);
}
