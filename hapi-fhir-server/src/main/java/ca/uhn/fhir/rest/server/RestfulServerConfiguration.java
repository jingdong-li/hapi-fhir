package ca.uhn.fhir.rest.server;

/*
 * #%L
 * HAPI FHIR - Server Framework
 * %%
 * Copyright (C) 2014 - 2021 Smile CDR, Inc.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.RuntimeResourceDefinition;
import ca.uhn.fhir.context.RuntimeSearchParam;
import ca.uhn.fhir.rest.api.RestSearchParameterTypeEnum;
import ca.uhn.fhir.rest.server.method.BaseMethodBinding;
import ca.uhn.fhir.rest.server.method.OperationMethodBinding;
import ca.uhn.fhir.rest.server.method.SearchMethodBinding;
import ca.uhn.fhir.rest.server.method.SearchParameter;
import ca.uhn.fhir.rest.server.util.ISearchParamRegistry;
import ca.uhn.fhir.util.VersionUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.instance.model.api.IPrimitiveType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class RestfulServerConfiguration implements ISearchParamRegistry {

	private static final Logger ourLog = LoggerFactory.getLogger(RestfulServerConfiguration.class);
	private Collection<ResourceBinding> resourceBindings;
	private List<BaseMethodBinding<?>> serverBindings;
	private Map<String, Class<? extends IBaseResource>> resourceNameToSharedSupertype;
	private String implementationDescription;
	private String serverVersion = VersionUtil.getVersion();
	private String serverName = "HAPI FHIR";
	private FhirContext fhirContext;
	private IServerAddressStrategy serverAddressStrategy;
	private IPrimitiveType<Date> myConformanceDate;

	/**
	 * Constructor
	 */
	public RestfulServerConfiguration() {
		super();
	}

	/**
	 * Get the resourceBindings
	 *
	 * @return the resourceBindings
	 */
	public Collection<ResourceBinding> getResourceBindings() {
		return resourceBindings;
	}

	/**
	 * Set the resourceBindings
	 *
	 * @param resourceBindings the resourceBindings to set
	 */
	public RestfulServerConfiguration setResourceBindings(Collection<ResourceBinding> resourceBindings) {
		this.resourceBindings = resourceBindings;
		return this;
	}

	/**
	 * Get the serverBindings
	 *
	 * @return the serverBindings
	 */
	public List<BaseMethodBinding<?>> getServerBindings() {
		return serverBindings;
	}

	/**
	 * Set the theServerBindings
	 */
	public RestfulServerConfiguration setServerBindings(List<BaseMethodBinding<?>> theServerBindings) {
		this.serverBindings = theServerBindings;
		return this;
	}

	public Map<String, Class<? extends IBaseResource>> getNameToSharedSupertype() {
		return resourceNameToSharedSupertype;
	}

	public RestfulServerConfiguration setNameToSharedSupertype(Map<String, Class<? extends IBaseResource>> resourceNameToSharedSupertype) {
		this.resourceNameToSharedSupertype = resourceNameToSharedSupertype;
		return this;
	}

	/**
	 * Get the implementationDescription
	 *
	 * @return the implementationDescription
	 */
	public String getImplementationDescription() {
		if (isBlank(implementationDescription)) {
			return "HAPI FHIR";
		}
		return implementationDescription;
	}

	/**
	 * Set the implementationDescription
	 *
	 * @param implementationDescription the implementationDescription to set
	 */
	public RestfulServerConfiguration setImplementationDescription(String implementationDescription) {
		this.implementationDescription = implementationDescription;
		return this;
	}

	/**
	 * Get the serverVersion
	 *
	 * @return the serverVersion
	 */
	public String getServerVersion() {
		return serverVersion;
	}

	/**
	 * Set the serverVersion
	 *
	 * @param serverVersion the serverVersion to set
	 */
	public RestfulServerConfiguration setServerVersion(String serverVersion) {
		this.serverVersion = serverVersion;
		return this;
	}

	/**
	 * Get the serverName
	 *
	 * @return the serverName
	 */
	public String getServerName() {
		return serverName;
	}

	/**
	 * Set the serverName
	 *
	 * @param serverName the serverName to set
	 */
	public RestfulServerConfiguration setServerName(String serverName) {
		this.serverName = serverName;
		return this;
	}

	/**
	 * Gets the {@link FhirContext} associated with this server. For efficient processing, resource providers and plain providers should generally use this context if one is needed, as opposed to
	 * creating their own.
	 */
	public FhirContext getFhirContext() {
		return this.fhirContext;
	}

	/**
	 * Set the fhirContext
	 *
	 * @param fhirContext the fhirContext to set
	 */
	public RestfulServerConfiguration setFhirContext(FhirContext fhirContext) {
		this.fhirContext = fhirContext;
		return this;
	}

	/**
	 * Get the serverAddressStrategy
	 *
	 * @return the serverAddressStrategy
	 */
	public IServerAddressStrategy getServerAddressStrategy() {
		return serverAddressStrategy;
	}

	/**
	 * Set the serverAddressStrategy
	 *
	 * @param serverAddressStrategy the serverAddressStrategy to set
	 */
	public void setServerAddressStrategy(IServerAddressStrategy serverAddressStrategy) {
		this.serverAddressStrategy = serverAddressStrategy;
	}

	/**
	 * Get the date that will be specified in the conformance profile
	 * exported by this server. Typically this would be populated with
	 * an InstanceType.
	 */
	public IPrimitiveType<Date> getConformanceDate() {
		return myConformanceDate;
	}

	/**
	 * Set the date that will be specified in the conformance profile
	 * exported by this server. Typically this would be populated with
	 * an InstanceType.
	 */
	public void setConformanceDate(IPrimitiveType<Date> theConformanceDate) {
		myConformanceDate = theConformanceDate;
	}

	public Bindings provideBindings() {
		IdentityHashMap<SearchMethodBinding, String> myNamedSearchMethodBindingToName = new IdentityHashMap<>();
		HashMap<String, List<SearchMethodBinding>> mySearchNameToBindings = new HashMap<>();
		IdentityHashMap<OperationMethodBinding, String> myOperationBindingToName = new IdentityHashMap<>();
		HashMap<String, List<OperationMethodBinding>> myOperationNameToBindings = new HashMap<>();

		Map<String, List<BaseMethodBinding<?>>> resourceToMethods = collectMethodBindings();
		for (Map.Entry<String, List<BaseMethodBinding<?>>> nextEntry : resourceToMethods.entrySet()) {
			List<BaseMethodBinding<?>> nextMethodBindings = nextEntry.getValue();
			for (BaseMethodBinding<?> nextMethodBinding : nextMethodBindings) {
				if (nextMethodBinding instanceof OperationMethodBinding) {
					OperationMethodBinding methodBinding = (OperationMethodBinding) nextMethodBinding;
					if (myOperationBindingToName.containsKey(methodBinding)) {
						continue;
					}

					String name = createOperationName(methodBinding);
					ourLog.debug("Detected operation: {}", name);

					myOperationBindingToName.put(methodBinding, name);
					if (myOperationNameToBindings.containsKey(name) == false) {
						myOperationNameToBindings.put(name, new ArrayList<>());
					}
					myOperationNameToBindings.get(name).add(methodBinding);
				} else if (nextMethodBinding instanceof SearchMethodBinding) {
					SearchMethodBinding methodBinding = (SearchMethodBinding) nextMethodBinding;
					if (myNamedSearchMethodBindingToName.containsKey(methodBinding)) {
						continue;
					}

					String name = createNamedQueryName(methodBinding);
					ourLog.debug("Detected named query: {}", name);

					myNamedSearchMethodBindingToName.put(methodBinding, name);
					if (!mySearchNameToBindings.containsKey(name)) {
						mySearchNameToBindings.put(name, new ArrayList<>());
					}
					mySearchNameToBindings.get(name).add(methodBinding);
				}
			}
		}

		return new Bindings(myNamedSearchMethodBindingToName, mySearchNameToBindings, myOperationNameToBindings, myOperationBindingToName);
	}

	public Map<String, List<BaseMethodBinding<?>>> collectMethodBindings() {
		Map<String, List<BaseMethodBinding<?>>> resourceToMethods = new TreeMap<>();
		for (ResourceBinding next : getResourceBindings()) {
			String resourceName = next.getResourceName();
			for (BaseMethodBinding<?> nextMethodBinding : next.getMethodBindings()) {
				if (resourceToMethods.containsKey(resourceName) == false) {
					resourceToMethods.put(resourceName, new ArrayList<>());
				}
				resourceToMethods.get(resourceName).add(nextMethodBinding);
			}
		}
		for (BaseMethodBinding<?> nextMethodBinding : getServerBindings()) {
			String resourceName = "";
			if (resourceToMethods.containsKey(resourceName) == false) {
				resourceToMethods.put(resourceName, new ArrayList<>());
			}
			resourceToMethods.get(resourceName).add(nextMethodBinding);
		}
		return resourceToMethods;
	}

	/*
	 * Populates {@link #resourceNameToSharedSupertype} by scanning the given resource providers. Only resource provider getResourceType values
	 * are taken into account. {@link ProvidesResources} and method return types are deliberately ignored.
	 *
	 * Given a resource name, the common superclass for all getResourceType return values for that name's providers is the common superclass
	 * for all returned/received resources with that name. Since {@link ProvidesResources} resources and method return types must also be
	 * subclasses of this common supertype, they can't affect the result of this method.
	 */
	public void computeSharedSupertypeForResourcePerName(Collection<IResourceProvider> providers) {
		Map<String, CommonResourceSupertypeScanner> resourceNameToScanner = new HashMap<>();

		List<Class<? extends IBaseResource>> providedResourceClasses = providers.stream()
			.map(provider -> provider.getResourceType())
			.collect(Collectors.toList());
		providedResourceClasses.stream()
			.forEach(resourceClass -> {
				RuntimeResourceDefinition baseDefinition = getFhirContext().getResourceDefinition(resourceClass).getBaseDefinition();
				CommonResourceSupertypeScanner scanner = resourceNameToScanner.computeIfAbsent(baseDefinition.getName(), key -> new CommonResourceSupertypeScanner());
				scanner.register(resourceClass);
			});

		resourceNameToSharedSupertype = resourceNameToScanner.entrySet().stream()
			.filter(entry -> entry.getValue().getLowestCommonSuperclass().isPresent())
			.collect(Collectors.toMap(
				entry -> entry.getKey(),
				entry -> entry.getValue().getLowestCommonSuperclass().get()));
	}

	private String createOperationName(OperationMethodBinding theMethodBinding) {
		StringBuilder retVal = new StringBuilder();
		if (theMethodBinding.getResourceName() != null) {
			retVal.append(theMethodBinding.getResourceName());
		}

		retVal.append('-');
		if (theMethodBinding.isCanOperateAtInstanceLevel()) {
			retVal.append('i');
		}
		if (theMethodBinding.isCanOperateAtServerLevel()) {
			retVal.append('s');
		}
		retVal.append('-');

		// Exclude the leading $
		retVal.append(theMethodBinding.getName(), 1, theMethodBinding.getName().length());

		return retVal.toString();
	}


	private String createNamedQueryName(SearchMethodBinding searchMethodBinding) {
		StringBuilder retVal = new StringBuilder();
		if (searchMethodBinding.getResourceName() != null) {
			retVal.append(searchMethodBinding.getResourceName());
		}
		retVal.append("-query-");
		retVal.append(searchMethodBinding.getQueryName());

		return retVal.toString();
	}

	@Override
	public RuntimeSearchParam getActiveSearchParam(String theResourceName, String theParamName) {
		return getActiveSearchParams(theResourceName).get(theParamName);
	}

	@Override
	public Map<String, RuntimeSearchParam> getActiveSearchParams(@Nonnull String theResourceName) {
		Validate.notBlank(theResourceName, "theResourceName must not be null or blank");

		Map<String, RuntimeSearchParam> retVal = new LinkedHashMap<>();

		collectMethodBindings()
			.getOrDefault(theResourceName, Collections.emptyList())
			.stream()
			.filter(t -> theResourceName.equals(t.getResourceName()))
			.filter(t -> t instanceof SearchMethodBinding)
			.map(t -> (SearchMethodBinding) t)
			.filter(t -> t.getQueryName() == null)
			.forEach(t -> createRuntimeBinding(retVal, t));

		return retVal;
	}

	@Nullable
	@Override
	public RuntimeSearchParam getActiveSearchParamByUrl(String theUrl) {
		throw new UnsupportedOperationException();
	}

	private void createRuntimeBinding(Map<String, RuntimeSearchParam> theMapToPopulate, SearchMethodBinding theSearchMethodBinding) {

		List<SearchParameter> parameters = theSearchMethodBinding
			.getParameters()
			.stream()
			.filter(t -> t instanceof SearchParameter)
			.map(t -> (SearchParameter) t)
			.sorted(SearchParameterComparator.INSTANCE)
			.collect(Collectors.toList());

		for (SearchParameter nextParameter : parameters) {

			String nextParamName = nextParameter.getName();

			String nextParamUnchainedName = nextParamName;
			if (nextParamName.contains(".")) {
				nextParamUnchainedName = nextParamName.substring(0, nextParamName.indexOf('.'));
			}

			String nextParamDescription = nextParameter.getDescription();

			/*
			 * If the parameter has no description, default to the one from the resource
			 */
			if (StringUtils.isBlank(nextParamDescription)) {
				RuntimeResourceDefinition def = getFhirContext().getResourceDefinition(theSearchMethodBinding.getResourceName());
				RuntimeSearchParam paramDef = def.getSearchParam(nextParamUnchainedName);
				if (paramDef != null) {
					nextParamDescription = paramDef.getDescription();
				}
			}

			if (theMapToPopulate.containsKey(nextParamUnchainedName)) {
				continue;
			}

			IIdType id = getFhirContext().getVersion().newIdType().setValue("SearchParameter/" + theSearchMethodBinding.getResourceName() + "-" + nextParamName);
			String uri = null;
			String description = nextParamDescription;
			String path = null;
			RestSearchParameterTypeEnum type = nextParameter.getParamType();
			Set<String> providesMembershipInCompartments = Collections.emptySet();
			Set<String> targets = Collections.emptySet();
			RuntimeSearchParam.RuntimeSearchParamStatusEnum status = RuntimeSearchParam.RuntimeSearchParamStatusEnum.ACTIVE;
			Collection<String> base = Collections.singletonList(theSearchMethodBinding.getResourceName());
			RuntimeSearchParam param = new RuntimeSearchParam(id, uri, nextParamName, description, path, type, providesMembershipInCompartments, targets, status, false, null, base);
			theMapToPopulate.put(nextParamName, param);

		}

	}


	private static class SearchParameterComparator implements Comparator<SearchParameter> {
		private static final SearchParameterComparator INSTANCE = new SearchParameterComparator();

		@Override
		public int compare(SearchParameter theO1, SearchParameter theO2) {
			if (theO1.isRequired() == theO2.isRequired()) {
				return theO1.getName().compareTo(theO2.getName());
			}
			if (theO1.isRequired()) {
				return -1;
			}
			return 1;
		}
	}
}
