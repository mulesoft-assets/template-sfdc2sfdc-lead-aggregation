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
import org.mule.DefaultMuleMessage;
import org.mule.api.MuleContext;
import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;

import com.google.common.collect.Lists;

@SuppressWarnings("unchecked")
@RunWith(MockitoJUnitRunner.class)
public class SortLeadListTest {
	@Mock
	private MuleContext muleContext;

	@Test
	public void testSort() throws TransformerException {
		
		MuleMessage message = new DefaultMuleMessage(createOriginalList().iterator(),
				muleContext);

		SortLeadList transformer = new SortLeadList();
		List<Map<String, String>> sortedList = Lists.newArrayList((Iterator<Map<String, String>>) transformer
				.transform(message, "UTF-8"));

		System.out.println(sortedList);
		Assert.assertEquals("The merged list obtained is not as expected",
				createExpectedList(), sortedList);

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
		leadList.add(lead2);
		leadList.add(lead1);

		return leadList;

	}

	private List<Map<String, String>> createOriginalList() {
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

}
