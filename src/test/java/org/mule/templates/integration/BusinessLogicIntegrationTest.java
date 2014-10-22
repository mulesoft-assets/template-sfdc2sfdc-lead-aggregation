/**
 * Mule Anypoint Template
 * Copyright (c) MuleSoft, Inc.
 * All rights reserved.  http://www.mulesoft.com
 */

package org.mule.templates.integration;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mule.MessageExchangePattern;
import org.mule.api.MuleEvent;
import org.mule.processor.chain.SubflowInterceptingChainLifecycleWrapper;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.templates.builders.SfdcObjectBuilder;

import com.google.common.collect.Lists;
import com.sforce.soap.partner.SaveResult;

/**
 * The objective of this class is to validate the correct behavior of the flows for this Mule Template that make calls to external systems.
 */
public class BusinessLogicIntegrationTest extends AbstractTemplateTestCase {

	protected static final String TEMPLATE_NAME = "lead-aggregation";
	private static List<Map<String, Object>> createdLeadsInA = new ArrayList<Map<String, Object>>();
	private static List<Map<String, Object>> createdLeadsInB = new ArrayList<Map<String, Object>>();

	@Rule
	public DynamicPort port = new DynamicPort("http.port");

	@Before
	public void setUp() throws Exception {
		createTestLeadsInSandBox();
	}

	@After
	public void tearDown() throws Exception {
		deleteTestLeadsFromSandBox(createdLeadsInA, "deleteLeadFromAFlow");
		deleteTestLeadsFromSandBox(createdLeadsInB, "deleteLeadFromBFlow");
	}

	@Test
	public void testGatherDataFlow() throws Exception {
		SubflowInterceptingChainLifecycleWrapper flow = getSubFlow("gatherDataFlow");
		flow.setMuleContext(muleContext);
		flow.initialise();
		flow.start();
		MuleEvent event = flow.process(getTestEvent("", MessageExchangePattern.REQUEST_RESPONSE));
		List<Map<String, String>> mergedLeadList = Lists.newArrayList((Iterator<Map<String, String>>)event.getMessage().getPayload());
		Assert.assertTrue("There should be leads from source A or source B.", mergedLeadList.size() != 0);
	}

	@Test
	public void testMainFlow() throws Exception {
		MuleEvent event = runFlow("mainFlow");

		Assert.assertTrue("The payload should not be null.", "Please find attached your Leads Report".equals(event.getMessage().getPayload()));
	}

	@SuppressWarnings("unchecked")
	private void createTestLeadsInSandBox() throws Exception {
		SubflowInterceptingChainLifecycleWrapper flow = getSubFlow("createLeadInAFlow");
		flow.initialise();

		Map<String, Object> lead = createLead("A", 0, true);
		createdLeadsInA.add(lead);

		MuleEvent event = flow.process(getTestEvent(createdLeadsInA, MessageExchangePattern.REQUEST_RESPONSE));
		List<SaveResult> results = (List<SaveResult>) event.getMessage().getPayload();
		for (int i = 0; i < results.size(); i++) {
			createdLeadsInA.get(i).put("Id", results.get(i).getId());
		}

		flow = getSubFlow("createLeadInBFlow");
		flow.initialise();

		lead = createLead("B", 0, true);
		createdLeadsInB.add(lead);

		event = flow.process(getTestEvent(createdLeadsInB, MessageExchangePattern.REQUEST_RESPONSE));
		results = (List<SaveResult>) event.getMessage().getPayload();

		for (int i = 0; i < results.size(); i++) {
			createdLeadsInB.get(i).put("Id", results.get(i).getId());
		}
	}

	private void deleteTestLeadsFromSandBox(List<Map<String, Object>> createdLeads, String deleteFlow) throws Exception {
		List<String> idList = new ArrayList<String>();

		SubflowInterceptingChainLifecycleWrapper flow = getSubFlow(deleteFlow);
		flow.initialise();
		for (Map<String, Object> c : createdLeads) {
			idList.add((String) c.get("Id"));
		}
		flow.process(getTestEvent(idList, MessageExchangePattern.REQUEST_RESPONSE));
		idList.clear();
	}

	private Map<String, Object> createLead(String orgId, int sequence, boolean createEmail) {
		Map<String, Object> lead = SfdcObjectBuilder.aLead()
				.with("FirstName", "FirstName_" + orgId + sequence)
				.with("LastName", buildUniqueName(TEMPLATE_NAME, "LastName_" + sequence + "_"))
				.with("Title", "Dr")
				.with("Company", "Fake Company llc")
				.with("Description", "Some fake description")
				.with("Phone", "123456789")
				.build();
		if(createEmail)
			lead.put("Email", buildUniqueEmail("some.email." + sequence));
		return lead;
	}
	
	private String buildUniqueName(String templateName, String name) {
		String timeStamp = new Long(new Date().getTime()).toString();

		StringBuilder builder = new StringBuilder();
		builder.append(name);
		builder.append(templateName);
		builder.append(timeStamp);

		return builder.toString();
	}
	
	private String buildUniqueEmail(String user) {
		String server = "fakemail";

		StringBuilder builder = new StringBuilder();
		builder.append(buildUniqueName(TEMPLATE_NAME, user));
		builder.append("@");
		builder.append(server);
		builder.append(".com");

		return builder.toString();
	}

}
