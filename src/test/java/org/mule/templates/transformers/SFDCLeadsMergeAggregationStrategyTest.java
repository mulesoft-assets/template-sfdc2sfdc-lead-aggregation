/**
 * Mule Anypoint Template
 * Copyright (c) MuleSoft, Inc.
 * All rights reserved.  http://www.mulesoft.com
 */

package org.mule.templates.transformers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mule.api.MuleContext;
import org.mule.api.MuleEvent;
import org.mule.api.routing.AggregationContext;
import org.mule.templates.integration.AbstractTemplateTestCase;

import com.google.common.collect.Lists;

@SuppressWarnings("unchecked")
@RunWith(MockitoJUnitRunner.class)
public class SFDCLeadsMergeAggregationStrategyTest extends AbstractTemplateTestCase {
	
	@Mock
	private MuleContext muleContext;
  
	
	@Test
	public void testAggregate() throws Exception {
		List<Map<String, String>> leadsA = createLeadLists("A", 0, 1);
		List<Map<String, String>> leadsB = createLeadLists("B", 1, 2);
		
		MuleEvent testOriginalEvent = getTestEvent("");
		MuleEvent testEventA = getTestEvent("");
		MuleEvent testEventB = getTestEvent("");
		
		testEventA.getMessage().setPayload(leadsA.iterator());
		testEventB.getMessage().setPayload(leadsB.iterator());
		
		List<MuleEvent> testEvents = new ArrayList<MuleEvent>();
		testEvents.add(testEventA);
		testEvents.add(testEventB);
		
		AggregationContext aggregationContext = new AggregationContext(testOriginalEvent, testEvents);
		
		SFDCLeadMergeAggregationStrategy sfdcLeadMerge = new SFDCLeadMergeAggregationStrategy();
		List<Map<String, String>> mergedList = Lists.newArrayList((Iterator<Map<String, String>>) sfdcLeadMerge.aggregate(aggregationContext).getMessage().getPayload());

		System.out.println(mergedList);
		Assert.assertEquals("The merged list obtained is not as expected", createExpectedList(), mergedList);

	}

	private List<Map<String, String>> createExpectedList() {
		Map<String, String> lead0 = new HashMap<String, String>();
		lead0.put("IDInA", "0");
		lead0.put("IDInB", "");
		lead0.put("Email", "some.email.0@fakemail.com");
		lead0.put("Name", "SomeName_0");

		Map<String, String> lead1 = new HashMap<String, String>();
		lead1.put("IDInA", "1");
		lead1.put("IDInB", "1");
		lead1.put("Email", "some.email.1@fakemail.com");
		lead1.put("Name", "SomeName_1");

		Map<String, String> lead2 = new HashMap<String, String>();
		lead2.put("IDInA", "");
		lead2.put("IDInB", "2");
		lead2.put("Email", "some.email.2@fakemail.com");
		lead2.put("Name", "SomeName_2");

		List<Map<String, String>> leadList = new ArrayList<Map<String, String>>();
		leadList.add(lead0);
		leadList.add(lead1);
		leadList.add(lead2);

		return leadList;

	}

	private List<Map<String, String>> createLeadLists(String orgId, int start, int end) {
		List<Map<String, String>> leadList = new ArrayList<Map<String, String>>();
		for (int i = start; i <= end; i++) {
			leadList.add(createLead(orgId, i));
		}
		return leadList;
	}

	private Map<String, String> createLead(String orgId, int sequence) {
		Map<String, String> lead = new HashMap<String, String>();
		lead.put("Id", new Integer(sequence).toString());
		lead.put("Name", "SomeName_" + sequence);
		lead.put("Email", "some.email." + sequence + "@fakemail.com");
		return lead;
	}
}
