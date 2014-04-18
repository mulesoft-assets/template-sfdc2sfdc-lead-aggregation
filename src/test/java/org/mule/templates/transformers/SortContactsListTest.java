package org.mule.templates.transformers;

import java.util.ArrayList;
import java.util.HashMap;
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

@SuppressWarnings("unchecked")
@RunWith(MockitoJUnitRunner.class)
public class SortContactsListTest {
	@Mock
	private MuleContext muleContext;

	@Test
	public void testSort() throws TransformerException {

		MuleMessage message = new DefaultMuleMessage(createOriginalList(),
				muleContext);

		SortLeadList transformer = new SortLeadList();
		List<Map<String, String>> sortedList = (List<Map<String, String>>) transformer
				.transform(message, "UTF-8");

		System.out.println(sortedList);
		Assert.assertEquals("The merged list obtained is not as expected",
				createExpectedList(), sortedList);

	}

	private List<Map<String, String>> createExpectedList() {
		Map<String, String> contact0 = new HashMap<String, String>();
		contact0.put("IDInA", "0");
		contact0.put("IDInB", "");
		contact0.put("Email", "some.email.0@fakemail.com");
		contact0.put("Name", "SomeName_0");

		Map<String, String> contact1 = new HashMap<String, String>();
		contact1.put("IDInA", "1");
		contact1.put("IDInB", "1");
		contact1.put("Email", "some.email.1@fakemail.com");
		contact1.put("Name", "SomeName_1");

		Map<String, String> contact2 = new HashMap<String, String>();
		contact2.put("IDInA", "");
		contact2.put("IDInB", "2");
		contact2.put("Email", "some.email.2@fakemail.com");
		contact2.put("Name", "SomeName_2");

		List<Map<String, String>> contactList = new ArrayList<Map<String, String>>();
		contactList.add(contact0);
		contactList.add(contact2);
		contactList.add(contact1);

		return contactList;

	}

	private List<Map<String, String>> createOriginalList() {
		Map<String, String> contact0 = new HashMap<String, String>();
		contact0.put("IDInA", "0");
		contact0.put("IDInB", "");
		contact0.put("Email", "some.email.0@fakemail.com");
		contact0.put("Name", "SomeName_0");

		Map<String, String> contact1 = new HashMap<String, String>();
		contact1.put("IDInA", "1");
		contact1.put("IDInB", "1");
		contact1.put("Email", "some.email.1@fakemail.com");
		contact1.put("Name", "SomeName_1");

		Map<String, String> contact2 = new HashMap<String, String>();
		contact2.put("IDInA", "");
		contact2.put("IDInB", "2");
		contact2.put("Email", "some.email.2@fakemail.com");
		contact2.put("Name", "SomeName_2");

		List<Map<String, String>> contactList = new ArrayList<Map<String, String>>();
		contactList.add(contact0);
		contactList.add(contact1);
		contactList.add(contact2);

		return contactList;

	}

}
