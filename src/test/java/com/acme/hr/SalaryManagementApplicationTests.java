package com.acme.hr;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"app.bootstrap.hr-manager.username=test-hr",
		"app.bootstrap.hr-manager.password=test-hr-password",
		"app.bootstrap.admin.username=test-admin",
		"app.bootstrap.admin.password=test-admin-password"
})
class SalaryManagementApplicationTests {

	@Test
	void contextLoads() {
	}

}
