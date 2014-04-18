package org.mule.templates.transformers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageTransformer;

import com.google.common.collect.Lists;

/**
 * This transformer will take to list as input and create a third one that will
 * be the merge of the previous two. The identity of an element of the list is
 * defined by its email.
 */
public class SFDCLeadMerge extends AbstractMessageTransformer {

	private static final String QUERY_COMPANY_A = "leadsFromOrgA";
	private static final String QUERY_COMPANY_B = "leadsFromOrgB";

	@Override
	public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {
		List<Map<String, String>> mergedLeadList = mergeList(getLeadsList(message, QUERY_COMPANY_A), getLeadsList(message, QUERY_COMPANY_B));
		return mergedLeadList;
	}

	private List<Map<String, String>> getLeadsList(MuleMessage message, String propertyName) {
		Iterator<Map<String, String>> iterator = message.getInvocationProperty(propertyName);
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
			} else {
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

	private Map<String, String> findLeadInList(String leadName, List<Map<String, String>> orgList) {
		for (Map<String, String> lead : orgList) {
			if (lead.get("Email").equals(leadName)) {
				return lead;
			}
		}
		return null;
	}
}
