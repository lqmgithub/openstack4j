package org.openstack4j.openstack.networking.domain.ext;


import org.openstack4j.model.network.ext.LbPoolUpdate;
import org.openstack4j.model.network.ext.builder.LbPoolUpdateBuilder;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.google.common.base.Objects;

/**
 * A entity used to update a lb pool
 * @author liujunpeng
 *
 */
@JsonRootName("pool")
@JsonIgnoreProperties(ignoreUnknown = true)
public class NeutronLbPoolUpdate implements LbPoolUpdate {

	private static final long serialVersionUID = 1L;
	private String name;
	private String description;
	@JsonProperty("lb_method")
	private String lbMethod;
	@JsonProperty("admin_state_up")
	private boolean adminStateUp;
	/**
	 * {@inheritDoc}
	 */
	@Override
	public LbPoolUpdateBuilder toBuilder() {
		return new LbPoolUpdateContreteBuilder(this);
	}

	
	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("adminStateUp", adminStateUp)
				.add("description", description)
				.add("lbMethod", lbMethod)
				.add("name", name)
				.toString();
	}
	

	/**
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public boolean isAdminStateUp() {
		
		return adminStateUp;
	}
	/**
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public String getName() {
		return name;
	}
	/**
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public String getLbMethod() {
		return lbMethod;
	}
	/**
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public String getDescription() {
		return description;
	}


	public static class LbPoolUpdateContreteBuilder implements LbPoolUpdateBuilder{

		private NeutronLbPoolUpdate m;
		public LbPoolUpdateContreteBuilder (){
			this(new NeutronLbPoolUpdate());
		}
		public LbPoolUpdateContreteBuilder(NeutronLbPoolUpdate m){
			this.m = m;
		}
		@Override
		public LbPoolUpdate build() {
			
			return m;
		}

		@Override
		public LbPoolUpdateBuilder from(LbPoolUpdate in) {
			m = (NeutronLbPoolUpdate)in;
			return this;
		}

		/**
		 * 
		 * {@inheritDoc}
		 */
		@Override
		public LbPoolUpdateBuilder lbMethod(String lbMethod) {
			m.lbMethod = lbMethod;
			return this;
		}

		/**
		 * 
		 * {@inheritDoc}
		 */
		@Override
		public LbPoolUpdateBuilder name(String name) {
			m.name = name;
			return this;
		}
		/**
		 * 
		 * {@inheritDoc}
		 */
		@Override
		public LbPoolUpdateBuilder description(String description) {
			m.description = description;
			return this;
		}
		@Override
		public LbPoolUpdateBuilder adminStateUp(boolean adminStateUp) {
			m.adminStateUp = adminStateUp;
			return this;
		}
	}
	public static LbPoolUpdateBuilder builder(){
		return new LbPoolUpdateContreteBuilder();
	}
}
