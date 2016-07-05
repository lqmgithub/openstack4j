package org.openstack4j.openstack.common;

import org.openstack4j.model.common.NameEntity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;

public class NameResourceEntity implements NameEntity {

    private static final long serialVersionUID = 1L;
    
    @JsonProperty
    private String name;
    
    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
      this.name = name;
    }
    
    @Override
    public String toString() {
        return Objects.toStringHelper(getClass()).omitNullValues()
                     .add("name", name)
                     .toString();
    }
}
