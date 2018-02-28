package nl.tudelft.distributed.team17.api.rest;

import nl.tudelft.distributed.team17.Application;
import nl.tudelft.distributed.team17.MessageService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {Application.class})
public class EndpointsTest
{
	@MockBean
	MessageService mockedMessageService;

	@Autowired
	Endpoints endpoints;

	@Test
	public void message() throws Exception
	{
		Mockito.when(mockedMessageService.processMsg(Matchers.anyString())).then(invocation -> "this is a mock");

		String val = endpoints.message("henk");

		Assert.assertEquals("this is mock", val);
	}

}