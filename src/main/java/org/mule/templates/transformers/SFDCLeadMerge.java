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
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.lang.StringUtils;
import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageTransformer;

import com.google.common.collect.Lists;

/**
 * This transformer will take to list as input and create a third one that will be the merge of the previous two. The identity of an element of the list is defined by its email.
 */
public class SFDCLeadMerge extends AbstractMessageTransformer {

	private static final String QUERY_COMPANY_A = "leadsFromOrgA";
	private static final String QUERY_COMPANY_B = "leadsFromOrgB";

	@Override
	public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {
		List<Map<String, String>> mergedLeadList = mergeList(getLeadsList(message, 0), getLeadsList(message, 1));
		return mergedLeadList;
	}

	private List<Map<String, String>> getLeadsList(MuleMessage message, int index) {
//		Iterator<Map<String, String>> iterator = message.getInvocationProperty(propertyName);
		Iterator<Map<String, String>> iterator = (Iterator<Map<String, String>>) ((CopyOnWriteArrayList) message.getPayload()).get(index);
		return Lists.newArrayList(iterator);
	}

	/**
	 * The method will merge the leads from the two lists creating a new one.
	 * 
	 * @param leadsFromOrgA
	 *            leads from organization A
	 * @param leadsFromOrgB
	 *            leads from organization B
	 * @return a list with the merged content of the to input lists
	 */
	private List<Map<String, String>> mergeList(List<Map<String, String>> leadsFromOrgA, List<Map<String, String>> leadsFromOrgB) {
		List<Map<String, String>> mergedLeadsList = new ArrayList<Map<String, String>>();

		// Put all leads from A in the merged mergedleadsList
		for (Map<String, String> leadFromA : leadsFromOrgA) {
			Map<String, String> mergedlead = createMergedLead(leadFromA);
			mergedlead.put("IDInA", leadFromA.get("Id"));
			mergedLeadsList.add(mergedlead);
		}

		// Add the new leads from B and update the exiting ones
		for (Map<String, String> leadsFromB : leadsFromOrgB) {
			Map<String, String> leadFromA = findLeadInList(leadsFromB.get("Email"), mergedLeadsList);
			if (leadFromA != null) {
				leadFromA.put("IDInB", leadsFromB.get("Id"));
			}
			else {
				Map<String, String> mergedLead = createMergedLead(leadsFromB);
				mergedLead.put("IDInB", leadsFromB.get("Id"));
				mergedLeadsList.add(mergedLead);
			}
		}
		return mergedLeadsList;
	}

	private Map<String, String> createMergedLead(Map<String, String> lead) {
		Map<String, String> mergedLead = new HashMap<String, String>();
		mergedLead.put("Name", lead.get("Name"));
		mergedLead.put("Email", lead.get("Email"));
		mergedLead.put("IDInA", "");
		mergedLead.put("IDInB", "");
		return mergedLead;
	}

	private Map<String, String> findLeadInList(String leadEmail, List<Map<String, String>> orgList) {
		if(StringUtils.isBlank(leadEmail))
			return null;
		for (Map<String, String> lead : orgList) {
			if (StringUtils.isNotBlank(lead.get("Email")) && lead.get("Email").equals(leadEmail)) {
				return lead;
			}
		}
		return null;
	}
}
