package com.acme.hr;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"app.bootstrap.hr-manager.username=test-hr",
		"app.bootstrap.hr-manager.password=test-hr-password",
		"app.bootstrap.admin.username=test-admin",
		"app.bootstrap.admin.password=test-admin-password",
		"app.security.jwt.secret=test-jwt-signing-secret-at-least-32-characters-long"
})
class SalaryManagementApplicationTests {

	@Test
	void contextLoads() {
	}

}
