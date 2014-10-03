package org.openstack4j.openstack.common;

import java.net.URI;
import java.util.Date;
import java.util.List;

import org.openstack4j.model.common.Extension;
import org.openstack4j.model.common.Link;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.google.common.base.Objects;

/**
 * Represents an Extension which adds additional functionality to the OpenStack API
 * 
 * @author Jeremy Unruh
 */
public class ExtensionValue implements Extension {

	private static final long serialVersionUID = 1L;
	String name;
	URI namespace;
	String alias;
	Date updated;
	String description;
	List<GenericLink> links;
	
	/**
	 * {@inheritDoc}
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public URI getNamespace() {
		return namespace;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getAlias() {
		return alias;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Date getUpdated() {
		return updated;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public List<? extends Link> getLinks() {
		return links;
	}
	
	@Override
	public String toString() {
		return Objects.toStringHelper(Extension.class).omitNullValues()
						.add("name", name).add("namespace", namespace).add("description", description)
						.add("alias", alias).add("updated", updated).add("links", links)
						.addValue("\n")
						.toString();
	}
	
	@JsonRootName("extensions")
	public static class Extensions extends ListResult<ExtensionValue> {

		private static final long serialVersionUID = 1L;
		@JsonProperty("values")
		private List<ExtensionValue> list;
		
		public List<ExtensionValue> value() {
			return list;
		}
	}
	
	public static class NovaExtensions extends ListResult<ExtensionValue> {

		private static final long serialVersionUID = 1L;
		@JsonProperty("extensions")
		private List<ExtensionValue> list;
		
		public List<ExtensionValue> value() {
			return list;
		}
	}
}
