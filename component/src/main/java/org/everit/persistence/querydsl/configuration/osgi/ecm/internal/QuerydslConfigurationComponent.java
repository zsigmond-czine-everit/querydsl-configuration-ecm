/*
 * Copyright (C) 2011 Everit Kft. (http://www.everit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.everit.persistence.querydsl.configuration.osgi.ecm.internal;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;

import org.everit.osgi.ecm.annotation.Activate;
import org.everit.osgi.ecm.annotation.Component;
import org.everit.osgi.ecm.annotation.ConfigurationPolicy;
import org.everit.osgi.ecm.annotation.Deactivate;
import org.everit.osgi.ecm.annotation.ServiceRef;
import org.everit.osgi.ecm.annotation.attribute.BooleanAttribute;
import org.everit.osgi.ecm.annotation.attribute.BooleanAttributes;
import org.everit.osgi.ecm.annotation.attribute.StringAttribute;
import org.everit.osgi.ecm.annotation.attribute.StringAttributes;
import org.everit.osgi.ecm.component.ComponentContext;
import org.everit.osgi.ecm.extender.ECMExtenderConstants;
import org.everit.persistence.querydsl.configuration.osgi.ecm.QuerydslConfigurationConstants;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;

import com.querydsl.sql.Configuration;
import com.querydsl.sql.SQLTemplates;

import aQute.bnd.annotation.headers.ProvideCapability;

/**
 * Simple component that registers Querydsl configuration as an OSGi service.
 */
@Component(componentId = QuerydslConfigurationConstants.SERVICE_FACTORYPID_QUERYDSL_CONFIGURATION,
    configurationPolicy = ConfigurationPolicy.FACTORY, label = "Everit Querydsl Configuration",
    description = "A Component that registers Querydsl Configuration instance as OSGi service.")
@ProvideCapability(ns = ECMExtenderConstants.CAPABILITY_NS_COMPONENT,
    value = ECMExtenderConstants.CAPABILITY_ATTR_CLASS + "=${@class}")
@BooleanAttributes({
    @BooleanAttribute(attributeId = QuerydslConfigurationConstants.ATTR_USE_LITERALS,
        defaultValue = false, priority = QuerydslConfigurationComponent.PRIORITY_USE_LITERALS_02,
        label = "Use literals",
        description = "Whether to use literals in SQL statements or not.") })
@StringAttributes({
    @StringAttribute(attributeId = Constants.SERVICE_DESCRIPTION,
        defaultValue = QuerydslConfigurationConstants.DEFAULT_SERVICE_DESCRIPTION,
        label = "Service description",
        description = "Optional description for the instantiated Jetty server.") })
public class QuerydslConfigurationComponent {

  public static final int PRIORITY_SQL_TEMPLATES_01 = 1;

  public static final int PRIORITY_USE_LITERALS_02 = 5;

  private ServiceRegistration<Configuration> serviceRegistration;

  /**
   * SQLTemplates reference.
   */
  private SQLTemplates sqlTemplates;

  /**
   * Component activator method.
   */
  @Activate
  public void activate(final ComponentContext<QuerydslConfigurationComponent> componentContext) {
    Configuration configuration = new Configuration(sqlTemplates);

    Map<String, Object> componentPropeties = componentContext.getProperties();
    Object useLiteralsProp =
        componentPropeties.get(QuerydslConfigurationConstants.ATTR_USE_LITERALS);
    if (useLiteralsProp != null) {
      configuration.setUseLiterals(Boolean.valueOf(useLiteralsProp.toString()));
    }

    Dictionary<String, Object> serviceProperties =
        new Hashtable<String, Object>(componentPropeties);
    serviceRegistration =
        componentContext.registerService(Configuration.class, configuration, serviceProperties);
  }

  /**
   * Component deactivate method.
   */
  @Deactivate
  public void deactivate() {
    if (serviceRegistration != null) {
      serviceRegistration.unregister();
    }
  }

  @ServiceRef(attributeId = QuerydslConfigurationConstants.ATTR_SQL_TEMPLATES_TARGET,
      defaultValue = "", attributePriority = PRIORITY_SQL_TEMPLATES_01,
      label = "SQLTemplates OSGi filter",
      description = "OSGi filter for the sqlTemplates reference that will be embedded into the "
          + "configuration.")
  public void setSqlTemplates(final SQLTemplates sqlTemplates) {
    this.sqlTemplates = sqlTemplates;
  }
}
