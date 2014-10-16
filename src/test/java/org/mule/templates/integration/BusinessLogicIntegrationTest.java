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
import java.util.concurrent.CopyOnWriteArrayList;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mule.MessageExchangePattern;
import org.mule.api.MuleEvent;
import org.mule.processor.chain.SubflowInterceptingChainLifecycleWrapper;
import org.mule.streaming.ConsumerIterator;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.templates.builders.SfdcObjectBuilder;
import org.mule.templates.transformers.SFDCLeadMerge;

import com.google.common.collect.Lists;
import com.sforce.soap.partner.SaveResult;

/**
 * The objective of this class is to validate the correct behavior of the flows for this Mule Template that make calls to external systems.
 */
public class BusinessLogicIntegrationTest extends AbstractTemplateTestCase {

	protected static final String TEMPLATE_NAME = "lead-aggregation";
	private List<Map<String, Object>> createdLeadsInA = new ArrayList<Map<String, Object>>();
	private List<Map<String, Object>> createdLeadsInB = new ArrayList<Map<String, Object>>();
	
	@Rule
	public DynamicPort port = new DynamicPort("http.port");

	@Before
	public void setUp() throws Exception {
		createLeads();
	}

	@After
	public void tearDown() throws Exception {
		deleteTestLeadFromSandBox(createdLeadsInA, "deleteLeadFromAFlow");
		deleteTestLeadFromSandBox(createdLeadsInB, "deleteLeadFromBFlow");
	}

	@Test
	public void testGatherDataFlow() throws Exception {
		SubflowInterceptingChainLifecycleWrapper flow = getSubFlow("gatherDataFlow");
		flow.setMuleContext(muleContext);
		flow.initialise();
		flow.start();
		MuleEvent event = flow.process(getTestEvent("", MessageExchangePattern.REQUEST_RESPONSE));
		Iterator<Map<String, String>> list = (Iterator<Map<String, String>>)event.getMessage().getPayload();
		Assert.assertTrue("There should be leads from source A or source B.", Lists.newArrayList(list).size() != 0);
	}

	/*
	@Test
	@SuppressWarnings("rawtypes")
	public void testAggregationFlow() throws Exception {
		MuleEvent testEvent = prepareTestEvent();

		SubflowInterceptingChainLifecycleWrapper flow = getSubFlow("aggregationFlow");
		flow.initialise();
		MuleEvent event = flow.process(testEvent);

		Assert.assertTrue("The payload should not be null.", event.getMessage().getPayload() != null);
		Assert.assertFalse("The lead list should not be empty.", ((List) event.getMessage().getPayload()).isEmpty());
	}
	*/

	@Test
	public void testFormatOutputFlow() throws Exception {
		SubflowInterceptingChainLifecycleWrapper flow = getSubFlow("gatherDataFlow");
		flow.setMuleContext(muleContext);
		flow.initialise();
		flow.start();
		MuleEvent event = flow.process(getTestEvent("", MessageExchangePattern.REQUEST_RESPONSE));

		flow = getSubFlow("formatOutputFlow");
		flow.setMuleContext(muleContext);
		flow.initialise();
		flow.start();
		event = flow.process(event);

		Assert.assertTrue("The payload should not be null.", event.getMessage().getPayload() != null);
	}

	@Test
	public void testFormatOutputFlowWithEmptyEmail() throws Exception {		
		SubflowInterceptingChainLifecycleWrapper flow = getSubFlow("gatherDataFlow");
		flow.setMuleContext(muleContext);
		flow.initialise();
		flow.start();
		MuleEvent event = flow.process(getTestEvent("", MessageExchangePattern.REQUEST_RESPONSE));
		
		flow = getSubFlow("formatOutputFlow");
		flow.setMuleContext(muleContext);
		flow.initialise();
		flow.start();
		event = flow.process(event);
		
		Assert.assertTrue("The payload should not be null.", event.getMessage().getPayload() != null);
	}

	private MuleEvent prepareTestEvent() throws Exception {
		List<Map<String, Object>> leadsFromOrgA = createLeadLists("A", 0, 1, false);
		List<Map<String, Object>> leadsFromOrgB = createLeadLists("B", 1, 2, false);
		
		MuleEvent testEvent = getTestEvent("", MessageExchangePattern.REQUEST_RESPONSE);
		CopyOnWriteArrayList<Iterator<Map<String, Object>>> list = new CopyOnWriteArrayList<Iterator<Map<String,Object>>>(); 
		list.add(leadsFromOrgA.iterator());
		list.add(leadsFromOrgB.iterator());
		testEvent.getMessage().setPayload(list);

		return testEvent;
	}

	@SuppressWarnings("unchecked")
	private void createLeads() throws Exception {
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

	private List<Map<String, Object>> createLeadLists(String orgId, int start, int end, boolean createEmail) {
		List<Map<String, Object>> leadList = new ArrayList<Map<String, Object>>();
		for (int i = start; i <= end; i++) {
			leadList.add(createLead(orgId, i, createEmail));
		}
		return leadList;
	}

	private void deleteTestLeadFromSandBox(List<Map<String, Object>> createdLeads, String deleteFlow) throws Exception {
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
